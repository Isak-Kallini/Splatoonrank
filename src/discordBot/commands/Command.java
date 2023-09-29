package discordBot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {
    protected String name;
    protected String desc;
    protected List<OptionData> options = new ArrayList<>();
    public abstract void run(SlashCommandInteractionEvent event);
    public String getName() {
        return name;
    }
    public String getDesc(){return desc;}
    public List<OptionData> getOptions(){
        return options;
    }
}
