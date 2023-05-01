package isak;

import org.json.JSONObject;

import jakarta.persistence.*;

@Entity
@Table(name = "teams")
public class Team {
    @Column(name = "name")
    private String name;
    //private JSONObject json;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "battlefy_id")
    private String battlefy_id;
    @Column(name = "elo")
    private Integer elo;

    public Team(){}
    /*public Team(JSONObject o){
        json = o;
        setName(json.getJSONObject("team").getString("name"));
        setBattlefyId(json.getJSONObject("team").getString("_id"));
    }*/

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
