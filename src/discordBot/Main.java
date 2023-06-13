package discordBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.EnumSet;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Main {
    public static SessionFactory factory;
    public static void initialize(SessionFactory hibernateFactory){
        factory = hibernateFactory;
        String token = null;
        try {
            token = Files.readString(Paths.get("token.txt")).trim();
        } catch (IOException e) {
            ErrorLogger.log(e);
        }

        JDA jda = JDABuilder.createLight(token, EnumSet.noneOf(GatewayIntent.class))
                .addEventListeners(new CommandHandler())
                .build();
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("elo", "Get elo of a team")
                        .addOptions(new OptionData(STRING, "team", "filter by team")
                                .setRequired(true))
        );

        commands.addCommands(
                Commands.slash("update", "Get new matches from battlefy")
        );

        commands.addCommands(
                Commands.slash("ranking", "get team ranking")
        );

        commands.queue();
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            ErrorLogger.log(e);
        }
    }
}
