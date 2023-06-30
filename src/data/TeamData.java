package data;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "teams")
public class TeamData {
    @Column(name = "name", length = 100)
    private String name;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "battlefy_id", unique = true)
    private String battlefy_id;

    @Column(name = "elo")
    private Integer elo;

    @ElementCollection
    @CollectionTable(name = "playerss", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "player")
    private List<PlayerData> players;

    private String ident;

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

    public Integer getId() {
        return id;
    }

    public List<PlayerData> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerData> players) {
        this.players = players;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }
}
