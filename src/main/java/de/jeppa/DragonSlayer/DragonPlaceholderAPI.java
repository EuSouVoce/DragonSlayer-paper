package de.jeppa.DragonSlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

@SuppressWarnings("deprecation")
public class DragonPlaceholderAPI extends PlaceholderExpansion {
    DragonSlayer plugin;

    public DragonPlaceholderAPI(DragonSlayer instance) { this.plugin = instance; }

    public String getAuthor() { return "Jeppa"; }

    public String getIdentifier() { return this.plugin.getName().toLowerCase(); }

    public String getVersion() { return this.plugin.getDescription().getVersion(); }

    public boolean canRegister() { return Bukkit.getPluginManager().isPluginEnabled(this.plugin.getName()); }

    public boolean register() {
        if (this.canRegister()) {
            try {
                return PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().register(this);
            } catch (Exception | NoSuchMethodError var2) {
                this.plugin.getLogger().warning("Error while registering DragonSlayer into PAPI");
            }
        }

        return false;
    }

    public boolean persist() { return true; }

    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("prefix")) {
            if (this.plugin.configManager.getPrefixEnabled() && this.plugin.getSlayerUUIDString().equals(player.getUniqueId().toString())) {
                String prefix = this.plugin.configManager.getPrefix();
                if (!player.getDisplayName().contains(prefix.trim())) {
                    return prefix;
                }
            }
        } else {
            if (identifier.contains("time") || identifier.equals("nextmap") || identifier.contains("reset")) {
                String world = player.getWorld().getName().toLowerCase();
                String identifier2 = null;
                if (identifier.replaceAll("nexttime([h]?)m([s]?)", "nexttime").startsWith("nexttime_")) {
                    world = identifier.substring(identifier.indexOf("_") + 1, identifier.length()).toLowerCase();
                    identifier2 = identifier.substring(0, identifier.indexOf("_"));
                }

                String textReturn = this.getTimerParsed(world, identifier2 != null ? identifier2 : identifier);
                if (textReturn != null) {
                    return textReturn;
                }

                long Nexttime = -1L;
                String Mapname = "";
                if (!identifier.contains("time") && !identifier.equals("nextmap")) {
                    if (identifier.contains("reset")) {
                        for (WorldRefreshOrReset Res : DragonSlayer.ResetimerList) {
                            long Runtime = System.currentTimeMillis() / 50L - Res.StartTime;
                            long Resttime = Res.OrigRuntime - Runtime;
                            if (Resttime > 0L && Resttime <= Nexttime || Nexttime == -1L) {
                                Nexttime = Resttime;
                                Mapname = Res.Mapname;
                            }
                        }
                    }
                } else {
                    for (DragonRespawn Resp : this.plugin.timerManager.RespawnList) {
                        long Runtime = System.currentTimeMillis() / 50L - Resp.StartTime;
                        long Resttime = Resp.OrigRuntime - Runtime;
                        if (Resttime > 0L && Resttime <= Nexttime || Nexttime == -1L) {
                            Nexttime = Resttime;
                            Mapname = Resp.Mapname;
                        }
                    }
                }

                if (!identifier.equals("nextmap") && !identifier.equals("nextresetmap")) {
                    textReturn = this.getTimerParsed(Mapname, identifier);
                    if (textReturn != null) {
                        return textReturn;
                    }

                    return this.plugin.configManager.getNoTimerPlaceholder();
                }

                return Mapname;
            }

            if (identifier.startsWith("place_")) {
                String uuid = player.getUniqueId().toString();
                ConfigurationSection allScores = this.plugin.leaderManager.Leader.getConfigurationSection("Scores");
                if (allScores != null) {
                    String Str = identifier.substring(identifier.indexOf("_") + 1);
                    String numStr = Str.replaceAll("[_a-zA-Z]", "");
                    int num = Integer.valueOf(!numStr.isEmpty() ? numStr : "0");
                    if (num > 0) {
                        String scoreStr = "";
                        String pName = "";
                        String uid = "NONE";
                        if (LeaderManager.sortKillList.size() >= num) {
                            Entry<String, Object> id = (Entry<String, Object>) LeaderManager.sortKillList.get(num - 1);
                            if (id != null) {
                                int score = ((MemorySection) id.getValue()).getInt("score");
                                scoreStr = !String.valueOf(score).isEmpty() ? String.valueOf(score)
                                        : this.plugin.configManager.getNoRankScorePlaceholder();
                                uid = (String) id.getKey();
                                OfflinePlayer player_ = Bukkit.getOfflinePlayer(UUID.fromString(uid.trim()));
                                pName = player_.getName();
                                if (pName == null || pName.isEmpty()) {
                                    pName = this.plugin.configManager.getUnknownNamePlaceholder();
                                }
                            }
                        } else {
                            scoreStr = this.plugin.configManager.getNoRankScorePlaceholder();
                            pName = this.plugin.configManager.getNoRankNamePlaceholder();
                        }

                        if (Str.contains("_")) {
                            String ident2 = Str.substring(Str.lastIndexOf("_") + 1).trim().toLowerCase();
                            if (ident2.equals("name")) {
                                return pName;
                            }

                            if (ident2.equals("score")) {
                                return scoreStr;
                            }
                        }

                        String retString = this.plugin.configManager.getScoreboardString().replace("$player", pName).replace("$score",
                                scoreStr);
                        if (retString == null || retString != null && retString.isEmpty()) {
                            retString = pName + " : " + scoreStr;
                        }

                        if (uid.equals(uuid)) {
                            retString = ChatColor.RED + ">>" + retString + ChatColor.RED + "<<" + ChatColor.RESET;
                        }

                        return retString;
                    }
                }
            } else {
                if (identifier.equals("mykills")) {
                    String myScore = this.plugin.leaderManager.Leader.getString("Scores." + player.getUniqueId().toString() + ".score");
                    return myScore != null ? myScore : "0";
                }

                if (identifier.equals("myplace")) {
                    String uuid = player.getUniqueId().toString();
                    List<String> retVal = new ArrayList<String>();
                    LeaderManager.sortKillList.forEach((uidx) -> {
                        if (((String) uidx.getKey()).equals(uuid)) {
                            retVal.add(String.valueOf(LeaderManager.sortKillList.indexOf(uidx) + 1));
                        }

                    });
                    return retVal.size() > 0 ? (String) retVal.get(0) : "0";
                }

                if (identifier.equals("slayer")) {
                    String slayer = this.plugin.configManager.getSlayerPAPINick();
                    return slayer != null ? slayer : "";
                }
            }
        }

        return "";
    }

    private String getTimerParsed(String Mapname, String identifier) {
        String[] nextSpawn = this.plugin.getWorldsNextSpawnsOrReset(Mapname, true, false);
        String[] nextReset = this.plugin.getWorldsNextSpawnsOrReset(Mapname, false, true);
        if (nextSpawn != null) {
            if (identifier.equals("timer")) {
                return Mapname + ": " + String.format("%s Day(s), %s:%s:%s", nextSpawn[0], nextSpawn[1], nextSpawn[2], nextSpawn[3]);
            }

            if (identifier.equals("nexttime")) {
                return String.format("%s Day(s), %s:%s:%s", nextSpawn[0], nextSpawn[1], nextSpawn[2], nextSpawn[3]);
            }

            if (identifier.equals("nexttimehms")) {
                return String.format("%s:%s:%s",
                        String.format("%02d", Integer.parseInt(nextSpawn[0]) * 24 + Integer.parseInt(nextSpawn[1])), nextSpawn[2],
                        nextSpawn[3]);
            }

            if (identifier.equals("nexttimehm")) {
                return String.format("%s:%s", String.format("%02d", Integer.parseInt(nextSpawn[0]) * 24 + Integer.parseInt(nextSpawn[1])),
                        nextSpawn[2]);
            }

            if (identifier.equals("nexttimems")) {
                return String.format("%s:%s", String.format("%02d",
                        (Integer.parseInt(nextSpawn[0]) * 24 + Integer.parseInt(nextSpawn[1])) * 60 + Integer.parseInt(nextSpawn[2])),
                        nextSpawn[3]);
            }
        }

        if (nextReset != null) {
            if (identifier.equals("nextreset")) {
                return String.format("%s Day(s), %s:%s:%s", nextReset[0], nextReset[1], nextReset[2], nextReset[3]);
            }

            if (identifier.equals("nextresethms")) {
                return String.format("%s:%s:%s",
                        String.format("%02d", Integer.parseInt(nextReset[0]) * 24 + Integer.parseInt(nextReset[1])), nextReset[2],
                        nextReset[3]);
            }

            if (identifier.equals("nextresethm")) {
                return String.format("%s:%s", String.format("%02d", Integer.parseInt(nextReset[0]) * 24 + Integer.parseInt(nextReset[1])),
                        nextReset[2]);
            }

            if (identifier.equals("nextresetms")) {
                return String.format("%s:%s", String.format("%02d",
                        (Integer.parseInt(nextReset[0]) * 24 + Integer.parseInt(nextReset[1])) * 60 + Integer.parseInt(nextReset[2])),
                        nextReset[3]);
            }
        }

        return null;
    }

    public String onRequest(OfflinePlayer player, String identifier) {
        return player != null && player.isOnline() ? this.onPlaceholderRequest((Player) player, identifier) : null;
    }
}
