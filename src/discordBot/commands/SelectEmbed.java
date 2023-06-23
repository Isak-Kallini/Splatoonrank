package discordBot.commands;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface SelectEmbed {
    String table();
    void next(ButtonInteractionEvent event);
    void previous(ButtonInteractionEvent event);
    void last(ButtonInteractionEvent event);
    void first(ButtonInteractionEvent event);
}
