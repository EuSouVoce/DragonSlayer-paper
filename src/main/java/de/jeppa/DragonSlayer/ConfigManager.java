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
        boolean hasHeader = false;

        try {
            this.plugin.saveDefaultConfig();
            final List<String> headerStrings = this.plugin.getConfig().options().getHeader();
            final YamlConfiguration defaultConfig = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(this.plugin.getResource("config.yml")));
            final List<String> defaultHeaderStrings = defaultConfig.options().getHeader();
            if (!headerStrings.equals(defaultHeaderStrings)) {
                this.plugin.getConfig().options().setHeader(defaultHeaderStrings);
            }

            hasHeader = true;
            final Set<String> currentConfigKeys = this.plugin.getConfig().getKeys(true);

            for (final String defaultKey : defaultConfig.getKeys(true)) {
                if (!currentConfigKeys.contains(defaultKey)) {
                    this.plugin.getConfig().set(defaultKey, defaultConfig.get(defaultKey));
                }

                final List<String> currentComment = this.plugin.getConfig().getComments(defaultKey);
                final List<String> defaultComment = defaultConfig.getComments(defaultKey);
                if (!currentComment.equals(defaultComment) && defaultComment != null) {
                    this.plugin.getConfig().setComments(defaultKey, defaultComment);
                }

                final List<String> currentInlineComment = this.plugin.getConfig().getInlineComments(defaultKey);
                final List<String> defaultInlineComment = defaultConfig.getInlineComments(defaultKey);
                if (!currentInlineComment.equals(defaultInlineComment) && defaultInlineComment != null) {
                    this.plugin.getConfig().setInlineComments(defaultKey, defaultInlineComment);
                }
            }
        } catch (final NoSuchMethodError ignored) {
        }

        if (!hasHeader) {
            this.plugin.getConfig().options().copyDefaults(true);
            this.plugin.getConfig().options().parseComments(true);
            this.plugin.saveDefaultConfig();
        }

        this.plugin.saveConfig();
    }

    private boolean getConfigBoolean(final String worldName, final String key) {
        return Boolean.parseBoolean(this.getConfigString(worldName, key));
    }

    private int getConfigInt(final String worldName, final String key) { return Integer.parseInt(this.getConfigString(worldName, key)); }

    private double getConfigDouble(final String worldName, final String key) {
        return Double.parseDouble(this.getConfigString(worldName, key));
    }

    private String getConfigString(final String worldName, final String key) {
        final String TestWord = this.plugin.getConfig().getString("dragon." + worldName + "." + key);
        if (TestWord == null) {
            this.plugin.getConfig().set("dragon." + worldName + "." + key, this.plugin.getConfig().getString("dragon._default." + key));
        }

        return this.plugin.getConfig().getString("dragon." + worldName + "." + key);
    }

    private List<String> getConfigStringList(final String worldName, final String key) {
        final List<String> strList = this.plugin.getConfig().getStringList("dragon." + worldName + "." + key);
        if (strList.isEmpty()) {
            final String str = this.getConfigString(worldName, key);
            if (str != null) {
                final String[] splitted = str.split(";");
                for (final String split : splitted) {
                    strList.add(split);
                }

                if (!strList.isEmpty()) {
                    this.plugin.getConfig().set("dragon." + worldName + "." + key, strList);
                    this.plugin.saveConfig();
                    if (this.getVerbosity()) {
                        this.plugin.logger.info("Old command format found and converted...");
                    }
                }
            }
        }

        return strList;
    }

    int getDelay(final String worldName) {
        final int delay = this.getConfigInt(worldName, "respawndelay");
        if (delay == -1) {
            return 0;
        } else if (delay == -2) {
            return -1;
        } else if (delay < 1) {
            this.plugin.logger.warning("Invalid dragon respawn delay set, reverting to default: 360 minutes (6 hours)");
            this.plugin.getConfig().set("dragon." + worldName + ".respawndelay", 360);
            this.plugin.saveConfig();
            return 432000;
        } else {
            return delay * 1200;
        }
    }

    boolean getNoAutoRespawn(final String worldName, final int dragonId) {
        final String TestForRespawn = this.plugin.getConfig()
                .getString("dragon." + worldName + ".noautorespawn_" + String.valueOf(dragonId));
        return TestForRespawn == null ? false : Boolean.parseBoolean(TestForRespawn);
    }

    boolean getResetWorld(final String worldName) { return this.getConfigBoolean(worldName, "resetworld"); }

    boolean getRefreshWorld(final String worldName) { return this.getConfigBoolean(worldName, "resetcrystal"); }

    int getResetDelay(final String worldName) {
        final int delay = this.getConfigInt(worldName, "resetworlddelay");
        if (delay < 1) {
            this.plugin.logger.warning("Invalid world reset delay set, reverting to default: 300 minutes (5 hours)");
            this.plugin.getConfig().set("dragon." + worldName + ".resetworlddelay", 300);
            this.plugin.saveConfig();
            return 360000;
        } else {
            return delay * 1200;
        }
    }

    int getWarnTime(final String worldName) {
        return this.getConfigInt(worldName, "resetwarntime") < 0 ? 1200 : this.getConfigInt(worldName, "resetwarntime") * 1200;
    }

    boolean getRespawnPlayer(final String worldName) { return this.getConfigBoolean(worldName, "respawnplayers"); }

    boolean getSlayerByPercent(final String worldName) {
        return this.plugin.getConfig().getBoolean("dragon." + worldName + ".slayerbypercent",
                Boolean.parseBoolean(this.plugin.getConfig().getString("global.slayerbypercent")));
    }

    boolean getSlayerByRank() { return Boolean.parseBoolean(this.plugin.getConfig().getString("global.slayerbyrank")); }

    private boolean getAutofix() { return Boolean.parseBoolean(this.plugin.getConfig().getString("global.trydragonautofix")); }

    boolean getAutofix(final String worldName) {
        return this.plugin.getConfig().getBoolean("dragon." + worldName + ".trydragonautofix", this.getAutofix());
    }

    boolean getDark(final String worldName) {
        return this.plugin.getConfig().getBoolean("dragon." + worldName + ".darkness",
                this.plugin.getConfig().getBoolean("global.darkness"));
    }

    boolean getfirstjoin(final String worldName) {
        return this.plugin.getConfig().getBoolean("dragon." + worldName + ".first_join_dragonspawn",
                this.plugin.getConfig().getBoolean("global.first_join_dragonspawn"));
    }

    boolean getVerbosity() { return Boolean.parseBoolean(this.plugin.getConfig().getString("global.verbose")); }

    boolean debugOn() { return this.plugin.getConfig().getBoolean("global.debug", false); }

    boolean keepChunksLoaded() { return this.plugin.getConfig().getBoolean("global.keepchunks", true); }

    boolean getDragonTeleport(final String worldName) {
        return this.plugin.getConfig().getBoolean("dragon." + worldName + ".teleportdragons",
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

    public Location getDragonSpawn(final String worldName) {
        return new Location(this.plugin.getDragonWorldFromString(worldName),
                this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".x"),
                this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".y"),
                this.plugin.getConfig().getDouble("spawnpoint." + worldName + ".z"));
    }

    int getPortalXdef(final String worldName) {
        double x = this.plugin.getConfig().getDouble("spawnpoint." + worldName.toLowerCase() + ".x");
        if (x < 0.0D) {
            --x;
        }

        return this.getMultiPortal() && this.plugin.checkServerStarted() ? (int) x : 0;
    }

    int getPortalZdef(final String worldName) {
        double z = this.plugin.getConfig().getDouble("spawnpoint." + worldName.toLowerCase() + ".z");
        if (z < 0.0D) {
            --z;
        }

        return this.getMultiPortal() && this.plugin.checkServerStarted() ? (int) z : 0;
    }

    int getPortalX(final String worldName, final int id) {
        return this.getPortalX(worldName, id, this.getMultiPortal(), this.plugin.checkServerStarted());
    }

    int getPortalZ(final String worldName, final int id) {
        return this.getPortalZ(worldName, id, this.getMultiPortal(), this.plugin.checkServerStarted());
    }

    int getPortalX(final String worldName, final int id, final boolean isMultiPortal, final boolean hasServerStarted) {
        double x = 0.0D;
        final String testValue = id > 0
                ? this.plugin.getConfig().getString("spawnpoint." + worldName.toLowerCase() + ".dragon_" + id + ".x")
                : null;
        if (testValue != null) {
            x = Double.parseDouble(testValue);
        }

        if (x < 0.0D) {
            --x;
        }

        return isMultiPortal && hasServerStarted ? (testValue != null ? (int) x : this.getPortalXdef(worldName)) : 0;
    }

    int getPortalZ(final String worldName, final int id, final boolean isMultiPortal, final boolean hasServerStarted) {
        double z = 0.0D;
        final String testValue = id > 0
                ? this.plugin.getConfig().getString("spawnpoint." + worldName.toLowerCase() + ".dragon_" + id + ".z")
                : null;
        if (testValue != null) {
            z = Double.parseDouble(testValue);
        }

        if (z < 0.0D) {
            --z;
        }

        return isMultiPortal && hasServerStarted ? (testValue != null ? (int) z : this.getPortalZdef(worldName)) : 0;
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

    boolean getDragonDeathFix(final String worldName) {
        return this.plugin.getConfig().getBoolean("dragon." + worldName + ".fixdeathflight",
                Boolean.parseBoolean(this.plugin.getConfig().getString("global.fixdeathflight")));
    }

    public String getPrefix() { return this.plugin.getConfig().getString("prefix.prefix").replace('&', '§'); }

    String getDragonDefaultName(final String worldName) {
        return this.plugin.getConfig().getString("dragon." + worldName + ".name").replace('&', '§');
    }

    String getDragonDefaultName(final String worldName, final Integer id) {
        String id_ = "";
        if (id != null && id != 0) {
            id_ = "_" + String.valueOf(id);
        }

        return this.plugin.getConfig().getString("dragon." + worldName + ".name" + id_).replace('&', '§');
    }

    public String[] getDragonNameAndID(final String worldName) {
        this.getConfigString(worldName, "name");
        final int maxD = this.getMaxdragons(worldName);
        final Set<String> NameDrags = new HashSet<String>();
        final World world = this.plugin.getDragonWorldFromString(worldName);
        final Collection<EnderDragon> dragons = this.plugin.getDragonList(world, worldName);
        if (this.debugOn()) {
            this.plugin.logger.info("Dragons found on " + worldName + " :" + dragons.size());
        }

        for (final EnderDragon dragon : dragons) {
            if (!dragon.isDead() && (dragon.getPhase() != Phase.DYING)) {
                NameDrags.add(dragon.getName().replaceAll("§[f0r]", "").trim());
            }
        }

        if (!this.getOneByOne(worldName)) {
            for (int i = 1; i <= maxD; ++i) {
                final String retName = this.getDragonNameX(worldName, i, NameDrags);
                if (retName != null) {
                    return new String[] { retName, String.valueOf(i) };
                }
            }
        } else if (maxD > 0) {
            final int i = (new Random()).nextInt(maxD) + 1;
            final String retName = this.getDragonNameX(worldName, i, NameDrags);
            if (retName != null) {
                return new String[] { retName, String.valueOf(i) };
            }
        }

        return new String[] { this.getDragonDefaultName(worldName), "0" };
    }

    private String getDragonNameX(final String worldName, final int dragonId, final Set<String> dragonsNames) {
        String TestAddName = this.plugin.getConfig().getString("dragon." + worldName + ".name_" + dragonId);
        if (TestAddName != null) {
            TestAddName = TestAddName.replace('&', '§');
            final String Testname = TestAddName.replaceAll("§[f0r]", "");
            if (!dragonsNames.contains(Testname)) {
                return TestAddName;
            }
        }

        return null;
    }

    String getDragonKillMessage(final String worldName, final Integer dragonId) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.onkill"), worldName, dragonId);
    }

    String getRespawnMessage(final String worldName, final Integer dragonId) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.respawn"), worldName, dragonId);
    }

    String getSlayerMessage() { return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.slayer"), (String) null); }

    String getRewardMessage(final String worldName, final String reward, final Integer dragonId) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.reward").replace("$reward", reward), worldName,
                dragonId);
    }

    String getXPRewardMessage(final String worldName, final String reward, final Integer dragonId) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.xpreward").replace("$reward", reward), worldName,
                dragonId);
    }

    String getNoSlayerMessage() { return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.noslayer"), (String) null); }

    String getDragonReKillMessage(final String worldName, final Integer dragonId) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.onrekill"), worldName, dragonId);
    }

    String getProtectMessage(final String worldName) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.protect"), worldName);
    }

    String getResetMessage(final String worldName) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.reset"), worldName);
    }

    String getPlayerRespawnMessage(final String worldName) {
        return this.plugin.replaceValues(this.plugin.getConfig().getString("messages.playerrespawn"), worldName);
    }

    private List<String> getConfigCommand(final String worldName, final String commandName, final Integer commandNumber,
            final Integer dragonId) {
        List<String> commands = this.getConfigStringList(worldName,
                commandNumber != null && commandNumber != 0 ? commandName + "_" + commandNumber : commandName);
        if (commands.isEmpty()) {
            commands = this.getConfigStringList(worldName, commandName);
        }

        final List<String> newCommands = new ArrayList<String>();
        newCommands.addAll(commands);
        newCommands.replaceAll(command -> this.plugin.replaceValues(command, worldName, dragonId));
        return newCommands;
    }

    List<String> getDragonCommand(final String worldName, final int dragonId) {
        return this.getConfigCommand(worldName, "command", dragonId, dragonId);
    }

    List<String> getRankCommand(final String worldName, final int rank, final Integer dragonId) {
        return this.getConfigCommand(worldName, "rankcommand" + (rank != 0 ? "_" + rank : ""), dragonId, dragonId);
    }

    List<String> getSpawnCommand(final String worldName, final Integer dragonId) {
        return this.getConfigCommand(worldName, "spawncommand", dragonId, dragonId);
    }

    List<String> getRespawnCommand(final String worldName) {
        return this.getConfigCommand(worldName, "respawncommand", (Integer) null, (Integer) null);
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
        Set<String> worlds = new HashSet<String>();
        if (this.plugin.getConfig().isConfigurationSection("spawnpoint")) {
            worlds = this.plugin.getConfig().getConfigurationSection("spawnpoint").getKeys(false);
        }

        return worlds;
    }

    public int getMaxdragons(final String worldName) { return this.getConfigInt(worldName, "maxdragons"); }

    boolean getOneByOne(final String worldName) { return this.getConfigBoolean(worldName, "onebyone"); }

    private double getDragonHealth(final String worldName) {
        final double health = this.getConfigDouble(worldName, "health");
        double maxH = 2048.0D;
        if (DragonSlayer.spigot) {
            maxH = (double) Bukkit.spigot().getConfig().getInt("settings.attribute.maxHealth.max");
        }

        if (health >= 0.0D && health <= maxH) {
            return health;
        } else {
            this.plugin.logger.warning("Invalid dragon health set, reverting to default: 200 (100 hearts)");
            this.plugin.getConfig().set("dragon." + worldName + ".health", 200.0D);
            this.plugin.saveConfig();
            return 200.0D;
        }
    }

    int getDragonHealth_n(final String worldName, final int dragonId) {
        final Integer health = (int) this.getDragonHealth(worldName);
        final String healthStr = this.plugin.getConfig().getString("dragon." + worldName + ".health_" + dragonId);
        final int health2 = healthStr != null ? (int) Double.parseDouble(healthStr) : -1;
        double maxH = 2048.0D;
        if (DragonSlayer.spigot) {
            maxH = (double) Bukkit.spigot().getConfig().getInt("settings.attribute.maxHealth.max");
        }

        return (double) health2 >= 0.0D && (double) health2 <= maxH ? health2 : health;
    }

    int getRegenSecs(final String worldName) { return this.getConfigInt(worldName, "regen_seconds"); }

    int getRegenAmount(final String worldName) { return this.getConfigInt(worldName, "regen_amount"); }

    Double getDragonDamage(final String worldName, final int dragonId) {
        final Double value = this.getConfigDouble(worldName, "damage");
        final String TestValue = dragonId > 0 ? this.plugin.getConfig().getString("dragon." + worldName + ".damage_" + dragonId) : null;
        return TestValue != null ? Double.parseDouble(TestValue) : value;
    }

    int getDragonExp(final String worldName, final int dragonId) {
        int e = this.getConfigInt(worldName, "exp");
        final String TestValue = dragonId > 0 ? this.plugin.getConfig().getString("dragon." + worldName + ".exp_" + dragonId) : null;
        if (TestValue != null) {
            e = Integer.parseInt(TestValue);
        }

        if (e >= 0 && e <= 1000000000) {
            return e;
        } else {
            this.plugin.logger.warning("Invalid dragon exp set, reverting to default: 12000");
            this.plugin.getConfig().set("dragon." + worldName + ".exp" + (dragonId > 0 ? "_" + String.valueOf(dragonId) : ""), 12000);
            this.plugin.saveConfig();
            return 12000;
        }
    }

    double getReward_double(final String worldName, final int dragonId) {
        final Double value = this.getConfigDouble(worldName, "reward");
        final String TestValue = dragonId > 0 ? this.plugin.getConfig().getString("dragon." + worldName + ".reward_" + dragonId) : null;
        return TestValue != null ? Double.parseDouble(TestValue) : value;
    }

    int getDragonRange(final String worldName, final int dragonId) {
        int e = this.getConfigInt(worldName, "range");
        final String TestValue = dragonId > 0 ? this.plugin.getConfig().getString("dragon." + worldName + ".range_" + dragonId) : null;
        if (TestValue != null) {
            e = Integer.parseInt(TestValue);
        }

        if (e >= 0 && e <= 2048) {
            return e;
        } else {
            this.plugin.logger.warning("Invalid dragon range set, reverting to default: 16");
            this.plugin.getConfig().set("dragon." + worldName + ".range" + (dragonId > 0 ? "_" + String.valueOf(dragonId) : ""), 16);
            this.plugin.saveConfig();
            return 16;
        }
    }

    int getBossbarDistance(final String worldName) {
        final int e = this.getConfigInt(worldName, "bossbar_distance");
        if (e >= 60) {
            return e;
        } else {
            this.plugin.logger.warning("Invalid dragon distance set, minimum is : 60");
            this.plugin.getConfig().set("dragon." + worldName + ".bossbar_distance", 60);
            this.plugin.saveConfig();
            return 60;
        }
    }

    int getDragonEggChance(final String worldName) {
        final double chance = this.getConfigDouble(worldName, "eggchance") * 100.0D;
        if (chance >= 0.0D && chance <= 100.0D) {
            return (int) chance;
        } else {
            this.plugin.logger.warning("Invalid dragon egg chance set, reverting to default: 0.3");
            this.plugin.getConfig().set("dragon." + worldName + ".eggchance", 0.3D);
            this.plugin.saveConfig();
            return 30;
        }
    }

    int getPortalEggChance(final String worldName) {
        final double chance = this.getConfigDouble(worldName, "portaleggchance") * 100.0D;
        if (chance >= 0.0D && chance <= 100.0D) {
            return (int) chance;
        } else {
            this.plugin.logger.warning("Invalid portal egg chance set, reverting to default: 1.0");
            this.plugin.getConfig().set("dragon." + worldName + ".portaleggchance", 1.0D);
            this.plugin.saveConfig();
            return 100;
        }
    }

    boolean getEggItem(final String worldName) { return this.getConfigBoolean(worldName, "eggasitem"); }

    int getSkullChance(final String worldName) {
        final double chance = this.getConfigDouble(worldName, "skullchance") * 100.0D;
        if (chance >= 0.0D && chance <= 100.0D) {
            return (int) chance;
        } else {
            this.plugin.logger.warning("Invalid dragon skull chance set, reverting to default: 0.03");
            this.plugin.getConfig().set("dragon." + worldName + ".skullchance", 0.03D);
            this.plugin.saveConfig();
            return 3;
        }
    }

    boolean getSkullItem(final String worldName) { return this.getConfigBoolean(worldName, "skullitem"); }

    boolean getEggCancel(final String worldName) { return this.getConfigBoolean(worldName, "cancelegg"); }

    private boolean getCreatePortalDefault(final String worldName) {
        final String TestForPortal = this.plugin.getConfig().getString("dragon." + worldName + ".createportal");
        if (TestForPortal == null) {
            this.plugin.getConfig().set("dragon." + worldName + ".createportal", true);
        }

        return this.getConfigBoolean(worldName, "createportal");
    }

    boolean getCreatePortal(final String worldName, final Integer portalId) {
        String id_ = "";
        if (portalId != null && portalId != 0) {
            id_ = "_" + String.valueOf(portalId);
        }

        final String TestForPortal = this.plugin.getConfig().getString("dragon." + worldName + ".createportal" + id_);
        return TestForPortal == null ? this.getCreatePortalDefault(worldName) : Boolean.parseBoolean(TestForPortal);
    }

    boolean checkCreatePortalID(final String worldName, final int portalId) {
        final String id_ = String.valueOf(portalId);
        final String TestForPortal = this.plugin.getConfig().getString("dragon." + worldName + ".createportal_" + id_);
        return TestForPortal != null;
    }

    void setCreatePortal(final boolean value, final String worldName) {
        this.plugin.getConfig().set("dragon." + worldName + ".createportal", value);
        this.plugin.saveConfig();
    }

    boolean getAlternativeReward(final String worldName) { return this.getConfigBoolean(worldName, "alternativereward"); }

    boolean getDisplayDragonName(final String worldName) { return this.getConfigBoolean(worldName, "displaydragonname"); }

    boolean getGlowEffect(final String worldName) { return this.getConfigBoolean(worldName, "glow_effect"); }

    String getGlowColor(final String worldName, final int id) {
        final String id_ = id > 0 ? "_" + String.valueOf(id) : "";
        final String DragCol_id = this.getConfigString(worldName, "glow_color" + id_) != null
                ? this.getConfigString(worldName, "glow_color" + id_)
                : this.getConfigString(worldName, "glow_color");
        return DragCol_id.toUpperCase();
    }

    boolean getHitEffect(final String worldName) { return this.getConfigBoolean(worldName, "hit_indicator"); }

    boolean getCreateGateways(final String worldName) { return this.getConfigBoolean(worldName, "creategateways"); }

    boolean getFixGateways(final String worldName) { return this.getConfigBoolean(worldName, "fixgateways"); }

    boolean getOldPortal(final String worldName) { return this.getConfigBoolean(worldName, "oldportals"); }

    boolean getCrystalDeny(final String worldName) { return this.getConfigBoolean(worldName, "denycrystalplace"); }

    boolean getDenyCrystalExplosion(final String worldName) { return this.getConfigBoolean(worldName, "denycrystalexplode"); }

    boolean getDenyBedExplosion(final String worldName) { return this.getConfigBoolean(worldName, "denybedexplode"); }

    boolean getDisableOrigDragonRespawn(final String worldName) { return this.getConfigBoolean(worldName, "nomcdragonrespawn"); }

    boolean getBlockGrief(final String worldName) {
        return this.plugin.getConfig().getBoolean("dragon." + worldName + ".blockgrief",
                Boolean.parseBoolean(this.plugin.getConfig().getString("global.blockgrief")));
    }

    int getTimerfunc(final String w) { return this.getConfigInt(w, "timerfunc"); }

    void setDragonDefaults() {
        for (final String worldName : this.getMaplist()) {
            if (Bukkit.getWorld(worldName) != null) {
                this.getDragonNameAndID(worldName);
                this.getDragonDamage(worldName, 0);
                this.getDragonHealth(worldName);
                this.getRegenSecs(worldName);
                this.getRegenAmount(worldName);
                this.getDragonRange(worldName, 0);
                this.getBossbarDistance(worldName);
                this.getDragonExp(worldName, 0);
                this.getReward_double(worldName, 0);
                this.getDragonEggChance(worldName);
                this.getPortalEggChance(worldName);
                this.getEggItem(worldName);
                this.getSkullChance(worldName);
                this.getSkullItem(worldName);
                this.getEggCancel(worldName);
                this.getDelay(worldName);
                this.getCreatePortal(worldName, (Integer) null);
                this.getOldPortal(worldName);
                this.getCrystalDeny(worldName);
                this.getDenyCrystalExplosion(worldName);
                this.getDenyBedExplosion(worldName);
                this.getCreateGateways(worldName);
                this.getFixGateways(worldName);
                this.getAlternativeReward(worldName);
                this.getDisplayDragonName(worldName);
                this.getResetWorld(worldName);
                this.getRefreshWorld(worldName);
                this.getResetDelay(worldName);
                this.getWarnTime(worldName);
                this.getTimerfunc(worldName);
                this.getRespawnPlayer(worldName);
                this.getRespawnCommand(worldName);
                this.getSpawnCommand(worldName, (Integer) null);
                this.getDragonCommand(worldName, 0);
                this.getRankCommand(worldName, 1, 0);
                this.getRankCommand(worldName, 2, 0);
                this.getDisableOrigDragonRespawn(worldName);
                this.getGlowEffect(worldName);
                this.getGlowColor(worldName, 0);
                this.getHitEffect(worldName);
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
