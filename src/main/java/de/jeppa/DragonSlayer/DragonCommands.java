package de.jeppa.DragonSlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.ImmutableMap;

@SuppressWarnings("deprecation")
public class DragonCommands implements CommandExecutor, TabCompleter {
    DragonSlayer plugin;
    private long removeSpawnConfirmTimer = 0L;
    private long resetConfirmTimer = 0L;
    private final Map<String, String> mainArgumentsAndPerms = ImmutableMap.<String, String> builder().put("help", "dragonslayer")
            .put("reload", "dragonslayer.reload").put("setspawn", "dragonslayer.setspawn").put("getspawn", "dragonslayer.getspawn")
            .put("addspawn", "dragonslayer.setspawn").put("remspawn", "dragonslayer.setspawn").put("setstatue", "dragonslayer.setspawn")
            .put("getstatue", "dragonslayer.getspawn").put("remstatue", "dragonslayer.setspawn")
            .put("forcerespawn", "dragonslayer.forcerespawn").put("forceallrespawn", "dragonslayer.forcerespawn")
            .put("worldrefresh", "dragonslayer.worldreset").put("worldreset", "dragonslayer.worldreset")
            .put("showtimer", "dragonslayer.info").put("cleartimer", "dragonslayer.reload").put("removedragons", "dragonslayer.admin")
            .put("scoreboardreset", "dragonslayer.admin").put("firstjoinreset", "dragonslayer.admin").put("name", "dragonslayer.admin")
            .put("config", "dragonslayer.admin").build();
    private final String typeofDoubleList = "damage,health,reward,eggchance,skullchance,portaleggchance";
    private final String typeofBooleanList = "eggasitem,skullitem,cancelegg,denycrystalplace,denycrystalexplode,denybedexplode,createportal,oldportals,creategateways,fixgateways,resetworld,resetcrystal,respawnplayers,nomcdragonrespawn,alternativereward,onebyone,darkness,blockgrief,trydragonautofix,first_join_dragonspawn,teleportdragons,fixdeathflight,displaydragonname,glow_effect,hit_indicator";
    private final String typeofIntegerList = "maxdragons,exp,range,respawndelay,resetworlddelay,resetwarntime,timerfunc,regen_seconds,regen_amount,bossbar_distance";

