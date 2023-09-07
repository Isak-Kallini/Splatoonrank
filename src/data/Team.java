package data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String name;
    private JSONObject json;
    private Integer id;
    private String battlefy_id;

    private Integer elo = 1000;

    private List<Player> players;

    public Team(JSONObject o){
        players = new ArrayList<>();
        json = o;
        String temp = json.getJSONObject("team").getString("name");
        if(temp.length() > 100){
            setName(temp.substring(0,100));
        }else{
            setName(temp);
        }
        try {
            setBattlefyId(json.getJSONObject("team").getString("persistentTeamID"));
        }catch (JSONException e){
            setBattlefyId("invalid");
        }
        JSONArray playerIds = json.getJSONObject("team").getJSONArray("playerIDs");
        for(Object s: playerIds){
            players.add(new Player((String) s));
        }
    }

    public TeamData getData(){
        TeamData data = new TeamData();
        data.setBattlefyId(battlefy_id);
        data.setElo(elo);
        data.setName(name);
        List<PlayerData> pdata = new ArrayList<>();
        for(Player p: players){
            pdata.add(p.getPlayerData());
        }
        data.setPlayers(pdata);
        return data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBattlefy_id() {
        return battlefy_id;
    }

    public void setBattlefyId(String battlefy_id) {
        this.battlefy_id = battlefy_id;
    }

    public Integer getElo() {
        return elo;
    }

    public void setElo(Integer elo) {
        this.elo = elo;
    }
}

