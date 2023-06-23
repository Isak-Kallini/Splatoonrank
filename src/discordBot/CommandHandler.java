package discordBot;

import discordBot.commands.*;
import jakarta.activation.CommandMap;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

import static discordBot.commands.Ranking.*;

public class CommandHandler extends ListenerAdapter {
    static Map<String, Command> commandMap = new HashMap<>();

    public static void init(){
        commandMap.put("elo", new Elo());
        commandMap.put("update", new Update());
        commandMap.put("ranking", new Ranking());
    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if (event.getGuild() == null)
            return;
        try {
            commandMap.get(event.getName()).run(event);
        }catch (Exception e) {
            ErrorLogger.log(e);
        }
    }

    //componentId format: "<class> <function>"
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event){
        String[] s = event.getComponentId().split(" ");
        Command c = commandMap.get(s[0]);
        String func = s[1];
        switch (func) {
            case "start" -> ((SelectEmbed) c).first(event);
            case "next" -> ((SelectEmbed) c).next(event);
            case "previous" -> ((SelectEmbed) c).previous(event);
            case "end" -> ((SelectEmbed) c).last(event);
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event){
        if(event.getComponentId().equals("teamEloCommand")){
            ((Elo) commandMap.get("elo")).replyTeamStats(event, Integer.parseInt(event.getValues().get(event.getValues().size() - 1).split(" ")[0]), Main.factory.openSession());
        }
    }
}
