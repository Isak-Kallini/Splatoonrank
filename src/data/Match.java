package data;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Match implements Comparable<Match>{
    private Integer id;
    private Team top;
    private Integer topScore;
    private Integer topElo = 0;
    private Team bot;
    private Integer botScore;
    private Integer botElo = 0;
    private Calendar date;

    private String battlefyId;

    public Match(JSONObject json){
        top = new Team(json.getJSONObject("top"));
        topScore = json.getJSONObject("top").getInt("score");
        bot = new Team(json.getJSONObject("bottom"));
        botScore = json.getJSONObject("bottom").getInt("score");

        String completed = json.getString("completedAt");
        date = new GregorianCalendar();
        date.set(Calendar.YEAR, Integer.parseInt(completed.substring(0, 4)));
        date.set(Calendar.MONTH, Integer.parseInt(completed.substring(5, 7)) - 1);
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(completed.substring(8, 10)));
        date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(completed.substring(11, 13)));
        date.set(Calendar.MINUTE, Integer.parseInt(completed.substring(14,16)));
        date.set(Calendar.SECOND, Integer.parseInt(completed.substring(17, 19)));
        setBattlefyId(json.getString("_id"));
        System.out.println("match: " + top.getName() + " vs " + bot.getName() + " - " + date.get(Calendar.YEAR) + date.get(Calendar.MONTH) + date.get(Calendar.DAY_OF_MONTH));
    }

    public MatchData getData(){
        MatchData data = new MatchData();
        data.setBot(getBot().getData());
        data.setBotElo(getBotElo());
        data.setBotScore(getBotScore());
        data.setTop(getTop().getData());
        data.setTopElo(getTopElo());
        data.setTopScore(getTopScore());
        data.setDate(getDate());
        data.setBattlefyId(getBattlefyId());
        return data;
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

    public void setDate(Calendar date) {
        this.date = date;
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

    public Calendar getDate() { return date; }
    public String getBattlefyId() {
        return battlefyId;
    }

    public void setBattlefyId(String battlefyId) {
        this.battlefyId = battlefyId;
    }
}
