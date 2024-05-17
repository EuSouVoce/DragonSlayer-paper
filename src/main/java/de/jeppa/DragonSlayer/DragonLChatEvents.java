package de.jeppa.DragonSlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DragonLChatEvents implements Listener {
    DragonSlayer plugin;

    public DragonLChatEvents(final DragonSlayer instance) { this.plugin = instance; }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void lchatListener(final imperio.games.eventos.shaded.legendchatapi.api.events.ChatMessageEvent event) {
        final String tagname = "dragonslayer";
        final CommandSender sender = event.getSender();
        if (sender instanceof final Player player) {
            if (this.plugin.configManager.getPrefixEnabled() && this.plugin.getSlayerUUIDString().equals(player.getUniqueId().toString())) {
                final String prefix = this.plugin.configManager.getPrefix();
                if (!player.getDisplayName().contains(prefix.trim())) {
                    if (event.getTags().contains(tagname)) {
                        event.setTagValue(tagname, prefix);
                    } else {
                        event.addTag(tagname, prefix);
                    }
                }
            }
        }

    }
}
