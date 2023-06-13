package discordBot;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

import static discordBot.commands.Ranking.*;

public class CommandHandler extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if (event.getGuild() == null)
            return;
        try {
            switch (event.getName()){
                case "elo" -> discordBot.commands.Elo.run(event);
                case "update" -> discordBot.commands.Update.run(event);
                case "ranking" -> discordBot.commands.Ranking.run(event);
            }
        }catch (Exception e) {
            ErrorLogger.log(e);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event){
        if(event.getComponentId().startsWith("start")){
            editViewMessage(event,
                    "```" + first() + "```");
        } else if(event.getComponentId().startsWith("next")){
            editViewMessage(event,
                    "```" + next() + "```");
        }else if(event.getComponentId().startsWith("previous")){
            editViewMessage(event,
                    "```" + previous() + "```");
        }else if(event.getComponentId().startsWith("end")){
            editViewMessage(event,
                    "```" + last() + "```");
        }
    }
}
