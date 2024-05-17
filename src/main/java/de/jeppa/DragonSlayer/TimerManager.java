package de.jeppa.DragonSlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class TimerManager {
    DragonSlayer plugin;
    ArrayList<DragonRespawn> RespawnList = new ArrayList<DragonRespawn>();
    private FileConfiguration Timer = null;
    private File TimerFile = null;

    public TimerManager(final DragonSlayer instance) { this.plugin = instance; }

    private void reloadTimers() {
        if (this.TimerFile == null) {
            this.TimerFile = new File(this.plugin.getDataFolder(), "TimerList.yml");
        }

        this.Timer = YamlConfiguration.loadConfiguration(this.TimerFile);
    }

    FileConfiguration getTimerlist() {
        if (this.Timer == null) {
            this.reloadTimers();
        }

        return this.Timer;
    }

    void saveTimerlist() {
        if (this.Timer != null && this.TimerFile != null) {
            try {

                final List<String> headerStrings = new ArrayList<String>();
                headerStrings.add("### This file contains the timers that were running when the server restarted ###");
                headerStrings.add("###  and the list of players who already used their first-join-dragon-spawn.  ###");
                this.getTimerlist().options().setHeader(headerStrings);

                this.getTimerlist().save(this.TimerFile);
            } catch (final IOException var2) {
                this.plugin.logger.warning("Could not save timerlist to " + this.TimerFile);
            }

        }
    }

    void RestartTimers() {
        if (this.Timer.contains("Timerlist.1")) {
            for (int i = 1; this.Timer.getConfigurationSection("Timerlist." + i) != null; ++i) {
                final long Resttime = this.Timer.getLong("Timerlist." + i + ".Resttime");
                final String Mapname = this.Timer.getString("Timerlist." + i + ".Mapname");
                this.StartTimer(Mapname, Resttime);
                if (this.plugin.configManager.getVerbosity()) {
                    this.plugin.logger.info("Restarting a timer for dragonspawn on: " + Mapname);
                }
            }
        }

        for (final String Mapname : this.plugin.configManager.getMaplist()) {
            boolean found = false;
            final int Resttime = 6000;

            for (final DragonRespawn Resp : this.RespawnList) {
                if (Mapname.equals(Resp.worldName)) {
                    found = true;
                    break;
                }
            }

            if (!found && this.plugin.configManager.getAutofix(Mapname) && this.plugin.configManager.getDelay(Mapname.toLowerCase()) > 0) {
                this.StartTimer(Mapname, (long) Resttime);
                if (this.plugin.configManager.getVerbosity()) {
                    this.plugin.logger.info("Start of an additional fix-timer on: " + Mapname);
                }
            }
        }

        if (this.Timer.contains("ResetTimerlist.1")) {
            for (int i = 1; this.Timer.getConfigurationSection("ResetTimerlist." + i) != null; ++i) {
                final long Resttime = this.Timer.getLong("ResetTimerlist." + i + ".Resttime");
                final String Mapname = this.Timer.getString("ResetTimerlist." + i + ".Mapname");
                this.plugin.StartWorldResetTimer(Mapname, Resttime, (long) this.plugin.configManager.getWarnTime(Mapname));
                if (this.plugin.configManager.getVerbosity()) {
                    this.plugin.logger.info("Restarting a reset-timer on: " + Mapname);
                }
            }
        }

    }

    public void StartTimer(final String Mapname, final long Resttime) {
        final DragonRespawn Resp = this.createStartTimer(Mapname, Resttime);
        Resp.taskId = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, Resp, Resttime).getTaskId();
    }

    DragonRespawn createStartTimer(final String Mapname, final long Resttime) {
        final DragonRespawn Resp = new DragonRespawn(this.plugin);
        Resp.worldName = Mapname;
        Resp.OrigRuntime = Resttime;
        return Resp;
    }

    public void clearTimers() {
        this.Timer.set("Timerlist", (Object) null);
        this.Timer.set("ResetTimerlist", (Object) null);

        for (DragonRespawn Resp : this.RespawnList) {
            this.plugin.getServer().getScheduler().cancelTask(Resp.taskId);
            Resp = null;
        }

        for (WorldRefreshOrReset Res : DragonSlayer.ResetimerList) {
            this.plugin.getServer().getScheduler().cancelTask(Res.taskId);
            Res = null;
        }

        this.RespawnList.clear();
        DragonSlayer.ResetimerList.clear();
        this.saveTimerlist();
    }

    void updateTimerList() {
        this.Timer.set("Timerlist", (Object) null);
        this.Timer.set("ResetTimerlist", (Object) null);
        int i = 1;

        for (final DragonRespawn Resp : this.RespawnList) {
            final Long Resttime = this.plugin.remainingTimerDuration(Resp);
            if (Resttime != null && Resttime > 0L) {
                this.Timer.set("Timerlist." + i + ".Mapname", Resp.worldName);
                this.Timer.set("Timerlist." + i + ".Resttime", Resttime);
                ++i;
            }
        }

        for (int var5 = 1; var5 <= DragonSlayer.ResetimerList.size(); ++var5) {
            final WorldRefreshOrReset Res = (WorldRefreshOrReset) DragonSlayer.ResetimerList.get(var5 - 1);
            final Long Resttime = this.plugin.remainingResetDuration(Res);
            if (Resttime != null && Resttime > 0L) {
                this.Timer.set("ResetTimerlist." + var5 + ".Mapname", Res.Mapname);
                this.Timer.set("ResetTimerlist." + var5 + ".Resttime", Resttime);
            }
        }

    }

    void addPlayerToList(final Player player, final String world) {
        final String uuid = player.getUniqueId().toString();
        if (!this.checkPlayerOnList(player, world)) {
            this.Timer.set("PlayersUsedFirstspawn." + world + "." + uuid, true);
            this.saveTimerlist();
        }

    }

    boolean checkPlayerOnList(final Player player, final String world) {
        final String uuid = player.getUniqueId().toString();
        final String section = "PlayersUsedFirstspawn." + world;
        return this.Timer.getConfigurationSection(section) != null && this.Timer.getConfigurationSection(section).contains(uuid)
                ? this.Timer.getConfigurationSection(section).getBoolean(uuid)
                : false;
    }

    void clearPlayerList() {
        final String section = "PlayersUsedFirstspawn";
        if (this.Timer.getConfigurationSection(section) != null) {
            this.Timer.set(section, (Object) null);
            this.saveTimerlist();
        }

    }
}
