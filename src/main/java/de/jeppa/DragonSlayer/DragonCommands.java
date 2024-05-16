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
    private long remspawnConfirmTimer = 0L;
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
    private final String DoubleVarList = "damage,health,reward,eggchance,skullchance,portaleggchance";
    private final String BoolVarList = "eggasitem,skullitem,cancelegg,denycrystalplace,denycrystalexplode,denybedexplode,createportal,oldportals,creategateways,fixgateways,resetworld,resetcrystal,respawnplayers,nomcdragonrespawn,alternativereward,onebyone,darkness,blockgrief,trydragonautofix,first_join_dragonspawn,teleportdragons,fixdeathflight,displaydragonname,glow_effect,hit_indicator";
    private final String IntVarList = "maxdragons,exp,range,respawndelay,resetworlddelay,resetwarntime,timerfunc,regen_seconds,regen_amount,bossbar_distance";

    public DragonCommands(final DragonSlayer instance) { this.plugin = instance; }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, String[] args) {
        if (sender instanceof final Player p) {
            if (cmd.getName().equalsIgnoreCase("dragonslayer")) {
                if (args.length == 0) {
                    if (p.hasPermission("dragonslayer")) {
                        final String name = this.plugin.getSlayer();
                        if (name != null) {
                            p.sendMessage(this.plugin.configManager.getSlayerMessage());
                            return true;
                        }

                        p.sendMessage(this.plugin.configManager.getNoSlayerMessage());
                        return true;
                    }

                    p.sendMessage(ChatColor.RED + "You don't have permission");
                } else if (args.length >= 1) {
                    boolean isDouble = false;
                    boolean isBool = false;
                    boolean isInt = false;
                    args[0] = this.argsTranslator(args[0]);
                    if (!args[0].equalsIgnoreCase("setspawn") && !args[0].equalsIgnoreCase("addspawn")) {
                        if (args[0].equalsIgnoreCase("remspawn")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                if (args.length == 1) {
                                    final String w = p.getWorld().getName().toLowerCase();
                                    return this.remDragonSpawn(p, w);
                                }

                                if (args.length == 2) {
                                    if (this.isWorld(args[1])) {
                                        final String w = args[1].trim().toLowerCase();
                                        return this.remDragonSpawn(p, w);
                                    }

                                    p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("getspawn")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                if (args.length == 1) {
                                    final String ThisWorld = p.getWorld().getName().toLowerCase();
                                    return this.getDragonSpawn(p, ThisWorld);
                                }

                                if (args.length == 2) {
                                    if (this.isWorld(args[1])) {
                                        final String ThisWorld = args[1].trim().toLowerCase();
                                        return this.getDragonSpawn(p, ThisWorld);
                                    }

                                    p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("reload")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.reloadConfig();
                                this.plugin.configManager.loadConfiguration();

                                for (final Scoreboard timerDisplay : DragonSlayer.timerDisplays.values()) {
                                    timerDisplay.clearSlot(DisplaySlot.SIDEBAR);
                                    timerDisplay.clearSlot(DisplaySlot.BELOW_NAME);
                                    timerDisplay.clearSlot(DisplaySlot.PLAYER_LIST);
                                }

                                DragonSlayer.timerDisplays = new HashMap<String, Scoreboard>();
                                p.sendMessage(ChatColor.GREEN + "Config reloaded!");
                                return true;
                            }

                            p.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("forcerespawn")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                boolean done = false;
                                String world = p.getWorld().getName();
                                if (args.length == 1) {
                                    done = this.plugin.SpawnForceDragon(world.toLowerCase());
                                } else if (args.length == 2) {
                                    if (!this.isWorld(args[1].trim())) {
                                        p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                        return false;
                                    }

                                    world = Bukkit.getServer().getWorld(args[1].trim()).getName();
                                    done = this.plugin.SpawnForceDragon(world.toLowerCase());
                                }

                                if (done) {
                                    p.sendMessage(ChatColor.GREEN + "Dragonspawn in world " + world + " started!");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Dragonspawn in world " + world + " not necessary!");
                                }

                                return true;
                            }

                            p.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("forceallrespawn")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.SpawnForceAllDragons();
                                p.sendMessage(ChatColor.GREEN + "Dragonspawn in all worlds started!");
                                return true;
                            }

                            p.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("worldreset")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                if (this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core") != null) {
                                    String ThisWorldsName = null;
                                    if (args.length == 1) {
                                        ThisWorldsName = p.getWorld().getName();
                                    } else if (args.length == 2 && this.isWorld(args[1])) {
                                        ThisWorldsName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                                    }

                                    if (ThisWorldsName != null && this.plugin.checkWorld(ThisWorldsName.toLowerCase())) {
                                        this.executeWorldReset(ThisWorldsName, p);
                                        return true;
                                    }

                                    p.sendMessage(ChatColor.RED + "World not found!");
                                } else {
                                    p.sendMessage(ChatColor.RED + "This command needs multiverse core installed!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("worldrefresh")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                String ThisWorldsName = null;
                                if (args.length == 1) {
                                    ThisWorldsName = p.getWorld().getName();
                                } else if (args.length == 2 && this.isWorld(args[1])) {
                                    ThisWorldsName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                                }

                                if (ThisWorldsName != null && this.plugin.checkWorld(ThisWorldsName.toLowerCase())) {
                                    this.plugin.WorldRefresh(ThisWorldsName);
                                    p.sendMessage(ChatColor.GREEN + "World " + ThisWorldsName + " crystalls will get respawned!");
                                    return true;
                                }

                                p.sendMessage(ChatColor.RED + "World not found!");

                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("setstatue")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                if (args.length == 1) {
                                    final Location loc = p.getLocation();
                                    final double x = loc.getX();
                                    final double y = loc.getY();
                                    final double z = loc.getZ();
                                    final float yaw = loc.getYaw();
                                    final String w = p.getWorld().getName().toLowerCase();
                                    this.setArmorStand(p, x, y, z, w, yaw);
                                    return true;
                                }

                                if (args.length >= 4 && args.length <= 6 && DragonCommands.isDouble(args[1])
                                        && DragonCommands.isDouble(args[2]) && DragonCommands.isDouble(args[3])) {
                                    String w = null;
                                    float yaw = 0.0F;
                                    if (args.length != 5 && args.length != 6) {
                                        w = p.getWorld().getName().toLowerCase();
                                    } else {
                                        if (args.length == 6 && DragonCommands.isDouble(args[5])) {
                                            yaw = Float.parseFloat(args[5]);
                                        }

                                        if (this.isWorld(args[4])) {
                                            w = args[4].trim().toLowerCase();
                                        } else {
                                            p.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                        }
                                    }

                                    final double x = Double.parseDouble(args[1]);
                                    final double y = Double.parseDouble(args[2]);
                                    final double z = Double.parseDouble(args[3]);
                                    if (w != null) {
                                        this.setArmorStand(p, x, y, z, w, yaw);
                                        return true;
                                    }
                                }

                                this.dragonSpawnHelp(p);
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("getstatue")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase())) && args.length >= 1) {
                                this.getArmorStand(p);
                                return true;
                            }

                            p.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("remstatue")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase())) && args.length == 1) {
                                this.remArmorStand(p);
                                return true;
                            }

                            p.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("removedragons")) {
                            if (!p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            } else {
                                if (args.length == 1 || args.length == 2 && args[1].equals("force")) {
                                    final String ThisWorld = p.getWorld().getName().toLowerCase();
                                    this.remDrags(ThisWorld, args.length == 2 && args[1].equals("force"));
                                    p.sendMessage(ChatColor.GREEN + "Dragons in " + ThisWorld + " should be gone!");
                                    return true;
                                }

                                if (args.length >= 2) {
                                    if (this.isWorld(args[1])) {
                                        final String ThisWorld = args[1].trim().toLowerCase();
                                        this.remDrags(ThisWorld, args.length > 2 && args[2].equals("force"));
                                        p.sendMessage(ChatColor.GREEN + "Dragons in " + ThisWorld + " should be gone!");
                                        return true;
                                    }

                                    p.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("scoreboardreset")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.leaderManager.clearLeaderList();
                                p.sendMessage(ChatColor.GREEN + "Scoreboard cleared!");
                                return true;
                            }

                            p.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("firstjoinreset")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.timerManager.clearPlayerList();
                                p.sendMessage(ChatColor.GREEN + "First Join List cleared!");
                                return true;
                            }

                            p.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("cleartimer")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                this.plugin.timerManager.clearTimers();
                                p.sendMessage(ChatColor.GREEN + "Timerlist cleared!");
                                return true;
                            }

                            p.sendMessage(ChatColor.RED + "You don't have permission");
                        } else if (args[0].equalsIgnoreCase("showtimer")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                String world = null;
                                String returnString = null;
                                String returnString2 = null;
                                if (args.length == 1) {
                                    world = p.getWorld().getName();
                                } else if (args.length >= 2 && this.isWorld(args[1])) {
                                    world = args[1].trim();
                                }

                                if (world != null) {
                                    final String[] nextSpawn = this.plugin.getWorldsNextSpawnsOrReset(world.toLowerCase(), true, false);
                                    final String[] nextReset = this.plugin.getWorldsNextSpawnsOrReset(world.toLowerCase(), false, true);
                                    if (nextSpawn != null) {
                                        returnString = this.plugin.configManager.getShowtime();
                                        if (returnString != null && !returnString.trim().isEmpty()) {
                                            returnString = returnString.replace("$days", nextSpawn[0])
                                                    .replace("$hours", String.valueOf(nextSpawn[1]))
                                                    .replace("$minutes", String.valueOf(nextSpawn[2]))
                                                    .replace("$seconds", String.valueOf(nextSpawn[3])).replace("$world", world);
                                        } else {
                                            returnString = "Next spawn is: " + world + ": " + String.format("%s Day(s), %s:%s:%s",
                                                    nextSpawn[0], nextSpawn[1], nextSpawn[2], nextSpawn[3]);
                                        }
                                    }

                                    if (nextReset != null) {
                                        returnString2 = this.plugin.configManager.getShowreset();
                                        if (returnString2 != null && !returnString2.trim().isEmpty()) {
                                            returnString2 = returnString2.replace("$days", nextReset[0]).replace("$hours", nextReset[1])
                                                    .replace("$minutes", nextReset[2]).replace("$seconds", nextReset[3])
                                                    .replace("$world", world);
                                        } else {
                                            returnString2 = "Next reset is: " + world + ": " + String.format("%s Day(s), %s:%s:%s",
                                                    nextReset[0], nextReset[1], nextReset[2], nextReset[3]);
                                        }
                                    }

                                    if (returnString != null) {
                                        p.sendMessage(ChatColor.GREEN + returnString);
                                    }

                                    if (returnString2 != null) {
                                        sender.sendMessage(ChatColor.GREEN + returnString2);
                                    }

                                    if (returnString == null && returnString2 == null) {
                                        returnString = this.plugin.configManager.getShowtimeN();
                                        if (returnString != null && !returnString.trim().isEmpty()) {
                                            p.sendMessage(ChatColor.RED + returnString.replace("$world", world));
                                        } else {
                                            p.sendMessage(ChatColor.RED + "No timers found for world " + world + "...");
                                        }
                                    }

                                    return true;
                                }

                                p.sendMessage(ChatColor.RED + "World not found!");
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (args[0].equalsIgnoreCase("name")) {
                            if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                                String world = p.getWorld().getName().toLowerCase();
                                int id = 0;
                                String name = "";
                                if (args.length < 2 || this.isWorld(args[1])) {
                                    if (args.length > 1) {
                                        world = args[1].trim().toLowerCase();
                                    }

                                    if (this.plugin.checkWorld(world)) {
                                        for (final String name2 : this.plugin.getConfig().getConfigurationSection("dragon." + world)
                                                .getKeys(false)) {
                                            if (name2.startsWith("name")) {
                                                final String name2_ = this.plugin.getConfig().getString("dragon." + world + "." + name2);
                                                p.sendMessage(
                                                        ChatColor.GREEN + "Dragon name \"" + name2 + "\" is: " + name2_.replace('&', '§'));
                                            }
                                        }

                                        return true;
                                    }

                                    p.sendMessage(ChatColor.RED + "World " + world + " is not used by this plugin!");
                                    return false;
                                }

                                name = args[1];
                                if (args.length >= 3) {
                                    if (this.isWorld(args[2])) {
                                        world = args[2].trim().toLowerCase();
                                        if (args.length >= 4) {
                                            if (!DragonCommands.isInteger(args[3])) {
                                                p.sendMessage(ChatColor.RED + "3rd argument must be the dragon's number!");
                                                return false;
                                            }

                                            id = Integer.parseInt(args[3]);
                                        }
                                    } else {
                                        if (!DragonCommands.isInteger(args[2])) {
                                            p.sendMessage(ChatColor.RED + "2nd argument must be the world's name or the dragon's number!");
                                            return false;
                                        }

                                        id = Integer.parseInt(args[2]);
                                    }
                                }

                                if (this.plugin.checkWorld(world)) {
                                    this.setDragonName(name, world, id, p);
                                    p.sendMessage(ChatColor.GREEN + "Dragon name set!");
                                    return true;
                                }

                                p.sendMessage(ChatColor.RED + "World " + world + " is not used by this plugin!");
                            } else {
                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            }
                        } else if (!args[0].equalsIgnoreCase("config")) {
                            this.dragonSpawnHelp(p);
                        } else {
                            final int length = args.length;
                            final String[] newArgs = new String[length - 1];

                            for (int i = 1; i < length; ++i) {
                                newArgs[i - 1] = args[i];
                            }

                            args = newArgs;
                            if (newArgs[0].toLowerCase().startsWith("createportal_")) {
                                if (p.hasPermission((String) this.mainArgumentsAndPerms.get("config"))) {
                                    final String arg = newArgs[0].trim().toLowerCase();
                                    String world = p.getWorld().getName().toLowerCase();
                                    boolean set = false;
                                    Boolean bool = false;
                                    if (newArgs.length >= 2) {
                                        final String arg1 = newArgs[1].trim().toLowerCase();
                                        if (this.isWorld(arg1)) {
                                            world = arg1;
                                            if (newArgs.length >= 3 && (newArgs[2].trim().toLowerCase().equals("false")
                                                    || newArgs[2].trim().toLowerCase().equals("true")
                                                    || newArgs[2].trim().toLowerCase().equals("null")
                                                    || newArgs[2].trim().toLowerCase().equals("default"))) {
                                                bool = !newArgs[2].trim().toLowerCase().equals("null")
                                                        && !newArgs[2].trim().toLowerCase().equals("default")
                                                                ? Boolean.parseBoolean(newArgs[2].trim())
                                                                : null;
                                                set = true;
                                            }
                                        } else {
                                            if (!arg1.equals("false") && !arg1.equals("true") && !arg1.equals("null")
                                                    && !arg1.equals("default")) {
                                                p.sendMessage(ChatColor.RED + "Wrong syntax...");
                                                return false;
                                            }

                                            bool = !arg1.equals("null") && !arg1.equals("default") ? Boolean.parseBoolean(arg1) : null;
                                            set = true;
                                            if (newArgs.length >= 3) {
                                                if (!this.isWorld(newArgs[2].trim())) {
                                                    p.sendMessage(ChatColor.RED + "World " + newArgs[2] + " doesn't exist!");
                                                    return false;
                                                }

                                                world = newArgs[2].trim().toLowerCase();
                                            }
                                        }
                                    }

                                    for (int i = 1; i <= this.plugin.configManager.getMaxdragons(world); ++i) {
                                        if (arg.equals("createportal_" + String.valueOf(i))) {
                                            if (!set) {
                                                this.getConfigVar(p, arg, world);
                                            } else {
                                                this.setConfigVar(p, arg, bool, world);
                                            }

                                            return true;
                                        }
                                    }

                                    p.sendMessage(ChatColor.RED + newArgs[0] + " doesn't exist!");
                                    return false;
                                }

                                p.sendMessage(ChatColor.RED + "You don't have permission");
                            } else if (this.DoubleVarList.contains(newArgs[0].toLowerCase().trim())
                                    || this.BoolVarList.contains(newArgs[0].toLowerCase().trim())
                                    || this.IntVarList.contains(newArgs[0].toLowerCase().trim())) {
                                if (!p.hasPermission((String) this.mainArgumentsAndPerms.get("config"))) {
                                    p.sendMessage(ChatColor.RED + "You don't have permission");
                                } else {
                                    String[] splitted = this.DoubleVarList.split(",");
                                    for (final String test : splitted) {
                                        if (args[0].equalsIgnoreCase(test)) {
                                            isDouble = true;
                                            break;
                                        }
                                    }

                                    if (!isDouble) {
                                        for (final String test : splitted = this.IntVarList.split(",")) {
                                            if (args[0].equalsIgnoreCase(test)) {
                                                isInt = true;
                                                break;
                                            }
                                        }

                                        if (!isInt) {
                                            for (final String test : splitted = this.BoolVarList.split(",")) {
                                                if (args[0].equalsIgnoreCase(test)) {
                                                    isBool = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (args.length == 1 || args.length == 2 && this.isWorld(args[1].trim())) {
                                        String ThisWorld = p.getWorld().getName().toLowerCase();
                                        if (args.length == 2) {
                                            ThisWorld = args[1].trim().toLowerCase();
                                        }

                                        if (!isDouble && !isInt && !isBool) {
                                            p.sendMessage(ChatColor.RED + "Typing error !!??");
                                            this.dragonSpawnHelp(p);
                                            return false;
                                        }

                                        this.getConfigVar(p, args[0].toLowerCase().trim(), ThisWorld);
                                        return true;
                                    }

                                    String ThisWorld;
                                    if (args.length == 2) {
                                        ThisWorld = p.getWorld().getName().toLowerCase();
                                    } else {
                                        if (args.length != 3) {
                                            p.sendMessage(ChatColor.RED + "Wrong syntax.");
                                            return false;
                                        }

                                        if (!this.isWorld(args[2].trim())) {
                                            p.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                                            return false;
                                        }

                                        ThisWorld = args[2].trim().toLowerCase();
                                    }

                                    final String arg1 = args[1].trim().toLowerCase();
                                    if (DragonCommands.isInteger(arg1) && isInt) {
                                        this.setConfigVar(p, args[0].toLowerCase().trim(), Integer.parseInt(arg1), ThisWorld);
                                        return true;
                                    }

                                    if (DragonCommands.isDouble(arg1) && isDouble) {
                                        this.setConfigVar(p, args[0].toLowerCase().trim(), Double.parseDouble(arg1), ThisWorld);
                                        return true;
                                    }

                                    if ((arg1.equals("false") || arg1.equals("true") || arg1.equals("default") || arg1.equals("null"))
                                            && isBool) {
                                        this.setConfigVar(p, args[0].toLowerCase().trim(),
                                                !arg1.equals("default") && !arg1.equals("null") ? Boolean.parseBoolean(arg1) : null,
                                                ThisWorld);
                                        return true;
                                    }

                                    p.sendMessage(ChatColor.RED + "Wrong format!");
                                }
                            }
                        }
                    } else if (p.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                        Integer isAdd = null;
                        if (args.length <= 2) {
                            if (args[0].toLowerCase().startsWith("add")) {
                                if (args.length != 2 || !DragonCommands.isInteger(args[1])) {
                                    p.sendMessage(ChatColor.RED + "You need to enter a number as argument, see help...");
                                    return false;
                                }

                                isAdd = Integer.parseInt(args[1]);
                            }

                            final Location loc = p.getLocation();
                            final double x = loc.getX();
                            final double y = loc.getY();
                            final double z = loc.getZ();
                            final String w = p.getWorld().getName().toLowerCase();
                            this.setDragonSpawn(p, x, y, z, w, isAdd);
                            this.plugin.configManager.setDragonDefaults();
                            final World NewWorld = Bukkit.getServer().getWorld(w);
                            if (NewWorld != null) {
                                this.plugin.UpdateEndgatewayPosList(NewWorld);
                                return true;
                            }
                        } else if (args.length >= 4 && args.length <= 6 && DragonCommands.isDouble(args[1])
                                && DragonCommands.isDouble(args[2]) && DragonCommands.isDouble(args[3])) {
                            String w = null;
                            if (args.length >= 5) {
                                if (args[0].toLowerCase().startsWith("add") && args.length >= 6) {
                                    if (!DragonCommands.isInteger(args[5])) {
                                        p.sendMessage(ChatColor.RED + "You need to enter a number as 5th argument, see help...");
                                        return false;
                                    }

                                    isAdd = Integer.parseInt(args[5]);
                                }

                                if (this.isWorld(args[4])) {
                                    w = args[4].trim().toLowerCase();
                                } else {
                                    if (!args[0].toLowerCase().startsWith("add")) {
                                        p.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                        return false;
                                    }

                                    if (args.length != 5 || !DragonCommands.isInteger(args[4])) {
                                        p.sendMessage(ChatColor.RED + "You need to enter a number as 4th argument, see help...");
                                        return false;
                                    }

                                    isAdd = Integer.parseInt(args[4]);
                                    w = p.getWorld().getName().toLowerCase();
                                }
                            } else {
                                w = p.getWorld().getName().toLowerCase();
                            }

                            final double x = Double.parseDouble(args[1]);
                            final double y = Double.parseDouble(args[2]);
                            final double z = Double.parseDouble(args[3]);
                            if (w != null) {
                                this.setDragonSpawn(p, x, y, z, w, isAdd);
                                this.plugin.configManager.setDragonDefaults();
                                final World NewWorld = Bukkit.getServer().getWorld(w);
                                if (NewWorld != null) {
                                    this.plugin.UpdateEndgatewayPosList(NewWorld);
                                    return true;
                                }
                            }
                        }

                        this.dragonSpawnHelp(p);
                    } else {
                        p.sendMessage(ChatColor.RED + "You don't have permission");
                    }
                }
            }
        } else if (sender instanceof ConsoleCommandSender && cmd.getName().equalsIgnoreCase("dragonslayer")) {
            if (args.length == 0) {
                final String name = this.plugin.getSlayer();
                if (name != null) {
                    sender.sendMessage(this.plugin.configManager.getSlayerMessage());
                    return true;
                }

                sender.sendMessage(this.plugin.configManager.getNoSlayerMessage());
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
                                final String w = args[1].trim().toLowerCase();
                                return this.remDragonSpawn(sender, w);
                            }

                            sender.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                        } else {
                            this.dragonSpawnHelp(sender);
                        }
                    } else if (args[0].equalsIgnoreCase("getspawn")) {
                        if (args.length >= 2) {
                            if (this.isWorld(args[1])) {
                                final String ThisWorld = args[1].trim().toLowerCase();
                                return this.getDragonSpawn(sender, ThisWorld);
                            }

                            sender.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                        } else {
                            this.dragonSpawnHelp(sender);
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
                            sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
                            return true;
                        }

                        if (args[0].equalsIgnoreCase("forcerespawn")) {
                            boolean done = false;
                            if (args.length == 1) {
                                sender.sendMessage(ChatColor.RED + "This command needs a world as 2nd argument!");
                                return false;
                            }

                            if (args.length == 2) {
                                if (!this.isWorld(args[1])) {
                                    sender.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                                    return false;
                                }

                                done = this.plugin.SpawnForceDragon(args[1].trim().toLowerCase());
                            }

                            if (done) {
                                sender.sendMessage(ChatColor.GREEN + "Dragonspawn in world " + args[1].trim() + " started!");
                            } else {
                                sender.sendMessage(ChatColor.RED + "Dragonspawn in world " + args[1].trim() + " not necessary!");
                            }

                            return true;
                        }

                        if (args[0].equalsIgnoreCase("forceallrespawn")) {
                            this.plugin.SpawnForceAllDragons();
                            sender.sendMessage(ChatColor.GREEN + "Dragonspawn in all worlds started!");
                            return true;
                        }

                        if (args[0].equalsIgnoreCase("worldreset")) {
                            if (this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core") != null) {
                                String ThisWorldsName = null;
                                if (args.length == 2 && this.isWorld(args[1])) {
                                    ThisWorldsName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                                }

                                if (ThisWorldsName != null && this.plugin.checkWorld(ThisWorldsName.toLowerCase())) {
                                    this.executeWorldReset(ThisWorldsName, sender);
                                    return true;
                                }

                                sender.sendMessage(ChatColor.RED + "World not found!");
                            } else {
                                sender.sendMessage(ChatColor.RED + "This command needs multiverse core installed!");
                            }
                        } else if (args[0].equalsIgnoreCase("worldrefresh")) {
                            String ThisWorldsName = null;
                            if (args.length == 2 && this.isWorld(args[1])) {
                                ThisWorldsName = Bukkit.getServer().getWorld(args[1].trim()).getName();
                            }

                            if (ThisWorldsName != null && this.plugin.checkWorld(ThisWorldsName.toLowerCase())) {
                                this.plugin.WorldRefresh(ThisWorldsName);
                                sender.sendMessage(ChatColor.GREEN + "World " + ThisWorldsName + " crystalls will respawn!");
                                return true;
                            }

                            sender.sendMessage(ChatColor.RED + "World not found!");

                        } else if (args[0].equalsIgnoreCase("setstatue")) {
                            if (args.length >= 5 && args.length <= 6 && DragonCommands.isDouble(args[1]) && DragonCommands.isDouble(args[2])
                                    && DragonCommands.isDouble(args[3])) {
                                String w = null;
                                float yaw = 0.0F;
                                if (args.length == 6 && DragonCommands.isDouble(args[5])) {
                                    yaw = Float.parseFloat(args[5]);
                                }

                                if (this.isWorld(args[4])) {
                                    w = args[4].trim().toLowerCase();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                                }

                                final double x = Double.parseDouble(args[1]);
                                final double y = Double.parseDouble(args[2]);
                                final double z = Double.parseDouble(args[3]);
                                if (w != null) {
                                    this.setArmorStand(sender, x, y, z, w, yaw);
                                    return true;
                                }
                            }

                            this.dragonSpawnHelp(sender);
                        } else if (args[0].equalsIgnoreCase("getstatue")) {
                            if (args.length >= 1) {
                                this.getArmorStand(sender);
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("remstatue")) {
                            if (args.length >= 1) {
                                this.remArmorStand(sender);
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("removedragons")) {
                            if (args.length >= 2) {
                                if (this.isWorld(args[1])) {
                                    final String ThisWorld = args[1].trim().toLowerCase();
                                    this.remDrags(ThisWorld, args.length > 2 && args[2].equals("force"));
                                    sender.sendMessage(ChatColor.GREEN + "Dragons in " + ThisWorld + " should be gone!");
                                    return true;
                                }

                                sender.sendMessage(ChatColor.RED + "World " + args[1] + " doesn't exist!");
                            } else {
                                this.dragonSpawnHelp(sender);
                            }
                        } else if (args[0].equalsIgnoreCase("scoreboardreset")) {
                            if (args.length >= 1) {
                                this.plugin.leaderManager.clearLeaderList();
                                sender.sendMessage(ChatColor.GREEN + "Scoreboard cleared!");
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("firstjoinreset")) {
                            if (args.length >= 1) {
                                this.plugin.timerManager.clearPlayerList();
                                sender.sendMessage(ChatColor.GREEN + "First Join List cleared!");
                                return true;
                            }
                        } else {
                            if (args[0].equalsIgnoreCase("cleartimer")) {
                                this.plugin.timerManager.clearTimers();
                                sender.sendMessage(ChatColor.GREEN + "Timerlist cleared!");
                                return true;
                            }

                            if (args[0].equalsIgnoreCase("showtimer")) {
                                String returnString = null;
                                String returnString2 = null;
                                if (args.length >= 2) {
                                    if (this.isWorld(args[1])) {
                                        final String world = args[1].trim();
                                        final String[] nextSpawn = this.plugin.getWorldsNextSpawnsOrReset(world.toLowerCase(), true, false);
                                        final String[] nextReset = this.plugin.getWorldsNextSpawnsOrReset(world.toLowerCase(), false, true);
                                        if (nextSpawn != null) {
                                            returnString = this.plugin.configManager.getShowtime();
                                            if (returnString != null && !returnString.trim().isEmpty()) {
                                                returnString = returnString.replace("$days", nextSpawn[0]).replace("$hours", nextSpawn[1])
                                                        .replace("$minutes", nextSpawn[2]).replace("$seconds", nextSpawn[3])
                                                        .replace("$world", world);
                                            } else {
                                                returnString = "Next spawn is: " + world + ": " + String.format("%s Day(s), %s:%s:%s",
                                                        nextSpawn[0], nextSpawn[1], nextSpawn[2], nextSpawn[3]);
                                            }
                                        }

                                        if (nextReset != null) {
                                            returnString2 = this.plugin.configManager.getShowreset();
                                            if (returnString2 != null && !returnString2.trim().isEmpty()) {
                                                returnString2 = returnString2.replace("$days", nextReset[0]).replace("$hours", nextReset[1])
                                                        .replace("$minutes", nextReset[2]).replace("$seconds", nextReset[3])
                                                        .replace("$world", world);
                                            } else {
                                                returnString2 = "Next reset is: " + world + ": " + String.format("%s Day(s), %s:%s:%s",
                                                        nextReset[0], nextReset[1], nextReset[2], nextReset[3]);
                                            }
                                        }

                                        if (returnString != null) {
                                            sender.sendMessage(ChatColor.GREEN + returnString);
                                        }

                                        if (returnString2 != null) {
                                            sender.sendMessage(ChatColor.GREEN + returnString2);
                                        }

                                        if (returnString == null && returnString2 == null) {
                                            returnString = this.plugin.configManager.getShowtimeN();
                                            if (returnString != null && !returnString.trim().isEmpty()) {
                                                sender.sendMessage(ChatColor.RED + returnString.replace("$world", world));
                                            } else {
                                                sender.sendMessage(ChatColor.RED + "No timers found for world " + world + "...");
                                            }
                                        }

                                        return true;
                                    }

                                    sender.sendMessage(ChatColor.RED + "World not found!");
                                } else {
                                    this.dragonSpawnHelp(sender);
                                }
                            } else if (args[0].equalsIgnoreCase("name")) {
                                String world = "";
                                int id = 0;
                                String name = "";
                                if (args.length < 3 || this.isWorld(args[1])) {
                                    if (args.length > 1) {
                                        world = args[1].trim().toLowerCase();
                                    }

                                    if (this.plugin.checkWorld(world)) {
                                        for (final String name2 : this.plugin.getConfig().getConfigurationSection("dragon." + world)
                                                .getKeys(false)) {
                                            if (name2.startsWith("name")) {
                                                final String name2_ = this.plugin.getConfig().getString("dragon." + world + "." + name2);
                                                sender.sendMessage(
                                                        ChatColor.GREEN + "Dragon name \"" + name2 + "\" is: " + name2_.replace('&', '§'));
                                            }
                                        }

                                        return true;
                                    }

                                    if (!world.isEmpty()) {
                                        sender.sendMessage(ChatColor.RED + "World " + world + " is not used by this plugin!");
                                    } else {
                                        sender.sendMessage(
                                                ChatColor.RED + "You need to give a World's name as argument for getting the names...");
                                    }

                                    return false;
                                }

                                name = args[1];
                                if (args.length >= 3) {
                                    if (!this.isWorld(args[2])) {
                                        sender.sendMessage(ChatColor.RED + "2nd argument must be the world's name!");
                                        return false;
                                    }

                                    world = args[2].trim().toLowerCase();
                                    if (args.length >= 4) {
                                        if (!DragonCommands.isInteger(args[3])) {
                                            sender.sendMessage(ChatColor.RED + "3rd argument must be the dragon's number!");
                                            return false;
                                        }

                                        id = Integer.parseInt(args[3]);
                                    }
                                }

                                if (this.plugin.checkWorld(world)) {
                                    this.setDragonName(name, world, id, sender);
                                    sender.sendMessage(ChatColor.GREEN + "Dragon name set!");
                                    return true;
                                }

                                sender.sendMessage(ChatColor.RED + "World " + world + " is not used by this plugin!");
                            } else if (args[0].equalsIgnoreCase("config")) {
                                final int lenght = args.length;
                                final String[] newArgs = new String[lenght - 1];

                                for (int i = 1; i < lenght; ++i) {
                                    newArgs[i - 1] = args[i];
                                }

                                args = newArgs;
                                if (newArgs[0].toLowerCase().startsWith("createportal_")) {
                                    final String arg = newArgs[0].trim().toLowerCase();
                                    String world = null;
                                    boolean set = false;
                                    Boolean bool = false;
                                    String arg1 = "";
                                    if (newArgs.length >= 2) {
                                        arg1 = newArgs[1].trim().toLowerCase();
                                        if (this.isWorld(arg1)) {
                                            world = arg1;
                                        }
                                    }

                                    if (newArgs.length >= 3) {
                                        if (this.isWorld(arg1)) {
                                            world = arg1;
                                            if (!newArgs[2].trim().toLowerCase().equals("false")
                                                    && !newArgs[2].trim().toLowerCase().equals("true")
                                                    && !newArgs[2].trim().toLowerCase().equals("null")
                                                    && !newArgs[2].trim().toLowerCase().equals("default")) {
                                                sender.sendMessage(ChatColor.RED + "Wrong syntax...");
                                                return false;
                                            }

                                            bool = !newArgs[2].trim().toLowerCase().equals("null")
                                                    && !newArgs[2].trim().toLowerCase().equals("default")
                                                            ? Boolean.parseBoolean(newArgs[2].trim())
                                                            : null;
                                            set = true;
                                        } else {
                                            if (!arg1.equals("false") && !arg1.equals("true") && !arg1.equals("null")
                                                    && !arg1.equals("default")) {
                                                sender.sendMessage(ChatColor.RED + "Wrong syntax...");
                                                return false;
                                            }

                                            bool = !arg1.equals("null") && !arg1.equals("default") ? Boolean.parseBoolean(arg1) : null;
                                            set = true;
                                            if (!this.isWorld(newArgs[2].trim())) {
                                                sender.sendMessage(ChatColor.RED + "World " + newArgs[2] + " doesn't exist!");
                                                return false;
                                            }

                                            world = newArgs[2].trim().toLowerCase();
                                        }
                                    }

                                    if (world == null) {
                                        sender.sendMessage(ChatColor.RED + "Wrong syntax...");
                                        return false;
                                    }

                                    for (int i = 1; i <= this.plugin.configManager.getMaxdragons(world); ++i) {
                                        if (arg.equals("createportal_" + String.valueOf(i))) {
                                            if (!set) {
                                                this.getConfigVar(sender, arg, world);
                                            } else {
                                                this.setConfigVar(sender, arg, bool, world);
                                            }

                                            return true;
                                        }
                                    }

                                    sender.sendMessage(ChatColor.RED + newArgs[0] + " doesn't exist!");
                                    return false;
                                }

                                if (this.DoubleVarList.contains(newArgs[0].toLowerCase().trim())
                                        || this.BoolVarList.contains(newArgs[0].toLowerCase().trim())
                                        || this.IntVarList.contains(newArgs[0].toLowerCase().trim())) {
                                    String[] splitted = this.DoubleVarList.split(",");
                                    for (final String test : splitted) {
                                        if (args[0].equalsIgnoreCase(test)) {
                                            isDouble = true;
                                            break;
                                        }
                                    }

                                    if (!isDouble) {
                                        for (final String test : splitted = this.IntVarList.split(",")) {
                                            if (args[0].equalsIgnoreCase(test)) {
                                                isInt = true;
                                                break;
                                            }
                                        }

                                        if (!isInt) {
                                            for (final String test : splitted = this.BoolVarList.split(",")) {
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
                                            this.getConfigVar(sender, args[0].toLowerCase().trim(), ThisWorld);
                                            return true;
                                        }

                                        sender.sendMessage(ChatColor.RED + "Typing error in world name or variable name !?");
                                        this.dragonSpawnHelp(sender);
                                        return false;
                                    }

                                    if (args.length != 3) {
                                        sender.sendMessage(ChatColor.RED + "Wrong syntax.");
                                        return false;
                                    }

                                    if (!this.isWorld(args[2].trim())) {
                                        sender.sendMessage(ChatColor.RED + "World " + args[2] + " doesn't exist!");
                                        return false;
                                    }

                                    final String ThisWorld = args[2].trim().toLowerCase();
                                    final String arg1 = args[1].trim().toLowerCase();
                                    if (DragonCommands.isInteger(arg1) && isInt) {
                                        this.setConfigVar(sender, args[0].toLowerCase().trim(), Integer.parseInt(arg1), ThisWorld);
                                        return true;
                                    }

                                    if (DragonCommands.isDouble(arg1) && isDouble) {
                                        this.setConfigVar(sender, args[0].toLowerCase().trim(), Double.parseDouble(arg1), ThisWorld);
                                        return true;
                                    }

                                    if ((arg1.equals("false") || arg1.equals("true") || arg1.equals("default") || arg1.equals("null"))
                                            && isBool) {
                                        this.setConfigVar(sender, args[0].toLowerCase().trim(),
                                                !arg1.equals("default") && !arg1.equals("null") ? Boolean.parseBoolean(arg1) : null,
                                                ThisWorld);
                                        return true;
                                    }

                                    sender.sendMessage(ChatColor.RED + "Wrong format!");
                                }
                            } else {
                                this.dragonSpawnHelp(sender);
                            }
                        }
                    }
                } else {
                    if (args.length >= 5 && DragonCommands.isDouble(args[1]) && DragonCommands.isDouble(args[2])
                            && DragonCommands.isDouble(args[3])) {
                        String w = null;
                        if (!this.isWorld(args[4])) {
                            sender.sendMessage(ChatColor.RED + "World " + args[4] + " doesn't exist!");
                            return false;
                        }

                        w = args[4].trim().toLowerCase();
                        final double newArgs = Double.parseDouble(args[1]);
                        final double arg1 = Double.parseDouble(args[2]);
                        final double bool = Double.parseDouble(args[3]);
                        if (w != null) {
                            Integer isAdd = null;
                            if (args[0].toLowerCase().startsWith("add")) {
                                if (args.length < 6 || !DragonCommands.isInteger(args[5])) {
                                    sender.sendMessage(ChatColor.RED + "You need to enter a number as 5th argument, see help...");
                                    return false;
                                }

                                isAdd = Integer.parseInt(args[5]);
                            }

                            this.setDragonSpawn(sender, newArgs, arg1, bool, w, isAdd);
                            this.plugin.configManager.setDragonDefaults();
                            return true;
                        }
                    }

                    this.dragonSpawnHelp(sender);
                }
            }
        }

        return false;
    }

    private boolean getDragonSpawn(final CommandSender p, final String Worldname) {
        if (!this.checkWorld(p, Worldname)) {
            return false;
        } else {
            double x = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + ".x");
            double y = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + ".y");
            double z = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + ".z");
            p.sendMessage(ChatColor.GREEN + "Dragon spawn is set at: " + (int) x + " " + (int) y + " " + (int) z + " in " + Worldname);

            for (final String spawn : this.plugin.getConfig().getConfigurationSection("spawnpoint." + Worldname).getKeys(false)) {
                if (spawn.startsWith("dragon_")) {
                    x = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + "." + spawn + ".x");
                    y = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + "." + spawn + ".y");
                    z = this.plugin.getConfig().getDouble("spawnpoint." + Worldname + "." + spawn + ".z");
                    p.sendMessage(ChatColor.GREEN + "Additional Dragon spawn is set to: " + (int) x + " " + (int) y + " " + (int) z
                            + " for " + spawn);
                }
            }

            return true;
        }
    }

    private void setDragonSpawn(final CommandSender p, final double x, final double y, final double z, String w, final Integer dragon) {
        final World W = this.plugin.getDragonWorldFromString(w);
        final Environment env = W.getEnvironment();
        if (env != Environment.THE_END) {
            p.sendMessage(ChatColor.YELLOW + "World " + W.getName() + " is not an End-World !");
            if (env == Environment.NETHER) {
                p.sendMessage(ChatColor.RED + "Nether is not supported...");
                return;
            }

            p.sendMessage(ChatColor.YELLOW + "Be cautious what you do with this...!");
        }

        if (dragon != null) {
            w = w + ".dragon_" + dragon;
        }

        this.plugin.getConfig().set("spawnpoint." + w + ".x", x);
        this.plugin.getConfig().set("spawnpoint." + w + ".y", y);
        this.plugin.getConfig().set("spawnpoint." + w + ".z", z);
        this.plugin.saveConfig();
        p.sendMessage(ChatColor.GREEN + "Dragon spawn set to: " + (int) x + " " + (int) y + " " + (int) z + " in " + w
                + (dragon != null ? " (used as ADDspawn)" : " (used as SETspawn)"));
    }

    private boolean remDragonSpawn(final CommandSender p, final String w) {
        if (!this.checkWorld(p, w)) {
            return false;
        } else {
            final long now = System.currentTimeMillis() / 50L;
            final long duration = 200L;
            if (now - this.remspawnConfirmTimer > duration) {
                this.remspawnConfirmTimer = now;
                p.sendMessage(ChatColor.GREEN + "Do you realy want to remove all (!) spawns from " + w
                        + " (and the world from being used by this plugin)?");
                p.sendMessage(ChatColor.GREEN + "You have to confirm by repeating the same command within 10 seconds to run it!");
                return false;
            } else {
                this.plugin.getConfig().set("spawnpoint." + w, (Object) null);
                this.plugin.getConfig().set("dragon." + w, (Object) null);
                this.plugin.saveConfig();
                p.sendMessage(ChatColor.GREEN + "Dragon spawn removed from world: " + w);
                this.remspawnConfirmTimer = 0L;
                return true;
            }
        }
    }

    private boolean checkWorld(final CommandSender p, final String Worldname) {
        final World W = this.plugin.getDragonWorldFromString(Worldname);
        if (W.getEnvironment() != Environment.THE_END) {
            p.sendMessage(ChatColor.RED + "World " + Worldname + " is not an End-World !");
        }

        if (!this.plugin.checkWorld(Worldname)) {
            p.sendMessage(ChatColor.RED + "World " + Worldname + " is not used by DragonSlayer !");
            return false;
        } else {
            return true;
        }
    }

    private void setConfigVar(final CommandSender p, final String var, final Object value, final String ThisWorld) {
        if (this.plugin.checkWorld(ThisWorld)) {
            this.plugin.getConfig().set("dragon." + ThisWorld + "." + var, value);
            this.plugin.saveConfig();
            p.sendMessage(ChatColor.GREEN + var + " set to " + String.valueOf(value).replace('&', '§'));
        } else {
            p.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }

    }

    private void getConfigVar(final CommandSender p, final String var, final String ThisWorld) {
        if (this.plugin.checkWorld(ThisWorld)) {
            final String value = this.plugin.getConfig().getString("dragon." + ThisWorld + "." + var);
            if (value != null) {
                p.sendMessage(ChatColor.GREEN + var + " is " + value + " !");
            } else {
                p.sendMessage(ChatColor.RED + var + " doesn't exist !");
            }
        } else {
            p.sendMessage(ChatColor.RED + "This world is not used by the plugin, yet!");
        }

    }

    private void dragonSpawnHelp(final CommandSender p) {
        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("reload"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer reload");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("setspawn"))) {
            if (p instanceof Player) {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer setspawn [x y z [worldname]]");
            } else {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer setspawn {x y z} {worldname}");
            }
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("getspawn"))) {
            if (p instanceof Player) {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer getspawn [world]");
            } else {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer getspawn {worldname}");
            }
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("addspawn"))) {
            if (p instanceof Player) {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer addspawn {number}");
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer addspawn {x y z} [worldname] {number}");
            } else {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer addspawn {x y z} {worldname} {number}");
            }
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("remspawn"))) {
            if (p instanceof Player) {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer remspawn [world]");
            } else {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer remspawn {worldname}");
            }
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("setstatue"))) {
            if (p instanceof Player) {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer setstatue [x y z [world [yaw]] ]");
            } else {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer setstatue {x y z} {world} [yaw]");
            }
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("getstatue"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer getstatue");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("remstatue"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer remstatue");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("forcerespawn"))) {
            if (p instanceof Player) {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer forcerespawn [world]");
            } else {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer forcerespawn {world}");
            }
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("forceallrespawn"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer forceallrespawn");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("worldrefresh"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer worldrefresh [world]");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("worldreset"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer worldreset [world]");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("showtimer"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer showtimer [world]");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("cleartimer"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer cleartimer");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("removedragons"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer removedragons [world] [force]");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("scoreboardreset"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer scoreboardreset");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("firstjoinreset"))) {
            p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer firstjoinreset");
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("name"))) {
            if (p instanceof Player) {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer name [world]");
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer name {New Name} [world] [Dragon number]");
            } else {
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer name {world}");
                p.sendMessage(ChatColor.RED + "Proper usage: /dragonslayer name {New Name} {world} [Dragon number]");
            }
        }

        if (p.hasPermission((String) this.mainArgumentsAndPerms.get("config"))) {
            p.sendMessage(ChatColor.RED + "Proper usage for getting config values:");
            p.sendMessage(ChatColor.RED + "/dragonslayer config {config value} [world]");
            p.sendMessage(ChatColor.RED + "Proper usage for setting config values:");
            p.sendMessage(ChatColor.RED + "/dragonslayer config {config value} {number} [world]");
            p.sendMessage(ChatColor.RED + "/dragonslayer config {config value} {true/false/default} [world]");
        }

    }

    private boolean isWorld(final String string) { return Bukkit.getWorld(string) != null; }

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

    private void setArmorStand(final CommandSender p, final double x, final double y, final double z, final String w, final float yaw) {
        this.plugin.RemoveArmorStand();
        this.plugin.getConfig().set("statue.world", w);
        this.plugin.getConfig().set("statue.x", x);
        this.plugin.getConfig().set("statue.y", y);
        this.plugin.getConfig().set("statue.z", z);
        this.plugin.getConfig().set("statue.yaw", yaw);
        this.plugin.saveConfig();
        this.plugin.PlaceArmorStand(w, x, y, z, yaw);
        p.sendMessage(ChatColor.GREEN + "Statue set to: " + (int) x + " " + (int) y + " " + (int) z + " in " + w);
    }

    private void getArmorStand(final CommandSender p) {
        final String world = this.plugin.getConfig().getString("statue.world");
        if (world != null) {
            final String ThisWorldsName = Bukkit.getServer().getWorld(world).getName();
            final double x = this.plugin.getConfig().getDouble("statue.x");
            final double y = this.plugin.getConfig().getDouble("statue.y");
            final double z = this.plugin.getConfig().getDouble("statue.z");
            p.sendMessage(ChatColor.GREEN + "Statue is placed at: " + (int) x + " " + (int) y + " " + (int) z + " in " + ThisWorldsName);
        } else {
            p.sendMessage(ChatColor.GREEN + "There is no statue placed anywhere... ");
        }

    }

    private void remArmorStand(final CommandSender p) {
        if (DragonSlayer.protLibHandler != null) {
            DragonSlayer.protLibHandler.removeNPCStatue();
        }

        if (this.plugin.RemoveArmorStand()) {
            this.plugin.getConfig().set("statue", (Object) null);
            this.plugin.saveConfig();
            p.sendMessage(ChatColor.GREEN + "Statue removed !");
        } else {
            p.sendMessage(ChatColor.RED + "Statue remove failed !");
        }

    }

    private void remDrags(final String ThisWorldsName, final boolean forceAll) {
        final World ThisWorld = Bukkit.getServer().getWorld(ThisWorldsName);
        this.plugin.RemoveDragons(ThisWorld, false, forceAll);
        DragonSlayer.cleanupDragons();
    }

    private void setDragonName(String name, final String world, final int id, final CommandSender sender) {
        name = name.replace('§', '&');
        if (id == 0) {
            this.setConfigVar(sender, "name", name, world);
        } else {
            this.setConfigVar(sender, "name_" + String.valueOf(id), name, world);
        }

    }

    private void executeWorldReset(final String ThisWorldsName, final CommandSender p) {
        final long now = System.currentTimeMillis() / 50L;
        final long duration = 200L;
        if (now - this.resetConfirmTimer > duration) {
            this.resetConfirmTimer = now;
            p.sendMessage(ChatColor.GREEN + "Do You realy want to reset world " + ThisWorldsName + " and lose all progress???");
            p.sendMessage(ChatColor.GREEN + "You have to confirm! Repeat the same command again within 10 seconds to execute it!");
        } else {
            this.resetConfirmTimer = 0L;
            p.sendMessage(ChatColor.GREEN + "The world " + ThisWorldsName + " will get recreated!");
            this.plugin.WorldReset(ThisWorldsName, true);
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (command.getName().equalsIgnoreCase("dragonslayer")) {
            final List<String> finalList = new ArrayList<String>();
            if (args.length == 1) {
                if (!args[0].equals("")) {
                    for (final String string : this.mainArgumentsAndPerms.keySet()) {
                        if (string.startsWith(args[0].toLowerCase())
                                && sender.hasPermission((String) this.mainArgumentsAndPerms.get(string))) {
                            finalList.add(string);
                        }
                    }
                } else {
                    this.mainArgumentsAndPerms.keySet().forEach(sx -> {
                        if (sender.hasPermission((String) this.mainArgumentsAndPerms.get(sx))) {
                            finalList.add(sx);
                        }

                    });
                }

                return finalList;
            }

            if (args.length == 0) {
                this.mainArgumentsAndPerms.keySet().forEach(sx -> {
                    if (sender.hasPermission((String) this.mainArgumentsAndPerms.get(sx))) {
                        finalList.add(sx);
                    }

                });
                return finalList;
            }

            if (args.length > 1) {
                args[0] = this.argsTranslator(args[0]);
                if (this.mainArgumentsAndPerms.get(args[0].toLowerCase()) == null) {
                    return null;
                }

                if (sender instanceof Player && !sender.hasPermission((String) this.mainArgumentsAndPerms.get(args[0].toLowerCase()))) {
                    return null;
                }

                Set<String> usedMapList;
                label198: {
                    usedMapList = this.plugin.configManager.getMaplist();
                    String var7;
                    switch ((var7 = args[0].toLowerCase()).hashCode()) {
                    case -1884257688:
                        if (!var7.equals("showtimer")) {
                            break label198;
                        }
                        break;
                    case -509310175:
                        if (!var7.equals("remspawn")) {
                            break label198;
                        }
                        break;
                    case -61066275:
                        if (!var7.equals("forcerespawn")) {
                            break label198;
                        }
                        break;
                    case 3373707:
                        if (!var7.equals("name")) {
                            break label198;
                        }
                        break;
                    case 475098365:
                        if (!var7.equals("worldreset")) {
                            break label198;
                        }
                        break;
                    case 1291366153:
                        if (!var7.equals("worldrefresh")) {
                            break label198;
                        }
                        break;
                    case 1443377084:
                        if (!var7.equals("removedragons")) {
                            break label198;
                        }
                        break;
                    case 1995016677:
                        if (!var7.equals("getspawn")) {
                            break label198;
                        }
                        break;
                    default:
                        break label198;
                    }

                    if (args.length == 2) {
                        finalList.addAll(usedMapList);
                    }
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
                } else if (!args[0].equalsIgnoreCase("setspawn") && !args[0].equalsIgnoreCase("addspawn")
                        && !args[0].equalsIgnoreCase("setstatue")) {
                    if (args[0].equalsIgnoreCase("config")) {
                        if (args.length == 2) {
                            final String[] doubleList = this.DoubleVarList.split(",");
                            final String[] boolList = this.BoolVarList.split(",");
                            final String[] intList = this.IntVarList.split(",");

                            for (final String s : doubleList) {
                                finalList.add(s);
                            }

                            for (final String s : boolList) {
                                finalList.add(s);
                            }

                            for (final String s : intList) {
                                finalList.add(s);
                            }

                            finalList.add("createportal_{number}");
                            Collections.sort(finalList);
                        } else if (args.length == 3) {
                            if (!this.BoolVarList.contains(args[1]) && !args[1].startsWith("createportal_")) {
                                if (this.DoubleVarList.contains(args[1]) || this.IntVarList.contains(args[1])) {
                                    finalList.add("{number}");
                                }
                            } else {
                                finalList.add("true");
                                finalList.add("false");
                                finalList.add("default");
                            }

                            finalList.addAll(usedMapList);
                        } else if (args.length == 4 && (this.DoubleVarList.contains(args[1]) || this.IntVarList.contains(args[1])
                                || this.BoolVarList.contains(args[1]) || args[1].startsWith("createportal_"))) {
                            finalList.addAll(usedMapList);
                        }
                    }
                } else {
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
                }
            }

            if (!finalList.isEmpty()) {
                return finalList;
            }
        }

        return null;
    }

    private String argsTranslator(String arg) {
        String var2;
        switch ((var2 = arg.toLowerCase()).hashCode()) {
        case -485484338:
            if (var2.equals("scoreres")) {
                arg = "scoreboardreset";
            }

            return arg;
        case 96417:
            if (var2.equals("add")) {
                arg = "addspawn";
            }

            return arg;
        case 102230:
            if (var2.equals("get")) {
                arg = "getspawn";
            }

            return arg;
        case 112793:
            if (var2.equals("rel")) {
                arg = "reload";
            }

            return arg;
        case 112794:
            if (var2.equals("rem")) {
                arg = "remspawn";
            }

            return arg;
        case 113762:
            if (var2.equals("set")) {
                arg = "setspawn";
            }

            return arg;
        case 3496714:
            if (var2.equals("remd")) {
                arg = "removedragons";
            }

            return arg;
        case 25374157:
            if (!var2.equals("getarmorstand")) {
                return arg;
            }

            return "getstatue";
        case 97618667:
            if (var2.equals("force")) {
                arg = "forcerespawn";
            }

            return arg;
        case 98246152:
            if (!var2.equals("getas")) {
                return arg;
            }

            return "getstatue";
        case 98246711:
            if (!var2.equals("getst")) {
                return arg;
            }

            return "getstatue";
        case 108398156:
            if (!var2.equals("remas")) {
                return arg;
            }
            break;
        case 108398715:
            if (!var2.equals("remst")) {
                return arg;
            }
            break;
        case 108404047:
            if (var2.equals("reset")) {
                arg = "worldreset";
            }

            return arg;
        case 109328404:
            if (!var2.equals("setas")) {
                return arg;
            }

            return "setstatue";
        case 109328963:
            if (!var2.equals("setst")) {
                return arg;
            }

            return "setstatue";
        case 464945878:
            if (var2.equals("forceall")) {
                arg = "forceallrespawn";
            }

            return arg;
        case 786666001:
            if (!var2.equals("remarmorstand")) {
                return arg;
            }
            break;
        case 799505369:
            if (!var2.equals("setarmorstand")) {
                return arg;
            }

            return "setstatue";
        case 1085444827:
            if (var2.equals("refresh")) {
                arg = "worldrefresh";
            }

            return arg;
        default:
            return arg;
        }

        return "remstatue";
    }
}