    public DragonCommands(final DragonSlayer instance) { this.plugin = instance; }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, String[] args) {
        if (sender instanceof final Player player) {
            if (cmd.getName().equalsIgnoreCase("dragonslayer")) {
                if (args.length == 0) {
                    if (player.hasPermission("dragonslayer")) {
                        final String name = this.plugin.getSlayer();
                        if (name != null) {
                            player.sendMessage(this.plugin.configManager.getSlayerMessage());
                            return true;
                        }

                        player.sendMessage(this.plugin.configManager.getNoSlayerMessage());
                        return true;
                    }

                    player.sendMessage(ChatColor.RED + "You don't have permission");
                } else if (args.length >= 1) {
                    boolean isDouble = false;
                    boolean isBool = false;
                    boolean isInt = false;
                    args[0] = this.argsTranslator(args[0]);
                    if (!args[0].equalsIgnoreCase("setspawn") && !args[0].equalsIgnoreCase("addspawn")) {
                        if (args[0].equalsIgnoreCase("remspawn")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                if (args.length == 1)
                                    return this.remDragonSpawn(player, player.getWorld().getName().toLowerCase());

                                if (args.length == 2) {
                                    if (this.isWorld(args[1]))
                                        return this.remDragonSpawn(player, args[1].trim().toLowerCase());
                                    player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("getspawn")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                if (args.length == 1)
                                    return this.getDragonSpawn(player, player.getWorld().getName().toLowerCase());

                                if (args.length == 2) {
                                    if (this.isWorld(args[1]))
                                        return this.getDragonSpawn(player, args[1].trim().toLowerCase());
                                    player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("reload")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.reloadConfig();
                                this.plugin.configManager.loadConfiguration();

                                for (final Scoreboard timerDisplay : DragonSlayer.timerDisplays.values()) {
                                    timerDisplay.clearSlot(DisplaySlot.SIDEBAR);
                                    timerDisplay.clearSlot(DisplaySlot.BELOW_NAME);
                                    timerDisplay.clearSlot(DisplaySlot.PLAYER_LIST);
                                }

                                DragonSlayer.timerDisplays = new HashMap<String, Scoreboard>();
                                player.sendMessage(ChatColor.GREEN + "Config reloaded!");
                                return true;
                            }

                            player.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("forcerespawn")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                boolean hasRespawned = false;
                                String worldName = player.getWorld().getName();
                                if (args.length == 1) {
                                    hasRespawned = this.plugin.SpawnForceDragon(worldName.toLowerCase());
                                } else if (args.length == 2) {
                                    if (!this.isWorld(args[1].trim())) {
                                        player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                        return false;
                                    }

                                    worldName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                                    hasRespawned = this.plugin.SpawnForceDragon(worldName.toLowerCase());
                                }

                                if (hasRespawned) {
                                    player.sendMessage(ChatColor.GREEN + "Dragonspawn in world " + worldName + " started!");
                                } else {
                                    player.sendMessage(ChatColor.RED + "Dragonspawn in world " + worldName + " not necessary!");
                                }

                                return true;
                            }

                            player.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("forceallrespawn")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.SpawnForceAllDragons();
                                player.sendMessage(ChatColor.GREEN + "Dragonspawn in all worlds started!");
                                return true;
                            }

                            player.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("worldreset")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                if (this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core") != null) {
                                    String worldName = null;
                                    if (args.length == 1) {
                                        worldName = player.getWorld().getName();
                                    } else if (args.length == 2 && this.isWorld(args[1])) {
                                        worldName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                                    }

                                    if (worldName != null && this.plugin.checkWorld(worldName.toLowerCase())) {
                                        this.executeWorldReset(worldName, player);
                                        return true;
                                    }

                                    player.sendMessage(ChatColor.RED + "World not found!");
                                } else {
                                    player.sendMessage(ChatColor.RED + "This command needs multiverse core installed!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("worldrefresh")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                String worldName = null;
                                if (args.length == 1) {
                                    worldName = player.getWorld().getName();
                                } else if (args.length == 2 && this.isWorld(args[1])) {
                                    worldName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                                }

                                if (worldName != null && this.plugin.checkWorld(worldName.toLowerCase())) {
                                    this.plugin.WorldRefresh(worldName);
                                    player.sendMessage(ChatColor.GREEN + "World " + worldName + " crystalls will get respawned!");
                                    return true;
                                }

                                player.sendMessage(ChatColor.RED + "World not found!");

                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("setstatue")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                if (args.length == 1) {
                                    final Location loc = player.getLocation();
                                    final double x = loc.getX();
                                    final double y = loc.getY();
                                    final double z = loc.getZ();
                                    final float yaw = loc.getYaw();
                                    final String w = player.getWorld().getName().toLowerCase();
                                    this.setArmorStand(player, x, y, z, w, yaw);
                                    return true;
                                }

                                if (args.length >= 4 && args.length <= 6 && DragonCommands.isDouble(args[1])
                                        && DragonCommands.isDouble(args[2]) && DragonCommands.isDouble(args[3])) {
                                    String worldName = null;
                                    float yaw = 0.0F;
                                    if (args.length != 5 && args.length != 6) {
                                        worldName = player.getWorld().getName().toLowerCase();
                                    } else {
                                        if (args.length == 6 && DragonCommands.isDouble(args[5])) {
                                            yaw = Float.parseFloat(args[5]);
                                        }

                                        if (this.isWorld(args[4])) {
                                            worldName = args[4].trim().toLowerCase();
                                        } else {
                                            player.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                        }
                                    }

                                    final double x = Double.parseDouble(args[1]);
                                    final double y = Double.parseDouble(args[2]);
                                    final double z = Double.parseDouble(args[3]);
                                    if (worldName != null) {
                                        this.setArmorStand(player, x, y, z, worldName, yaw);
                                        return true;
                                    }
                                }

                                this.dragonSpawnHelp(player);
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("getstatue")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase())) && args.length >= 1) {
                                this.getArmorStand(player);
                                return true;
                            }

                            player.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("remstatue")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase())) && args.length == 1) {
                                this.remArmorStand(player);
                                return true;
                            }

                            player.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("removedragons")) {
                            if (!player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            } else {
                                if (args.length == 1 || args.length == 2 && args[1].equals("force")) {
                                    final String worldName = player.getWorld().getName().toLowerCase();
                                    this.remDrags(worldName, args.length == 2 && args[1].equals("force"));
                                    player.sendMessage(ChatColor.GREEN + "Dragons in " + worldName + " should be gone!");
                                    return true;
                                }

                                if (args.length >= 2) {
                                    if (this.isWorld(args[1])) {
                                        final String worldName = args[1].trim().toLowerCase();
                                        this.remDrags(worldName, args.length > 2 && args[2].equals("force"));
                                        player.sendMessage(ChatColor.GREEN + "Dragons in " + worldName + " should be gone!");
                                        return true;
                                    }

                                    player.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("scoreboardreset")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.leaderManager.clearLeaderList();
                                player.sendMessage(ChatColor.GREEN + "Scoreboard cleared!");
                                return true;
                            }

                            player.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("firstjoinreset")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.timerManager.clearPlayerList();
                                player.sendMessage(ChatColor.GREEN + "First Join List cleared!");
                                return true;
                            }

                            player.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("cleartimer")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.timerManager.clearTimers();
                                player.sendMessage(ChatColor.GREEN + "Timerlist cleared!");
                                return true;
                            }

                            player.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("showtimer")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                String worldName = null;
                                String nextSpawnIn = null;
                                String nextResetIn = null;
                                if (args.length == 1) {
                                    worldName = player.getWorld().getName();
                                } else if (args.length >= 2 && this.isWorld(args[1])) {
                                    worldName = args[1].trim();
                                }

                                if (worldName != null) {
                                    final String[] nextSpawn = this.plugin.getWorldsNextSpawnsOrReset(worldName.toLowerCase(), true, false);
                                    final String[] nextReset = this.plugin.getWorldsNextSpawnsOrReset(worldName.toLowerCase(), false, true);
                                    if (nextSpawn != null) {
                                        nextSpawnIn = this.plugin.configManager.getShowtime();
                                        if (nextSpawnIn != null && !nextSpawnIn.trim().isEmpty()) {
                                            nextSpawnIn = nextSpawnIn.replace("$days", nextSpawn[0])
                                                    .replace("$hours", String.valueOf(nextSpawn[1]))
                                                    .replace("$minutes", String.valueOf(nextSpawn[2]))
                                                    .replace("$seconds", String.valueOf(nextSpawn[3])).replace("$world", worldName);
                                        } else {
                                            nextSpawnIn = "Next spawn is: " + worldName + ": " + String.format("%s Day(s), %s:%s:%s",
                                                    nextSpawn[0], nextSpawn[1], nextSpawn[2], nextSpawn[3]);
                                        }
                                    }

                                    if (nextReset != null) {
                                        nextResetIn = this.plugin.configManager.getShowreset();
                                        if (nextResetIn != null && !nextResetIn.trim().isEmpty()) {
                                            nextResetIn = nextResetIn.replace("$days", nextReset[0]).replace("$hours", nextReset[1])
                                                    .replace("$minutes", nextReset[2]).replace("$seconds", nextReset[3])
                                                    .replace("$world", worldName);
                                        } else {
                                            nextResetIn = "Next reset is: " + worldName + ": " + String.format("%s Day(s), %s:%s:%s",
                                                    nextReset[0], nextReset[1], nextReset[2], nextReset[3]);
                                        }
                                    }

                                    if (nextSpawnIn != null) {
                                        player.sendMessage(ChatColor.GREEN + nextSpawnIn);
                                    }

                                    if (nextResetIn != null) {
                                        sender.sendMessage(ChatColor.GREEN + nextResetIn);
                                    }

                                    if (nextSpawnIn == null && nextResetIn == null) {
                                        nextSpawnIn = this.plugin.configManager.getShowtimeN();
                                        if (nextSpawnIn != null && !nextSpawnIn.trim().isEmpty()) {
                                            player.sendMessage(ChatColor.RED + nextSpawnIn.replace("$world", worldName));
                                        } else {
                                            player.sendMessage(ChatColor.RED + "No timers found for world " + worldName + "...");
                                        }
                                    }

                                    return true;
                                }

                                player.sendMessage(ChatColor.RED + "World not found!");
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("name")) {
                            if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                String playerWorldName = player.getWorld().getName().toLowerCase();
                                int dragonId = 0;
                                String dragonName = "";
                                if (args.length < 2 || this.isWorld(args[1])) {
                                    if (args.length > 1) {
                                        playerWorldName = args[1].trim().toLowerCase();
                                    }

                                    if (this.plugin.checkWorld(playerWorldName)) {
                                        for (final String dragonSectionKeys : this.plugin.getConfig()
                                                .getConfigurationSection("dragon." + playerWorldName).getKeys(false)) {
                                            if (dragonSectionKeys.startsWith("name")) {
                                                final String currentDragonName = this.plugin.getConfig()
                                                        .getString("dragon." + playerWorldName + "." + dragonSectionKeys);
                                                player.sendMessage(ChatColor.GREEN + "Dragon name \"" + dragonSectionKeys + "\" is: "
                                                        + currentDragonName.replace('&', '§'));
                                            }
                                        }

                                        return true;
                                    }

                                    player.sendMessage(ChatColor.RED + "World " + playerWorldName + " is not used by this plugin!");
                                    return false;
                                }

                                dragonName = args[1];
                                if (args.length >= 3) {
                                    if (this.isWorld(args[2])) {
                                        playerWorldName = args[2].trim().toLowerCase();
                                        if (args.length >= 4) {
                                            if (!DragonCommands.isInteger(args[3])) {
                                                player.sendMessage(ChatColor.RED + "3rd argument must be the dragon's number!");
                                                return false;
                                            }

                                            dragonId = Integer.parseInt(args[3]);
                                        }
                                    } else {
                                        if (!DragonCommands.isInteger(args[2])) {
                                            player.sendMessage(
                                                    ChatColor.RED + "2nd argument must be the world's name or the dragon's number!");
                                            return false;
                                        }

                                        dragonId = Integer.parseInt(args[2]);
                                    }
                                }

                                if (this.plugin.checkWorld(playerWorldName)) {
                                    this.setDragonName(dragonName, playerWorldName, dragonId, player);
                                    player.sendMessage(ChatColor.GREEN + "Dragon name set!");
                                    return true;
                                }

                                player.sendMessage(ChatColor.RED + "World " + playerWorldName + " is not used by this plugin!");
                            } else {
                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (!args[0].equalsIgnoreCase("config")) {
                            this.dragonSpawnHelp(player);
                        } else {
                            final int length = args.length;
                            final String[] newArgs = new String[length - 1];

                            for (int i = 1; i < length; ++i) {
                                newArgs[i - 1] = args[i];
                            }

                            args = newArgs;
                            if (newArgs[0].toLowerCase().startsWith("createportal_")) {
                                if (player.hasPermission((String) this.mainArgumentsAndPerms.get("config"))) {
                                    final String arg0 = newArgs[0].trim().toLowerCase();
                                    String currentWorldName = player.getWorld().getName().toLowerCase();
                                    boolean isSet = false;
                                    Boolean bool = false;
                                    if (newArgs.length >= 2) {
                                        final String arg1 = newArgs[1].trim().toLowerCase();
                                        if (this.isWorld(arg1)) {
                                            currentWorldName = arg1;
                                            if (newArgs.length >= 3 && (newArgs[2].trim().toLowerCase().equals("false")
                                                    || newArgs[2].trim().toLowerCase().equals("true")
                                                    || newArgs[2].trim().toLowerCase().equals("null")
                                                    || newArgs[2].trim().toLowerCase().equals("default"))) {
                                                bool = !newArgs[2].trim().toLowerCase().equals("null")
                                                        && !newArgs[2].trim().toLowerCase().equals("default")
                                                                ? Boolean.parseBoolean(newArgs[2].trim())
                                                                : null;
                                                isSet = true;
                                            }
                                        } else {
                                            if (!arg1.equals("false") && !arg1.equals("true") && !arg1.equals("null")
                                                    && !arg1.equals("default")) {
                                                player.sendMessage(ChatColor.RED + "Wrong syntax...");
                                                return false;
                                            }

                                            bool = !arg1.equals("null") && !arg1.equals("default") ? Boolean.parseBoolean(arg1) : null;
                                            isSet = true;
                                            if (newArgs.length >= 3) {
                                                if (!this.isWorld(newArgs[2].trim())) {
                                                    player.sendMessage(ChatColor.RED + "World " + newArgs[2] + " doesn't exist!");
                                                    return false;
                                                }

                                                currentWorldName = newArgs[2].trim().toLowerCase();
                                            }
                                        }
                                    }

                                    for (int i = 1; i <= this.plugin.configManager.getMaxdragons(currentWorldName); ++i) {
                                        if (arg0.equals("createportal_" + String.valueOf(i))) {
                                            if (!isSet) {
                                                this.getConfigVar(player, arg0, currentWorldName);
                                            } else {
                                                this.setConfigVar(player, arg0, bool, currentWorldName);
                                            }

                                            return true;
                                        }
                                    }

                                    player.sendMessage(ChatColor.RED + newArgs[0] + " doesn't exist!");
                                    return false;
                                }

                                player.sendMessage(ChatColor.RED + "You don't have permission");
                            } else if (this.typeofDoubleList.contains(newArgs[0].toLowerCase().trim())
                                    || this.typeofBooleanList.contains(newArgs[0].toLowerCase().trim())
                                    || this.typeofIntegerList.contains(newArgs[0].toLowerCase().trim())) {
                                if (!player.hasPermission((String) this.mainArgumentsAndPerms.get("config"))) {
                                    player.sendMessage(ChatColor.RED + "You don't have permission");
                                } else {
                                    String[] splitted = this.typeofDoubleList.split(",");
                                    for (final String test : splitted) {
                                        if (args[0].equalsIgnoreCase(test)) {
                                            isDouble = true;
                                            break;
                                        }
                                    }

                                    if (!isDouble) {
                                        for (final String test : splitted = this.typeofIntegerList.split(",")) {
                                            if (args[0].equalsIgnoreCase(test)) {
                                                isInt = true;
                                                break;
                                            }
                                        }

                                        if (!isInt) {
                                            for (final String test : splitted = this.typeofBooleanList.split(",")) {
                                                if (args[0].equalsIgnoreCase(test)) {
                                                    isBool = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (args.length == 1 || args.length == 2 && this.isWorld(args[1].trim())) {
                                        String ThisWorld = player.getWorld().getName().toLowerCase();
                                        if (args.length == 2) {
                                            ThisWorld = args[1].trim().toLowerCase();
                                        }

                                        if (!isDouble && !isInt && !isBool) {
                                            player.sendMessage(ChatColor.RED + "Typing error !!??");
                                            this.dragonSpawnHelp(player);
                                            return false;
                                        }

                                        this.getConfigVar(player, args[0].toLowerCase().trim(), ThisWorld);
                                        return true;
                                    }

                                    String ThisWorld;
                                    if (args.length == 2) {
                                        ThisWorld = player.getWorld().getName().toLowerCase();
                                    } else {
                                        if (args.length != 3) {
                                            player.sendMessage(ChatColor.RED + "Wrong syntax.");
                                            return false;
                                        }

                                        if (!this.isWorld(args[2].trim())) {
                                            player.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                                            return false;
                                        }

                                        ThisWorld = args[2].trim().toLowerCase();
                                    }

                                    final String arg1 = args[1].trim().toLowerCase();
                                    if (DragonCommands.isInteger(arg1) && isInt) {
                                        this.setConfigVar(player, args[0].toLowerCase().trim(), Integer.parseInt(arg1), ThisWorld);
                                        return true;
                                    }

                                    if (DragonCommands.isDouble(arg1) && isDouble) {
                                        this.setConfigVar(player, args[0].toLowerCase().trim(), Double.parseDouble(arg1), ThisWorld);
                                        return true;
                                    }

                                    if ((arg1.equals("false") || arg1.equals("true") || arg1.equals("default") || arg1.equals("null"))
                                            && isBool) {
                                        this.setConfigVar(player, args[0].toLowerCase().trim(),
                                                !arg1.equals("default") && !arg1.equals("null") ? Boolean.parseBoolean(arg1) : null,
                                                ThisWorld);
                                        return true;
                                    }

                                    player.sendMessage(ChatColor.RED + "Wrong format!");
                                }
                            }
                        }
                    } else if (player.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                        Integer dragonId = null;
                        if (args.length <= 2) {
                            if (args[0].toLowerCase().startsWith("add")) {
                                if (args.length != 2 || !DragonCommands.isInteger(args[1])) {
                                    player.sendMessage(ChatColor.RED + "You need to enter a number as argument, see help...");
                                    return false;
                                }

                                dragonId = Integer.parseInt(args[1]);
                            }

                            final Location loc = player.getLocation();
                            final double x = loc.getX();
                            final double y = loc.getY();
                            final double z = loc.getZ();
                            final String w = player.getWorld().getName().toLowerCase();
                            this.setDragonSpawn(player, x, y, z, w, dragonId);
                            this.plugin.configManager.setDragonDefaults();
                            final World world = Bukkit.getServer().getWorld(w);
                            if (world != null) {
                                this.plugin.UpdateEndgatewayPosList(world);
                                return true;
                            }
                        } else if (args.length >= 4 && args.length <= 6 && DragonCommands.isDouble(args[1])
                                && DragonCommands.isDouble(args[2]) && DragonCommands.isDouble(args[3])) {
                            String worldName = null;
                            if (args.length >= 5) {
                                if (args[0].toLowerCase().startsWith("add") && args.length >= 6) {
                                    if (!DragonCommands.isInteger(args[5])) {
                                        player.sendMessage(ChatColor.RED + "You need to enter a number as 5th argument, see help...");
                                        return false;
                                    }

                                    dragonId = Integer.parseInt(args[5]);
                                }

                                if (this.isWorld(args[4])) {
                                    worldName = args[4].trim().toLowerCase();
                                } else {
                                    if (!args[0].toLowerCase().startsWith("add")) {
                                        player.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                        return false;
                                    }

                                    if (args.length != 5 || !DragonCommands.isInteger(args[4])) {
                                        player.sendMessage(ChatColor.RED + "You need to enter a number as 4th argument, see help...");
                                        return false;
                                    }

                                    dragonId = Integer.parseInt(args[4]);
                                    worldName = player.getWorld().getName().toLowerCase();
                                }
                            } else {
                                worldName = player.getWorld().getName().toLowerCase();
                            }

                            final double x = Double.parseDouble(args[1]);
                            final double y = Double.parseDouble(args[2]);
                            final double z = Double.parseDouble(args[3]);
                            if (worldName != null) {
                                this.setDragonSpawn(player, x, y, z, worldName, dragonId);
                                this.plugin.configManager.setDragonDefaults();
                                final World NewWorld = Bukkit.getServer().getWorld(worldName);
                                if (NewWorld != null) {
                                    this.plugin.UpdateEndgatewayPosList(NewWorld);
                                    return true;
                                }
                            }
                        }

                        this.dragonSpawnHelp(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permission");
                    }
                }
            }
        } else if (sender instanceof final ConsoleCommandSender console && cmd.getName().equalsIgnoreCase("dragonslayer")) {
            if (args.length == 0) {
                final String slayerName = this.plugin.getSlayer();
                if (slayerName != null) {
                    console.sendMessage(this.plugin.configManager.getSlayerMessage());
                    return true;
                }

                console.sendMessage(this.plugin.configManager.getNoSlayerMessage());
                return true;
            }

            if (args.length >= 1) {
                boolean isDouble = false;
                boolean isBool = false;
                boolean isInt = false;
                args[0] = this.argsTranslator(args[0]);
                if (!args[0].equalsIgnoreCase("setspawn") && !args[0].equalsIgnoreCase("addspawn")) {
                    if (args[0].equalsIgnoreCase("remspawn")) {
                        if (args.length >= 2) {
                            if (this.isWorld(args[1])) {
                                final String worldName = args[1].trim().toLowerCase();
                                return this.remDragonSpawn(console, worldName);
                            }

                            console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                        } else {
                            this.dragonSpawnHelp(console);
                        }
                    } else if (args[0].equalsIgnoreCase("getspawn")) {
                        if (args.length >= 2) {
                            if (this.isWorld(args[1])) {
                                final String worldName = args[1].trim().toLowerCase();
                                return this.getDragonSpawn(console, worldName);
                            }

                            console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                        } else {
                            this.dragonSpawnHelp(console);
                        }
                    } else {
                        if (args[0].equalsIgnoreCase("reload")) {
                            this.plugin.reloadConfig();
                            this.plugin.configManager.loadConfiguration();

                            for (final Scoreboard timerDisplay : DragonSlayer.timerDisplays.values()) {
                                timerDisplay.clearSlot(DisplaySlot.SIDEBAR);
                                timerDisplay.clearSlot(DisplaySlot.BELOW_NAME);
                                timerDisplay.clearSlot(DisplaySlot.PLAYER_LIST);
                            }

                            DragonSlayer.timerDisplays = new HashMap<String, Scoreboard>();
                            console.sendMessage(ChatColor.GREEN + "Config reloaded!");
                            return true;
                        }

                        if (args[0].equalsIgnoreCase("forcerespawn")) {
                            boolean hasSpawned = false;
                            if (args.length == 1) {
                                console.sendMessage(ChatColor.RED + "This command needs a world as 2nd argument!");
                                return false;
                            }

                            if (args.length == 2) {
                                if (!this.isWorld(args[1])) {
                                    console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                    return false;
                                }

                                hasSpawned = this.plugin.SpawnForceDragon(args[1].trim().toLowerCase());
                            }

                            if (hasSpawned) {
                                console.sendMessage(ChatColor.GREEN + "Dragonspawn in world " + args[1].trim() + " started!");
                            } else {
                                console.sendMessage(ChatColor.RED + "Dragonspawn in world " + args[1].trim() + " not necessary!");
                            }

                            return true;
                        }

                        if (args[0].equalsIgnoreCase("forceallrespawn")) {
                            this.plugin.SpawnForceAllDragons();
                            console.sendMessage(ChatColor.GREEN + "Dragonspawn in all worlds started!");
                            return true;
                        }

                        if (args[0].equalsIgnoreCase("worldreset")) {
                            if (this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core") != null) {
                                String worldName = null;
                                if (args.length == 2 && this.isWorld(args[1])) {
                                    worldName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                                }

                                if (worldName != null && this.plugin.checkWorld(worldName.toLowerCase())) {
                                    this.executeWorldReset(worldName, console);
                                    return true;
                                }

                                console.sendMessage(ChatColor.RED + "World not found!");
                            } else {
                                console.sendMessage(ChatColor.RED + "This command needs multiverse core installed!");
                            }
                        } else if (args[0].equalsIgnoreCase("worldrefresh")) {
                            String worldName = null;
                            if (args.length == 2 && this.isWorld(args[1])) {
                                worldName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                            }

                            if (worldName != null && this.plugin.checkWorld(worldName.toLowerCase())) {
                                this.plugin.WorldRefresh(worldName);
                                console.sendMessage(ChatColor.GREEN + "World " + worldName + " crystalls will respawn!");
                                return true;
                            }

                            console.sendMessage(ChatColor.RED + "World not found!");

                        } else if (args[0].equalsIgnoreCase("setstatue")) {
                            if (args.length >= 5 && args.length <= 6 && DragonCommands.isDouble(args[1]) && DragonCommands.isDouble(args[2])
                                    && DragonCommands.isDouble(args[3])) {
                                String worldName = null;
                                float yaw = 0.0F;
                                if (args.length == 6 && DragonCommands.isDouble(args[5])) {
                                    yaw = Float.parseFloat(args[5]);
                                }

                                if (this.isWorld(args[4])) {
                                    worldName = args[4].trim().toLowerCase();
                                } else {
                                    console.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                }

                                final double x = Double.parseDouble(args[1]);
                                final double y = Double.parseDouble(args[2]);
                                final double z = Double.parseDouble(args[3]);
                                if (worldName != null) {
                                    this.setArmorStand(console, x, y, z, worldName, yaw);
                                    return true;
                                }
                            }

                            this.dragonSpawnHelp(console);
                        } else if (args[0].equalsIgnoreCase("getstatue")) {
                            if (args.length >= 1) {
                                this.getArmorStand(console);
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("remstatue")) {
                            if (args.length >= 1) {
                                this.remArmorStand(console);
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("removedragons")) {
                            if (args.length >= 2) {
                                if (this.isWorld(args[1])) {
                                    final String ThisWorld = args[1].trim().toLowerCase();
                                    this.remDrags(ThisWorld, args.length > 2 && args[2].equals("force"));
                                    console.sendMessage(ChatColor.GREEN + "Dragons in " + ThisWorld + " should be gone!");
                                    return true;
                                }

                                console.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            } else {
                                this.dragonSpawnHelp(console);
                            }
                        } else if (args[0].equalsIgnoreCase("scoreboardreset")) {
                            if (args.length >= 1) {
                                this.plugin.leaderManager.clearLeaderList();
                                console.sendMessage(ChatColor.GREEN + "Scoreboard cleared!");
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("firstjoinreset")) {
                            if (args.length >= 1) {
                                this.plugin.timerManager.clearPlayerList();
                                console.sendMessage(ChatColor.GREEN + "First Join List cleared!");
                                return true;
                            }
                        } else {
                            if (args[0].equalsIgnoreCase("cleartimer")) {
                                this.plugin.timerManager.clearTimers();
                                console.sendMessage(ChatColor.GREEN + "Timerlist cleared!");
                                return true;
                            }

                            if (args[0].equalsIgnoreCase("showtimer")) {
                                String nextSpawnIn = null;
                                String nextResetIn = null;
                                if (args.length >= 2) {
                                    if (this.isWorld(args[1])) {
                                        final String world = args[1].trim();
                                        final String[] nextSpawn = this.plugin.getWorldsNextSpawnsOrReset(world.toLowerCase(), true, false);
                                        final String[] nextReset = this.plugin.getWorldsNextSpawnsOrReset(world.toLowerCase(), false, true);
                                        if (nextSpawn != null) {
                                            nextSpawnIn = this.plugin.configManager.getShowtime();
                                            if (nextSpawnIn != null && !nextSpawnIn.trim().isEmpty()) {
                                                nextSpawnIn = nextSpawnIn.replace("$days", nextSpawn[0]).replace("$hours", nextSpawn[1])
                                                        .replace("$minutes", nextSpawn[2]).replace("$seconds", nextSpawn[3])
                                                        .replace("$world", world);
                                            } else {
                                                nextSpawnIn = "Next spawn is: " + world + ": " + String.format("%s Day(s), %s:%s:%s",
                                                        nextSpawn[0], nextSpawn[1], nextSpawn[2], nextSpawn[3]);
                                            }
                                        }

                                        if (nextReset != null) {
                                            nextResetIn = this.plugin.configManager.getShowreset();
                                            if (nextResetIn != null && !nextResetIn.trim().isEmpty()) {
                                                nextResetIn = nextResetIn.replace("$days", nextReset[0]).replace("$hours", nextReset[1])
                                                        .replace("$minutes", nextReset[2]).replace("$seconds", nextReset[3])
                                                        .replace("$world", world);
                                            } else {
                                                nextResetIn = "Next reset is: " + world + ": " + String.format("%s Day(s), %s:%s:%s",
                                                        nextReset[0], nextReset[1], nextReset[2], nextReset[3]);
                                            }
                                        }

                                        if (nextSpawnIn != null) {
                                            console.sendMessage(ChatColor.GREEN + nextSpawnIn);
                                        }

                                        if (nextResetIn != null) {
                                            console.sendMessage(ChatColor.GREEN + nextResetIn);
                                        }

                                        if (nextSpawnIn == null && nextResetIn == null) {
                                            nextSpawnIn = this.plugin.configManager.getShowtimeN();
                                            if (nextSpawnIn != null && !nextSpawnIn.trim().isEmpty()) {
                                                console.sendMessage(ChatColor.RED + nextSpawnIn.replace("$world", world));
                                            } else {
                                                console.sendMessage(ChatColor.RED + "No timers found for world " + world + "...");
                                            }
                                        }

                                        return true;
                                    }

                                    console.sendMessage(ChatColor.RED + "World not found!");
                                } else {
                                    this.dragonSpawnHelp(console);
                                }
                            } else if (args[0].equalsIgnoreCase("name")) {
                                String worldName = "";
                                int dragonId = 0;
                                String dragonName = "";
                                if (args.length < 3 || this.isWorld(args[1])) {
                                    if (args.length > 1) {
                                        worldName = args[1].trim().toLowerCase();
                                    }

                                    if (this.plugin.checkWorld(worldName)) {
                                        for (final String dragonSectionKeys : this.plugin.getConfig()
                                                .getConfigurationSection("dragon." + worldName).getKeys(false)) {
                                            if (dragonSectionKeys.startsWith("name")) {
                                                final String currentDragonName = this.plugin.getConfig()
                                                        .getString("dragon." + worldName + "." + dragonSectionKeys);
                                                console.sendMessage(ChatColor.GREEN + "Dragon name \"" + dragonSectionKeys + "\" is: "
                                                        + currentDragonName.replace('&', '§'));
                                            }
                                        }

                                        return true;
                                    }

                                    if (!worldName.isEmpty()) {
                                        console.sendMessage(ChatColor.RED + "World " + worldName + " is not used by this plugin!");
                                    } else {
                                        console.sendMessage(
                                                ChatColor.RED + "You need to give a World's name as argument for getting the names...");
                                    }

                                    return false;
                                }

                                dragonName = args[1];
                                if (args.length >= 3) {
                                    if (!this.isWorld(args[2])) {
                                        console.sendMessage(ChatColor.RED + "2nd argument must be the world's name!");
                                        return false;
                                    }

                                    worldName = args[2].trim().toLowerCase();
                                    if (args.length >= 4) {
                                        if (!DragonCommands.isInteger(args[3])) {
                                            console.sendMessage(ChatColor.RED + "3rd argument must be the dragon's number!");
                                            return false;
                                        }

                                        dragonId = Integer.parseInt(args[3]);
                                    }
                                }

                                if (this.plugin.checkWorld(worldName)) {
                                    this.setDragonName(dragonName, worldName, dragonId, console);
                                    console.sendMessage(ChatColor.GREEN + "Dragon name set!");
                                    return true;
                                }

                                console.sendMessage(ChatColor.RED + "World " + worldName + " is not used by this plugin!");
                            } else if (args[0].equalsIgnoreCase("config")) {
                                final int lenght = args.length;
                                final String[] newArgs = new String[lenght - 1];

                                for (int i = 1; i < lenght; ++i) {
                                    newArgs[i - 1] = args[i];
                                }

                                args = newArgs;
                                if (newArgs[0].toLowerCase().startsWith("createportal_")) {
                                    final String arg0 = newArgs[0].trim().toLowerCase();
                                    String worldName = null;
                                    boolean isSet = false;
                                    Boolean bool = false;
                                    String arg1 = "";
                                    if (newArgs.length >= 2) {
                                        arg1 = newArgs[1].trim().toLowerCase();
                                        if (this.isWorld(arg1)) {
                                            worldName = arg1;
                                        }
                                    }

                                    if (newArgs.length >= 3) {
                                        if (this.isWorld(arg1)) {
                                            worldName = arg1;
                                            if (!newArgs[2].trim().toLowerCase().equals("false")
                                                    && !newArgs[2].trim().toLowerCase().equals("true")
                                                    && !newArgs[2].trim().toLowerCase().equals("null")
                                                    && !newArgs[2].trim().toLowerCase().equals("default")) {
                                                console.sendMessage(ChatColor.RED + "Wrong syntax...");
                                                return false;
                                            }

                                            bool = !newArgs[2].trim().toLowerCase().equals("null")
                                                    && !newArgs[2].trim().toLowerCase().equals("default")
                                                            ? Boolean.parseBoolean(newArgs[2].trim())
                                                            : null;
                                            isSet = true;
                                        } else {
                                            if (!arg1.equals("false") && !arg1.equals("true") && !arg1.equals("null")
                                                    && !arg1.equals("default")) {
                                                console.sendMessage(ChatColor.RED + "Wrong syntax...");
                                                return false;
                                            }

                                            bool = !arg1.equals("null") && !arg1.equals("default") ? Boolean.parseBoolean(arg1) : null;
                                            isSet = true;
                                            if (!this.isWorld(newArgs[2].trim())) {
                                                console.sendMessage(ChatColor.RED + "World " + newArgs[2] + " doesn't exist!");
                                                return false;
                                            }

                                            worldName = newArgs[2].trim().toLowerCase();
                                        }
                                    }

                                    if (worldName == null) {
                                        console.sendMessage(ChatColor.RED + "Wrong syntax...");
                                        return false;
                                    }

                                    for (int i = 1; i <= this.plugin.configManager.getMaxdragons(worldName); ++i) {
                                        if (arg0.equals("createportal_" + String.valueOf(i))) {
                                            if (!isSet) {
                                                this.getConfigVar(console, arg0, worldName);
                                            } else {
                                                this.setConfigVar(console, arg0, bool, worldName);
                                            }

                                            return true;
                                        }
                                    }

                                    console.sendMessage(ChatColor.RED + newArgs[0] + " doesn't exist!");
                                    return false;
                                }

                                if (this.typeofDoubleList.contains(newArgs[0].toLowerCase().trim())
                                        || this.typeofBooleanList.contains(newArgs[0].toLowerCase().trim())
                                        || this.typeofIntegerList.contains(newArgs[0].toLowerCase().trim())) {
                                    String[] splitted = this.typeofDoubleList.split(",");
                                    for (final String split : splitted) {
                                        if (args[0].equalsIgnoreCase(split)) {
                                            isDouble = true;
                                            break;
                                        }
                                    }

                                    if (!isDouble) {
                                        for (final String test : splitted = this.typeofIntegerList.split(",")) {
                                            if (args[0].equalsIgnoreCase(test)) {
                                                isInt = true;
                                                break;
                                            }
                                        }

                                        if (!isInt) {
                                            for (final String test : splitted = this.typeofBooleanList.split(",")) {
                                                if (args[0].equalsIgnoreCase(test)) {
                                                    isBool = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (args.length == 2) {
                                        if ((isDouble || isInt || isBool) && this.isWorld(args[1].trim())) {
                                            final String ThisWorld = args[1].trim().toLowerCase();
                                            this.getConfigVar(console, args[0].toLowerCase().trim(), ThisWorld);
                                            return true;
                                        }

                                        console.sendMessage(ChatColor.RED + "Typing error in world name or variable name !?");
                                        this.dragonSpawnHelp(console);
                                        return false;
                                    }

                                    if (args.length != 3) {
                                        console.sendMessage(ChatColor.RED + "Wrong syntax.");
                                        return false;
                                    }

                                    if (!this.isWorld(args[2].trim())) {
                                        console.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                                        return false;
                                    }

                                    final String worldName = args[2].trim().toLowerCase();
                                    final String arg1 = args[1].trim().toLowerCase();
                                    if (DragonCommands.isInteger(arg1) && isInt) {
                                        this.setConfigVar(console, args[0].toLowerCase().trim(), Integer.parseInt(arg1), worldName);
                                        return true;
                                    }

                                    if (DragonCommands.isDouble(arg1) && isDouble) {
                                        this.setConfigVar(console, args[0].toLowerCase().trim(), Double.parseDouble(arg1), worldName);
                                        return true;
                                    }

                                    if ((arg1.equals("false") || arg1.equals("true") || arg1.equals("default") || arg1.equals("null"))
                                            && isBool) {
                                        this.setConfigVar(console, args[0].toLowerCase().trim(),
                                                !arg1.equals("default") && !arg1.equals("null") ? Boolean.parseBoolean(arg1) : null,
                                                worldName);
                                        return true;
                                    }

                                    console.sendMessage(ChatColor.RED + "Wrong format!");
                                }
                            } else {
                                this.dragonSpawnHelp(console);
                            }
                        }
                    }
                } else {
                    if (args.length >= 5 && DragonCommands.isDouble(args[1]) && DragonCommands.isDouble(args[2])
                            && DragonCommands.isDouble(args[3])) {
                        String worldName = null;
                        if (!this.isWorld(args[4])) {
                            console.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                            return false;
                        }

                        worldName = args[4].trim().toLowerCase();
                        final double newArgs = Double.parseDouble(args[1]);
                        final double arg1 = Double.parseDouble(args[2]);
                        final double bool = Double.parseDouble(args[3]);
                        if (worldName != null) {
                            Integer isAdd = null;
                            if (args[0].toLowerCase().startsWith("add")) {
                                if (args.length < 6 || !DragonCommands.isInteger(args[5])) {
                                    console.sendMessage(ChatColor.RED + "You need to enter a number as 5th argument, see help...");
                                    return false;
                                }

                                isAdd = Integer.parseInt(args[5]);
                            }

                            this.setDragonSpawn(console, newArgs, arg1, bool, worldName, isAdd);
                            this.plugin.configManager.setDragonDefaults();
                            return true;
                        }
                    }

                    this.dragonSpawnHelp(console);
                }
            }
        }

        return false;
    }

    private boolean getDragonSpawn(final CommandSender sender, final String worldName) {
        if (!this.checkWorld(sender, worldName)) {
            return false;
        } else {
            double x = this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".x");
            double y = this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".y");
            double z = this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".z");
            sender.sendMessage(ChatColor.GREEN + "Dragon spawn is set at: " + (int) x + " " + (int) y + " " + (int) z + " in " + worldName);

            for (final String spawn : this.plugin.getConfig().getConfigurationSection("spawnpoint." + worldName).getKeys(false)) {
                if (spawn.startsWith("dragon_")) {
                    x = this.plugin.getConfig().getDouble("spawnpoint." + worldName + "." + spawn + ".x");
                    y = this.plugin.getConfig().getDouble("spawnpoint." + worldName + "." + spawn + ".y");
                    z = this.plugin.getConfig().getDouble("spawnpoint." + worldName + "." + spawn + ".z");
                    sender.sendMessage(ChatColor.GREEN + "Additional Dragon spawn is set to: " + (int) x + " " + (int) y + " " + (int) z
                            + " for " + spawn);
                }
            }

            return true;
        }
    }

    private void setDragonSpawn(final CommandSender sender, final double x, final double y, final double z, String worldName,
            final Integer dragonId) {
        final World world = this.plugin.getDragonWorldFromString(worldName);
        final Environment environment = world.getEnvironment();
        if (environment != Environment.THE_END) {
            sender.sendMessage(ChatColor.YELLOW + "World " + world.getName() + " is not an End-World !");
            if (environment == Environment.NETHER) {
                sender.sendMessage(ChatColor.RED + "Nether is not supported...");
                return;
            }

            sender.sendMessage(ChatColor.YELLOW + "Be cautious what you do with this...!");
        }

        if (dragonId != null) {
            worldName = worldName + ".dragon_" + dragonId;
        }

        this.plugin.getConfig().set("spawnpoint." + worldName + ".x", x);
        this.plugin.getConfig().set("spawnpoint." + worldName + ".y", y);
        this.plugin.getConfig().set("spawnpoint." + worldName + ".z", z);
        this.plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Dragon spawn set to: " + (int) x + " " + (int) y + " " + (int) z + " in " + worldName
                + (dragonId != null ? " (used as ADDspawn)" : " (used as SETspawn)"));
    }

    private boolean remDragonSpawn(final CommandSender sender, final String worldName) {
        if (!this.checkWorld(sender, worldName)) {
            return false;
        } else {
            final long now = System.currentTimeMillis() / 50L;
            final long duration = 200L;
            if (now - this.removeSpawnConfirmTimer > duration) {
                this.removeSpawnConfirmTimer = now;
                sender.sendMessage(ChatColor.GREEN + "Do you realy want to remove all (!) spawns from " + worldName
                        + " (and the world from being used by this plugin)?");
                sender.sendMessage(ChatColor.GREEN + "You have to confirm by repeating the same command within 10 seconds to run it!");
                return false;
            } else {
                this.plugin.getConfig().set("spawnpoint." + worldName, (Object) null);
                this.plugin.getConfig().set("dragon." + worldName, (Object) null);
                this.plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Dragon spawn removed from world: " + worldName);
                this.removeSpawnConfirmTimer = 0L;
                return true;
            }
        }
    }

    private boolean checkWorld(final CommandSender sender, final String worldName) {
        final World W = this.plugin.getDragonWorldFromString(worldName);
        if (W.getEnvironment() != Environment.THE_END) {
            sender.sendMessage(ChatColor.RED + "World " + worldName + " is not an End-World !");
        }

        if (!this.plugin.checkWorld(worldName)) {
            sender.sendMessage(ChatColor.RED + "World " + worldName + " is not used by DragonSlayer !");
            return false;
        } else {
            return true;
        }
    }

    private void setConfigVar(final CommandSender sender, final String key, final Object value, final String worldName) {
        if (this.plugin.checkWorld(worldName)) {
            this.plugin.getConfig().set("dragon." + worldName + "." + key, value);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + key + " set to " + String.valueOf(value).replace('&', '§'));
        } else {
            sender.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }

    }

    private void getConfigVar(final CommandSender sender, final String key, final String worldName) {
        if (this.plugin.checkWorld(worldName)) {
            final String value = this.plugin.getConfig().getString("dragon." + worldName + "." + key);
            if (value != null) {
                sender.sendMessage(ChatColor.GREEN + key + " is " + value + " !");
            } else {
                sender.sendMessage(ChatColor.RED + key + " doesn't exist !");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }

    }

    private void dragonSpawnHelp(final CommandSender sender) {
        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("reload"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer reload");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("setspawn"))) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer setspawn [x y z [worldname]]");
            } else {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer setspawn {x y z} {worldname}");
            }
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("getspawn"))) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer getspawn [world]");
            } else {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer getspawn {worldname}");
            }
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("addspawn"))) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer addspawn {number}");
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer addspawn {x y z} [worldname] {number}");
            } else {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer addspawn {x y z} {worldname} {number}");
            }
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("remspawn"))) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer remspawn [world]");
            } else {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer remspawn {worldname}");
            }
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("setstatue"))) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer setstatue [x y z [world [yaw]] ]");
            } else {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer setstatue {x y z} {world} [yaw]");
            }
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("getstatue"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer getstatue");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("remstatue"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer remstatue");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("forcerespawn"))) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer forcerespawn [world]");
            } else {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer forcerespawn {world}");
            }
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("forceallrespawn"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer forceallrespawn");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("worldrefresh"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer worldrefresh [world]");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("worldreset"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer worldreset [world]");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("showtimer"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer showtimer [world]");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("cleartimer"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer cleartimer");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("removedragons"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer removedragons [world] [force]");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("scoreboardreset"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer scoreboardreset");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("firstjoinreset"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer firstjoinreset");
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("name"))) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer name [world]");
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer name {New Name} [world] [Dragon number]");
            } else {
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer name {world}");
                sender.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer name {New Name} {world} [Dragon number]");
            }
        }

        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get("config"))) {
            sender.sendMessage(ChatColor.RED + "Proper usage for getting config values:");
            sender.sendMessage(ChatColor.RED + "/dragonslayer config {config value} [world]");
            sender.sendMessage(ChatColor.RED + "Proper usage for setting config values:");
            sender.sendMessage(ChatColor.RED + "/dragonslayer config {config value} {number} [world]");
            sender.sendMessage(ChatColor.RED + "/dragonslayer config {config value} {true/false/default} [world]");
        }

    }

    private boolean isWorld(final String worldName) { return Bukkit.getWorld(worldName) != null; }

    public static boolean isDouble(final String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (final NumberFormatException var2) {
            return false;
        }
    }

    public static boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (final NumberFormatException var2) {
            return false;
        }
    }

    private void setArmorStand(final CommandSender sender, final double x, final double y, final double z, final String worldName,
            final float yaw) {
        this.plugin.RemoveArmorStand();
        this.plugin.getConfig().set("statue.world", worldName);
        this.plugin.getConfig().set("statue.x", x);
        this.plugin.getConfig().set("statue.y", y);
        this.plugin.getConfig().set("statue.z", z);
        this.plugin.getConfig().set("statue.yaw", yaw);
        this.plugin.saveConfig();
        this.plugin.PlaceArmorStand(worldName, x, y, z, yaw);
        sender.sendMessage(ChatColor.GREEN + "Statue set to: " + (int) x + " " + (int) y + " " + (int) z + " in " + worldName);
    }

    private void getArmorStand(final CommandSender sender) {
        final String world = this.plugin.getConfig().getString("statue.world");
        if (world != null) {
            final String ThisWorldsName = Bukkit.getServer().getWorld(world).getName();
            final double x = this.plugin.getConfig().getDouble("statue.x");
            final double y = this.plugin.getConfig().getDouble("statue.y");
            final double z = this.plugin.getConfig().getDouble("statue.z");
            sender.sendMessage(
                    ChatColor.GREEN + "Statue is placed at: " + (int) x + " " + (int) y + " " + (int) z + " in " + ThisWorldsName);
        } else {
            sender.sendMessage(ChatColor.GREEN + "There is no statue placed anywhere... ");
        }

    }

    private void remArmorStand(final CommandSender sender) {
        if (DragonSlayer.protLibHandler != null) {
            DragonSlayer.protLibHandler.removeNPCStatue();
        }

        if (this.plugin.RemoveArmorStand()) {
            this.plugin.getConfig().set("statue", (Object) null);
            this.plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Statue removed !");
        } else {
            sender.sendMessage(ChatColor.RED + "Statue remove failed !");
        }

    }

    private void remDrags(final String worldName, final boolean forceAll) {
        final World ThisWorld = Bukkit.getServer().getWorld(worldName);
        this.plugin.RemoveDragons(ThisWorld, false, forceAll);
        DragonSlayer.cleanupDragons();
    }

    private void setDragonName(String dragonName, final String worldName, final int dragonId, final CommandSender sender) {
        dragonName = dragonName.replace('§', '&');
        if (dragonId == 0) {
            this.setConfigVar(sender, "name", dragonName, worldName);
        } else {
            this.setConfigVar(sender, "name_" + String.valueOf(dragonId), dragonName, worldName);
        }

    }

    private void executeWorldReset(final String worldName, final CommandSender sender) {
        final long now = System.currentTimeMillis() / 50L;
        final long duration = 200L;
        if (now - this.resetConfirmTimer > duration) {
            this.resetConfirmTimer = now;
            sender.sendMessage(ChatColor.GREEN + "Do You realy want to reset world " + worldName + " and lose all progress???");
            sender.sendMessage(ChatColor.GREEN + "You have to confirm! Repeat the same command again within 10 seconds to execute it!");
        } else {
            this.resetConfirmTimer = 0L;
            sender.sendMessage(ChatColor.GREEN + "The world " + worldName + " will get recreated!");
            this.plugin.WorldReset(worldName, true);
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (command.getName().equalsIgnoreCase("dragonslayer")) {
            final List<String> finalList = new ArrayList<String>();
            String s = null;
            if (args.length == 1) {
                if (!args[0].equals("")) {
                    for (final String string : this.mainArgumentsAndPerms.keySet()) {
                        if (string.startsWith(args[0].toLowerCase()) && sender.hasPermission(this.mainArgumentsAndPerms.get(string))) {
                            finalList.add(string);
                        }
                    }
                } else {
                    this.mainArgumentsAndPerms.keySet().forEach(str -> {
                        if (sender.hasPermission(this.mainArgumentsAndPerms.get(str))) {
                            finalList.add(str);
                        }
                        return;
                    });
                }
                return finalList;
            }
            if (args.length == 0) {
                this.mainArgumentsAndPerms.keySet().forEach(str -> {
                    if (sender.hasPermission(this.mainArgumentsAndPerms.get(str))) {
                        finalList.add(str);
                    }
                    return;
                });
                return finalList;
            }
            if (args.length > 1) {
                args[0] = this.argsTranslator(args[0]);
                if (this.mainArgumentsAndPerms.get(args[0].toLowerCase()) == null) {
                    return null;
                }
                if (sender instanceof Player && !sender.hasPermission(this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                    return null;
                }
                final Set<String> usedMapList = this.plugin.configManager.getMaplist();
                Boolean addAll = true;
                final String lowerCase = args[0].toLowerCase();
                switch (lowerCase) {
                case "showtimer":
                case "remspawn":
                case "forcerespawn":
                case "name":
                case "worldreset":
                case "worldrefresh":
                case "removedragons":
                case "getspawn":
                    break;
                default: {
                    addAll = false;
                    break;
                }
                }
                if (addAll && args.length == 2) {
                    finalList.addAll(usedMapList);
                }

                final Set<String> allMapList = new HashSet<String>();
                Bukkit.getWorlds().forEach(w -> allMapList.add(w.getName()));
                if (args[0].equalsIgnoreCase("name")) {
                    if (args.length == 2) {
                        finalList.add("[NewDragonName]");
                    } else if (args.length > 2 && Bukkit.getWorld(args[1]) == null) {
                        if (args.length == 3) {
                            finalList.addAll(usedMapList);
                        } else if (args.length == 4) {
                            finalList.add("{DragonNumber}");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("removedragons")) {
                    if (args.length == 2 || args.length == 3) {
                        finalList.add("force");
                    }
                } else if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("addspawn")
                        || args[0].equalsIgnoreCase("setstatue")) {
                    if (args.length == 2) {
                        finalList.add("{x}");
                    } else if (args.length == 3) {
                        finalList.add("{y}");
                    } else if (args.length == 4) {
                        finalList.add("{z}");
                    } else if (args.length == 5) {
                        finalList.addAll(allMapList);
                    }
                    if (args[0].equalsIgnoreCase("addspawn")) {
                        if (args.length == 2 || args.length == 6) {
                            finalList.add("{DragonNumber}");
                        }
                    } else if (args[0].equalsIgnoreCase("setstatue") && args.length == 6) {
                        finalList.add("[yaw]");
                    }
                } else if (args[0].equalsIgnoreCase("config")) {
                    if (args.length == 2) {
                        final String[] doubleList = this.typeofDoubleList.split(",");
                        final String[] boolList = this.typeofBooleanList.split(",");
                        final String[] intList = this.typeofIntegerList.split(",");
                        String[] array;
                        for (int length = (array = doubleList).length, i = 0; i < length; ++i) {
                            s = array[i];
                            finalList.add(s);
                        }
                        String[] array2;
                        for (int length2 = (array2 = boolList).length, j = 0; j < length2; ++j) {
                            s = array2[j];
                            finalList.add(s);
                        }
                        String[] array3;
                        for (int length3 = (array3 = intList).length, k = 0; k < length3; ++k) {
                            s = array3[k];
                            finalList.add(s);
                        }
                        finalList.add("createportal_{number}");
                        Collections.sort(finalList);
                    } else if (args.length == 3) {
                        if (this.typeofBooleanList.contains(args[1]) || args[1].startsWith("createportal_")) {
                            finalList.add("true");
                            finalList.add("false");
                            finalList.add("default");
                        } else if (this.typeofDoubleList.contains(args[1]) || this.typeofIntegerList.contains(args[1])) {
                            finalList.add("{number}");
                        }
                        finalList.addAll(usedMapList);
                    } else if (args.length == 4 && (this.typeofDoubleList.contains(args[1]) || this.typeofIntegerList.contains(args[1])
                            || this.typeofBooleanList.contains(args[1]) || args[1].startsWith("createportal_"))) {
                        finalList.addAll(usedMapList);
                    }
                }
            }
            if (!finalList.isEmpty()) {
                return finalList;
            }
        }
        return null;
    }

    private String argsTranslator(final String arg) {
        return switch (arg.toLowerCase()) {
        case "scoreres" -> "scoreboardreset";
        case "add" -> "addspawn";
        case "get" -> "getspawn";
        case "rel" -> "reload";
        case "rem" -> "remspawn";
        case "set" -> "setspawn";
        case "remd" -> "removedragons";
        case "force" -> "forcerespawn";
        case "refresh" -> "worldrefresh";
        case "reset" -> "worldreset";
        case "forceall" -> "forceallrespawn";
        case "getarmorstand", "getst", "getas" -> "getstatue";
        case "setas", "setst", "setarmorstand" -> "setstatue";
        case "remarmorstand", "remas", "remst" -> "remstatue";
        default -> arg.toLowerCase();
        };
    }
}
