package de.jeppa.DragonSlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class WorldRefreshOrReset implements Runnable {
    DragonSlayer plugin;
    public String Mapname = null;
    public long StartTime = System.currentTimeMillis() / 50L;
    public long OrigRuntime = 0L;
    public long Warntime = 1L;
    public int taskId;

    public WorldRefreshOrReset(DragonSlayer instance) {
        this.plugin = instance;
        DragonSlayer.ResetimerList.add(this);
    }

    public void run() {
        if (this.Mapname != null) {
            if (this.plugin.getDragonCount(this.Mapname) <= 0) {
                for (Entity player : Bukkit.getServer().getWorld(this.Mapname).getEntitiesByClasses(new Class[] { Player.class })) {
                    if (!this.plugin.configManager.getResetWorld(this.Mapname)
                            && !this.plugin.configManager.getRefreshWorld(this.Mapname)) {
                        if (this.plugin.configManager.getRespawnPlayer(this.Mapname)) {
                            ((Player) player).sendMessage(ChatColor.RED + this.plugin.configManager.getPlayerRespawnMessage(this.Mapname));
                        }
                    } else {
                        ((Player) player).sendMessage(ChatColor.RED + this.plugin.configManager.getResetMessage(this.Mapname));
                    }
                }
            }

            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                String origMapname = Bukkit.getServer().getWorld(this.Mapname).getName();
                if (this.plugin.getDragonCount(this.Mapname) <= 0) {
                    if (this.plugin.configManager.getRefreshWorld(this.Mapname)) {
                        this.plugin.WorldRefresh(origMapname);
                    } else {
                        this.plugin.WorldReset(origMapname, false);
                    }
                }

                DragonSlayer.ResetimerList.remove(this);
            }, this.Warntime - 20L);
        } else {
            DragonSlayer.ResetimerList.remove(this);
        }

    }
}
