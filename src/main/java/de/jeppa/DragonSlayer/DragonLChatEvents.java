package de.jeppa.DragonSlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DragonLChatEvents implements Listener {
    DragonSlayer plugin;

    public DragonLChatEvents(DragonSlayer instance) { this.plugin = instance; }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void lchatListener(imperio.games.eventos.shaded.legendchatapi.api.events.ChatMessageEvent event) {
        String tagname = "dragonslayer";
        CommandSender sender = event.getSender();
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (this.plugin.configManager.getPrefixEnabled() && this.plugin.getSlayerUUIDString().equals(p.getUniqueId().toString())) {
                String prefix = this.plugin.configManager.getPrefix();
                if (!p.getDisplayName().contains(prefix.trim())) {
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
