package discordBot.commands;

import data.TeamData;
import discordBot.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class Ranking extends ListenerAdapter {
    private static List<TeamData> list;
    private static int current = 0;
    private static int viewCount = 10;
    public static void run(SlashCommandInteractionEvent event) {
        Session s = Main.factory.openSession();
        Query<TeamData> query = s.createQuery("from data.TeamData order by elo desc", TeamData.class);
        list = query.list();

        MessageEmbed embed = new EmbedBuilder()
                .setDescription("```" + table() + "```")
                .setFooter("Viewing 1 to " + (current + viewCount)  + " of " + list.size()).build();

        event.replyEmbeds(embed).addActionRow(
                Button.primary("start", "start"),
                Button.primary("previous", "previous"),
                Button.primary("next", "next"),
                Button.primary("end", "end")).queue();
    }

    public static void editViewMessage(ButtonInteractionEvent event, String desc){
        event.editMessageEmbeds(new EmbedBuilder()
                .setDescription(desc)
                .setFooter("Viewing " + (current + 1) + " to " + (current + viewCount) + " of " + list.size()).build()).queue();
    }

    private static String table(){
        List<TeamData> currentList = list.subList(current, current + viewCount);
        int nameMaxLength = 0;
        for(TeamData t: currentList){
            nameMaxLength = Math.max(nameMaxLength, t.getName().length());
        }

        String result = "|Team" + spaces(nameMaxLength - 4) + "|Elo |\n" +
                dashes(nameMaxLength - 4) + "-----------\n" ;
        for(TeamData t: currentList){
            String name = t.getName();
            result += "|" + name + spaces(nameMaxLength - name.length()) + "|" + t.getElo() + spaces(4 - t.getElo().toString().length()) + "|\n";
        }
        return result;
    }

    public static String next(){
        current = Math.min(current + viewCount, list.size());
        return table();
    }

    public static String previous(){
        current = Math.max(current - viewCount, 0);
        return table();
    }

    public static String last(){
        current = list.size() - viewCount;
        return table();
    }

    public static String first(){
        current = 0;
        return table();
    }

    private static String spaces(int n){
        String res = "";
        for(int i = 0; i < n; i++)
            res += " ";
        return res;
    }

    private static String dashes(int n){
        String res = "";
        for(int i = 0; i < n; i++)
            res += "-";
        return res;
    }
}
