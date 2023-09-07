package data;

import java.util.List;
import java.util.Map;

public class Player {
    public static Map<String, String> players;
    private String name;
    private String battlefyid;

    public Player(String id){
        battlefyid = id;
        name = players.get(id);
    }

    public PlayerData getPlayerData(){
        PlayerData d = new PlayerData();
        d.setName(name);
        d.setBattlefy_id(battlefyid);
        return d;
    }
}
