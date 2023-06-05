package discordBot;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

public class CommandHandler extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if (event.getGuild() == null)
            return;
        try {
            switch (event.getName()){
                case "elo" -> discordBot.commands.Elo.run(event);
                case "update" -> discordBot.commands.Update.run(event);
            }
        }catch (Exception e) {
            ErrorLogger.log(e);
        }
    }
}
