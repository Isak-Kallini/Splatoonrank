package data;

import jakarta.persistence.*;

import java.util.Calendar;

@Entity
@Table(name = "matches")
public class MatchData{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "top_id", referencedColumnName = "id")
    private TeamData top;
    @Column(name = "topScore")
    private Integer topScore;
    @Column(name = "topelo")
    private Integer topElo;
    @ManyToOne
    @JoinColumn(name = "bot_id", referencedColumnName = "id")
    private TeamData bot;
    @Column(name = "botScore")
    private Integer botScore;
    @Column(name = "botelo")
    private Integer botElo;
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;
    @Column(name = "battlefy_id", unique = true)
    private String battlefyId;

    public MatchData(){}

    public void setTop(TeamData top) {
        this.top = top;
    }

    public void setTopScore(Integer topScore) {
        this.topScore = topScore;
    }

    public void setTopElo(Integer topElo) {
        this.topElo = topElo;
    }

    public void setBot(TeamData bot) {
        this.bot = bot;
    }

    public void setBotScore(Integer botScore) {
        this.botScore = botScore;
    }

    public void setBotElo(Integer botElo) {
        this.botElo = botElo;
    }

    public void set(TeamData t){
        if(t.getIdent().equals("top")){
            setTop(t);
        }else{
            setBot(t);
        }
    }

    public TeamData get(TeamData t){
        if(t.getIdent().equals("top")){
            return getTop();
        }else{
            return getBot();
        }
    }

    public TeamData getTop() {
        return top;
    }

    public Integer getTopScore() {
        return topScore;
    }

    public Integer getTopElo() {
        return topElo;
    }

    public TeamData getBot() {
        return bot;
    }

    public Integer getBotScore() {
        return botScore;
    }

    public Integer getBotElo() {
        return botElo;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getBattlefyId() {
        return battlefyId;
    }

    public void setBattlefyId(String battlefyId) {
        this.battlefyId = battlefyId;
    }
}
