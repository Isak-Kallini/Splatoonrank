package discordBot.commands;

import data.*;
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

public class Update extends Command {

    public Update(){
        super.name = "update";
        super.desc = "Get new matches from battlefy";
    }
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
        event.getChannel().sendMessage("Saving").queue();
        saveMatches(matches, factory);
        event.getChannel().sendMessage("Saved").queue();
    }

    public void runNoDiscord(){
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
        saveMatches(matches, factory);
    }



    public void saveMatches(List<Match> matches, SessionFactory factory){
        for(Match m: matches) {
            Session session = factory.openSession();
            if(!matchExists(m.getBattlefyId(), session)) {
                Transaction t = session.beginTransaction();
                MatchData matchData = m.getData();
                List<TeamData> dataList = new ArrayList<>();
                matchData.getTop().setIdent("top");
                matchData.getBot().setIdent("bot");
                dataList.add(matchData.getTop());
                dataList.add(matchData.getBot());

                for (TeamData d : dataList) {
                    if (!matchExists(matchData.getBattlefyId(), session)) {
                        TeamData team = exists(d, session);
                        if (team != null) {
                            team.setIdent(d.getIdent());
                            team.setName(d.getName());
                            team.setPlayers(d.getPlayers());
                            persistPlayers(team, session);
                            matchData.set(team);
                            session.persist(matchData.get(team));
                        } else {
                            persistPlayers(d, session);
                            session.persist(matchData.get(d));
                        }
                    }
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
                session.close();
            }
        }
    }

    public void persistPlayers(TeamData d, Session session){
        for (PlayerData p : d.getPlayers()) {
            if(!exists(p, session)) {
                session.persist(p);
            }else{
                Query<PlayerData> q = session.createQuery("from data.PlayerData t where t.battlefy_id = :key", PlayerData.class);
                q.setParameter("key", p.getBattlefy_id());
                List<PlayerData> ps = d.getPlayers();
                ps.set(d.getPlayers().indexOf(p), q.uniqueResult());
                d.setPlayers(ps);
            }
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

    public TeamData exists(TeamData t, Session s){
        Query<TeamData> query = s.
                createQuery("from data.TeamData t where t.battlefy_id = :key", TeamData.class);
        query.setParameter("key", t.getBattlefy_id());
        if(query.uniqueResult() != null){
            return query.uniqueResult();
        }else{
            Query<TeamData> allTeamsQuery = s.createQuery("from data.TeamData t", TeamData.class);
            List<List<PlayerData>> playerListList = new ArrayList<>();
            allTeamsQuery.list().forEach(p -> playerListList.add(p.getPlayers()));
            List<PlayerData> res = new ArrayList<>();
            for(List<PlayerData> pl: playerListList){
                int buffer = 4 - pl.size();
                for(PlayerData p: pl){
                    boolean found = false;
                    for(PlayerData p2: t.getPlayers()){
                        if(p2.getBattlefy_id().equals(p.getBattlefy_id())){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        buffer--;
                    }
                    if(buffer < 0){
                        break;
                    }
                }
            }

            return t;
        }
    }

    public boolean exists(PlayerData p, Session s){
        Query<PlayerData> query = s.
                createQuery("from data.PlayerData p where p.battlefy_id = :key", PlayerData.class);
        query.setParameter("key", p.getBattlefy_id());
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
