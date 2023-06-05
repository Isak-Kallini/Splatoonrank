package discordBot.commands;

import data.MatchData;
import data.Team;
import data.TeamData;
import discordBot.ErrorLogger;
import discordBot.Main;
import graphs.EloGraph;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jfree.chart.ChartUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class Elo {

    public static void run(SlashCommandInteractionEvent event) {
        String name = event.getOption("team", null, OptionMapping::getAsString);
        Session s = Main.factory.openSession();
        Query nameQuery = s.createQuery("from data.TeamData t where t.name like :name");
        nameQuery.setParameter("name", "%" + name + "%");
        Iterator<TeamData> nameIterator = (Iterator<TeamData>) nameQuery.stream().iterator();
        if(nameIterator.hasNext()) {
            TeamData team = nameIterator.next();
            System.out.println(team.getName());
            Query query = s.
                    createQuery("from data.MatchData t where t.top = :key");
            query.setParameter("key", team);
            List<MatchData> topres = (List<MatchData>) query.stream().toList();

            Query query2 = s.
                    createQuery("from data.MatchData t where t.bot = :key");
            query2.setParameter("key", team);
            List<MatchData> botres = (List<MatchData>) query2.stream().toList();

            TreeMap<Calendar, Integer> res = new TreeMap<>();
            for (MatchData m : topres) {
                res.put(m.getDate(), m.getTopElo());
            }
            for (MatchData m : botres) {
                res.put(m.getDate(), m.getBotElo());
            }
            List<Integer> data = res.values().stream().toList();

            EloGraph test = new EloGraph(team.getName(), data);
            File file = new File(team.getName() + ".png");
            try {
                FileOutputStream out = new FileOutputStream(file);
                ChartUtils.writeChartAsPNG(out, test.getChart(), 900, 500);
            } catch (Exception e) {
                ErrorLogger.log(e);
            }
            int wins = 0;
            int losses = 0;

            for (MatchData m : topres) {
                if (m.getTopScore() > m.getBotScore())
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
            //event.reply("yo").queue();
            event.replyEmbeds(embed).addFiles(FileUpload.fromData(file, "graph.png")).queue();

            file.deleteOnExit();
        }else{
            event.reply("Team not found").queue();
        }
    }
}
