package discordBot.commands;

import data.MatchData;
import data.TeamData;
import discordBot.ErrorLogger;
import discordBot.Main;
import graphs.EloGraph;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jfree.chart.ChartUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Elo extends Command implements SelectEmbed {
    private List<TeamData> list;
    private List<String> teamStringList;
    private int current = 0;
    private final int viewCount = 10;

    public Elo() {
        super.name = "elo";
        super.desc = "Get elo of a team";
        super.options.add(new OptionData(STRING, "team", "filter by team").setRequired(true));
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String name = event.getOption("team", null, OptionMapping::getAsString);
        Session s = Main.factory.openSession();
        Query<TeamData> nameQuery = s.createQuery("from data.TeamData t where t.name like :name order by (select MAX(date) FROM data.MatchData m WHERE m.top = t OR m.bot = t) desc", TeamData.class);
        nameQuery.setParameter("name", "%" + name + "%");
        List<TeamData> nameList = nameQuery.list();
        list = nameList;
        if(!nameList.isEmpty()) {
            if(nameList.size() == 1) {
                replyTeamStats(event, 1, s);
            }else{
                StringBuilder result = new StringBuilder();
                teamStringList = new ArrayList<>();
                int i = 1;
                for(TeamData t: nameList){
                    String str = i + " " + t.getElo() + " " + t.getName() + "\n";
                    result.append(str);
                    teamStringList.add(str);
                    i++;
                    if(i > viewCount){
                        break;
                    }
                }

                MessageEmbed embed = (new EmbedBuilder()
                        .setTitle("Teams matching: " + name)
                        .setDescription(result.toString())).build();


                event.replyEmbeds(embed).addActionRow(
                        StringSelectMenu.create("teamEloCommand").addOptions(getOptionList()).build())
                        .addActionRow(
                        Button.primary("elo start", "start"),
                        Button.primary("elo previous", "previous"),
                        Button.primary("elo next", "next"),
                        Button.primary("elo end", "end")
                ).queue();
            }
        }else{
            event.reply("Team not found").queue();
        }
        s.close();
    }

    public void editSearchMessage(ButtonInteractionEvent event, String desc){
        System.out.println(teamStringList.get(0));
        event.editComponents(
                ActionRow.of(StringSelectMenu.create("teamEloCommand").addOptions(getOptionList()).build()),
                ActionRow.of(
                        Button.primary("elo start", "start"),
                        Button.primary("elo previous", "previous"),
                        Button.primary("elo next", "next"),
                        Button.primary("elo end", "end")
                )).queue();
        event.getInteraction().getHook().editOriginalEmbeds(new EmbedBuilder()
                .setDescription(desc)
                .setFooter("Viewing " + (current + 1) + " to " + (current + viewCount) + " of " + list.size()).build()).queue();
    }

    public String table(){
        String result = "";
        teamStringList = new ArrayList<>();
        int i = 1;
        for(TeamData t: list.subList(current, Math.min(current + viewCount, list.size()))){
            String str = i + " " + t.getElo() + " " + t.getName() + "\n";
            result += str;
            teamStringList.add(str);
            i++;
        }
        return result;
    }

    public void next(ButtonInteractionEvent event){
        if(list.size() > viewCount) {
            current = Math.min(current + viewCount, list.size() - 1);
            editSearchMessage(event, table());
        }else {
            event.deferEdit().queue();
        }
    }

    public void previous(ButtonInteractionEvent event){
        current = Math.max(current - viewCount, 0);
        editSearchMessage(event, table());
    }

    public void last(ButtonInteractionEvent event){
        if(list.size() > viewCount) {
            current = list.size() - viewCount;
            editSearchMessage(event, table());
        }else {
            event.deferEdit().queue();
        }
    }

    public void first(ButtonInteractionEvent event){
        current = 0;
        editSearchMessage(event, table());
    }

    private List<SelectOption> getOptionList(){
        List<SelectOption> optionList = new ArrayList<>();
        for(String k: teamStringList){
            optionList.add(SelectOption.of(k, k));
        }
        return optionList;
    }

    public void replyTeamStats(IReplyCallback event, Integer n, Session s) {

        TeamData team = list.get(current + n - 1);

        /*Query<MatchData> query = s.
                createQuery("from data.MatchData t where t.top = :key", MatchData.class);
        query.setParameter("key", team);
        List<MatchData> topres = query.stream().toList();

        Query<MatchData> query2 = s.
                createQuery("from data.MatchData t where t.bot = :key", MatchData.class);
        query2.setParameter("key", team);
        List<MatchData> botres = query2.stream().toList();

        TreeMap<Calendar, Integer> res = new TreeMap<>();
        for (MatchData m : topres) {
            res.put(m.getDate(), m.getTopElo());
        }
        for (MatchData m : botres) {
            res.put(m.getDate(), m.getBotElo());
        }*/

        Query<MatchData> matchDataQuery = s.createQuery("FROM data.MatchData t WHERE t.bot = :key OR t.top = :key", MatchData.class);
        matchDataQuery.setParameter("key", team);
        List<MatchData> queryRes = matchDataQuery.list();

        TreeMap<Calendar, Integer> res = new TreeMap<>();
        for(MatchData m: queryRes){
            if(m.getTop().equals(team)){
                res.put(m.getDate(), m.getTopElo());
            }else if(m.getBot().equals(team)){
                res.put(m.getDate(), m.getBotElo());
            }
        }

        List<Integer> data = res.values().stream().toList();

        EloGraph test = new EloGraph(team.getName(), data);
        File file = new File(team.getName() + ".png");
        try {
            FileOutputStream out = new FileOutputStream(file);
            ChartUtils.writeChartAsPNG(out, test.getChart(), 900, 500);
            out.close();
        } catch (Exception e) {
            ErrorLogger.log(e);
        }
        int wins = 0;
        int losses = 0;

        for (MatchData m : queryRes) {
            if(isWin(m, team))
                wins++;
            else
                losses++;
        }

        MessageEmbed embed = new EmbedBuilder()
                .setTitle(team.getName())
                .setDescription("Current elo: " + team.getElo() + "\n" +
                        "Number of matches: " + data.size() + "\n" +
                        "Winrate: " + ((float) wins / (wins + losses)) + "\n" +
                        "Highest elo: " + Collections.max(data) + "\n" +
                        "Lowest elo: " + Collections.min(data))
                .setImage("attachment://graph.png").build();
        event.replyEmbeds(embed).addFiles(FileUpload.fromData(file, "graph.png")).complete();

        file.delete();
    }

    private boolean isWin(MatchData m, TeamData team){
        boolean teamIsTop = m.getTop().equals(team);
        return m.getTopScore() > m.getBotScore() && teamIsTop || m.getTopScore() < m.getBotScore() && !teamIsTop;
    }
}
