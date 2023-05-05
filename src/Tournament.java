import isak.Match;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Tournament {
    private URL url;
    private String id;

    public JSONObject getJson() {
        return json;
    }

    private JSONObject json;
    private List<String> stageids = new ArrayList<>();
    private List<Match> matches = new ArrayList<>();
    public Tournament(String tournamentId) throws IOException {
        id = tournamentId;
        url = new URL("https://dtmwra1jsgyb0.cloudfront.net/tournaments/" + id);
        json = new JSONObject(IOUtils.toString(url, Charset.forName("UTF-8")));
        JSONArray stageArray = json.getJSONArray("stageIDs");
        for(Object o: stageArray){
            stageids.add((String) o);
        }

        System.out.println("tournament: " + id);
        for(String s: stageids){
            URL url = new URL("https://dtmwra1jsgyb0.cloudfront.net/stages/" + s + "/matches");
            JSONArray stageJson = new JSONArray(IOUtils.toString(url, Charset.forName("UTF-8")));
            for(Object o: stageJson){
                JSONObject jo = (JSONObject) o;
                if(jo.has("top") && jo.has("bottom") && jo.has("isBye") &&
                        jo.getJSONObject("top").has("team") && jo.getJSONObject("top").has("score") && jo.getJSONObject("top").has("winner") &&
                jo.getJSONObject("bottom").has("team") && jo.getJSONObject("bottom").has("score") && jo.getJSONObject("bottom").has("winner")
                        && !jo.getBoolean("isBye") && (jo.getJSONObject("top").getBoolean("winner") || jo.getJSONObject("bottom").getBoolean("winner"))) {
                    if(jo.getBoolean("isComplete") && Main.parseTime(jo.getString("completedAt")).compareTo(Main.getLastMatch()) > 0
                    && jo.getJSONObject("top").getInt("score") >= 0 && jo.getJSONObject("bottom").getInt("score") >= 0) {
                        matches.add(new Match(jo));
                    }
                }
            }
        }
    }

    public String getId(){
        return id;
    }

    public List<Match> getMatches() {
        return matches;
    }
}
