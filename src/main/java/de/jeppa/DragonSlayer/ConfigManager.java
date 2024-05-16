package de.jeppa.DragonSlayer;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;

import me.clip.placeholderapi.PlaceholderAPI;

public class ConfigManager {
    DragonSlayer plugin;

    public ConfigManager(final DragonSlayer instance) { this.plugin = instance; }

    public void loadConfiguration() {
        boolean headerSet = false;

        try {
            this.plugin.saveDefaultConfig();
            final List<String> headerStrings = this.plugin.getConfig().options().getHeader();
            final YamlConfiguration defConf = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(this.plugin.getResource("config.yml")));
            final List<String> headerStrings_neu = defConf.options().getHeader();
            if (!headerStrings.equals(headerStrings_neu)) {
                this.plugin.getConfig().options().setHeader(headerStrings_neu);
            }

            headerSet = true;
            final Set<String> oldList = this.plugin.getConfig().getKeys(true);

            for (final String confPunkt : defConf.getKeys(true)) {
                if (!oldList.contains(confPunkt)) {
                    this.plugin.getConfig().set(confPunkt, defConf.get(confPunkt));
                }

                final List<String> comment = this.plugin.getConfig().getComments(confPunkt);
                final List<String> comment_def = defConf.getComments(confPunkt);
                if (!comment.equals(comment_def) && comment_def != null) {
                    this.plugin.getConfig().setComments(confPunkt, comment_def);
                }

                final List<String> inl_comment = this.plugin.getConfig().getInlineComments(confPunkt);
                final List<String> inl_comment_def = defConf.getInlineComments(confPunkt);
                if (!inl_comment.equals(inl_comment_def) && inl_comment_def != null) {
                    this.plugin.getConfig().setInlineComments(confPunkt, inl_comment_def);
                }
            }
        } catch (final NoSuchMethodError var12) {
        }

        if (!headerSet) {
            this.plugin.getConfig().options().copyDefaults(true);
            this.plugin.getConfig().options().parseComments(true);
            this.plugin.saveDefaultConfig();
        }

