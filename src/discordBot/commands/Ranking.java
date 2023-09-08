package discordBot.commands;

import data.TeamData;
import discordBot.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.*;

public class Ranking extends Command implements SelectEmbed {
    private List<TeamData> templist;
    private List<TeamData> list;
    private List<Calendar> dates;
    private int current = 0;
    private final int viewCount = 10;

    public Ranking(){
        super.name = "ranking";
        super.desc = "get team ranking";
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.reply("loading").queue();
        current = 0;
        Session s = Main.factory.openSession();
        Query<TeamData> query = s.createQuery("from data.TeamData order by elo desc", TeamData.class);
        templist = query.list();
        list = new ArrayList<>();
        dates = new ArrayList<>();
        for(TeamData t: templist){
            Query<Calendar> lastDate = s.createQuery("select MAX(date) FROM data.MatchData m WHERE m.top = :team OR m.bot = :team", Calendar.class);
            lastDate.setParameter("team", t);
            Calendar lastMatch = lastDate.getSingleResult();
            Calendar currentCal = new GregorianCalendar();
            long diff = currentCal.getTime().getTime() - lastMatch.getTimeInMillis();
            if(diff < 30L *3*24*60*60*1000){
                list.add(t);
                dates.add(lastMatch);
            }
        }

        MessageEmbed embed = new EmbedBuilder()
                .setDescription("```" + table() + "```")
                .setFooter("Viewing 1 to " + (current + viewCount)  + " of " + list.size()).build();

        event.getChannel().sendMessageEmbeds(embed).addActionRow(
                Button.primary("ranking start", "start"),
                Button.primary("ranking previous", "previous"),
                Button.primary("ranking next", "next"),
                Button.primary("ranking end", "end")).queue();
    }


    public void editViewMessage(ButtonInteractionEvent event, String desc){
        event.editMessageEmbeds(new EmbedBuilder()
                .setDescription(desc)
                .setFooter("Viewing " + (current + 1) + " to " + (current + viewCount) + " of " + list.size()).build()).queue();
    }

    public String table(){
        List<TeamData> currentList = list.subList(current, current + viewCount);
        int nameMaxLength = 0;
        for(TeamData t: currentList){
            nameMaxLength = Math.max(nameMaxLength, t.getName().length());
        }

        String result = "|Team" + spaces(nameMaxLength - 4) + "|Elo |\n" +
                dashes(nameMaxLength - 4) + "-----------\n" ;
        for(TeamData t: currentList){
            String name = t.getName();
            Calendar d = dates.get(list.indexOf(t));
            result += "|" + name + spaces(nameMaxLength - name.length()) + "|" + t.getElo() + spaces(4 - t.getElo().toString().length()) + "|" +
                    d.get(Calendar.DAY_OF_MONTH) + "/" + (d.get(Calendar.MONTH) + 1) + "/" + d.get(Calendar.YEAR) + "\n";
        }
        return result;
    }

    @Override
    public void next(ButtonInteractionEvent event){
        current = Math.min(current + viewCount, list.size());
        editViewMessage(event,
                "```" + table() + "```");
    }
    @Override
    public void previous(ButtonInteractionEvent event){
        current = Math.max(current - viewCount, 0);
        editViewMessage(event,
                "```" + table() + "```");
    }
    @Override
    public void last(ButtonInteractionEvent event){
        current = list.size() - viewCount;
        editViewMessage(event,
                "```" + table() + "```");
    }
    @Override
    public void first(ButtonInteractionEvent event){
        current = 0;
        editViewMessage(event,
                "```" + table() + "```");
    }

    private String spaces(int n){
        String res = "";
        for(int i = 0; i < n; i++)
            res += " ";
        return res;
    }

    private String dashes(int n){
        String res = "";
        for(int i = 0; i < n; i++)
            res += "-";
        return res;
    }
}
