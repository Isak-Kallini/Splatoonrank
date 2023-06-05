package data;

import org.json.JSONException;
import org.json.JSONObject;

public class Team {
    private String name;
    private JSONObject json;
    private Integer id;
    private String battlefy_id;

    private Integer elo = 1000;

    public Team(JSONObject o){
        json = o;
        setName(json.getJSONObject("team").getString("name"));
        try {
            setBattlefyId(json.getJSONObject("team").getString("persistentTeamID"));
        }catch (JSONException e){
            setBattlefyId("invalid");
        }
    }

    public TeamData getData(){
        TeamData data = new TeamData();
        data.setBattlefyId(battlefy_id);
        data.setElo(elo);
        data.setName(name);
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