        this.plugin.saveConfig();
    }

    private boolean getConfigBoolean(final String Mapname, final String var) {
        return Boolean.parseBoolean(this.getConfigString(Mapname, var));
    }

    private int getConfigInt(final String Mapname, final String var) { return Integer.parseInt(this.getConfigString(Mapname, var)); }

    private double getConfigDouble(final String Mapname, final String var) {
        return Double.parseDouble(this.getConfigString(Mapname, var));
    }

    private String getConfigString(final String Mapname, final String var) {
        final String TestWord = this.plugin.getConfig().getString("dragon." + Mapname + "." + var);
        if (TestWord == null) {
            this.plugin.getConfig().set("dragon." + Mapname + "." + var, this.plugin.getConfig().getString("dragon._default." + var));
        }

        return this.plugin.getConfig().getString("dragon." + Mapname + "." + var);
    }

    private List<String> getConfigStringList(final String mapname, final String var) {
        final List<String> strList = this.plugin.getConfig().getStringList("dragon." + mapname + "." + var);
        if (strList.isEmpty()) {
            final String str = this.getConfigString(mapname, var);
            if (str != null) {
                String[] splitted = str.split(";");
                for (final String str2 : splitted) {
                    strList.add(str2);
                }

                if (!strList.isEmpty()) {
                    this.plugin.getConfig().set("dragon." + mapname + "." + var, strList);
                    this.plugin.saveConfig();
                    if (this.getVerbosity()) {
                        this.plugin.logger.info("Old command format found and converted...");
                    }
                }
            }
        }

        return strList;
    }

    int getDelay(final String Mapname) {
        final int delay = this.getConfigInt(Mapname, "respawndelay");
        if (delay == -1) {
            return 0;
        } else if (delay == -2) {
            return -1;
        } else if (delay < 1) {
            this.plugin.logger.warning("Invalid dragon respawn delay set, reverting to default: 360 minutes (6 hours)");
            this.plugin.getConfig().set("dragon." + Mapname + ".respawndelay", 360);
            this.plugin.saveConfig();
            return 432000;
        } else {
            return delay * 1200;
        }
    }

    boolean getNoAutoRespawn(final String Mapname, final int id) {
        final String id_ = String.valueOf(id);
        final String TestForRespawn = this.plugin.getConfig().getString("dragon." + Mapname + ".noautorespawn_" + id_);
        return TestForRespawn == null ? false : Boolean.parseBoolean(TestForRespawn);
    }

    boolean getResetWorld(final String Mapname) { return this.getConfigBoolean(Mapname, "resetworld"); }

    boolean getRefreshWorld(final String Mapname) { return this.getConfigBoolean(Mapname, "resetcrystal"); }

    int getResetDelay(final String Mapname) {
        final int delay = this.getConfigInt(Mapname, "resetworlddelay");
        if (delay < 1) {
            this.plugin.logger.warning("Invalid world reset delay set, reverting to default: 300 minutes (5 hours)");
            this.plugin.getConfig().set("dragon." + Mapname + ".resetworlddelay", 300);
            this.plugin.saveConfig();
            return 360000;
        } else {
            return delay * 1200;
        }
    }

    int getWarnTime(final String Mapname) {
        return this.getConfigInt(Mapname, "resetwarntime") < 0 ? 1200 : this.getConfigInt(Mapname, "resetwarntime") * 1200;
    }

    boolean getRespawnPlayer(final String Mapname) { return this.getConfigBoolean(Mapname, "respawnplayers"); }

    boolean getSlayerByPercent(final String mapname) {
        return this.plugin.getConfig().getBoolean("dragon." + mapname + ".slayerbypercent",
                Boolean.parseBoolean(this.plugin.getConfig().getString("global.slayerbypercent")));
    }

    boolean getSlayerByRank() { return Boolean.parseBoolean(this.plugin.getConfig().getString("global.slayerbyrank")); }

    private boolean getAutofix() { return Boolean.parseBoolean(this.plugin.getConfig().getString("global.trydragonautofix")); }

    boolean getAutofix(final String mapName) {
        return this.plugin.getConfig().getBoolean("dragon." + mapName + ".trydragonautofix", this.getAutofix());
    }

    boolean getDark(final String Mapname) {
        return this.plugin.getConfig().getBoolean("dragon." + Mapname + ".darkness", this.plugin.getConfig().getBoolean("global.darkness"));
    }

    boolean getfirstjoin(final String Mapname) {
        return this.plugin.getConfig().getBoolean("dragon." + Mapname + ".first_join_dragonspawn",
                this.plugin.getConfig().getBoolean("global.first_join_dragonspawn"));
    }

    boolean getVerbosity() { return Boolean.parseBoolean(this.plugin.getConfig().getString("global.verbose")); }

    boolean debugOn() { return this.plugin.getConfig().getBoolean("global.debug", false); }

    boolean keepChunksLoaded() { return this.plugin.getConfig().getBoolean("global.keepchunks", true); }

    boolean getDragonTeleport(final String mapName) {
        return this.plugin.getConfig().getBoolean("dragon." + mapName + ".teleportdragons",
                Boolean.parseBoolean(this.plugin.getConfig().getString("global.teleportdragons")));
    }

    boolean usePapiASName() {
        this.translateASConfigName("usepapi");
        return Boolean.parseBoolean(this.plugin.getConfig().getString("global.statue_usepapi"));
    }

    String getAS_PapiPH() {
        this.translateASConfigName("placeholder");
        return this.plugin.getConfig().getString("global.statue_placeholder");
    }

    String getAS_PapiFormat() {
        this.translateASConfigName("format");
        return this.plugin.getConfig().getString("global.statue_format").replace('&', '§');
    }

    void translateASConfigName(final String var) {
        if (this.plugin.getConfig().getString("global.armorstand_" + var) != null) {
            this.plugin.getConfig().set("global.statue_" + var, this.plugin.getConfig().getString("global.armorstand_" + var));
            this.plugin.getConfig().set("global.armorstand_" + var, (Object) null);
            this.plugin.saveConfig();
        }

    }

    String getSlayerPAPINick() {
        final String playerDisplayName = this.getArmorstandPAPIname();
        return playerDisplayName != null ? playerDisplayName : this.plugin.getSlayer();
    }

    String getSlayerPAPIFormatNickString() {
        final String playerDisplayName = this.getArmorstandPAPIname();
        return playerDisplayName != null ? this.getAS_PapiFormat().replace("%slayer%", playerDisplayName)
                : this.getAS_PapiFormat().replace("%slayer%", this.plugin.getSlayer() != null ? this.plugin.getSlayer() : "NONE");
    }

    String getArmorstandPAPIname() {
        if (DragonSlayer.PAPIenabled && this.usePapiASName()) {
            String playerDisplayName = this.getAS_PapiPH();
            if (playerDisplayName != null & !playerDisplayName.isEmpty()) {
                final OfflinePlayer slayr = this.plugin.getOfflineSlayer();
                if (slayr != null) {
                    playerDisplayName = PlaceholderAPI.setPlaceholders(slayr, playerDisplayName);
                }

                if (!playerDisplayName.isEmpty() && !playerDisplayName.contains("name%")) {
                    return playerDisplayName;
                }
            }
        }

        return null;
    }

    boolean getMultiPortal() { return Boolean.parseBoolean(this.plugin.getConfig().getString("global.multiportal")); }

    public Location getDragonSpawn(final String Mapname) {
        return new Location(this.plugin.getDragonWorldFromString(Mapname),
                this.plugin.getConfig().getDouble("spawnpoint." + Mapname + ".x"),
                this.plugin.getConfig().getDouble("spawnpoint." + Mapname + ".y"),
                this.plugin.getConfig().getDouble("spawnpoint." + Mapname + ".z"));
    }

    int getPortalXdef(final String Mapname) {
        double x = this.plugin.getConfig().getDouble("spawnpoint." + Mapname.toLowerCase() + ".x");
        if (x < 0.0D) {
            --x;
        }

        return this.getMultiPortal() && this.plugin.checkServerStarted() ? (int) x : 0;
    }

    int getPortalZdef(final String Mapname) {
        double z = this.plugin.getConfig().getDouble("spawnpoint." + Mapname.toLowerCase() + ".z");
        if (z < 0.0D) {
            --z;
        }

        return this.getMultiPortal() && this.plugin.checkServerStarted() ? (int) z : 0;
    }

    int getPortalX(final String Mapname, final int id) {
        return this.getPortalX(Mapname, id, this.getMultiPortal(), this.plugin.checkServerStarted());
    }

    int getPortalZ(final String Mapname, final int id) {
        return this.getPortalZ(Mapname, id, this.getMultiPortal(), this.plugin.checkServerStarted());
    }

    int getPortalX(final String Mapname, final int id, final boolean getMultiPortal, final boolean checkServerStarted) {
        double x = 0.0D;
        final String testValue = id > 0 ? this.plugin.getConfig().getString("spawnpoint." + Mapname.toLowerCase() + ".dragon_" + id + ".x")
                : null;
        if (testValue != null) {
            x = Double.parseDouble(testValue);
        }

        if (x < 0.0D) {
            --x;
        }

        return getMultiPortal && checkServerStarted ? (testValue != null ? (int) x : this.getPortalXdef(Mapname)) : 0;
    }

    int getPortalZ(final String Mapname, final int id, final boolean getMultiPortal, final boolean checkServerStarted) {
        double z = 0.0D;
        final String testValue = id > 0 ? this.plugin.getConfig().getString("spawnpoint." + Mapname.toLowerCase() + ".dragon_" + id + ".z")
                : null;
        if (testValue != null) {
            z = Double.parseDouble(testValue);
        }

        if (z < 0.0D) {
            --z;
        }

        return getMultiPortal && checkServerStarted ? (testValue != null ? (int) z : this.getPortalZdef(Mapname)) : 0;
    }

    int getPortalAggression(final boolean forceOff) {
        if (this.plugin.getConfig().getString("global.portalagression") != null) {
            this.plugin.getConfig().set("global.portal_aggression", this.plugin.getConfig().getString("global.portalagression"));
            this.plugin.getConfig().set("global.portalagression", (Object) null);
            this.plugin.saveConfig();
        }

        final int i = Integer.parseInt(this.plugin.getConfig().getString("global.portal_aggression"));
        if (forceOff) {
            return 9999;
        } else {
            return i >= -2 ? i : 0;
        }
    }

    boolean getNoSpawnSound() { return Boolean.parseBoolean(this.plugin.getConfig().getString("global.disable_global_spawnsound")); }

    boolean getDragonDeathFix(final String mapName) {
        return this.plugin.getConfig().getBoolean("dragon." + mapName + ".fixdeathflight",
                Boolean.parseBoolean(this.plugin.getConfig().getString("global.fixdeathflight")));
    }

    public String getPrefix() { return this.plugin.getConfig().getString("prefix.prefix").replace('&', '§'); }

    String getDragonDefaultName(final String Mapname) {
        return this.plugin.getConfig().getString("dragon." + Mapname + ".name").replace('&', '§');
    }

    String getDragonDefaultName(final String Mapname, final Integer id) {
        String id_ = "";
        if (id != null && id != 0) {
            id_ = "_" + String.valueOf(id);
        }

        return this.plugin.getConfig().getString("dragon." + Mapname + ".name" + id_).replace('&', '§');
    }

    public String[] getDragonNameAndID(final String Mapname) {
        this.getConfigString(Mapname, "name");
        final int maxD = this.getMaxdragons(Mapname);
        final Set<String> NameDrags = new HashSet<String>();
        final World MyWorld = this.plugin.getDragonWorldFromString(Mapname);
        final Collection<EnderDragon> Testdrags = this.plugin.getDragonList(MyWorld, Mapname);
        if (this.debugOn()) {
            this.plugin.logger.info("Dragons found on " + Mapname + " :" + Testdrags.size());
        }

        for (final EnderDragon Testdrag : Testdrags) {
            if (!Testdrag.isDead() && (Testdrag.getPhase() != Phase.DYING)) {
                NameDrags.add(Testdrag.getName().replaceAll("§[f0r]", "").trim());
            }
        }

        if (!this.getOneByOne(Mapname)) {
            for (int i = 1; i <= maxD; ++i) {
                final String retName = this.getDragonNameX(Mapname, i, NameDrags);
                if (retName != null) {
                    return new String[] { retName, String.valueOf(i) };
                }
            }
        } else if (maxD > 0) {
            final int i = (new Random()).nextInt(maxD) + 1;
            final String retName = this.getDragonNameX(Mapname, i, NameDrags);
            if (retName != null) {
                return new String[] { retName, String.valueOf(i) };
            }
        }

        return new String[] { this.getDragonDefaultName(Mapname), "0" };
    }

    private String getDragonNameX(final String Mapname, final int i, final Set<String> NameDrags) {
        String TestAddName = this.plugin.getConfig().getString("dragon." + Mapname + ".name_" + i);
        if (TestAddName != null) {
            TestAddName = TestAddName.replace('&', '§');
            final String Testname = TestAddName.replaceAll("§[f0r]", "");
            if (!NameDrags.contains(Testname)) {
                return TestAddName;
            }
        }

        return null;
    }

    String getDragonKillMessage(final String world, final Integer dragonID) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.onkill"), world, dragonID);
    }

    String getRespawnMessage(final String world, final Integer dragonID) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.respawn"), world, dragonID);
    }

    String getSlayerMessage() { return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.slayer"), (String) null); }

    String getRewardMessage(final String world, final String value, final Integer dragonID) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.reward").replace("$reward", value), world, dragonID);
    }

    String getXPRewardMessage(final String world, final String value, final Integer dragonID) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.xpreward").replace("$reward", value), world, dragonID);
    }

    String getNoSlayerMessage() { return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.noslayer"), (String) null); }

    String getDragonReKillMessage(final String world, final Integer dragonID) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.onrekill"), world, dragonID);
    }

    String getProtectMessage(final String world) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.protect"), world);
    }

    String getResetMessage(final String world) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.reset"), world);
    }

    String getPlayerRespawnMessage(final String world) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.playerrespawn"), world);
    }

    private List<String> getConfigCommand(final String world, final String commandName, final Integer commandNumber,
            final Integer dragonId) {
        List<String> commands = this.getConfigStringList(world,
                commandNumber != null && commandNumber != 0 ? commandName + "_" + commandNumber : commandName);
        if (commands.isEmpty()) {
            commands = this.getConfigStringList(world, commandName);
        }

        final List<String> commands2 = new ArrayList<String>();
        commands2.addAll(commands);
        commands2.replaceAll(command -> this.plugin.replaceValues(command, world, dragonId));
        return commands2;
    }

    List<String> getDragonCommand(final String world, final int i) { return this.getConfigCommand(world, "command", i, i); }

    List<String> getRankCommand(final String world, final int rank, final Integer dragonID) {
        return this.getConfigCommand(world, "rankcommand" + (rank != 0 ? "_" + rank : ""), dragonID, dragonID);
    }

    List<String> getSpawnCommand(final String world, final Integer dragonID) {
        return this.getConfigCommand(world, "spawncommand", dragonID, dragonID);
    }

    List<String> getRespawnCommand(final String world) {
        return this.getConfigCommand(world, "respawncommand", (Integer) null, (Integer) null);
    }

    String getDiedMessage() { return this.plugin.getConfig().getString("messages.died"); }

    String getTimertext() { return this.plugin.getConfig().getString("messages.timertext"); }

    String getTimerline() { return this.plugin.getConfig().getString("messages.timerline", ""); }

    String getResetline() { return this.plugin.getConfig().getString("messages.resetline", ""); }

    String getNoTimerPlaceholder() { return this.plugin.getConfig().getString("messages.notimer_ph").replace('&', '§'); }

    String getNoRankNamePlaceholder() { return this.plugin.getConfig().getString("messages.noname_ph").replace('&', '§'); }

    String getUnknownNamePlaceholder() { return this.plugin.getConfig().getString("messages.unknownname_ph").replace('&', '§'); }

    String getNoRankScorePlaceholder() { return this.plugin.getConfig().getString("messages.noscore_ph").replace('&', '§'); }

    String getShowtime() { return this.plugin.getConfig().getString("messages.showtime").replace('&', '§'); }

    String getShowreset() { return this.plugin.getConfig().getString("messages.showreset").replace('&', '§'); }

    String getShowtimeN() { return this.plugin.getConfig().getString("messages.showtime_n").replace('&', '§'); }

    String getScoreboardString() { return this.plugin.getConfig().getString("messages.scoreboard").replace('&', '§'); }

    String getCrystalDenyString() { return this.plugin.getConfig().getString("messages.crystaldeny"); }

    boolean getPrefixEnabled() { return this.plugin.getConfig().getBoolean("prefix.enabled"); }

    boolean getPrefixAsSuffix() { return this.plugin.getConfig().getBoolean("prefix.assuffix"); }

    boolean getForcePrefix() { return this.plugin.getConfig().getBoolean("prefix.force"); }

    boolean getTabListEnable() { return this.plugin.getConfig().getBoolean("prefix.tablist"); }

    int getTabListTime() {
        final int t = this.plugin.getConfig().getInt("prefix.tablist_time");
        if ((double) t >= 1.0D && t <= 30) {
            return t;
        } else {
            this.plugin.logger.warning("Invalid tablist time set, reverting to default: 15 seconds");
            this.plugin.getConfig().set("prefix.tablist_time", 15);
            this.plugin.saveConfig();
            return 15;
        }
    }

    public Set<String> getMaplist() {
        Set<String> WorldsList = new HashSet<String>();
        if (this.plugin.getConfig().isConfigurationSection("spawnpoint")) {
            WorldsList = this.plugin.getConfig().getConfigurationSection("spawnpoint").getKeys(false);
        }

        return WorldsList;
    }

    public int getMaxdragons(final String Mapname) { return this.getConfigInt(Mapname, "maxdragons"); }

    boolean getOneByOne(final String Mapname) { return this.getConfigBoolean(Mapname, "onebyone"); }

    private double getDragonHealth(final String Mapname) {
        final double h = this.getConfigDouble(Mapname, "health");
        double maxH = 2048.0D;
        if (DragonSlayer.spigot) {
            maxH = (double) Bukkit.spigot().getConfig().getInt("settings.attribute.maxHealth.max");
        }

        if (h >= 0.0D && h <= maxH) {
            return h;
        } else {
            this.plugin.logger.warning("Invalid dragon health set, reverting to default: 200 (100 hearts)");
            this.plugin.getConfig().set("dragon." + Mapname + ".health", 200.0D);
            this.plugin.saveConfig();
            return 200.0D;
        }
    }

    int getDragonHealth_n(final String Mapname, final int dragonId) {
        final Integer health = (int) this.getDragonHealth(Mapname);
        final String healthStr = this.plugin.getConfig().getString("dragon." + Mapname + ".health_" + dragonId);
        final int health2 = healthStr != null ? (int) Double.parseDouble(healthStr) : -1;
        double maxH = 2048.0D;
        if (DragonSlayer.spigot) {
            maxH = (double) Bukkit.spigot().getConfig().getInt("settings.attribute.maxHealth.max");
        }

        return (double) health2 >= 0.0D && (double) health2 <= maxH ? health2 : health;
    }

    int getRegenSecs(final String Mapname) { return this.getConfigInt(Mapname, "regen_seconds"); }

    int getRegenAmount(final String Mapname) { return this.getConfigInt(Mapname, "regen_amount"); }

    Double getDragonDamage(final String Mapname, final int id) {
        final Double value = this.getConfigDouble(Mapname, "damage");
        final String TestValue = id > 0 ? this.plugin.getConfig().getString("dragon." + Mapname + ".damage_" + id) : null;
        return TestValue != null ? Double.parseDouble(TestValue) : value;
    }

    int getDragonExp(final String Mapname, final int id) {
        int e = this.getConfigInt(Mapname, "exp");
        final String TestValue = id > 0 ? this.plugin.getConfig().getString("dragon." + Mapname + ".exp_" + id) : null;
        if (TestValue != null) {
            e = Integer.parseInt(TestValue);
        }

        if (e >= 0 && e <= 1000000000) {
            return e;
        } else {
            this.plugin.logger.warning("Invalid dragon exp set, reverting to default: 12000");
            this.plugin.getConfig().set("dragon." + Mapname + ".exp" + (id > 0 ? "_" + String.valueOf(id) : ""), 12000);
            this.plugin.saveConfig();
            return 12000;
        }
    }

    double getReward_double(final String Mapname, final int id) {
        final Double value = this.getConfigDouble(Mapname, "reward");
        final String TestValue = id > 0 ? this.plugin.getConfig().getString("dragon." + Mapname + ".reward_" + id) : null;
        return TestValue != null ? Double.parseDouble(TestValue) : value;
    }

    int getDragonRange(final String Mapname, final int id) {
        int e = this.getConfigInt(Mapname, "range");
        final String TestValue = id > 0 ? this.plugin.getConfig().getString("dragon." + Mapname + ".range_" + id) : null;
        if (TestValue != null) {
            e = Integer.parseInt(TestValue);
        }

        if (e >= 0 && e <= 2048) {
            return e;
        } else {
            this.plugin.logger.warning("Invalid dragon range set, reverting to default: 16");
            this.plugin.getConfig().set("dragon." + Mapname + ".range" + (id > 0 ? "_" + String.valueOf(id) : ""), 16);
            this.plugin.saveConfig();
            return 16;
        }
    }

    int getBossbarDistance(final String Mapname) {
        final int e = this.getConfigInt(Mapname, "bossbar_distance");
        if (e >= 60) {
            return e;
        } else {
            this.plugin.logger.warning("Invalid dragon distance set, minimum is : 60");
            this.plugin.getConfig().set("dragon." + Mapname + ".bossbar_distance", 60);
            this.plugin.saveConfig();
            return 60;
        }
    }

    int getDragonEggChance(final String Mapname) {
        final double chance = this.getConfigDouble(Mapname, "eggchance") * 100.0D;
        if (chance >= 0.0D && chance <= 100.0D) {
            return (int) chance;
        } else {
            this.plugin.logger.warning("Invalid dragon egg chance set, reverting to default: 0.3");
            this.plugin.getConfig().set("dragon." + Mapname + ".eggchance", 0.3D);
            this.plugin.saveConfig();
            return 30;
        }
    }

    int getPortalEggChance(final String Mapname) {
        final double chance = this.getConfigDouble(Mapname, "portaleggchance") * 100.0D;
        if (chance >= 0.0D && chance <= 100.0D) {
            return (int) chance;
        } else {
            this.plugin.logger.warning("Invalid portal egg chance set, reverting to default: 1.0");
            this.plugin.getConfig().set("dragon." + Mapname + ".portaleggchance", 1.0D);
            this.plugin.saveConfig();
            return 100;
        }
    }

    boolean getEggItem(final String Mapname) { return this.getConfigBoolean(Mapname, "eggasitem"); }

    int getSkullChance(final String Mapname) {
        final double chance = this.getConfigDouble(Mapname, "skullchance") * 100.0D;
        if (chance >= 0.0D && chance <= 100.0D) {
            return (int) chance;
        } else {
            this.plugin.logger.warning("Invalid dragon skull chance set, reverting to default: 0.03");
            this.plugin.getConfig().set("dragon." + Mapname + ".skullchance", 0.03D);
            this.plugin.saveConfig();
            return 3;
        }
    }

    boolean getSkullItem(final String Mapname) { return this.getConfigBoolean(Mapname, "skullitem"); }

    boolean getEggCancel(final String Mapname) { return this.getConfigBoolean(Mapname, "cancelegg"); }

    private boolean getCreatePortalDefault(final String Mapname) {
        final String TestForPortal = this.plugin.getConfig().getString("dragon." + Mapname + ".createportal");
        if (TestForPortal == null) {
            this.plugin.getConfig().set("dragon." + Mapname + ".createportal", true);
        }

        return this.getConfigBoolean(Mapname, "createportal");
    }

    boolean getCreatePortal(final String Mapname, final Integer id) {
        String id_ = "";
        if (id != null && id != 0) {
            id_ = "_" + String.valueOf(id);
        }

        final String TestForPortal = this.plugin.getConfig().getString("dragon." + Mapname + ".createportal" + id_);
        return TestForPortal == null ? this.getCreatePortalDefault(Mapname) : Boolean.parseBoolean(TestForPortal);
    }

    boolean checkCreatePortalID(final String Mapname, final int id) {
        final String id_ = String.valueOf(id);
        final String TestForPortal = this.plugin.getConfig().getString("dragon." + Mapname + ".createportal_" + id_);
        return TestForPortal != null;
    }

    void setCreatePortal(final boolean b, final String Mapname) {
        this.plugin.getConfig().set("dragon." + Mapname + ".createportal", b);
        this.plugin.saveConfig();
    }

    boolean getAlternativeReward(final String Mapname) { return this.getConfigBoolean(Mapname, "alternativereward"); }

    boolean getDisplayDragonName(final String Mapname) { return this.getConfigBoolean(Mapname, "displaydragonname"); }

    boolean getGlowEffect(final String Mapname) { return this.getConfigBoolean(Mapname, "glow_effect"); }

    String getGlowColor(final String Mapname, final int id) {
        final String id_ = id > 0 ? "_" + String.valueOf(id) : "";
        final String DragCol_id = this.getConfigString(Mapname, "glow_color" + id_) != null
                ? this.getConfigString(Mapname, "glow_color" + id_)
                : this.getConfigString(Mapname, "glow_color");
        return DragCol_id.toUpperCase();
    }

    boolean getHitEffect(final String Mapname) { return this.getConfigBoolean(Mapname, "hit_indicator"); }

    boolean getCreateGateways(final String Mapname) { return this.getConfigBoolean(Mapname, "creategateways"); }

    boolean getFixGateways(final String Mapname) { return this.getConfigBoolean(Mapname, "fixgateways"); }

    boolean getOldPortal(final String Mapname) { return this.getConfigBoolean(Mapname, "oldportals"); }

    boolean getCrystalDeny(final String Mapname) { return this.getConfigBoolean(Mapname, "denycrystalplace"); }

    boolean getDenyCrystalExplosion(final String Mapname) { return this.getConfigBoolean(Mapname, "denycrystalexplode"); }

    boolean getDenyBedExplosion(final String Mapname) { return this.getConfigBoolean(Mapname, "denybedexplode"); }

    boolean getDisableOrigDragonRespawn(final String Mapname) { return this.getConfigBoolean(Mapname, "nomcdragonrespawn"); }

    boolean getBlockGrief(final String mapName) {
        return this.plugin.getConfig().getBoolean("dragon." + mapName + ".blockgrief",
                Boolean.parseBoolean(this.plugin.getConfig().getString("global.blockgrief")));
    }

    int getTimerfunc(final String w) { return this.getConfigInt(w, "timerfunc"); }

    void setDragonDefaults() {
        for (final String Mapname : this.getMaplist()) {
            if (Bukkit.getWorld(Mapname) != null) {
                this.getDragonNameAndID(Mapname);
                this.getDragonDamage(Mapname, 0);
                this.getDragonHealth(Mapname);
                this.getRegenSecs(Mapname);
                this.getRegenAmount(Mapname);
                this.getDragonRange(Mapname, 0);
                this.getBossbarDistance(Mapname);
                this.getDragonExp(Mapname, 0);
                this.getReward_double(Mapname, 0);
                this.getDragonEggChance(Mapname);
                this.getPortalEggChance(Mapname);
                this.getEggItem(Mapname);
                this.getSkullChance(Mapname);
                this.getSkullItem(Mapname);
                this.getEggCancel(Mapname);
                this.getDelay(Mapname);
                this.getCreatePortal(Mapname, (Integer) null);
                this.getOldPortal(Mapname);
                this.getCrystalDeny(Mapname);
                this.getDenyCrystalExplosion(Mapname);
                this.getDenyBedExplosion(Mapname);
                this.getCreateGateways(Mapname);
                this.getFixGateways(Mapname);
                this.getAlternativeReward(Mapname);
                this.getDisplayDragonName(Mapname);
                this.getResetWorld(Mapname);
                this.getRefreshWorld(Mapname);
                this.getResetDelay(Mapname);
                this.getWarnTime(Mapname);
                this.getTimerfunc(Mapname);
                this.getRespawnPlayer(Mapname);
                this.getRespawnCommand(Mapname);
                this.getSpawnCommand(Mapname, (Integer) null);
                this.getDragonCommand(Mapname, 0);
                this.getRankCommand(Mapname, 1, 0);
                this.getRankCommand(Mapname, 2, 0);
                this.getDisableOrigDragonRespawn(Mapname);
                this.getGlowEffect(Mapname);
                this.getGlowColor(Mapname, 0);
                this.getHitEffect(Mapname);
            }
        }

        this.plugin.saveConfig();
    }

    void checkOldConfig() {
        String[] oldConfVars = new String[] { "name", "damage", "health", "exp", "reward", "eggchance", "eggasitem", "skullchance",
                "skullitem", "cancelegg", "alternativereward", "respawndelay", "maxdragons", "oldportals", "creategateways", "resetworld",
                "resetcrystal", "resetworlddelay", "respawnplayers", "nomcdragonrespawn", "command", "respawncommand", "timerfunc" };
        this.checkOldConfig_sub(oldConfVars, "dragon._default.");
        oldConfVars = new String[] { "blockgrief", "slayer", "trydragonautofix", "bypassdragongateway", "teleportdragons" };
        this.checkOldConfig_sub(oldConfVars, "global.");
    }

    private void checkOldConfig_sub(final String[] oldConfVars, final String section) {
        boolean found = false;

        for (int i = 0; i < oldConfVars.length; ++i) {
            final String test = this.plugin.getConfig().getString("dragon." + oldConfVars[i]);
            if (test != null) {
                if (!found) {
                    this.plugin.reloadConfig();
                }

                found = true;
                this.plugin.getConfig().set(section + oldConfVars[i], test);
                this.plugin.getConfig().set("dragon." + oldConfVars[i], (Object) null);
            }
        }

        if (found) {
            this.plugin.saveConfig();
        }

    }
}
