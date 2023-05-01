import isak.Team;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException {
        StandardServiceRegistry ssr = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
        Metadata meta = new MetadataSources(ssr).getMetadataBuilder().build();
        SessionFactory factory = meta.getSessionFactoryBuilder().build();

        String IPLid = "5c6dbd2da605be0329ecf36a";
        /*List<Tournament> tournaments = new ArrayList<>();// = getTournamentIds(IPLid);
        tournaments.add(new Tournament("6422e78c31ff8e2d9a497927"));*/
        List<Match> matches = new ArrayList<>();
        Team top = new Team();
        top.setBattlefyId("3");
        top.setName("två");
        top.setElo(17);
        //tournaments.forEach(t -> matches.addAll(t.getMatches()));
        matches.sort(Comparator.naturalOrder());
        matches.forEach(m -> System.out.println(m.toString()));

        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        if(!exists(top, session)) {
            session.save(top);
        }
        t.commit();
        session.close();

        /*for(Match m: matches) {
            Session session = factory.openSession();
            Transaction t = session.beginTransaction();
            isak.Team top = m.getTop();
            if(exists(top, session)){

            }else{
                session.save(top);
            }
            isak.Team bot = m.getBot();
            if(exists(bot, session)){

            }else{
                session.save(bot);
            }
            t.commit();
        }*/
    }

    public static boolean exists(Team t, Session s){
        Query query = s.
                createQuery("select 1 from isak.Team t where t.battlefy_id = :key");
        query.setParameter("key", t.getBattlefy_id());
        return query.uniqueResult() != null;
    }

    public static List<Tournament> getTournamentIds(String org) throws IOException {
        String startUrl = "https://search.battlefy.com/tournament/organization/" + org;
        URL url = new URL(startUrl + "/past?page=1&size=20");
        String json = IOUtils.toString(url, Charset.forName("UTF-8"));
        JSONObject ob = new JSONObject(json);
        int nTournaments = ob.getInt("total");
        List<Tournament> tournaments = new ArrayList<>();

        for(int i = 1; i <= 1 + 1/* nTournaments/20 */; i++){
            url = new URL(startUrl + "/past?page=" + i + "&size=1");
            if(i != 1){
                json = IOUtils.toString(url, Charset.forName("UTF-8"));
                ob = new JSONObject(json);
            }
            JSONArray ar = ob.getJSONArray("tournaments");
            for(Object o: ar){
                JSONObject obj = (JSONObject) o;
                tournaments.add(new Tournament(obj.getString("_id")));
            }
        }

        return tournaments;
    }
}

//CREATE TABLE matches(
//  id INTEGER PRIMARY KEY,
//  datum DATETIME NOT NULL,
//  top_id INT NOT NULL,
//  topscore INT NOT NULL,
//  topelo INT NOT NULL,
//  bot_id INT NOT NULL,
//  botscore INT NOT NULL,
//  botelo INT NOT NULL,
//  FOREIGN KEY (top_id) REFERENCES teams (id),
//  FOREIGN KEY (bot_id) REFERENCES teams (id)
//  );

//CREATE TABLE teams(
//  id INTEGER PRIMARY KEY,
//  battlefy_id VARCHAR(30) NOT NULL,
//  name VARCHAR(30) NOT NULL,
//  elo INT NOT NULL);