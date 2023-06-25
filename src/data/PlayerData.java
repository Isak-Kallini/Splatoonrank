package data;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class PlayerData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name", length = 5000)
    private String name;
    @Column(name = "battlefy_id", unique = true)
    private String battlefy_id;

    public PlayerData(){}

    public String getBattlefy_id() {
        return battlefy_id;
    }

    public void setBattlefy_id(String battlefy_id) {
        this.battlefy_id = battlefy_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
