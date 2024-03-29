package data;

import data.Match;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static discordBot.Utils.getLastMatchTime;
import static discordBot.Utils.parseTime;

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
        Player.players = new HashMap<>();
        url = new URL("https://dtmwra1jsgyb0.cloudfront.net/tournaments/" + id + "/participants");
        JSONArray participantJson = new JSONArray(IOUtils.toString(url, StandardCharsets.UTF_8));
        for (Object j:
             participantJson) {
            Player.players.put(((JSONObject) j).getString("_id"), ((JSONObject) j).getString("inGameName"));
        }


        url = new URL("https://dtmwra1jsgyb0.cloudfront.net/tournaments/" + id);
        json = new JSONObject(IOUtils.toString(url, StandardCharsets.UTF_8));
        JSONArray stageArray = json.getJSONArray("stageIDs");
        for(Object o: stageArray){
            stageids.add((String) o);
        }

        System.out.println("tournament: " + json.getString("name") + " - " + json.getString("updatedAt"));
        for(String s: stageids){
            URL url = new URL("https://dtmwra1jsgyb0.cloudfront.net/stages/" + s + "/matches");
            JSONArray stageJson = new JSONArray(IOUtils.toString(url, StandardCharsets.UTF_8));
            for(Object o: stageJson){
                JSONObject jo = (JSONObject) o;
                if(jo.has("top") && jo.has("bottom") && jo.has("isBye") &&
                        jo.getJSONObject("top").has("team") && jo.getJSONObject("top").has("score") && jo.getJSONObject("top").has("winner") &&
                jo.getJSONObject("bottom").has("team") && jo.getJSONObject("bottom").has("score") && jo.getJSONObject("bottom").has("winner")
                        && !jo.getBoolean("isBye") && (jo.getJSONObject("top").getBoolean("winner") || jo.getJSONObject("bottom").getBoolean("winner"))) {
                    if(jo.getBoolean("isComplete") && parseTime(jo.getString("completedAt")).compareTo(getLastMatchTime()) > 0
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
