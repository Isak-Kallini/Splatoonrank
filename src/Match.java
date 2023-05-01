import isak.Team;
import jakarta.persistence.*;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Match implements Comparable<Match>{
    @Id
    @Column(name = "id")
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "top_id", referencedColumnName = "id")
    private Team top;
    @Column(name = "topScore")
    private Integer topScore;
    @Column(name = "topelo")
    private Integer topElo;

    @ManyToOne
    @JoinColumn(name = "bot_id", referencedColumnName = "id")
    private Team bot;
    @Column(name = "botScore")
    private Integer botScore;
    @Column(name = "botelo")
    private Integer botElo;
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar date;
    public Match(JSONObject json){
        System.out.println("match: " + json.getString("_id"));
        //top = new Team(json.getJSONObject("top"));
        topScore = json.getJSONObject("top").getInt("score");
        //bot = new Team(json.getJSONObject("bottom"));
        botScore = json.getJSONObject("bottom").getInt("score");

        String completed = json.getString("completedAt");
        date = new GregorianCalendar();
        date.set(Calendar.YEAR, Integer.parseInt(completed.substring(0, 4)));
        date.set(Calendar.MONTH, Integer.parseInt(completed.substring(5, 7)));
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(completed.substring(8, 10)));
        date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(completed.substring(11, 13)));
        date.set(Calendar.MINUTE, Integer.parseInt(completed.substring(14,16)));
        date.set(Calendar.SECOND, Integer.parseInt(completed.substring(17, 19)));
    }

    public String toString(){
        return top.getName() + " " + topScore + "-" + botScore + " " + bot.getName();
    }

    @Override
    public int compareTo(Match t) {
        return date.compareTo(t.date);
    }

    public void setTop(Team top) {
        this.top = top;
    }

    public void setTopScore(Integer topScore) {
        this.topScore = topScore;
    }

    public void setTopElo(Integer topElo) {
        this.topElo = topElo;
    }

    public void setBot(Team bot) {
        this.bot = bot;
    }

    public void setBotScore(Integer botScore) {
        this.botScore = botScore;
    }

    public void setBotElo(Integer botElo) {
        this.botElo = botElo;
    }

    public Team getTop() {
        return top;
    }

    public Integer getTopScore() {
        return topScore;
    }

    public Integer getTopElo() {
        return topElo;
    }

    public Team getBot() {
        return bot;
    }

    public Integer getBotScore() {
        return botScore;
    }

    public Integer getBotElo() {
        return botElo;
    }
}
