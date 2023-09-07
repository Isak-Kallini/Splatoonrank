package discordBot;

import discordBot.commands.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler extends ListenerAdapter {
    static Map<String, Command> commandMap = new HashMap<>();

    public static void init(CommandListUpdateAction commands){
        var classGraph = new ClassGraph().acceptPackages("discordBot.commands");
        try(ScanResult result = classGraph.scan()){
            ClassInfoList list = result.getSubclasses(Command.class);
            List<Class<?>> classes = list.loadClasses();
            classes.forEach(c -> {
                    try {
                        Command command = (Command) c.getDeclaredConstructor().newInstance();
                        commandMap.put(command.getName(), command);
                        commands.addCommands(
                                Commands.slash(command.getName(), command.getDesc())
                                        .addOptions(command.getOptions())
                        );
                    } catch (Exception e) {
                        ErrorLogger.log(e);
                    }
            });
        }
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
            List<String> eventValues = event.getValues();
            ((Elo) commandMap.get("elo")).replyTeamStats(event, Integer.parseInt(eventValues.get(eventValues.size() - 1).split(" ")[0]), Main.factory.openSession());
        }
    }
}
