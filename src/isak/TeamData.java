package isak;

import jakarta.persistence.*;

@Entity
@Table(name = "teams")
public class TeamData {
    @Column(name = "name", length = 5000)
    private String name;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "battlefy_id", unique = true)
    private String battlefy_id;
    @Column(name = "elo")
    private Integer elo;

    public TeamData(){}

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
