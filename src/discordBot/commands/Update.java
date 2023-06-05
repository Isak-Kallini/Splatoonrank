package discordBot.commands;

import data.Match;
import data.TeamData;
import data.Tournament;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static discordBot.Main.factory;

public class Update {

    private static Calendar lastMatch;

    public static void run(SlashCommandInteractionEvent event){
        event.reply("Updating").queue();
        Session session = factory.openSession();
        lastMatch = getLastMatchTime(session);
        session.close();
        List<String> orgIds = new ArrayList<>();
        orgIds.add("621a9ffb2ebfb728063d8153"); //Mulloway Institute of turfing
        orgIds.add("61c24af8dfe38d7b8d22a78f"); //20xx
        orgIds.add("600339ee8c847d119d4b773c"); //squid junction
        orgIds.add("5c6dbd2da605be0329ecf36a"); //IPL
        orgIds.add("5f3da7d7a3f8871d5075d66d"); //LSL
        orgIds.add("6223711c9b1bd8194d3622d3"); //Splatalittle
        orgIds.add("61536b3608427730510f9ea6"); //From The Ink UP
        List<Tournament> tournaments = new ArrayList<>();
        orgIds.forEach(o -> {
            try {
                tournaments.addAll(getTournamentIds(o));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        List<Match> matches = new ArrayList<>();
        tournaments.forEach(tournament -> matches.addAll(tournament.getMatches()));
        matches.sort(Comparator.naturalOrder());
        event.getChannel().sendMessage("Found " + matches.size() + " new matches").queue();
        saveMatches(matches, factory);
    }



    public static void saveMatches(List<Match> matches, SessionFactory factory){
        for(data.Match m: matches) {
            Session session = factory.openSession();
            Transaction t = session.beginTransaction();
            data.MatchData matchData = m.getData();
            data.TeamData top = matchData.getTop();
            if(!matchExists(matchData.getBattlefyId(), session)) {
                if (exists(top, session)) {
                    Query q = session.createQuery("select id from data.TeamData t where t.battlefy_id = :key");
                    q.setParameter("key", top.getBattlefy_id());
                    Integer id = (Integer) q.list().get(0);
                    matchData.setTop(session.get(TeamData.class, id));
                    matchData.getTop().setName(top.getName());
                    session.save(matchData.getTop());
                } else {
                    session.save(top);
                }
                data.TeamData bot = matchData.getBot();
                if (exists(bot, session)) {
                    Query q = session.createQuery("select id from data.TeamData t where t.battlefy_id = :key");
                    q.setParameter("key", bot.getBattlefy_id());
                    Integer id = (Integer) q.list().get(0);
                    matchData.setBot(session.get(TeamData.class, id));
                    matchData.getBot().setName(bot.getName());
                    session.save(matchData.getBot());
                } else {
                    session.save(bot);
                }

                int topelo = matchData.getTop().getElo();
                int topscore = matchData.getTopScore();
                int botelo = matchData.getBot().getElo();
                int botscore = matchData.getBotScore();
                int newtopelo = calculateElo(topelo, botelo, topscore, botscore);
                int newbotelo = calculateElo(botelo, topelo, botscore, topscore);
                matchData.setTopElo(newtopelo);
                matchData.setBotElo(newbotelo);
                matchData.getTop().setElo(newtopelo);
                matchData.getBot().setElo(newbotelo);

                session.save(matchData);
                t.commit();
            }
            session.close();
        }
    }

    public static boolean matchExists(String id, Session s){
        Query query = s.
                createQuery("select 1 from data.MatchData t where t.battlefyId = :key");
        query.setParameter("key", id);
        return query.uniqueResult() != null;
    }

    public static int calculateElo(Integer topElo, Integer botElo, Integer topScore, Integer botScore){
        if(topScore < 0)
            topScore = 0;
        if(botScore < 0)
            botScore = 0;

        if(topScore == botScore)
            return topElo;
        double s = (topScore.doubleValue()/(topScore.doubleValue() + botScore.doubleValue()));
        double e = (1.0/(1.0 + Math.pow(10.0, ((botElo.doubleValue() - topElo.doubleValue())/400))));


        return (int) (topElo.doubleValue() + (50.0 * (s - e)));
    }

    public static boolean exists(TeamData t, Session s){
        Query query = s.
                createQuery("select 1 from data.TeamData t where t.battlefy_id = :key");
        query.setParameter("key", t.getBattlefy_id());
        return query.uniqueResult() != null;
    }

    public static List<Tournament> getTournamentIds(String org) throws IOException {
        String startUrl = "https://search.battlefy.com/tournament/organization/" + org;
        URL url = new URL(startUrl + "/past?page=1&size=20");
        String json = IOUtils.toString(url, StandardCharsets.UTF_8);
        JSONObject ob = new JSONObject(json);
        int nTournaments = ob.getInt("total");
        List<Tournament> tournaments = new ArrayList<>();

        loop:
        for(int i = 1; i <= 1 + nTournaments/20 ; i++){
            url = new URL(startUrl + "/past?page=" + i + "&size=20");
            if(i != 1){
                json = IOUtils.toString(url, StandardCharsets.UTF_8);
                ob = new JSONObject(json);
            }
            JSONArray ar = ob.getJSONArray("tournaments");
            for(Object o: ar){
                JSONObject obj = (JSONObject) o;
                if((obj.getString("gameID").equals("62f3f317e6035f56260f090f") || obj.getString("gameID").equals("58f234b8452f9403401579ac")) && obj.getInt("playersPerTeam") == 4) {
                    Tournament tournament = new Tournament(obj.getString("_id"));
                    if(!tournament.getJson().has("lastCompletedMatchAt") || parseTime(tournament.getJson().getString("lastCompletedMatchAt")).compareTo(lastMatch) > 0) {
                        tournaments.add(new Tournament(obj.getString("_id")));
                    }else{
                        break loop;
                    }
                }
            }
        }

        return tournaments;
    }

    public static Calendar parseTime(String time){
        Calendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, Integer.parseInt(time.substring(0, 4)));
        date.set(Calendar.MONTH, Integer.parseInt(time.substring(5, 7)) - 1);
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(time.substring(8, 10)));
        date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(11, 13)));
        date.set(Calendar.MINUTE, Integer.parseInt(time.substring(14,16)));
        date.set(Calendar.SECOND, Integer.parseInt(time.substring(17, 19)));
        return date;
    }

    public static Calendar getLastMatch() {
        return lastMatch;
    }

    public static Calendar getLastMatchTime(Session s){
        Query q = s.createQuery("SELECT MAX(date) FROM data.MatchData");
        Calendar time = (Calendar) q.uniqueResult();
        if(time == null){
            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(0);
            return c;
        }else {
            return time;
        }
    }

}
