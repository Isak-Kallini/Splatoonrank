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
import static discordBot.Utils.getLastMatchTime;
import static discordBot.Utils.parseTime;

public class Update implements Command {

    @Override
    public void run(SlashCommandInteractionEvent event){
        event.reply("Updating").queue();
        Session session = factory.openSession();
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



    public void saveMatches(List<Match> matches, SessionFactory factory){
        for(data.Match m: matches) {
            Session session = factory.openSession();
            Transaction t = session.beginTransaction();
            data.MatchData matchData = m.getData();
            data.TeamData top = matchData.getTop();
            if(!matchExists(matchData.getBattlefyId(), session)) {
                if (exists(top, session)) {
                    Query<TeamData> q = session.createQuery("from data.TeamData t where t.battlefy_id = :key", TeamData.class);
                    q.setParameter("key", top.getBattlefy_id());
                    Integer id = q.list().get(0).getId();
                    matchData.setTop(session.get(TeamData.class, id));
                    matchData.getTop().setName(top.getName());
                    session.persist(matchData.getTop());
                } else {
                    session.persist(top);
                }
                data.TeamData bot = matchData.getBot();
                if (exists(bot, session)) {
                    Query<TeamData> q = session.createQuery("from data.TeamData t where t.battlefy_id = :key", TeamData.class);
                    q.setParameter("key", bot.getBattlefy_id());
                    Integer id = q.list().get(0).getId();
                    matchData.setBot(session.get(TeamData.class, id));
                    matchData.getBot().setName(bot.getName());
                    session.persist(matchData.getBot());
                } else {
                    session.persist(bot);
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

                session.persist(matchData);
                t.commit();
            }
            session.close();
        }
    }

    public boolean matchExists(String id, Session s){
        Query<Integer> query = s.
                createQuery("select 1 from data.MatchData t where t.battlefyId = :key", Integer.class);
        query.setParameter("key", id);
        return query.uniqueResult() != null;
    }

    public int calculateElo(Integer topElo, Integer botElo, Integer topScore, Integer botScore){
        if(topScore < 0)
            topScore = 0;
        if(botScore < 0)
            botScore = 0;

        if(topScore.equals(botScore))
            return topElo;
        double s = (topScore.doubleValue()/(topScore.doubleValue() + botScore.doubleValue()));
        double e = (1.0/(1.0 + Math.pow(10.0, ((botElo.doubleValue() - topElo.doubleValue())/400))));


        return (int) (topElo.doubleValue() + (50.0 * (s - e)));
    }

    public boolean exists(TeamData t, Session s){
        Query<Integer> query = s.
                createQuery("select 1 from data.TeamData t where t.battlefy_id = :key", Integer.class);
        query.setParameter("key", t.getBattlefy_id());
        return query.uniqueResult() != null;
    }

    public List<Tournament> getTournamentIds(String org) throws IOException {
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
                    if(!tournament.getJson().has("lastCompletedMatchAt") || parseTime(tournament.getJson().getString("lastCompletedMatchAt")).compareTo(getLastMatchTime()) > 0) {
                        tournaments.add(new Tournament(obj.getString("_id")));
                    }else{
                        break loop;
                    }
                }
            }
        }

        return tournaments;
    }



}
