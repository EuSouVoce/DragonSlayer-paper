package de.jeppa.DragonSlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.entity.CraftEnderDragon;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.onarandombox.MultiverseCore.MultiverseCore;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;

// @SuppressWarnings("deprecation")
@SuppressWarnings({ "deprecation" })
public class DragonSlayer extends JavaPlugin {
    private static DragonSlayer instance = null;
    final Logger logger = this.getLogger();
    ConfigManager configManager = new ConfigManager(this);
    TimerManager timerManager = new TimerManager(this);
    LeaderManager leaderManager = new LeaderManager(this);
    DragonEvents dragonListener = null;
    DragonLChatEvents dragonLChatListener = new DragonLChatEvents(this);
    PlayerListener playerListener = new PlayerListener(this);
    DragonCommands dragonCommandExecutor = new DragonCommands(this);
    static FourteenPlusOnlyRoutines newRoutines14 = null;
    static ProtLibHandler protLibHandler = null;
    static Economy econ = null;
    static boolean UCenabled = false;
    private static boolean LegendChatenabled = false;
    static boolean PAPIenabled = false;
    private static boolean EssentialsEnabled = false;
    private static boolean ProtocolLibEnabled = false;
    static boolean SkinsRestorerEnabled = false;
    static boolean EssChEnabled = false;
    static boolean TabListPlugin = false;
    static boolean spigot = false;
    private static boolean serverStarted = false;
    private static ArrayList<BossBar> BossBars = new ArrayList<BossBar>();
    private static HashMap<EnderDragon, BossBar> DragonBarList = new HashMap<EnderDragon, BossBar>();
    static HashMap<String, Scoreboard> timerDisplays = new HashMap<String, Scoreboard>();
    static ArrayList<WorldRefreshOrReset> ResetimerList = new ArrayList<WorldRefreshOrReset>();
    private static ArrayList<Location> Endgateways = new ArrayList<Location>();
    final ArrayList<World> ProtectResetWorlds = new ArrayList<World>();
    private final HashMap<String, Integer> healTickCounterList = new HashMap<String, Integer>();
    private static Method db_a = null;

    public static Boolean debugOn = false;
    private static Method endgatewayMethod = null;
    private static Field OrigGateways = null;
    private static Field DragonKilled = null;
    private static Field DragonPreviouslyKilled = null;
    private static Field DragonUUID = null;
    private static Field PortLoc_f = null;
    private static Field naviField = null;
    private static Method fillArray = null;
    private static Method getEDBMethod = null;
    private static Class<?> CraftWorldClass = null;
    private static Class<?> CraftEnderDragonClass = null;
    private static Field CrystalAmount_f = null;
    private static Object dragonId = null;
    private static Constructor<?> newBlockPos = null;
    private static Constructor<?> newPathPoint = null;
    private static Method pp_geta_func = null;
    private static Method Vec3_getX = null;
    private static Method Vec3_getY = null;
    private static Method Vec3_getZ = null;
    static final double Pi = Math.PI;
    static final double ZwPi = 0.15707963267948966D;
    private Method phaseControlManager_method = null;
    private Method getPathPointFromPathEnt_method = null;
    private Field pathEntList = null;
    static Method getTileEntity = null;
    static Method saveNBT = null;
    static int repeatingCounter = 0;
    static int tabListTime = 0;
    private static String ScoreBoardName_TimerDisplay = "" + ChatColor.BLACK + ChatColor.WHITE;
    private static String ScoreBoardName_ResetTimer = "" + ChatColor.BLUE + ChatColor.WHITE;
    private static ArrayList<Team> TeamList = new ArrayList<Team>();

    @Override
    public void onEnable() {
        DragonSlayer.instance = this;
        this.configManager.loadConfiguration();
        this.configManager.checkOldConfig();
        DragonSlayer.debugOn = this.configManager.debugOn();
        this.setupEconomy();
        this.setupDependPlugins();
        this.Protocollib();

        this.dragonListener = new DragonEvents(this);
        this.getServer().getPluginManager().registerEvents(this.dragonListener, this);
        if (this.configManager.getVerbosity()) {
            this.logger.info("Events 1.9+ enabled!");
        }

        try {
            Class.forName("org.spigotmc.SpigotConfig");
            DragonSlayer.spigot = true;
        } catch (ClassNotFoundException | NoClassDefFoundError var8) {
            this.logger.severe("org.spigotmc.SpigotConfig not found, disabling");
            this.getServer().getPluginManager().disablePlugin(this);
        }

        try {
            DragonSlayer.newRoutines14 = new FourteenPlusOnlyRoutines(this);
        } catch (final Exception var7) {
        }

        this.getServer().getPluginManager().registerEvents(this.playerListener, this);

        if (DragonSlayer.LegendChatenabled) {
            this.getServer().getPluginManager().registerEvents(this.dragonLChatListener, this);
        }

        if (DragonSlayer.PAPIenabled) {
            final String ver = this.getServer().getPluginManager().getPlugin("PlaceholderAPI").getPluginMeta().getVersion();
            String vers2 = "";

            final String[] var6 = ver.split("\\.");
            for (final String split : var6) {
                vers2 = vers2 + (split.length() == 2 ? split : "0" + split);
            }

            if (vers2.contains("-")) {
                vers2 = vers2.substring(0, vers2.indexOf("-"));
            }

            final int version2 = Integer.parseInt(vers2);
            if (version2 <= 21006) {
                (new DragonPlaceholderAPIold(this)).register();
            } else {
                (new DragonPlaceholderAPI(this)).register();
            }
        }

        if (DragonSlayer.ProtocolLibEnabled) {
            if (this.configManager.getVerbosity()) {
                this.logger.info("ProtocolLib found...");
            }
        } else if (this.getStatueVersion() >= 2) {
            this.getConfig().set("global.statue_version", 1);
            this.saveConfig();
            if (this.configManager.getVerbosity()) {
                this.logger.info("No ProtocolLib found... resetting statue_version to 1!");
            }
        }

        this.getCommand("dragonslayer").setExecutor(this.dragonCommandExecutor);

        if (!this.setupEconomy()) {
            if (this.configManager.getVerbosity()) {
                this.logger.warning("No Vault dependency found, rewards will be disabled!");
            }
        } else {
            this.logger.info("Vault dependency found, rewards will be enabled!");
        }

        for (final String DragonWorld : this.configManager.getMaplist()) {
            final World MyWorld = this.getDragonWorldFromString(DragonWorld);
            this.activateChunksAroundPosition(new Location(MyWorld, 0.0D, 75.0D, 0.0D), MyWorld, 7, false);
        }

        if (DragonSlayer.UCenabled) {
            this.logger.info("UChat found, tag will be used!");
        }

        if (DragonSlayer.LegendChatenabled) {
            this.logger.info("LegendChat found, tag will be used!");
        }

        if (DragonSlayer.PAPIenabled) {
            this.logger.info("PlaceholderAPI found, will be used!");
        }

        if (this.configManager.getVerbosity() && DragonSlayer.EssentialsEnabled) {
            this.logger.info("Essentials found, will be used!");
        }

        if (this.configManager.getVerbosity() && DragonSlayer.EssChEnabled) {
            this.logger.info("EssentialsChat found, will be used!");
        }

        if (DragonSlayer.TabListPlugin) {
            final Plugin tl = this.getServer().getPluginManager().getPlugin("TabList");
            if (tl.getPluginMeta().getMainClass().contains("montlikadani")) {
                final String[] vers1 = tl.getPluginMeta().getVersion().split("\\.");
                if (vers1.length >= 3) {
                    if (Integer.valueOf(String.format("%03d%03d%03d", Integer.valueOf(vers1[0]), Integer.valueOf(vers1[1]),
                            Integer.valueOf(vers1[2]))) <= 5007004) {
                        if (this.configManager.getVerbosity()) {
                            this.logger.info("Plugin 'TabList' <= 5.7.4 found, using fallback mode for Timer-Scoreboard !");
                        }
                    } else {
                        DragonSlayer.TabListPlugin = false;
                    }
                }
            } else {
                DragonSlayer.TabListPlugin = false;
            }
        }

        DragonSlayer.serverStarted = !this.configManager.getMultiPortal();
        this.countEndGatewaysAndContinue();
        this.setTestPortal();
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.configManager.setDragonDefaults();
            this.timerManager.getTimerlist();
            this.timerManager.RestartTimers();
            this.leaderManager.getLeaderlist();
            this.leaderManager.sortLeaderList();
            this.leaderManager.saveLeaderlist();
            if (DragonSlayer.ProtocolLibEnabled) {
                this.getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
                    if (this.getStatueVersion() >= 2) {
                        DragonSlayer.protLibHandler.getNewTextureArray((Player) null, true, true);
                    }

                }, 0L);
            }

            this.getServer().getScheduler().runTaskLater(this, (@NotNull Runnable) this::resetArmorStand, 30L);
            this.StartRepeatingTimer();
            this.StartSecondRepeatingTimer();
            for (final String DragonWorld : this.configManager.getMaplist()) {
                this.getDragonCount(DragonWorld);
            }

        }, 20L);
        DragonSlayer.dragonId = new NamespacedKey(this, "DragonID");

    }

    @Override
    public void onDisable() {
        this.timerManager.getTimerlist();
        for (final String DragonWorld : this.configManager.getMaplist()) {
            final World world = this.getDragonWorldFromString(DragonWorld);

            try {
                final boolean pendingRefresh = this.isRefreshRunning(world);
                final long resetTimerNotRunning = this.getResetTime(DragonWorld);
                final long timerValue = 60L;
                if (pendingRefresh && resetTimerNotRunning == -1L && this.configManager.getRefreshWorld(DragonWorld)) {
                    this.createWorldResetTimer(DragonWorld, timerValue, timerValue / 3L);
                    if (this.configManager.getVerbosity()) {
                        this.logger.info("Adding a Refresh-Timer for server restart for world " + DragonWorld);
                    }

                    if (!this.ProtectResetWorlds.contains(world)) {
                        this.timerManager.createStartTimer(DragonWorld, timerValue);
                        if (this.configManager.getVerbosity()) {
                            this.logger.info("This refresh will also spawn a dragon...");
                        }
                    }
                }
            } catch (final Exception var9) {
            }
        }

        this.timerManager.updateTimerList();
        this.timerManager.saveTimerlist();
        this.leaderManager.saveLeaderlist();
        this.saveConfig();
    }

    private void setupDependPlugins() {
        DragonSlayer.UCenabled = this.checkPlugin("UltimateChat");
        DragonSlayer.LegendChatenabled = this.checkPlugin("Legendchat");
        DragonSlayer.PAPIenabled = this.checkPlugin("PlaceholderAPI");
        DragonSlayer.EssentialsEnabled = this.checkPlugin("Essentials");
        DragonSlayer.EssChEnabled = this.checkPlugin("EssentialsChat");
        DragonSlayer.SkinsRestorerEnabled = this.checkPlugin("SkinsRestorer");
        DragonSlayer.TabListPlugin = this.checkPlugin("TabList");
    }

    private boolean checkPlugin(final String pluginName) { return this.getServer().getPluginManager().getPlugin(pluginName) != null; }

    private void Protocollib() {
        if (this.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                DragonSlayer.protLibHandler = new ProtLibHandler(this);
                DragonSlayer.ProtocolLibEnabled = true;
            } catch (final Exception var2) {
            }
        }

    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            final RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                DragonSlayer.econ = (Economy) rsp.getProvider();
                return true;
            }
        }
    }

    boolean checkServerStarted() { return DragonSlayer.serverStarted; }

    private int countRespawnTimers(final String worldName) {
        int Count = 0;

        for (final DragonRespawn Resp : this.timerManager.RespawnList) {
            if (Resp.worldName.toLowerCase().equals(worldName)) {
                ++Count;
            }
        }

        return Count;
    }

    public int missingDragons(final String worldName) {
        final int MaxDragons = this.configManager.getMaxdragons(worldName);
        final int Count = this.getDragonCount(worldName);
        final int runningTimers = this.countRespawnTimers(worldName);
        return runningTimers + Count < MaxDragons ? MaxDragons - (runningTimers + Count) : 0;
    }

    private long getNextRespawn(final String worldName) {
        long Resttime = -1L;

        for (final DragonRespawn Resp : this.timerManager.RespawnList) {
            if (Resp.worldName.equals(worldName)) {
                final Long check = this.remainingTimerDuration(Resp);
                if (check != null && (check < Resttime || Resttime == -1L)) {
                    Resttime = check;
                }
            }
        }

        return Resttime;
    }

    Long remainingTimerDuration(final DragonRespawn dragonRespawn) {
        final long Runtime = System.currentTimeMillis() / 50L - dragonRespawn.StartTime;
        final long Resttime = dragonRespawn.OrigRuntime - Runtime;
        return Resttime > 0L ? Resttime : null;
    }

    private long getResetTime(final String worldName) {
        long Resttime = -1L;

        for (final WorldRefreshOrReset Res : DragonSlayer.ResetimerList) {
            if (Res.Mapname.equals(worldName)) {
                final Long check = this.remainingResetDuration(Res);
                if (check != null && (check < Resttime || Resttime == -1L)) {
                    Resttime = check;
                }
            }
        }

        return Resttime;
    }

    Long remainingResetDuration(final WorldRefreshOrReset worldRefreshOrReset) {
        final long Runtime = System.currentTimeMillis() / 50L - worldRefreshOrReset.StartTime;
        final long Resttime = worldRefreshOrReset.OrigRuntime - Runtime;
        return Resttime > 0L ? Resttime : null;
    }

    void stopResetTimer(final String worldName) {
        final List<WorldRefreshOrReset> killList = new ArrayList<WorldRefreshOrReset>();

        for (final WorldRefreshOrReset Res_ : DragonSlayer.ResetimerList) {
            if (Res_.Mapname.toLowerCase().equals(worldName)) {
                this.getServer().getScheduler().cancelTask(Res_.taskId);
                killList.add(Res_);
            }
        }

        for (WorldRefreshOrReset Res_ : killList) {
            this.getServer().getScheduler().cancelTask(Res_.taskId);
            DragonSlayer.ResetimerList.remove(Res_);
            Res_ = null;
        }

        killList.clear();
    }

    public void StartWorldResetTimer(final String worldName, long Runtime, final long Warntime) {
        final WorldRefreshOrReset Res = this.createWorldResetTimer(worldName, Runtime, Warntime);
        if (Runtime < Warntime) {
            Res.Warntime = Runtime;
            Runtime = 0L;
        } else {
            Runtime -= Warntime;
        }

        Res.taskId = this.getServer().getScheduler().runTaskLater(this, Res, Runtime).getTaskId();
    }

    private WorldRefreshOrReset createWorldResetTimer(final String worldName, final long Runtime, final long Warntime) {
        final WorldRefreshOrReset Res = new WorldRefreshOrReset(this);
        Res.Mapname = worldName;
        Res.OrigRuntime = Runtime;
        Res.Warntime = Warntime;
        return Res;
    }

    public HashMap<Double, Player> sortDamagersRanks(final HashMap<Double, Player> orderList) {
        final List<Double> percentList = new ArrayList<Double>(orderList.keySet());
        Collections.sort(percentList);
        Collections.reverse(percentList);
        final HashMap<Double, Player> newOrderList = new HashMap<Double, Player>();
        double r = 1.0D;

        for (final Double val : percentList) {
            newOrderList.put(r++, (Player) orderList.get(val));
        }

        return newOrderList;
    }

    Integer getPlayerCount(final String worldName) {
        final World MyWorld = this.getDragonWorldFromString(worldName);
        return MyWorld == null ? 0 : MyWorld.getPlayers().size();
    }

    Integer getDragonCount(final String worldName) {
        final World MyWorld = this.getDragonWorldFromString(worldName);
        int Counter = 0;
        if (MyWorld == null) {
            return 0;
        } else {
            final Collection<EnderDragon> dragons = this.getDragonList(MyWorld, worldName);
            Counter = dragons.size();

            for (final EnderDragon dr : dragons) {
                if (!dr.isValid() || dr.isDead() || dr.getPhase() == Phase.DYING || !this.checkDSLDragon(dr)) {
                    --Counter;
                }
            }

            return Counter;
        }
    }

    public Collection<EnderDragon> getDragonList(final World world, final String worldName) {
        if (this.checkWorld(worldName)) {
            final Location DragSpawnPos = DragonSlayer.findPosForPortal((double) this.configManager.getPortalXdef(worldName),
                    (double) this.configManager.getPortalZdef(worldName), world, Material.BEDROCK);
            this.activateChunksAroundPosition(DragSpawnPos, world, 12, false);

            for (int i = 1; i <= this.configManager.getMaxdragons(worldName); ++i) {
                final Location PortLoc = DragonSlayer.findPosForPortal((double) this.configManager.getPortalX(worldName, i, true, true),
                        (double) this.configManager.getPortalZ(worldName, i, true, true), world, Material.BEDROCK);
                if (!DragSpawnPos.equals(PortLoc)) {
                    this.activateChunksAroundPosition(PortLoc, world, 6, false);
                }
            }
        }

        return world.getEntitiesByClass(EnderDragon.class);
    }

    void activateChunksAroundPosition(final Location startLocation, final World world, final int Radius, final boolean forceTicket) {
        if (world != null) {
            final int baseX = (int) (startLocation.getX() / 16.0D);
            final int baseZ = (int) (startLocation.getZ() / 16.0D);

            for (int x = -1 * Radius; x <= Radius; ++x) {
                final int ChunkX = baseX + x;

                for (int z = -1 * Radius; z <= Radius; ++z) {
                    final int ChunkZ = baseZ + z;
                    if ((this.configManager.keepChunksLoaded() || forceTicket) && (world.isChunkGenerated(ChunkX, ChunkZ))) {
                        try {
                            world.addPluginChunkTicket(ChunkX, ChunkZ, this);
                        } catch (final NoSuchMethodError var17) {
                            try {
                                world.setChunkForceLoaded(ChunkX, ChunkZ, true);
                            } catch (final NoSuchMethodError var16) {
                            }
                        }
                    }

                    final Chunk testChunk = world.getChunkAt(ChunkX, ChunkZ);
                    if (!world.isChunkLoaded(ChunkX, ChunkZ)) {
                        boolean load;
                        try {
                            load = world.loadChunk(ChunkX, ChunkZ, true);
                        } catch (final RuntimeException var15) {
                            load = false;
                        }

                        if (!load && this.configManager.getVerbosity()) {
                            this.logger.warning("Failed to load and activate Chunk at X: " + ChunkX * 16 + " Z: " + ChunkZ * 16 + " in "
                                    + world.getName());
                        }
                    }

                    try {
                        testChunk.getEntities();
                    } catch (final Exception var14) {
                    }
                }
            }

        }
    }

    public void RemoveDragons(final World worldName, final boolean deadOnly, final boolean forceAll) {
        final String Mapname = worldName.getName().toLowerCase();

        for (final EnderDragon Dragon : this.getDragonList(worldName, Mapname)) {
            if (this.checkDSLDragon(Dragon) || forceAll) {
                if (deadOnly) {
                    if (!Dragon.isValid()) {
                        Dragon.remove();
                    }
                } else {
                    Dragon.remove();
                }
            }
        }

    }

    boolean SpawnForceDragon(final String worldName) {
        if (this.checkWorld(worldName)) {
            final int ExistentDragons = this.getDragonCount(worldName);
            final int maxDragons = this.configManager.getMaxdragons(worldName);
            if (ExistentDragons < maxDragons) {
                this.SpawnXDragons(maxDragons - ExistentDragons, worldName);
                return true;
            }
        }

        return false;
    }

    void SpawnForceAllDragons() {
        for (final String DragonWorld : this.configManager.getMaplist()) {
            final int ExistentDragons = this.getDragonCount(DragonWorld);
            final int MaxDragons = this.configManager.getMaxdragons(DragonWorld);
            if (ExistentDragons < MaxDragons) {
                this.SpawnXDragons(MaxDragons - ExistentDragons, DragonWorld);
            }
        }

    }

    void SpawnXDragons(final int amount, final String worldName) {
        for (int i = 0; i < amount; ++i) {
            final DragonRespawn Resp = new DragonRespawn(this);
            Resp.worldName = worldName;
            this.getServer().getScheduler().runTaskLater(this, Resp, (long) (60 + i * 40));
        }

    }

    public String getSlayer() {
        String name = null;

        try {
            name = this.getOfflineSlayer().getName();
        } catch (IllegalArgumentException | NullPointerException var3) {
        }

        return name;
    }

    OfflinePlayer getOfflineSlayer() {
        OfflinePlayer player = null;

        try {
            player = Bukkit.getOfflinePlayer(this.getSlayerUUID());
        } catch (IllegalArgumentException | NullPointerException var3) {
        }

        return player;
    }

    public String getSlayerUUIDString() { return this.getConfig().getString("global.slayer"); }

    UUID getSlayerUUID() {
        UUID uuid;
        try {
            uuid = UUID.fromString(this.getSlayerUUIDString());
        } catch (IllegalArgumentException | NullPointerException var3) {
            uuid = null;
        }

        return uuid;
    }

    World getDragonWorldFromString(final String worldName) { return Bukkit.getServer().getWorld(worldName); }

    public void setSlayer(Player p) {
        this.resetTabListName();
        String uuid = p.getUniqueId().toString();
        int oldScore = 0;
        if (this.leaderManager.Leader.getConfigurationSection("Scores") != null
                && this.leaderManager.Leader.getConfigurationSection("Scores").contains(uuid)) {
            oldScore = this.leaderManager.Leader.getInt("Scores." + uuid + ".score");
        }

        this.leaderManager.Leader.set("Scores." + uuid + ".score", oldScore + 1);
        this.leaderManager.saveLeaderlist();
        this.leaderManager.sortLeaderList();
        if (this.configManager.getSlayerByRank()) {
            final String topKiller = this.leaderManager.getUUIDforRank(1);
            if (topKiller != null) {
                uuid = topKiller;
                p = Bukkit.getPlayer(UUID.fromString(topKiller.trim()));
            }
        }

        this.getConfig().set("global.slayer", uuid);
        this.saveConfig();
        if (p != null) {
            this.setTabListName(p);
        }

        if (DragonSlayer.ProtocolLibEnabled) {
            final Player pl = p;
            this.getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
                if (this.getStatueVersion() >= 2) {
                    DragonSlayer.protLibHandler.getNewTextureArray(pl, false, true);
                }

            }, 0L);
        }

    }

    private void resetTabListName() {
        if (this.configManager.getTabListEnable()) {
            final OfflinePlayer oldSlayer = this.getOfflineSlayer();
            if (oldSlayer != null && oldSlayer.isOnline()) {
                final Player oldSlayer_ = (Player) oldSlayer;
                oldSlayer_.setPlayerListName(oldSlayer_.getDisplayName());
            }
        }

    }

    void setTabListName(final Player p) {
        if (this.configManager.getTabListEnable()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                if (p.getUniqueId().toString().equals(this.getSlayerUUIDString())) {
                    p.setPlayerListName((!this.configManager.getPrefixAsSuffix() ? this.configManager.getPrefix() : "") + p.getDisplayName()
                            + (this.configManager.getPrefixAsSuffix() ? this.configManager.getPrefix() : ""));
                }

            }, 10L);
        }

    }

    String replaceValues(String s, final String worldName, final Integer id) {
        s = s.replace('&', '§');
        if (this.configManager.getSlayerPAPINick() != null) {
            s = s.replace("$slayername", this.configManager.getSlayerPAPINick());
        }

        if (this.getSlayer() != null) {
            s = s.replace("$slayer", this.getSlayer());
        }

        if (worldName != null) {
            final String baseworld = worldName.replace("_the_end", "");
            final int dragonID = id != null ? id : 0;
            s = s.replace("$world", worldName).replace("$baseworld", baseworld)
                    .replace("$dragon", this.configManager.getDragonDefaultName(worldName.toLowerCase(), dragonID) + "§r")
                    .replace("$reward", String.valueOf(this.configManager.getReward_double(worldName.toLowerCase(), dragonID)));
        } else {
            s = s.replace("$world", "-No World-").replace("$dragon", this.getConfig().getString("dragon._default.name") + "§r")
                    .replace("$reward", this.getConfig().getString("dragon._default.reward"));
        }

        return s;
    }

    String replaceValues(final String s, final String worldName) { return this.replaceValues(s, worldName, (Integer) null); }

    public boolean checkWorld(final String worldName) { return this.configManager.getMaplist().contains(worldName.toLowerCase()); }

    boolean checkDSLDragon(final EnderDragon dragon) {
        boolean checkDSLDragon = dragon.hasMetadata("DSL-Location");
        if (!checkDSLDragon) {
            checkDSLDragon = this.getDragonIDMeta(dragon) > 0;
        }

        return checkDSLDragon;
    }

    boolean checkOrigDragon(final EnderDragon dragon) {
        final Object newDragEnt = this.getEntityEnderDragon(dragon);
        return newDragEnt.getClass().getSimpleName().equals("EntityEnderDragon")
                && newDragEnt.getClass().getTypeName().contains("net.minecraft");
    }

    void FindPlayerAndAddToBossBar(final BossBar BossBar, final Entity dragon) {
        final List<Player> AddedPlayers = BossBar.getPlayers();
        final int distancePlayer = this.configManager.getBossbarDistance(dragon.getWorld().getName().toLowerCase());

        for (final Player player : dragon.getWorld().getPlayers()) {
            if (!AddedPlayers.contains(player)) {
                if (player.getLocation().distance(dragon.getLocation()) < (double) distancePlayer) {
                    BossBar.addPlayer(player);
                }
            } else if (player.getLocation().distance(dragon.getLocation()) >= (double) distancePlayer) {
                BossBar.removePlayer(player);
            }
        }

    }

    BossBar findFreeBar(final String worldName) {
        for (final BossBar BB : DragonSlayer.BossBars) {
            if (!BB.isVisible()) {
                BB.setVisible(true);
                this.setBBdark(BB, worldName);
                BB.removeAll();
                return BB;
            }
        }

        final BossBar BossBar = Bukkit.getServer().createBossBar("EnderDragon", BarColor.PURPLE, BarStyle.SOLID,
                new BarFlag[] { BarFlag.PLAY_BOSS_MUSIC });
        this.setBBdark(BossBar, worldName);
        BossBar.setVisible(true);
        DragonSlayer.BossBars.add(BossBar);
        return BossBar;
    }

    private void setBBdark(final BossBar BossBar, final String worldName) {
        if (this.configManager.getDark(worldName)) {
            BossBar.addFlag(BarFlag.CREATE_FOG);
            BossBar.addFlag(BarFlag.DARKEN_SKY);
        } else {
            BossBar.removeFlag(BarFlag.CREATE_FOG);
            BossBar.removeFlag(BarFlag.DARKEN_SKY);
        }

    }

    void handleBossbar(final World world) {
        for (final EnderDragon ThisDrag : world.getEntitiesByClass(EnderDragon.class)) {
            if (this.checkDSLDragon(ThisDrag)) {
                BossBar BossBar = DragonSlayer.getBossBarFromDragon(ThisDrag);
                if (BossBar != null) {
                    this.FindPlayerAndAddToBossBar(BossBar, ThisDrag);
                    this.setBossBarAmountNOW(ThisDrag, BossBar);
                } else if (ThisDrag.isValid() && !ThisDrag.isDead() && ThisDrag.getPhase() != Phase.DYING) {
                    BossBar = this.findFreeBar(world.getName().toLowerCase());
                    if (BossBar != null) {
                        BossBar.setTitle(ThisDrag.getName());
                        this.setBossBarAmountNOW(ThisDrag, BossBar);
                        DragonSlayer.putBossBarToDragon(ThisDrag, BossBar);
                        this.FindPlayerAndAddToBossBar(BossBar, ThisDrag);
                    }
                }

                this.OrigEnderDragonSetKilled(ThisDrag);
                if (this.configManager.getGlowEffect(world.getName().toLowerCase())) {
                    this.handleGlowTeams(world, this.getDragonIDMeta(ThisDrag), ThisDrag.getUniqueId().toString());
                }
            }
        }

    }

    int findDragonID(final String worldName, String dragonName) {
        final int maxD = this.configManager.getMaxdragons(worldName);

        for (int i = 0; i <= maxD; ++i) {
            String TestName = this.getConfig().getString(i == 0 ? "dragon." + worldName + ".name" : "dragon." + worldName + ".name_" + i);
            if (TestName != null && dragonName != null) {
                TestName = TestName.replace('&', '§');
                dragonName = dragonName.replaceAll("§[f0r]", "");
                TestName = TestName.replaceAll("§[f0r]", "");
                if (dragonName.equals(TestName)) {
                    return i;
                }
            }
        }

        return -1;
    }

    static void putBossBarToDragon(final EnderDragon dragon, final BossBar BossBar) { DragonSlayer.DragonBarList.put(dragon, BossBar); }

    static void delBossBarFromDragon(final EnderDragon dragon) { DragonSlayer.DragonBarList.remove(dragon); }

    static BossBar getBossBarFromDragon(final EnderDragon dragon) {
        BossBar BossBar = null;

        try {
            BossBar = (BossBar) DragonSlayer.DragonBarList.get(dragon);
        } catch (final Exception var3) {
        }

        return BossBar;
    }

    public static void resetDragonsBossbar(final Entity dragon) {
        final BossBar BossBar = DragonSlayer.getBossBarFromDragon((EnderDragon) dragon);
        if (BossBar != null) {
            BossBar.setProgress(0.0D);
            BossBar.removeAll();
            DragonSlayer.delBossBarFromDragon((EnderDragon) dragon);
            BossBar.setVisible(false);
        }

    }

    public static void cleanupDragons() {
        final Set<EnderDragon> testdrags = DragonSlayer.DragonBarList.keySet();
        final Set<EnderDragon> listDelDrags = new HashSet<EnderDragon>();

        for (final EnderDragon testdrag : testdrags) {
            if (!testdrag.isValid()) {
                final BossBar tobedeleted = DragonSlayer.getBossBarFromDragon(testdrag);
                tobedeleted.setVisible(false);
                listDelDrags.add(testdrag);
            }
        }

        for (final EnderDragon testdrag : listDelDrags) {
            DragonSlayer.DragonBarList.remove(testdrag);
        }

    }

    public static void deletePlayersBossBars(final Player player) {
        for (final BossBar BB : DragonSlayer.BossBars) {
            if (BB.getPlayers().contains(player)) {
                BB.removePlayer(player);
            }
        }

    }

    void setBossBarAmount(final EnderDragon dragon) {
        final String w = dragon.getWorld().getName();
        if (this.checkWorld(w)) {
            final BossBar bossBar = DragonSlayer.getBossBarFromDragon(dragon);
            if (bossBar != null) {
                this.setBossBarAmountNOW(dragon, bossBar);
            }
        }

    }

    void setBossBarAmountNOW(final EnderDragon dragon, final BossBar bossBar) {
        this.getServer().getScheduler().runTaskLater(this, () -> {
            final double maxHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            final double dragHealth = dragon.getHealth();
            double barHealthValue = dragHealth / maxHealth;
            if (barHealthValue < 0.0D) {
                barHealthValue = 0.0D;
            }

            if (barHealthValue > 1.0D) {
                barHealthValue = 1.0D;
            }

            bossBar.setProgress(barHealthValue);
        }, 0L);
    }

    static Location findPosForPortal(final double tempX, final double tempZ, final World world, final Material checkMat) {
        final Location portLoc = new Location(world, tempX, 60.0, tempZ);
        double y = 200.0;
        while (y > 35.0) {
            portLoc.setY(y);
            final Material testMat = portLoc.getBlock().getType();
            switch (testMat.toString()) {
            default: {
                if (testMat.equals(checkMat)) {
                    portLoc.setY(y - 3.0);
                }
                return portLoc;
            }
            case "STATIONARY_WATER":
            case "DRAGON_EGG":
            case "AIR":
            case "WATER":
            }
            y -= 1.0;
        }
        return portLoc;
    }

    private void PlaceEmptyPortal(final World world) {
        final String worldname = world.getName().toLowerCase();
        if (!this.configManager.getOldPortal(worldname) && this.configManager.getDisableOrigDragonRespawn(worldname)) {
            final Location PortLoc = DragonSlayer.findPosForPortal((double) this.configManager.getPortalXdef(worldname),
                    (double) this.configManager.getPortalZdef(worldname), world, Material.BEDROCK);
            this.placePortal(PortLoc, 0);
        }

    }

    private void PlaceEmptyPortals(final World world, final boolean place) {
        if (this.configManager.getMultiPortal()) {
            final String worldname = world.getName().toLowerCase();
            if (!this.configManager.getOldPortal(worldname)) {
                final Location defPortLoc = DragonSlayer.findPosForPortal((double) this.configManager.getPortalXdef(worldname),
                        (double) this.configManager.getPortalZdef(worldname), world, Material.BEDROCK);

                for (int i = 1; i <= this.configManager.getMaxdragons(worldname); ++i) {
                    final Location PortLoc = DragonSlayer.findPosForPortal((double) this.configManager.getPortalX(worldname, i),
                            (double) this.configManager.getPortalZ(worldname, i), world, Material.BEDROCK);
                    if (!defPortLoc.equals(PortLoc)) {
                        this.placePortal(PortLoc, place ? 0 : -1);
                    }
                }
            }
        }

    }

    void placePortal(Location location, final int endPortal) {
        final Material portalStone = (endPortal < 0) ? Material.END_STONE : Material.BEDROCK;
        final World world = location.getWorld();

        for (int y = 0; y <= 10; ++y) {
            final int maxXZ = Math.min(y < 2 ? y + 1 : 3, 3);

            for (int x = -maxXZ; x <= maxXZ; ++x) {
                for (int z = -maxXZ; z <= maxXZ; ++z) {
                    if (Math.abs(x * z) < maxXZ * 2) {
                        Material blockType = Material.AIR;
                        if (y <= 2) {
                            if (Math.abs(x * z) < (maxXZ - 1) * 2 && Math.abs(x) <= maxXZ - 1 && Math.abs(z) <= maxXZ - 1) {
                                blockType = (y == 2 && endPortal > 0) ? Material.END_PORTAL : portalStone;
                            } else {
                                blockType = portalStone;
                            }
                        }
                        location.getBlock().getRelative(x, y - 2, z).setType(blockType);
                    }
                }
            }
        }

        location.getBlock().setType(portalStone);
        location = location.getBlock().getRelative(0, 1, 0).getLocation();
        location.getBlock().setType((endPortal < 0) ? Material.AIR : portalStone);
        location.getBlock().getRelative(0, 1, 0).setType((endPortal < 0) ? Material.AIR : portalStone);
        location.getBlock().getRelative(0, 2, 0).setType((endPortal < 0) ? Material.AIR : portalStone);

        if (endPortal > 0 && new Random().nextInt(100) < this.configManager.getPortalEggChance(world.getName().toLowerCase())) {
            location.getBlock().getRelative(0, 3, 0).setType(Material.DRAGON_EGG);
        }

        if (endPortal >= 0) {
            final Block block = location.getBlock();
            block.getRelative(BlockFace.NORTH).setType(Material.BEDROCK);
            block.getRelative(BlockFace.SOUTH).setType(Material.BEDROCK);
            block.getRelative(BlockFace.EAST).setType(Material.BEDROCK);
            block.getRelative(BlockFace.WEST).setType(Material.BEDROCK);

            final Location torchLocation = block.getRelative(0, 1, 0).getLocation();
            final Set<BlockFace> AllowedFaces = Set.of(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH);
            for (final BlockFace face : AllowedFaces) {

                DragonSlayer.setTorch(torchLocation, face);

            }

            location = torchLocation.getBlock().getRelative(0, -1, 0).getLocation();
            DragonSlayer.delSurroundingBlocks(location);
        }

        for (final Item item : world.getEntitiesByClass(Item.class)) {
            if (item.getType() == EntityType.ITEM && item.getLocation().distance(location) <= 3.0D
                    && item.getItemStack().getType().equals(Material.TORCH)) {
                item.remove();
            }
        }
    }

    private static void delSurroundingBlocks(final Location location) {
        location.getBlock().getRelative(BlockFace.NORTH).setType(Material.AIR);
        location.getBlock().getRelative(BlockFace.SOUTH).setType(Material.AIR);
        location.getBlock().getRelative(BlockFace.EAST).setType(Material.AIR);
        location.getBlock().getRelative(BlockFace.WEST).setType(Material.AIR);
    }

    private static void setTorch(final Location location, final BlockFace blockFace) {
        // Todo: discover why this is throwing errors...
        Bukkit.getScheduler().runTask(DragonSlayer.instance, new Runnable() {
            @Override
            public void run() {
                final Block TorchBlock = location.getBlock().getRelative(blockFace);
                TorchBlock.setType(Material.WALL_TORCH);

                final BlockData blockData = TorchBlock.getBlockData();
                if (blockData instanceof final Directional torchData) {
                    torchData.setFacing(blockFace.getOppositeFace());
                }
            }
        });
    }

    private void WorldRefresh2(final String worldName) {
        final World ThisWorld = this.getDragonWorldFromString(worldName);
        try {
            final Object edb = this.getEnderDragonBattle(ThisWorld);
            if (DragonSlayer.db_a == null) {
                DragonSlayer.db_a = edb.getClass().getDeclaredMethod("a", List.class);
            }

            if (DragonSlayer.db_a != null) {
                DragonSlayer.db_a.setAccessible(true);
                DragonSlayer.db_a.invoke(edb, Collections.emptyList());
            }
        } catch (SecurityException | NullPointerException | InvocationTargetException | IllegalAccessException
                | NoSuchMethodException var4) {
            this.logger.info("Can not handle world refresh..");
        }

        this.PlaceEmptyPortals(ThisWorld, true);
        this.getServer().getScheduler().runTaskLater(this, () -> {
            final Location PortLoc = DragonSlayer.findPosForPortal((double) this.configManager.getPortalXdef(worldName),
                    (double) this.configManager.getPortalZdef(worldName), ThisWorld, Material.BEDROCK);
            this.activateChunksAroundPosition(PortLoc, ThisWorld, 2, false);

            for (final Entity ent : PortLoc.getWorld().getEntitiesByClass(EnderCrystal.class)) {
                if (PortLoc.distance(ent.getLocation()) <= 4.0D) {
                    ent.remove();
                }
            }

        }, 1200L);
    }

    public void WorldRefresh(final String worldName) {
        if (worldName != null && this.checkWorld(worldName.toLowerCase())) {
            final World ThisWorld = this.getDragonWorldFromString(worldName);
            if (ThisWorld.getEnvironment() != Environment.THE_END) {
                this.logger.info("World refresh not possible! " + worldName + " is not an END-World!");
                return;
            }

            this.setWorldRefreshRun(worldName);
            final Location PortLoc = DragonSlayer.findPosForPortal((double) this.configManager.getPortalXdef(worldName),
                    (double) this.configManager.getPortalZdef(worldName), ThisWorld, Material.BEDROCK);

            for (double x = -3.0D; x <= 3.0D; x += 3.0D) {
                for (double z = -3.0D; z <= 3.0D; z += 6.0D) {
                    if (x != 0.0D) {
                        z = 0.0D;
                    }

                    final Entity crystal = ThisWorld.spawnEntity(PortLoc.clone().add(x + 0.5D, 1.0D, z + 0.5D), EntityType.END_CRYSTAL);
                    crystal.setInvulnerable(true);
                }
            }

            if (this.configManager.getMultiPortal()) {
                this.setExitPortalLocation(ThisWorld, PortLoc.getBlockX(), PortLoc.getBlockY(), PortLoc.getBlockZ(), true, false);
            }

            this.PlaceEmptyPortals(ThisWorld, false);

            ThisWorld.getEnderDragonBattle().initiateRespawn();

            this.configManager.setCreatePortal(true, worldName.toLowerCase());
            this.WorldRefresh2(worldName);
        }

    }

    private void setWorldRefreshRun(final String worldName) {
        final World ThisWorld = this.getDragonWorldFromString(worldName);
        this.ProtectResetWorlds.add(ThisWorld);
        this.getServer().getScheduler().runTaskLater(this, () -> {
            if (this.configManager.getRespawnPlayer(worldName.toLowerCase())) {
                this.WorldReset(worldName, false);
            }

        }, 2400L);
    }

    void WorldReset(final String worldName, boolean force) {
        if (this.getServer().getPluginManager().getPlugin("Multiverse-Core") != null) {
            if (worldName != null && this.checkWorld(worldName.toLowerCase())) {
                if (this.configManager.getResetWorld(worldName.toLowerCase())) {
                    force = true;
                }

                final World ThisWorld = this.getDragonWorldFromString(worldName);
                final Collection<Player> PlayerList = ThisWorld.getPlayers();
                World BaseWorld = this.getDragonWorldFromString(worldName.replace("_the_end", ""));
                if (BaseWorld == null) {
                    BaseWorld = this.getMultiverseCore().getMVWorldManager().getMVWorld(ThisWorld).getRespawnToWorld();
                }

                final List<String> command = this.configManager.getRespawnCommand(worldName.toLowerCase());
                if (command != null && !command.isEmpty()) {
                    for (final String command2 : command) {
                        if (!command2.contains("$player")) {
                            this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command2);
                        }
                    }
                }

                for (final Player player : PlayerList) {
                    DragonSlayer.deletePlayersBossBars(player);
                    if (command != null && !command.isEmpty()) {
                        for (String command2 : command) {
                            if (command2.contains("$player")) {
                                command2 = command2.replace("$player", player.getName());
                                this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command2);
                            }
                        }
                    } else if (BaseWorld != null) {
                        final Location Spawn = this.getMultiverseCore().getMVWorldManager().getMVWorld(BaseWorld).getSpawnLocation();
                        Spawn.setWorld(BaseWorld);
                        player.teleport(Spawn);
                    }
                }

                if (force) {
                    this.RemoveDragons(ThisWorld, false, false);
                    this.getServer().getScheduler().runTaskLater(this, () -> {
                        final Long WorldsOldSeed = ThisWorld.getSeed();
                        this.getMultiverseCore().getMVWorldManager().regenWorld(worldName, true, false, WorldsOldSeed.toString());
                        if (this.configManager.getVerbosity()) {
                            this.logger.info(ChatColor.GREEN + "The world " + ThisWorld.getName() + " will get recreated!");
                        }

                        final World NewWorld = this.getDragonWorldFromString(worldName);
                        if (NewWorld != null) {
                            this.UpdateEndgatewayPosList(NewWorld);
                            if (!this.configManager.getCreateGateways(worldName.toLowerCase())) {
                                this.prepareEndGateways(NewWorld, new ArrayList<Integer>());
                            }

                            Location Spawn = null;
                            Spawn = this.getMultiverseCore().getMVWorldManager().getMVWorld(NewWorld).getSpawnLocation();

                            final Material endstone = Material.END_STONE;

                            Spawn.setY(DragonSlayer.findPosForPortal(Spawn.getX(), Spawn.getZ(), NewWorld, endstone).getY() + 4.0D);
                            Spawn.setWorld(NewWorld);
                            this.getMultiverseCore().getMVWorldManager().getMVWorld(NewWorld).setSpawnLocation(Spawn);
                            if (!this.configManager.getRespawnPlayer(worldName.toLowerCase())) {
                                for (final Player player : PlayerList) {
                                    player.teleport(Spawn);
                                }
                            }

                            this.getServer().getScheduler().runTaskLater(this, () -> {
                                this.PlaceEmptyPortal(NewWorld);
                                this.PlaceEmptyPortals(NewWorld, true);
                            }, 1L);
                        }

                        this.configManager.setCreatePortal(true, worldName.toLowerCase());
                        if (this.configManager.getDelay(worldName.toLowerCase()) > 0) {
                            if (NewWorld != null) {
                                final Location Position = new Location(NewWorld, 0.0D, 1.0D, 0.0D);
                                if (this.configManager.getDisableOrigDragonRespawn(worldName.toLowerCase())) {
                                    this.setTestPortal2(Position);
                                }
                            }

                            this.SpawnXDragons(this.missingDragons(worldName.toLowerCase()), worldName.toLowerCase());
                        } else if (NewWorld != null) {
                            final Location Position = new Location(NewWorld, 0.0D, 1.0D, 0.0D);
                            this.setTestPortal2(Position);
                            final int ExistDrags = this.getDragonCount(worldName);
                            if (ExistDrags > 0) {
                                this.RemoveDragons(NewWorld, false, true);
                            }
                        }

                    }, 60L);
                }
            }
        } else {
            this.logger.warning("World reset needs multiverse core installed!");
        }

    }

    private void checkForSleepingDragons() {
        final Collection<EnderDragon> entityList = new HashSet<EnderDragon>();

        for (final String mapname : this.configManager.getMaplist()) {
            final World theWorld = this.getServer().getWorld(mapname);
            if (theWorld != null) {
                final Collection<EnderDragon> entityList_ = theWorld.getEntitiesByClass(EnderDragon.class);
                entityList.addAll(entityList_);
            }
        }

        for (final EnderDragon ent : DragonSlayer.DragonBarList.keySet()) {
            if (!entityList.contains(ent)) {
                entityList.add(ent);
            }
        }

        for (final EnderDragon dragon : entityList) {
            if (dragon.isValid()) {
                final int dragonId = this.getDragonIDMeta(dragon);
                if (dragonId >= 0) {
                    final Location actLoc = dragon.getLocation();
                    final Location oldLoc = DragonSlayer.getDragonPosMeta(dragon);
                    if (oldLoc != null && oldLoc.equals(actLoc)) {
                        final String worldName = dragon.getWorld().getName().toLowerCase();

                        final Phase nowPhase = dragon.getPhase();
                        if (nowPhase != Phase.DYING && nowPhase != Phase.SEARCH_FOR_BREATH_ATTACK_TARGET && nowPhase != Phase.BREATH_ATTACK
                                && nowPhase != Phase.ROAR_BEFORE_ATTACK) {
                            dragon.setPhase(Phase.CIRCLING);
                            dragon.teleport(new Location(dragon.getWorld(), (double) this.configManager.getPortalX(worldName, dragonId),
                                    actLoc.getY(), (double) this.configManager.getPortalZ(worldName, dragonId)));
                        }

                    }

                    this.setDragonPosMeta(dragon, actLoc);
                }
            }
        }

    }

    private MultiverseCore getMultiverseCore() {
        if (this.getServer().getPluginManager().getPlugin("Multiverse-Core") instanceof MultiverseCore) {
            return (MultiverseCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        } else {
            throw new RuntimeException("MultiVerse not found!");
        }
    }

    void AtKillCommand(final String worldName, final Player player, final EnderDragon dragon) {
        if (worldName != null && this.checkWorld(worldName.toLowerCase())) {
            final int dragonId = this.getDragonIDMeta(dragon);
            if (dragonId >= 0) {
                this.getServer().getScheduler().runTaskLater(this, () -> {
                    final World ThisWorld = this.getDragonWorldFromString(worldName);
                    final List<String> commands = this.configManager.getDragonCommand(worldName.toLowerCase(), dragonId);
                    if (!commands.isEmpty()) {
                        this.myCommandsHandler(commands, ThisWorld, player);
                    }

                }, 60L);
            }
        }

    }

    void myCommandsHandler(final List<String> commands, final World world, final Player p) {
        if (commands != null && !commands.isEmpty()) {
            final String ThisWorldName = world.getName();
            commands.forEach(command -> {
                if (!command.isEmpty()) {
                    final List<Player> pj = new ArrayList<Player>();
                    if (command.contains("$player") && p == null) {
                        pj.addAll(world.getPlayers());
                    } else {
                        pj.add(p);
                    }

                    pj.forEach(player -> {
                        final String pn = player == null ? "" : player.getName();
                        String command3 = command.replace("$player", pn).replace(ThisWorldName.toLowerCase(), ThisWorldName);
                        if (DragonSlayer.PAPIenabled) {
                            try {
                                command3 = PlaceholderAPI.setPlaceholders(
                                        player == null ? this.getOfflineSlayer() : this.getServer().getOfflinePlayer(player.getUniqueId()),
                                        command3);
                            } catch (final NullPointerException var7) {
                            }
                        }

                        this.commandPercentageCall(command3, ThisWorldName);
                    });
                }

            });
        }

    }

    private void commandPercentageCall(String command, final String worldName) {
        int perc_ = 100;
        String percAdd = "";
        if (command.startsWith("{") && command.contains("}")) {
            final String perc = command.substring(1, command.indexOf("}")).replaceAll("[ _a-zA-Z%-]", "");
            if (!perc.isEmpty()) {
                perc_ = Integer.parseInt(perc);
                command = command.substring(command.indexOf("}") + 1);
                percAdd = " on a " + perc + "% chance!";
            }
        }

        final int i = (new Random()).nextInt(100);
        if (i < perc_) {
            this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command);
            if (this.configManager.getVerbosity()) {
                this.logger.info(ChatColor.GREEN + "In the world " + worldName + " Command: '" + command + "' was executed..." + percAdd);
            }
        }

    }

    void setEndGatewayPortals(final World world) {
        if (this.configManager.getCreateGateways(world.getName().toLowerCase())) {
            final Object drb = this.getEnderDragonBattle(world);
            if (drb != null) {
                try {
                    if (DragonSlayer.endgatewayMethod == null) {
                        String methodeName = null;
                        methodeName = "spawnNewGateway";

                        DragonSlayer.endgatewayMethod = drb.getClass().getDeclaredMethod(methodeName);
                    }

                    if (DragonSlayer.endgatewayMethod != null) {
                        DragonSlayer.endgatewayMethod.setAccessible(true);
                        DragonSlayer.endgatewayMethod.invoke(drb);
                    }
                } catch (SecurityException | NullPointerException | InvocationTargetException | IllegalAccessException
                        | NoSuchMethodException var5) {
                    this.logger.warning(
                            "Unknown or unsupported Version :" + DragonSlayer.getVersion() + ", can't handle end-gateways...(yet?)");
                }
            }
        }

    }

    private void prepareEndGateways(final World world, final ArrayList<Integer> remainingGateways) {
        final Object DrBatt = this.getEnderDragonBattle(world);
        if (DrBatt != null) {
            try {
                if (DragonSlayer.OrigGateways == null) {
                    DragonSlayer.OrigGateways = this.getFieldByName(DrBatt.getClass(), "gateways");
                }

                if (DragonSlayer.OrigGateways != null) {
                    DragonSlayer.OrigGateways.setAccessible(true);
                    final ObjectArrayList<Integer> newObjectArray = new ObjectArrayList<Integer>();
                    newObjectArray.addAll(remainingGateways);
                    DragonSlayer.OrigGateways.set(DrBatt, newObjectArray);

                }
            } catch (NullPointerException | IllegalAccessException | SecurityException var5) {
                this.logger
                        .warning("Unknown or unsupported Version :" + DragonSlayer.getVersion() + ", can't handle end-gateways...(yet?)");
            }
        }

    }

    static String getVersion() { return "1.20.6"; }

    private void setTestPortal() {
        for (final String DragonWorld : this.configManager.getMaplist()) {
            if (Bukkit.getWorld(DragonWorld) != null) {
                final World W = this.getDragonWorldFromString(DragonWorld);
                final Location Position = new Location(W, 0.0D, 1.0D, 0.0D);
                final Location Position2 = new Location(W, -2.0D, 1.0D, 2.0D);
                Position.getBlock().setType(Material.AIR);
                Position2.getBlock().setType(Material.AIR);
                if (this.configManager.getDisableOrigDragonRespawn(DragonWorld)) {
                    this.setTestPortal2(Position);
                    this.setTestPortal2(Position2);
                }
            }
        }

    }

    private void setTestPortal2(final Location location) {
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            location.getBlock().getRelative(BlockFace.NORTH).setType(Material.BEDROCK);
            location.getBlock().getRelative(BlockFace.SOUTH).setType(Material.BEDROCK);
            location.getBlock().getRelative(BlockFace.WEST).setType(Material.BEDROCK);
            location.getBlock().getRelative(BlockFace.EAST).setType(Material.BEDROCK);
            location.getBlock().getRelative(BlockFace.UP).setType(Material.BEDROCK);

            location.getBlock().setType(Material.END_PORTAL);

        });
    }

    private void countEndGatewaysAndContinue() {
        for (final String DragonWorld : this.configManager.getMaplist()) {
            this.findAndUseEndgateways(DragonWorld);
        }

    }

    void findAndUseEndgateways(final String worldName) {

        final World ThisWorld = this.getDragonWorldFromString(worldName);
        if (ThisWorld != null) {
            int Counter = 0;
            final long oldSeed = ThisWorld.getSeed();
            ArrayList<Integer> shuffledList = new ArrayList<Integer>();
            shuffledList.addAll(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
            Collections.shuffle(shuffledList, new Random(oldSeed));
            final String Slots = " free slots...";

            for (int x = 1; x <= 20; ++x) {
                final double x2 = 96.0D * Math.cos(2.0D * (-Math.PI + 0.15707963267948966D * (double) x));
                final double z2 = 96.0D * Math.sin(2.0D * (-Math.PI + 0.15707963267948966D * (double) x));
                int x1 = (int) x2;
                int z1 = (int) z2;

                if (x2 < (double) x1) {
                    --x1;
                }

                if (z2 < (double) z1) {
                    --z1;
                }

                final Location Testlocation = new Location(ThisWorld, (double) x1, 75.0D, (double) z1);
                if (!DragonSlayer.Endgateways.contains(Testlocation)) {
                    DragonSlayer.Endgateways.add(Testlocation);
                }

                final Block TestBlock = Testlocation.getBlock();
                if (TestBlock.getType() == Material.END_GATEWAY) {
                    ++Counter;
                    shuffledList.remove(shuffledList.indexOf(x == 20 ? 0 : x));
                }
            }

            if (!this.configManager.getCreateGateways(worldName.toLowerCase())) {
                shuffledList = new ArrayList<Integer>();
            }

            this.prepareEndGateways(ThisWorld, shuffledList);
            if (this.configManager.getVerbosity()) {
                this.logger.info(Counter + " endgateways found on " + ThisWorld.getName() + " with " + shuffledList.size() + Slots);
            }
        }

    }

    void UpdateEndgatewayPosList(final World world) {
        for (int x = 1; x <= 20; ++x) {
            final double x2 = 96.0D * Math.cos(2.0D * (-Math.PI + 0.15707963267948966D * (double) x));
            final double z2 = 96.0D * Math.sin(2.0D * (-Math.PI + 0.15707963267948966D * (double) x));
            int x1 = (int) x2;
            int z1 = (int) z2;
            if (x2 < (double) x1) {
                --x1;
            }

            if (z2 < (double) z1) {
                --z1;
            }

            final Location Testlocation = new Location(world, (double) x1, 75.0D, (double) z1);
            if (!DragonSlayer.Endgateways.contains(Testlocation)) {
                DragonSlayer.Endgateways.add(Testlocation);
            }
        }

        final ArrayList<Location> newEndgateways = new ArrayList<Location>();

        for (final Location Loc : DragonSlayer.Endgateways) {
            boolean worldExists = false;

            try {
                if (Loc.isWorldLoaded()) {
                    worldExists = true;
                }
            } catch (final NoSuchMethodError var11) {
                try {
                    Loc.getBlock().getWorld().getChunkAt(0, 0);
                    worldExists = true;
                } catch (final Exception var10) {
                }
            }

            if (worldExists && this.checkWorld(Loc.getWorld().getName().toLowerCase())) {
                newEndgateways.add(Loc);
            }
        }

        DragonSlayer.Endgateways = newEndgateways;
    }

    void OrigEnderDragonSetKilled(final EnderDragon dragon) {

        final Object DrBatt = this.getEnderDragonBattle(dragon);
        if (DrBatt != null) {
            try {
                if (DragonSlayer.DragonKilled == null) {
                    DragonSlayer.DragonKilled = this.getFieldByName(DrBatt.getClass(), "dragonKilled");
                }
                if (DragonSlayer.DragonUUID == null) {
                    DragonSlayer.DragonUUID = this.getFieldByType(DrBatt.getClass(), "UUID");
                }

                if (DragonSlayer.DragonKilled != null && DragonSlayer.DragonUUID != null) {
                    DragonSlayer.DragonKilled.setAccessible(true);
                    DragonSlayer.DragonUUID.setAccessible(true);
                    DragonSlayer.DragonKilled.setBoolean(DrBatt, true);
                    DragonSlayer.DragonUUID.set(DrBatt, (Object) null);
                }
            } catch (SecurityException | NullPointerException | IllegalAccessException var5) {
                this.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion() + " ,can't handle this here... (yet?)");
                if (this.configManager.debugOn()) {
                    var5.printStackTrace();
                }
            }
        }

    }

    void setEDBPreviouslyKilled(final EnderDragon dragon, final boolean previouslyKilled) {

        final Object DrBatt = this.getEnderDragonBattle(dragon);

        if (DrBatt != null) {
            try {
                if (DragonSlayer.DragonPreviouslyKilled == null) {
                    DragonSlayer.DragonPreviouslyKilled = this.getFieldByName(DrBatt.getClass(), "previouslyKilled");
                }

                if (DragonSlayer.DragonPreviouslyKilled != null) {
                    DragonSlayer.DragonPreviouslyKilled.setAccessible(true);
                    DragonSlayer.DragonPreviouslyKilled.setBoolean(DrBatt, previouslyKilled);
                }
            } catch (SecurityException | NullPointerException | IllegalAccessException var7) {
                this.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion() + " ,can't handle this here... (yet?)");
                if (this.configManager.debugOn()) {
                    var7.printStackTrace();
                }
            }
        }

    }

    boolean getEnderDragonPreviouslyKilled(final EnderDragon dragon) {

        try {
            if (dragon.getWorld().getEnvironment() == Environment.THE_END) {
                return dragon.getDragonBattle() != null ? dragon.getDragonBattle().hasBeenPreviouslyKilled()
                        : dragon.getWorld().getEnderDragonBattle().hasBeenPreviouslyKilled();
            }
        } catch (final NullPointerException var5) {
            if (this.configManager.debugOn()) {
                this.logger.warning("NullPointerException in EnderDragonPreviouslyKilled... no API DragonBattle?:");
                var5.printStackTrace();
            }
        }

        return true;
    }

    void WorldGenEndTrophyPositionSet(final EnderDragon dragon, final boolean setEDBforce) {
        if (this.configManager.getMultiPortal() || DragonSlayer.serverStarted) {
            if (!this.configManager.getMultiPortal() || !DragonSlayer.serverStarted || this.checkDSLDragon(dragon)) {
                final World ThisWorld = dragon.getWorld();
                final int dragonId = this.getDragonIDMeta(dragon);
                final String Worldname = ThisWorld.getName().toLowerCase();
                boolean errored = false;

                try {
                    if (!this.configManager.getOldPortal(Worldname)) {
                        this.getServer().getScheduler().runTaskLater(this, () -> {
                            final String phaseController_m_c_name = "c";
                            final Object newDragEnt = this.getEntityEnderDragon(dragon);
                            Object phase_control = null;
                            String newPhase = "";
                            final int newX = this.configManager.getPortalX(Worldname, dragonId);
                            final int newZ = this.configManager.getPortalZ(Worldname, dragonId);
                            final int newY = DragonSlayer.findPosForPortal((double) newX, (double) newZ, ThisWorld, Material.BEDROCK)
                                    .getBlockY() + 4;

                            try {
                                if (this.phaseControlManager_method == null) {
                                    this.phaseControlManager_method = this.getMethodByReturntype(newDragEnt.getClass(),
                                            "EnderDragonPhaseManager", (Class<?>[]) null);
                                }

                                final Object phase_control_manager = this.phaseControlManager_method.invoke(newDragEnt);
                                phase_control = this
                                        .getMethodByReturntype(phase_control_manager.getClass(), "DragonPhaseInstance", new Class[0])
                                        .invoke(phase_control_manager);
                                newPhase = this.getMethodByReturntype(phase_control.getClass(), "EnderDragonPhase", (Class<?>[]) null)
                                        .invoke(phase_control).toString();
                            } catch (IllegalArgumentException | InvocationTargetException | SecurityException
                                    | IllegalAccessException var24) {
                                if (this.configManager.debugOn()) {
                                    var24.printStackTrace();
                                }
                            }

                            if (newPhase.startsWith("LandingApproach")) {
                                try {
                                    final Field pathEnt_f = this.getFieldByName(phase_control.getClass(), "currentPath");
                                    pathEnt_f.setAccessible(true);
                                    final Object pathEnt = pathEnt_f.get(phase_control);
                                    if (pathEnt != null) {
                                        if (this.getPathPointFromPathEnt_method == null) {
                                            this.getPathPointFromPathEnt_method = this.getMethodByReturntype(pathEnt.getClass(), "Node",
                                                    new Class[] { Integer.TYPE });
                                        }

                                        int pathEntListSize;
                                        if (this.pathEntList == null) {
                                            final Object pathPoint = this.getPathPointFromPathEnt_method.invoke(pathEnt, 0);
                                            this.pathEntList = this.getFieldByType(pathEnt.getClass(),
                                                    "List<" + pathPoint.getClass().getName() + ">");
                                            this.pathEntList.setAccessible(true);
                                        }

                                        pathEntListSize = ((List<?>) this.pathEntList.get(pathEnt)).size();

                                        for (int j = 0; j < pathEntListSize; ++j) {
                                            final Object pathPointAusPathEnt = this.getPathPointFromPathEnt_method.invoke(pathEnt, j);
                                            if (this.checkPositionRange(pathPointAusPathEnt, newX, newZ, 90)) {
                                                final Object newPathPoint = pathPointAusPathEnt.getClass()
                                                        .getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE)
                                                        .newInstance(newX, newY, newZ);
                                                this.getMethodByReturntype(pathEnt.getClass(), "void",
                                                        new Class[] { Integer.TYPE, newPathPoint.getClass() })
                                                        .invoke(pathEnt, j, newPathPoint);
                                            }
                                        }
                                    }
                                } catch (final Exception var23) {
                                    if (this.configManager.debugOn()) {
                                        this.logger.warning("Can not handle landing flight (PathEntity)!");
                                        var23.printStackTrace();
                                    }
                                }
                            } else if (newPhase.startsWith("Landing")
                                    || newPhase.startsWith("Dying") && this.configManager.getDragonDeathFix(Worldname)) {
                                try {
                                    final Object vec3 = this.getMethodByReturntype(phase_control.getClass(), "Vec3", (Class<?>[]) null)
                                            .invoke(phase_control);
                                    if (vec3 != null) {
                                        final String ved3d_add_name = "add";
                                        final double v_x = (double) vec3.getClass().getMethod("x").invoke(vec3);
                                        final double v_z = (double) vec3.getClass().getMethod("z").invoke(vec3);

                                        if (Math.abs(v_x - (double) newX) > 1.0D && Math.abs(v_z - (double) newZ) > 1.0D) {
                                            Object vec3d_new = null;

                                            try {
                                                vec3d_new = vec3.getClass().getConstructor(Double.TYPE, Double.TYPE, Double.TYPE)
                                                        .newInstance(newX, newY, newZ);
                                            } catch (final InstantiationException var21) {
                                                if (this.configManager.debugOn()) {
                                                    this.logger
                                                            .warning("Debug: create Vec3D with dynamic reflection NMS... not directly...!");
                                                }

                                                final Object vec3d_new1 = this
                                                        .getMethodByReturntype(vec3.getClass(), "Vec3", new Class[] { Double.TYPE })
                                                        .invoke(vec3, 0);
                                                vec3d_new = vec3d_new1.getClass()
                                                        .getMethod(ved3d_add_name, Double.TYPE, Double.TYPE, Double.TYPE)
                                                        .invoke(vec3d_new1, newX, newY, newZ);
                                            }

                                            final Field vec3c_f = this.getFieldByType(phase_control.getClass(), "Vec3");
                                            vec3c_f.setAccessible(true);
                                            vec3c_f.set(phase_control, vec3d_new);
                                            phase_control.getClass().getDeclaredMethod(phaseController_m_c_name).invoke(phase_control);
                                        }
                                    }
                                } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException
                                        | InvocationTargetException | NullPointerException var22) {
                                    if (this.configManager.debugOn()) {
                                        this.logger.warning("Debug: Can not handle landing vector!");
                                        var22.printStackTrace();
                                    }
                                }
                            }

                        }, 5L);
                    }
                } catch (SecurityException | IllegalArgumentException | ArrayIndexOutOfBoundsException var8) {
                    errored = true;
                    if (this.configManager.debugOn()) {
                        var8.printStackTrace();
                    }
                }

                if (errored) {
                    this.logger.warning("Error while trying to set new portal position! (TrophyClass)");
                }

                this.getServer().getScheduler().runTaskLater(this, () -> {
                    boolean useAll = false;
                    if (this.checkServerStarted() ^ this.configManager.getMultiPortal()) {
                        this.invertServerstartedAndSetAllNavi();
                        useAll = true;
                    } else if (dragon.isValid()) {
                        this.setDragonNavi(dragon);
                    }

                    if (this.checkDSLDragon(dragon)) {
                        this.setExitPortalLocation(ThisWorld, this.configManager.getPortalX(Worldname, dragonId), (Integer) null,
                                this.configManager.getPortalZ(Worldname, dragonId), setEDBforce, useAll);
                    }

                }, 2L);
            }
        }
    }

    void setDragonNavi(final EnderDragon dragon) {
        Object entDrag;
        int dragonId;
        String world;

        if (!this.checkDSLDragon(dragon)) {
            return;
        }
        String funcName = "";
        world = dragon.getWorld().getName().toLowerCase();
        dragonId = this.getDragonIDMeta(dragon);
        entDrag = this.getEntityEnderDragon(dragon);
        if (DragonSlayer.naviField == null) {
            funcName = "findClosestNode";

            try {
                DragonSlayer.naviField = this.getFieldByName(entDrag.getClass(), "nodes");
                DragonSlayer.fillArray = entDrag.getClass().getDeclaredMethod(funcName, new Class[0]);
            } catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
                if (this.configManager.debugOn()) {
                    e.printStackTrace();
                }
                this.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion()
                        + ", can't handle EnderDragon's pathpoints...(yet?)");

                this.logger.warning("Dragon is:" + dragon.getName());
            }
        }

        if (DragonSlayer.naviField != null && DragonSlayer.fillArray != null) {
            try {
                DragonSlayer.naviField.setAccessible(true);
                Object pathArray = DragonSlayer.naviField.get(entDrag);
                final int newX = this.configManager.getPortalX(world, dragonId);
                final int newZ = this.configManager.getPortalZ(world, dragonId);
                int newY = DragonSlayer.findPosForPortal(newX, newZ, dragon.getWorld(), Material.BEDROCK).getBlockY();
                newY = newY < 48 || newY > 80 ? newY - 64 : 0;
                int n = 20;
                while (n <= 23) {
                    Object path_n = ((Object[]) pathArray)[n];
                    if (path_n == null || path_n != null && this.checkPositionRange(path_n, newX, newZ, 20)) {
                        ((Object[]) pathArray)[0] = null;
                        DragonSlayer.naviField.set(entDrag, pathArray);
                        DragonSlayer.fillArray.setAccessible(true);
                        DragonSlayer.fillArray.invoke(entDrag, new Object[0]);
                        pathArray = DragonSlayer.naviField.get(entDrag);
                        int i = 0;
                        while (i < ((Object[]) pathArray).length) {
                            ((Object[]) pathArray)[i] = this.makeMovedPathpointObject(((Object[]) pathArray)[i], newX, newY, newZ);
                            ++i;
                        }
                        DragonSlayer.naviField.set(entDrag, pathArray);
                        final Location teleLoc = dragon.getLocation();
                        if (path_n == null) {
                            path_n = "x=" + teleLoc.getBlockX() + ",y=" + teleLoc.getBlockY() + ",z=" + teleLoc.getBlockZ();
                        }
                        if (this.checkPositionRange(path_n, newX, newZ, 100)) {
                            teleLoc.setX(newX);
                            teleLoc.setZ(newZ);
                            teleLoc.setY(teleLoc.getBlockY() + new Random().nextInt(50));
                            dragon.teleport(teleLoc);
                        }
                        break;
                    }
                    ++n;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (this.configManager.debugOn()) {
                    e.printStackTrace();
                }
                this.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion()
                        + " ,can't handle EnderDragon's pathpoints...(yet?)");
            }
        }
    }

    private void invertServerstartedAndSetAllNavi() {
        DragonSlayer.serverStarted = !this.checkServerStarted();

        for (final String Mapname : this.configManager.getMaplist()) {
            final World World = this.getDragonWorldFromString(Mapname);
            if (World != null) {
                final Collection<EnderDragon> dragList = this.getDragonList(World, Mapname);
                if (dragList != null) {
                    for (final EnderDragon drag : dragList) {
                        if (this.getDragonIDMeta(drag) >= 0) {
                            this.setDragonNavi(drag);
                            if (drag.getPhase().equals(Phase.HOVER)) {
                                drag.setPhase(Phase.CIRCLING);
                            }
                        }
                    }
                }
            }
        }

    }

    void setExitPortalLocation(final World world, final int newX, Integer newY, final int newZ, boolean setEDBforce, final boolean useAll) {
        final Object DragonBattle = this.getEnderDragonBattle(world);
        final HashMap<Object, World> BattleList = new HashMap<Object, World>();
        if (!useAll) {
            if (DragonBattle != null) {
                BattleList.put(DragonBattle, world);
            }
        } else {
            setEDBforce = true;

            for (final String Mapname : this.configManager.getMaplist()) {
                final World World = this.getDragonWorldFromString(Mapname);
                if (World != null && World.getEnvironment() == Environment.THE_END) {
                    BattleList.put(this.getEnderDragonBattle(World), World);
                }
            }
        }

        try {
            if (setEDBforce) {
                for (final Object Battle : BattleList.keySet()) {
                    if (DragonSlayer.PortLoc_f == null) {
                        DragonSlayer.PortLoc_f = this.getFieldByType(Battle.getClass(), "BlockPos", true);
                    }

                    DragonSlayer.PortLoc_f.setAccessible(true);
                    final Object EDB_PortLoc = DragonSlayer.PortLoc_f.get(Battle);
                    if (EDB_PortLoc != null && (this.checkPositionRange(EDB_PortLoc, newX, newZ, 0) || newY != null)
                            || EDB_PortLoc == null) {
                        if (newY == null) {
                            newY = DragonSlayer
                                    .findPosForPortal((double) newX, (double) newZ, (World) BattleList.get(Battle), Material.BEDROCK)
                                    .getBlockY();
                        }

                        final Object BlockPosN = this.makeBlockPositionObject(newX, newY, newZ);
                        DragonSlayer.PortLoc_f.set(Battle, BlockPosN);
                    }
                }
            }
        } catch (NullPointerException | IllegalAccessException var13) {
            if (this.configManager.debugOn()) {
                this.logger.warning("Error while trying to set new portal position!!");
                var13.printStackTrace();
            }
        }

        if (DragonBattle != null) {
            this.setCrystalAmount(DragonBattle, this.configManager.getOldPortal(world.getName().toLowerCase()));
        }

    }

    private boolean checkPositionRange(final Object loc, final int newX, final int newZ, final int dist) {
        final String[] locArray = loc.toString().replace("BlockPos", "").replace("Node", "").replaceAll("[_ }{=]", "").split(",");
        int newX2 = 0;
        int newZ2 = 0;
        if (locArray != null && locArray.length == 3) {
            for (final String n : locArray) {
                if (n.substring(0, 1).equals("x")) {
                    newX2 = Integer.parseInt(n.substring(1));
                }

                if (n.substring(0, 1).equals("z")) {
                    newZ2 = Integer.parseInt(n.substring(1));
                }
            }

        }

        return Math.abs(newX - newX2) > dist || Math.abs(newZ - newZ2) > dist;
    }

    private Object makeBlockPositionObject(final int newX, final int newY, final int newZ) {

        try {
            if (DragonSlayer.newBlockPos == null) {
                final Class<?> BlockPosition_c = Class.forName("net.minecraft.core.BlockPos");

                DragonSlayer.newBlockPos = BlockPosition_c.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
            }

            if (DragonSlayer.newBlockPos != null) {
                return DragonSlayer.newBlockPos.newInstance(newX, newY, newZ);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | NullPointerException
                | IllegalArgumentException | InstantiationException | ClassNotFoundException var5) {
            this.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion() + " ,can't handle BlockPosition...(yet?)");
            if (this.configManager.debugOn()) {
                var5.printStackTrace();
            }
        }

        return null;

    }

    private void setCrystalAmount(final Object DragonBattle, final boolean forceOff) {
        try {
            if (DragonSlayer.CrystalAmount_f == null) {
                DragonSlayer.CrystalAmount_f = DragonBattle.getClass().getDeclaredField("crystalsAlive");
            }

            DragonSlayer.CrystalAmount_f.setAccessible(true);
            final Object CrystalAmount = DragonSlayer.CrystalAmount_f.get(DragonBattle);
            if (CrystalAmount != null && this.configManager.getMultiPortal()) {
                final int newCrystalAmount = this.configManager.getPortalAggression(forceOff);
                if ((Integer) CrystalAmount != newCrystalAmount) {
                    DragonSlayer.CrystalAmount_f.set(DragonBattle, newCrystalAmount);
                }
            }
        } catch (NoSuchFieldException | NullPointerException | IllegalAccessException var5) {
            if (this.configManager.debugOn()) {
                this.logger.warning("Error while trying to set new crystal amount !!");
                var5.printStackTrace();
            }
        }

    }

    boolean isRefreshRunning(final World world) {

        return !world.getEnderDragonBattle().getRespawnPhase().toString().equalsIgnoreCase("NONE");

    }

    /** {@link net.minecraft.world.entity.boss.enderdragon.EnderDragon} */
    @SuppressWarnings("unchecked")
    Object getEntityEnderDragon(final EnderDragon dragon) {
        Object returnwert = null;

        try {
            if (DragonSlayer.CraftEnderDragonClass == null) {
                DragonSlayer.CraftEnderDragonClass = (Class<CraftEnderDragon>) Class
                        .forName("org.bukkit.craftbukkit.entity.CraftEnderDragon");
            }

            if (DragonSlayer.CraftEnderDragonClass.isInstance(dragon)) {
                final Object craftDragon = DragonSlayer.CraftEnderDragonClass.cast(dragon);
                returnwert = DragonSlayer.CraftEnderDragonClass.getDeclaredMethod("getHandle").invoke(craftDragon);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | ClassNotFoundException var4) {
            this.logger
                    .warning("Unknown or unsupported Version :" + DragonSlayer.getVersion() + ", can't handle EntityEnderDragon...(yet?)");
            if (this.configManager.debugOn()) {
                var4.printStackTrace();
            }
        }

        return returnwert;
    }

    /**
     * {@link net.minecraft.world.level.pathfinder.Node}
     */
    private Object makeMovedPathpointObject(final Object point, final int newX, final int newY, final int newZ) {
        try {
            if (DragonSlayer.newPathPoint == null) {
                Class<?> pathPoint_cl = null;
                pathPoint_cl = Class.forName("net.minecraft.world.level.pathfinder.Node");

                DragonSlayer.newPathPoint = pathPoint_cl.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);

                DragonSlayer.pp_geta_func = this.getMethodByName(pathPoint_cl, "asVec3");
                final Class<?> Vec3_cl = Class.forName("net.minecraft.world.phys.Vec3");

                DragonSlayer.Vec3_getX = Vec3_cl.getDeclaredMethod("x");
                DragonSlayer.Vec3_getY = Vec3_cl.getDeclaredMethod("y");
                DragonSlayer.Vec3_getZ = Vec3_cl.getDeclaredMethod("z");

            }
            final Object baselockPos = DragonSlayer.pp_geta_func.invoke(point);
            final int x = ((Double) DragonSlayer.Vec3_getX.invoke(baselockPos)).intValue() + newX;
            final int y = ((Double) DragonSlayer.Vec3_getY.invoke(baselockPos)).intValue() + newY;
            final int z = ((Double) DragonSlayer.Vec3_getZ.invoke(baselockPos)).intValue() + newZ;

            return DragonSlayer.newPathPoint.newInstance(x, y, z);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException | NoSuchMethodException | SecurityException var7) {
            this.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion() + ", can't handle Pathpoints...(yet?)");
            return null;
        }
    }

    private Field getFieldByName(final Class<?> _class, final String name) { return this.getFieldByName(_class, name, false); }

    private Field getFieldByName(final Class<?> _class, final String name, final boolean onlyPublics) {
        final Field[] allFields = onlyPublics ? _class.getFields() : _class.getDeclaredFields();

        for (final Field field : allFields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }

        if (this.configManager.debugOn()) {
            this.logger.warning("No Field found with name " + name + " on class '" + _class.getName() + "'");
        }

        return null;
    }

    private Field getFieldByType(final Class<?> _class, final String returnType) { return this.getFieldByType(_class, returnType, false); }

    private Field getFieldByType(final Class<?> _class, final String returnType, final boolean onlyPublics) {
        final Field[] allFields = onlyPublics ? _class.getFields() : _class.getDeclaredFields();

        for (final Field field : allFields) {
            if (field.getGenericType().getTypeName().endsWith(returnType)) {
                return field;
            }
        }

        if (this.configManager.debugOn()) {
            this.logger.warning("No Field found for " + returnType);
        }

        return null;
    }

    Method getMethodByReturntype(final Class<?> _class, final String returnType, final Class<?>[] parameters) {
        return this.getMethodByReturntype(_class, returnType, parameters, false);
    }

    Method getMethodByReturntype(final Class<?> _class, final String returnType, final Class<?>[] parameters, final boolean noRaw) {
        final Method[] allMeth = _class.getMethods();

        label47: for (final Method meth : allMeth) {
            if (parameters != null) {
                final int paramLength = parameters.length;
                final Class<?>[] methParameters = meth.getParameterTypes();
                if (methParameters == null || paramLength != methParameters.length) {
                    continue;
                }

                for (int i = 0; i < paramLength; ++i) {
                    if (!parameters[i].equals(methParameters[i])) {
                        continue label47;
                    }
                }
            }

            if ((!noRaw || !meth.getName().toLowerCase().endsWith("raw")) && meth.getReturnType().getSimpleName().equals(returnType)) {
                return meth;
            }
        }

        if (this.configManager.debugOn()) {
            this.logger.warning("No Methode found for " + returnType);
        }

        return null;
    }

    private Object getEnderDragonBattle(final EnderDragon dragon) { return this.getEnderDragonBattle(dragon.getWorld()); }

    /** {@link net.minecraft.world.level.dimension.end.EndDragonFight} */
    private Object getEnderDragonBattle(final World world) {
        try {
            final Object worldServer = this.getWorldServer(world);
            if (DragonSlayer.getEDBMethod == null) {
                DragonSlayer.getEDBMethod = this.getMethodByReturntype(worldServer.getClass(), "EndDragonFight", (Class<?>[]) null);
            }

            Object edb = DragonSlayer.getEDBMethod.invoke(worldServer);
            if (edb == null && world.getEnvironment() == Environment.THE_END) {
                try {
                    final long ws_long = world.getSeed();
                    final Object emptyNBT = Class.forName("net.minecraft.nbt.CompoundTag").newInstance();
                    edb = Class.forName("net.minecraft.world.level.dimension.end.EndDragonFight")
                            .getConstructor(worldServer.getClass(), Long.TYPE, emptyNBT.getClass())
                            .newInstance(worldServer, ws_long, emptyNBT);

                    final Field ws_edb_f = this.getFieldByType(worldServer.getClass(), "EndDragonFight");
                    ws_edb_f.setAccessible(true);
                    ws_edb_f.set(worldServer, edb);
                    if (this.configManager.getVerbosity()) {
                        this.logger.warning("Started Hot-Fix for DragonBattle in world " + world.getName());
                    }
                } catch (final InstantiationException var7) {
                    this.logger.warning(
                            "unsupported Version :" + DragonSlayer.getVersion() + ", can't create own dragonbattle for this version...");
                    if (this.configManager.debugOn()) {
                        var7.printStackTrace();
                    }
                }
            }

            return edb;
        } catch (SecurityException | NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | ClassNotFoundException var8) {
            this.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion() + ", can't handle dragonbattle...(yet?)");
            if (this.configManager.debugOn()) {
                var8.printStackTrace();
            }
        }
        return null;
    }

    void PlaceArmorStand(final String worldName, final double x, final double y, final double z, final float yaw) {
        final Location as_loc = new Location(this.getDragonWorldFromString(worldName), x, y, z);
        as_loc.setYaw(yaw);
        final ArmorStand armorStand = this.spawnArmorStand1(as_loc);
        this.setArmorstandMeta(armorStand);
        if (this.getSlayer() != null && this.getStatueVersion() == 1) {
            this.setArmorStandNameETC(armorStand, as_loc.clone());
        }

        if (DragonSlayer.ProtocolLibEnabled) {
            if (this.getStatueVersion() >= 2) {
                this.changeArmorStand1_NPCValues(armorStand);

                this.getServer().getScheduler().runTaskLater(this, () -> {
                    final Location target_l = as_loc.clone().add(0.0D, 1.8D, 0.0D);
                    armorStand.teleport(target_l);
                    this.getServer().getScheduler().runTaskLater(this, () -> {
                        if (as_loc.equals(armorStand.getLocation())) {
                            try {
                                as_loc.getChunk().load();
                                as_loc.getChunk().getEntities();
                                armorStand.teleport(target_l);
                            } catch (final Exception var5) {
                            }

                            this.getServer().getScheduler().runTaskLater(this, () -> {
                                if (as_loc.equals(armorStand.getLocation())) {
                                    this.RemoveArmorStand();
                                    final ArmorStand armorStand_ = this.spawnArmorStand1(target_l);
                                    this.changeArmorStand1_NPCValues(armorStand_);
                                    this.setArmorstandMeta(armorStand_);
                                }

                            }, 10L);
                        }

                    }, 10L);
                }, 10L);

                DragonSlayer.protLibHandler.replaceNPCStatue();
            } else {
                DragonSlayer.protLibHandler.removeNPCStatue();
            }
        }

    }

    private ArmorStand spawnArmorStand1(final Location location) {
        final ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setGravity(false);
        armorStand.setArms(true);
        armorStand.setMarker(true);
        if (this.getSlayer() != null) {
            armorStand.setCustomName(this.getSlayer());
        } else {
            armorStand.setCustomName(this.getName());
        }

        armorStand.setCustomNameVisible(false);
        armorStand.setBasePlate(false);
        armorStand.setCanPickupItems(false);

        armorStand.setInvulnerable(true);

        armorStand.setVisible(true);
        armorStand.setCollidable(false);

        final double pitch = -20.0D;
        final float headYaw = 20.0F;
        DragonSlayer.setHeadDirection(armorStand, headYaw, pitch);
        final double armPitch = Math.toRadians(-100.0D);
        final double armYaw = Math.toRadians(12.0D);
        final double armRoll = Math.toRadians(-10.0D);
        final EulerAngle eangle = new EulerAngle(armPitch, armYaw, armRoll);
        armorStand.setRightArmPose(eangle);
        return armorStand;
    }

    private void changeArmorStand1_NPCValues(final ArmorStand armorStand) {
        final String playername = this.configManager.getSlayerPAPIFormatNickString();
        if (playername != null) {
            armorStand.setCustomName(playername);
        }

        armorStand.setVisible(false);
        if (this.getSlayer() != null) {
            armorStand.setCustomNameVisible(true);
        }

        armorStand.setCollidable(true);

    }

    private void setArmorStandNameETC(final ArmorStand armorStand, final Location location) {
        final Material[] matList = this.getArmorMat();
        final EntityEquipment Equip = armorStand.getEquipment();
        Equip.setHelmet(this.getPlayerHead());
        Equip.setBoots(new ItemStack(matList[0]));
        Equip.setChestplate(new ItemStack(matList[1]));
        Equip.setLeggings(new ItemStack(matList[2]));
        Equip.setItemInMainHand(new ItemStack(matList[3]));
        Equip.setItemInOffHand(DragonSlayer.getDragonSkull());

        final ArmorStand armorStand2 = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand2.setGravity(false);
        armorStand2.setMarker(false);
        final String playername = this.configManager.getSlayerPAPIFormatNickString();
        if (playername != null) {
            armorStand2.setCustomName(playername);
        } else {
            armorStand2.setCustomName(this.getName());
        }

        armorStand2.setCustomNameVisible(true);
        armorStand2.setBasePlate(false);
        armorStand2.setCanPickupItems(false);
        armorStand2.setInvulnerable(true);

        armorStand2.setVisible(false);
        armorStand2.setCollidable(false);

        armorStand.addPassenger(armorStand2);

        this.setArmorstandMeta(armorStand2);
    }

    static ItemStack getDragonSkull() { return new ItemStack(Material.DRAGON_HEAD); }

    Material[] getArmorMat() {
        final Material[] matList = new Material[5];
        this.configManager.translateASConfigName("material");
        String baseMat = this.getConfig().getString("global.statue_material", "DIAMOND").toUpperCase();
        baseMat = Material.getMaterial(baseMat + "_BOOTS") != null ? baseMat : "DIAMOND";
        matList[0] = Material.getMaterial(baseMat + "_BOOTS");
        matList[1] = Material.getMaterial(baseMat + "_CHESTPLATE");
        matList[2] = Material.getMaterial(baseMat + "_LEGGINGS");
        matList[4] = Material.getMaterial(baseMat + "_HELMET");
        baseMat = Material.getMaterial(baseMat + "_SWORD") != null ? baseMat : "DIAMOND";
        matList[3] = Material.getMaterial(baseMat + "_SWORD");
        return matList;
    }

    private void setArmorstandMeta(final ArmorStand armorstand) {
        final MetadataValue MdV_Armorstand = new FixedMetadataValue(this, true);
        armorstand.setMetadata("DSL-AS", MdV_Armorstand);
    }

    void setNPCStatueMeta(final Entity statue, final String value, final String metaAdd) {
        final MetadataValue mdV_Statue = new FixedMetadataValue(this, value);
        statue.setMetadata("DSL-AS" + (metaAdd != null ? metaAdd : ""), mdV_Statue);
    }

    String getNPCStatueMeta(final Entity statue, final String metaAdd) {
        String value = "";
        if (statue.hasMetadata("DSL-AS" + (metaAdd != null ? metaAdd : ""))) {
            final List<MetadataValue> list = statue.getMetadata("DSL-AS" + (metaAdd != null ? metaAdd : ""));
            if (list != null && list.size() != 0) {
                try {
                    value = (String) ((MetadataValue) list.get(0)).value();
                } catch (final Exception var6) {
                }
            }
        }

        return value;
    }

    int getStatueVersion() {
        final int i = Integer.parseInt(this.getConfig().getString("global.statue_version", "1"));
        return i >= 1 && i <= 2 ? i : 1;
    }

    World getArmorstandWorld() {
        final Location theArmorStandLoc = this.armorStandLoc(false);
        return theArmorStandLoc != null ? theArmorStandLoc.getWorld() : null;
    }

    ItemStack getPlayerHead() {
        final String offlineslayer = this.getSlayer();
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);

        final SkullMeta HeadMetadata = (SkullMeta) skull.getItemMeta();
        if (this.getSlayer() != null) {
            HeadMetadata.setOwningPlayer(this.getOfflineSlayer());
            HeadMetadata.setDisplayName(offlineslayer);
        }

        skull.setItemMeta(HeadMetadata);
        return skull;
    }

    private static void setHeadDirection(final ArmorStand armorStand, final float yaw, final double pitch) {
        final double xint = Math.toRadians(pitch);
        final double zint = 0.0D;
        final double yint = Math.toRadians((double) yaw);
        final EulerAngle eangle = new EulerAngle(xint, yint, zint);
        armorStand.setHeadPose(eangle);
    }

    boolean RemoveArmorStand() {
        final Location as_loc = this.armorStandLoc(true);
        if (as_loc == null) {
            return false;
        } else {
            final World w = as_loc.getWorld();
            final double as_x = as_loc.getX();
            final double as_z = as_loc.getZ();
            final double as_y = as_loc.getY();
            final Chunk MyChunk = w.getChunkAt(as_loc);
            if (!MyChunk.isLoaded()) {
                MyChunk.load();
            }

            final Entity[] chunkEntities = MyChunk.getEntities();
            final Collection<ArmorStand> ArmorStands = w.getEntitiesByClass(ArmorStand.class);
            final Collection<Entity> allEntities = w.getEntities();
            final Collection<Entity> newEntities = new ArrayList<Entity>();

            for (final Entity ent : chunkEntities) {
                if (ent instanceof ArmorStand && !newEntities.contains(ent)) {
                    newEntities.add(ent);
                }
            }

            for (final ArmorStand ent : ArmorStands) {
                if (!newEntities.contains(ent)) {
                    newEntities.add(ent);
                }
            }

            for (final Entity ent : allEntities) {
                if (ent instanceof ArmorStand && !newEntities.contains(ent)) {
                    newEntities.add(ent);
                }
            }

            boolean done = false;

            for (final Entity armorstand : newEntities) {
                final Location checkLoc = armorstand.getLocation();
                final double check_x = checkLoc.getX();
                final double check_z = checkLoc.getZ();
                final double check_y = checkLoc.getY();
                if (armorstand.hasMetadata("DSL-AS")
                        || Math.abs(check_x - as_x) <= 1.0D && Math.abs(check_z - as_z) <= 1.0D && Math.abs(check_y - as_y) <= 2.5D) {
                    armorstand.remove();
                    done = true;
                }
            }

            return done;
        }
    }

    Location armorStandLoc(final boolean simple) {
        if (this.getConfig().getConfigurationSection("armorstand") != null) {
            final ConfigurationSection oldSec = this.getConfig().getConfigurationSection("armorstand");
            final ConfigurationSection newSec = this.getConfig().createSection("statue");

            for (final Entry<String, Object> mapEntry : oldSec.getValues(true).entrySet()) {
                newSec.set((String) mapEntry.getKey(), mapEntry.getValue());
            }

            this.getConfig().set("armorstand", (Object) null);
            this.saveConfig();
        }

        if (this.getConfig().getString("statue.world") != null) {
            final String w = this.getConfig().getString("statue.world");
            final double x = this.getConfig().getDouble("statue.x");
            final double y = this.getConfig().getDouble("statue.y");
            final double z = this.getConfig().getDouble("statue.z");
            final float yaw = (float) this.getConfig().getDouble("statue.yaw");
            final World W = this.getDragonWorldFromString(w);
            final Location as_loc = new Location(W, x, y, z);
            if (!simple) {
                as_loc.setYaw(yaw);
            }

            return as_loc;
        } else {
            return null;
        }
    }

    private void resetArmorStand() {
        final Location as_loc = this.armorStandLoc(false);
        if (as_loc != null) {
            final World W = as_loc.getWorld();
            if (W != null) {
                final Chunk MyChunk = W.getChunkAt(as_loc);
                if (!MyChunk.isLoaded()) {
                    MyChunk.load();
                }

                MyChunk.getEntities();
                final int delay = 10;

                this.getServer().getScheduler().runTaskLater(this, () -> {
                    final Entity[] List = MyChunk.getEntities();
                    final int as_loc_y = as_loc.getBlockY();
                    as_loc.setY(0.0D);
                    int amount = 0;

                    for (final Entity armorstand : List) {
                        if (armorstand instanceof ArmorStand) {
                            final Location armstloc = armorstand.getLocation();
                            final int armorstand_y = armstloc.getBlockY();
                            armstloc.setY(0.0D);
                            if (armstloc.equals(as_loc) && armorstand_y - as_loc_y <= 1 && armorstand_y - as_loc_y >= 0) {
                                if (!armorstand.hasMetadata("DSL-AS")) {
                                    this.setArmorstandMeta((ArmorStand) armorstand);
                                }

                                ++amount;
                            }
                        }
                    }

                    if (amount <= 1 || this.getStatueVersion() != 1) {
                        if (amount == 0 || amount <= 1 && this.getStatueVersion() == 1 || this.getStatueVersion() >= 2
                                && DragonSlayer.protLibHandler != null && DragonSlayer.protLibHandler.NPCStatue == null) {
                            if (this.configManager.getVerbosity()) {
                                this.logger.info("Found that the Statue is not where it should be, resetting it!");
                            }

                            this.replaceArmorStand();
                        }

                    }
                }, (long) delay);
            } else {
                this.logger.warning("Could not find the world for resetting the ArmorStand! Is your config/setup OK?");
            }
        }

    }

    void replaceArmorStand() {
        final Location theArmorStandLoc = this.armorStandLoc(false);
        if (theArmorStandLoc != null) {
            try {
                theArmorStandLoc.getChunk().load();
                theArmorStandLoc.getChunk().getEntities();
            } catch (final Exception var3) {
            }

            this.getServer().getScheduler().runTaskLater(this, () -> {
                if (!this.RemoveArmorStand() && this.configManager.getVerbosity()) {
                    this.logger.info("No armorstand found to remove...");
                }

                this.PlaceArmorStand(theArmorStandLoc.getWorld().getName(), theArmorStandLoc.getX(), theArmorStandLoc.getY(),
                        theArmorStandLoc.getZ(), theArmorStandLoc.getYaw());
            }, 10L);
        }

    }

    private void StartRepeatingTimer() {

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            try {
                this.getServer().getScheduler().runTaskAsynchronously(this, () -> this.checkGWReverse(15));
            } catch (final Exception ignored) {
                if (this.configManager.debugOn())
                    ignored.printStackTrace();
            }

        }, 15L, 3L);
    }

    /**
     * Retrieves a method by looking at its name.
     *
     * @param nameRegex - regular expression that will match method names.
     * @return The first method that satisfies the regular expression.
     * @throws IllegalArgumentException If the method cannot be found.
     */
    public Method getMethodByName(final Class<?> clazz, final String nameRegex) {
        final Pattern match = Pattern.compile(nameRegex);
        for (final Method method : clazz.getMethods()) {
            if (match.matcher(method.getName()).matches()) {
                return method;
            }
        }

        throw new IllegalArgumentException(String.format("Unable to find a method in %s that matches \"%s\"", clazz.getName(), nameRegex));
    }

    @SuppressWarnings("unchecked")
    private void checkGWReverse(final int distance) {
        try {

            final boolean bypassON = Boolean.parseBoolean(this.getConfig().getString("global.bypassdragongateway"));
            int bypassFunc = Integer.parseInt(this.getConfig().getString("global.bypassfunc", "1"));
            bypassFunc = bypassFunc >= 1 && bypassFunc <= 2 ? bypassFunc : 1;
            if (bypassON) {
                for (final String mapname : this.configManager.getMaplist()) {
                    final World thisWorld = this.getDragonWorldFromString(mapname);
                    if (thisWorld != null) {
                        Collection<EnderDragon> dragons = null;

                        final Object thisCraftWorld = this.getCraftWorld(thisWorld);
                        if (thisCraftWorld != null) {
                            try {
                                final Method getMethodByName = this.getMethodByName(DragonSlayer.CraftWorldClass, "getEntitiesByClass");
                                dragons = (Collection<EnderDragon>) getMethodByName.invoke(thisCraftWorld, EnderDragon.class);

                            } catch (final Exception var19) {
                            }

                        } else {
                            try {
                                dragons = thisWorld.getEntitiesByClass(EnderDragon.class);
                            } catch (final Exception var19) {
                            }
                        }
                        if (dragons != null) {
                            for (final EnderDragon dragon : dragons) {
                                final Location dragLoc = dragon.getLocation();
                                final Block foundGateway = DragonSlayer.CheckGatewaysForDragon(thisWorld, dragLoc, distance);
                                if (foundGateway != null) {
                                    if (bypassFunc == 1) {
                                        final double dragY = dragLoc.getY();
                                        final double gateY = (double) foundGateway.getY();
                                        double diffY = gateY - dragY;
                                        diffY = diffY < 0.0D ? diffY - (double) distance * 1.2D : diffY + (double) distance * 1.2D;
                                        final Location target = dragLoc.clone().add(0.0D, diffY, 0.0D);
                                        this.syncTP(dragon, target);
                                    } else if (bypassFunc == 2) {
                                        this.getServer().getScheduler().runTaskLater(this, () -> {
                                            if (foundGateway.getType() == Material.END_GATEWAY) {
                                                try {
                                                    final Object worldServer = this.getWorldServer(thisWorld);
                                                    final Object blockPos = this.makeBlockPositionObject(foundGateway.getX(),
                                                            foundGateway.getY(), foundGateway.getZ());
                                                    Object tileEnt = null;
                                                    if (DragonSlayer.getTileEntity == null) {
                                                        final String funcName = "getBlockEntity";

                                                        try {
                                                            DragonSlayer.getTileEntity = Class.forName("net.minecraft.world.level.Level")
                                                                    .getDeclaredMethod(funcName, blockPos.getClass(), Boolean.TYPE);
                                                        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                                                        }
                                                    }

                                                    try {
                                                        DragonSlayer.getTileEntity.setAccessible(true);
                                                        tileEnt = DragonSlayer.getTileEntity.invoke(worldServer, blockPos, false);
                                                    } catch (final IllegalArgumentException var10) {
                                                        tileEnt = DragonSlayer.getTileEntity.invoke(worldServer, blockPos);
                                                    }

                                                    // Object nbt_1 = null;
                                                    if (tileEnt != null) {

                                                        if (DragonSlayer.saveNBT == null) {
                                                            // Until I learn how to get HolderLookup$Provider this will be skipped
                                                            // saveNBT = tileEnt.getClass().getDeclaredMethod("saveWithFullMetadata");

                                                            if (DragonSlayer.saveNBT != null) {
                                                                // nbt_1 = saveNBT.invoke(tileEnt);
                                                            } else {
                                                                // nbt_1 = Class.forName("net.minecraft.nbt.CompoundTag").newInstance();
                                                                if (this.configManager.debugOn()) {
                                                                    this.logger.warning(
                                                                            "Can not handle NBT recreate from TileEntity... using empty NBT...)");
                                                                }
                                                            }
                                                        }
                                                        final Object nbt_1 = Class.forName("net.minecraft.nbt.CompoundTag").newInstance();

                                                        foundGateway.breakNaturally();
                                                        this.getServer().getScheduler().runTaskLater(this, () -> {
                                                            foundGateway.setType(Material.END_GATEWAY);
                                                            Object tileEnt2 = null;

                                                            try {
                                                                try {
                                                                    DragonSlayer.getTileEntity.setAccessible(true);
                                                                    tileEnt2 = DragonSlayer.getTileEntity.invoke(worldServer, blockPos,
                                                                            false);
                                                                } catch (final IllegalArgumentException var7) {
                                                                    tileEnt2 = DragonSlayer.getTileEntity.invoke(worldServer, blockPos);
                                                                }

                                                                if (tileEnt2 != null) {
                                                                    tileEnt2.getClass()
                                                                            .getDeclaredMethod("loadAdditional", nbt_1.getClass())
                                                                            .invoke(tileEnt2, nbt_1);
                                                                }
                                                            } catch (final Exception var8) {
                                                                if (this.configManager.debugOn()) {
                                                                    this.logger.warning("Can not handle TileEntity/NBT recreate)");
                                                                    var8.printStackTrace();
                                                                }
                                                            }

                                                        }, 40L);
                                                    }
                                                } catch (final Exception var13) {
                                                    if (this.configManager.debugOn()) {
                                                        this.logger.warning("Can not handle TileEntity/NBT...)");
                                                        var13.printStackTrace();
                                                    }
                                                }
                                            }

                                        }, 0L);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (final Exception e) {

        }
    }

    private void syncTP(final EnderDragon dragon, final Location target) {
        this.getServer().getScheduler().runTaskLater(this, () -> dragon.teleport(target), 0L);
    }

    static Block CheckGatewaysForDragon(final World world, final Location DragLoc, final int distance) {
        for (final Location TestLoc : DragonSlayer.Endgateways) {
            if (TestLoc.getWorld() == world) {
                final Block testBlock = TestLoc.getBlock();
                if (testBlock != null && testBlock.getChunk().isLoaded() && testBlock.getType() == Material.END_GATEWAY
                        && DragLoc.distance(TestLoc) < (double) distance) {
                    return testBlock;
                }
            }
        }

        return null;
    }

    static Location getClosestGateway(final World world, final Location location) {
        double distance = -1.0D;
        Location returnLoc = null;

        for (final Location checkLoc : DragonSlayer.Endgateways) {
            if (checkLoc.getWorld() == world) {
                final Block testBlock = checkLoc.getBlock();
                if (testBlock != null && testBlock.getChunk().isLoaded() && testBlock.getType() == Material.END_GATEWAY) {
                    final double dist_ = location.distance(checkLoc);
                    if (distance == -1.0D || dist_ < distance) {
                        distance = dist_;
                        returnLoc = checkLoc;
                    }
                }
            }
        }

        return returnLoc;
    }

    Object getCraftWorld(final World world) {
        try {
            if (DragonSlayer.CraftWorldClass == null) {
                DragonSlayer.CraftWorldClass = (Class<?>) Class.forName("org.bukkit.craftbukkit.CraftWorld");
            }

            if (DragonSlayer.CraftWorldClass.isInstance(world)) {
                return DragonSlayer.CraftWorldClass.cast(world);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException | ClassNotFoundException var3) {
            if (this.configManager.debugOn()) {
                var3.printStackTrace();
            }
        }

        return null;
    }

    /** {@link net.minecraft.server.level.ServerLevel} */
    Object getWorldServer(final World world) {
        try {
            final Object castClass = this.getCraftWorld(world);
            return DragonSlayer.CraftWorldClass.getDeclaredMethod("getHandle").invoke(castClass);
        } catch (NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException var3) {
            if (this.configManager.debugOn()) {
                var3.printStackTrace();
            }

            return null;
        }
    }

    /** {@link net.minecraft.server.MinecraftServer} */
    Object getMinecraftServer(final World world) {
        final Object worldServer = this.getWorldServer(world);

        try {
            return this.getMethodByReturntype(worldServer.getClass(), "MinecraftServer", (Class<?>[]) null).invoke(worldServer);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException var4) {
            return null;
        }
    }

    void setDragonPosMeta(final Entity dragon, final Location location) {
        final MetadataValue MdV_DragonLocation = new FixedMetadataValue(this, location);
        dragon.setMetadata("DSL-Location", MdV_DragonLocation);
    }

    static Location getDragonPosMeta(final Entity dragon) {
        Location location = null;
        if (dragon.hasMetadata("DSL-Location")) {
            final List<MetadataValue> list = dragon.getMetadata("DSL-Location");
            if (list != null && list.size() != 0) {
                try {
                    location = (Location) ((MetadataValue) list.get(0)).value();
                } catch (final Exception var4) {
                    if (DragonSlayer.debugOn) {
                        Bukkit.getLogger().warning("'DSL-Location' not found in Dragon's Metadata");
                    }
                }
            }
        }

        return location;
    }

    public static DragonSlayer getInstance() { return DragonSlayer.instance; }

    void setDragonIDMeta(final EnderDragon dragon, final int dragonId) {
        final MetadataValue MdV_DragonID = new FixedMetadataValue(this, dragonId);
        dragon.setMetadata("DSL-dragID", MdV_DragonID);
        if (DragonSlayer.dragonId != null && dragonId >= 0
                && !dragon.getPersistentDataContainer().has((NamespacedKey) DragonSlayer.dragonId, PersistentDataType.INTEGER)) {
            dragon.getPersistentDataContainer().set((NamespacedKey) DragonSlayer.dragonId, PersistentDataType.INTEGER, dragonId);
        }

    }

    int getDragonIDMeta(final EnderDragon dragon) {
        int dragonId = -1;
        if (DragonSlayer.dragonId != null
                && dragon.getPersistentDataContainer().has((NamespacedKey) DragonSlayer.dragonId, PersistentDataType.INTEGER)) {
            dragonId = dragon.getPersistentDataContainer().get((NamespacedKey) DragonSlayer.dragonId, PersistentDataType.INTEGER);
            if (!dragon.hasMetadata("DSL-Location") && dragonId >= 0) {
                this.setDragonPosMeta(dragon, dragon.getEyeLocation());
            }

            return dragonId;
        } else {
            if (dragon.hasMetadata("DSL-dragID")) {
                final List<MetadataValue> list = dragon.getMetadata("DSL-dragID");
                if (list != null && list.size() != 0) {
                    try {
                        dragonId = (int) ((MetadataValue) list.get(0)).value();
                    } catch (final Exception var5) {
                    }
                }
            } else if (dragon.isCustomNameVisible()) {
                final String world = dragon.getWorld().getName().toLowerCase();
                dragonId = this.findDragonID(world, dragon.getCustomName());
                this.setDragonIDMeta(dragon, dragonId);
                if (dragonId >= 0) {
                    this.setDragonPosMeta(dragon, dragon.getEyeLocation());
                }
            }

            return dragonId;
        }
    }

    @SuppressWarnings("unchecked")
    void setDragonDamageMeta(final EnderDragon dragon, final Player player, double damage) {
        final String w = dragon.getWorld().getName().toLowerCase();
        if (this.checkWorld(w)) {
            HashMap<Player, Double> DragonMeta = new HashMap<Player, Double>();
            DragonMeta.put(player, damage);
            if (dragon.hasMetadata("DSL-Damage")) {
                final List<MetadataValue> list = dragon.getMetadata("DSL-Damage");
                if (list != null && list.size() != 0) {
                    try {
                        DragonMeta = (HashMap<Player, Double>) ((MetadataValue) list.get(0)).value();
                        final double oldDamage = DragonMeta.get(player) != null ? DragonMeta.get(player) : 0.0D;
                        damage += oldDamage;
                        DragonMeta.put(player, damage);
                    } catch (final Exception var10) {
                    }
                }
            }

            final MetadataValue MdV_DragonDamage = new FixedMetadataValue(this, DragonMeta);
            dragon.setMetadata("DSL-Damage", MdV_DragonDamage);
        }

    }

    private void StartSecondRepeatingTimer() {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (final String mapname : this.configManager.getMaplist()) {
                this.addTimerDisplay(mapname);
                this.updateTimerDisplay(mapname);
                if (!this.healTickCounterList.containsKey(mapname)) {
                    this.healTickCounterList.put(mapname, 0);
                }

                final int secs = this.configManager.getRegenSecs(mapname);
                final int healAmount = this.configManager.getRegenAmount(mapname);
                final int mapCounter = this.healTickCounterList.get(mapname);
                final boolean startHeal = secs > 0 ? mapCounter >= secs : false;
                if (startHeal) {
                    final World thisWorld = this.getDragonWorldFromString(mapname);
                    if (thisWorld != null) {
                        final Collection<EnderDragon> dragons = thisWorld.getEntitiesByClass(EnderDragon.class);
                        if (dragons != null) {
                            for (final EnderDragon dragon : dragons) {
                                if (dragon.isValid() && this.checkDSLDragon(dragon)) {
                                    double maxHealth;

                                    maxHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

                                    final double health = dragon.getHealth();
                                    if (health < maxHealth) {
                                        final double newHealth = health + (double) healAmount;
                                        dragon.setHealth(newHealth > maxHealth ? maxHealth : newHealth);
                                        this.setBossBarAmount(dragon);
                                    }
                                }
                            }
                        }
                    }
                }

                this.healTickCounterList.put(mapname, startHeal ? 0 : mapCounter + 1);
            }

            switch (DragonSlayer.repeatingCounter) {
            case 2:
                DragonSlayer.cleanupDragons();

                for (final String Mapname : this.configManager.getMaplist()) {
                    final World world = this.getServer().getWorld(Mapname);
                    if (world != null) {
                        this.handleBossbar(world);
                    }
                }
                break;
            case 3:
            case 7:
            case 11:
                this.checkForSleepingDragons();
                break;
            case 15:
                this.checkForSleepingDragons();
                DragonSlayer.repeatingCounter = 0;
            }

            ++DragonSlayer.repeatingCounter;
            if (++DragonSlayer.tabListTime > this.configManager.getTabListTime()) {
                DragonSlayer.tabListTime = 1;

                for (final Player p : this.getServer().getOnlinePlayers()) {
                    this.setTabListName(p);
                }
            }

        }, 600L, 20L);
    }

    String[] getWorldsNextSpawnsOrReset(final String worldName, final boolean spawn, final boolean reset) {
        long checkTimer = -1L;
        if (spawn) {
            checkTimer = this.getNextRespawn(worldName);
        }

        if (reset) {
            checkTimer = this.getResetTime(worldName);
        }

        if (checkTimer >= 0L) {
            checkTimer /= 20L;
            final int rest = (int) checkTimer % 86400;
            final int days = (int) checkTimer / 86400;
            final int hours = rest / 3600;
            final int minutes = rest / 60 - hours * 60;
            final int seconds = rest % 60;
            return String.format("%d,%02d,%02d,%02d", days, hours, minutes, seconds).split(",");
        } else {
            return null;
        }
    }

    private void addTimerDisplay(final String worldName) {
        if (!DragonSlayer.timerDisplays.containsKey(worldName)) {
            final Scoreboard timerDisplay = this.getServer().getScoreboardManager().getNewScoreboard();
            this.setDisplayBasics(timerDisplay);
            DragonSlayer.timerDisplays.put(worldName, timerDisplay);
            this.setTimerdisplayForWorld(worldName, timerDisplay);
        }
    }

    void setDisplayBasics(final Scoreboard timerDisplay) {
        Objective ScoreObj = timerDisplay.getObjective("DSL");
        if (ScoreObj == null) {
            try {
                final Criteria dslTimer = Criteria.create("DSLTimer");
                ScoreObj = timerDisplay.registerNewObjective("DSL", dslTimer, ChatColor.GREEN + "Next Spawn Time");
            } catch (NoSuchMethodError | NoClassDefFoundError var6) {
                try {
                    ScoreObj = timerDisplay.registerNewObjective("DSL", "DSLTimer", ChatColor.GREEN + "Next Spawn Time");
                } catch (final NoSuchMethodError var5) {
                    ScoreObj = timerDisplay.registerNewObjective("DSL", "DSLTimer");
                    ScoreObj.setDisplayName(ChatColor.GREEN + "Next Spawn Time");
                }
            }
        }

        ScoreObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Team team_spawn = timerDisplay.getTeam("T-Spawn");
        if (team_spawn == null) {
            team_spawn = timerDisplay.registerNewTeam("T-Spawn");
        }

        Team team_reset = timerDisplay.getTeam("T-Reset");
        if (team_reset == null) {
            team_reset = timerDisplay.registerNewTeam("T-Reset");
        }

        if (!team_spawn.hasEntry(DragonSlayer.ScoreBoardName_TimerDisplay)) {
            team_spawn.addEntry(DragonSlayer.ScoreBoardName_TimerDisplay);
        }

        if (!team_reset.hasEntry(DragonSlayer.ScoreBoardName_ResetTimer)) {
            team_reset.addEntry(DragonSlayer.ScoreBoardName_ResetTimer);
        }

    }

    private void updateTimerDisplay(final String worldName) {
        final World thisWorld_ = this.getDragonWorldFromString(worldName);
        if (thisWorld_ != null) {
            if (DragonSlayer.timerDisplays.containsKey(worldName)) {
                final Scoreboard sb = (Scoreboard) DragonSlayer.timerDisplays.get(worldName);
                Objective ScoreObj = sb.getObjective("DSL");
                final int timerFunc = this.configManager.getTimerfunc(worldName);
                final String[] times = this.getWorldsNextSpawnsOrReset(worldName, true, false);
                final String[] resTimes = this.getWorldsNextSpawnsOrReset(worldName, false, true);
                if ((times != null || resTimes != null) && timerFunc > 0) {
                    if (ScoreObj == null) {
                        this.setDisplayBasics(sb);
                        ScoreObj = sb.getObjective("DSL");
                        this.setTimerdisplayForWorld(worldName, sb);
                    }

                    final Team team_spawn = sb.getTeam("T-Spawn");
                    final Team team_reset = sb.getTeam("T-Reset");
                    Score fakePlayScore1 = null;
                    Score fakePlayScore2 = null;
                    String seconds1 = "";
                    String seconds2 = "";
                    String spawnsuffix = "";
                    String resetsuffix = "";
                    final int blahfixlength = 64;
                    boolean titleset = false;
                    final boolean showCount = timerFunc == 2;
                    if (times != null) {
                        fakePlayScore1 = ScoreObj.getScore(DragonSlayer.ScoreBoardName_TimerDisplay);
                        final String days = times[0];
                        final String hours = times[1];
                        final String minutes = times[2];
                        seconds1 = times[3];
                        String timerline = this.configManager.getTimerline().replace('&', '§');
                        if (!timerline.trim().isEmpty()) {
                            timerline = ChatColor.RED + timerline.replace("$days", days).replace("$hours", hours)
                                    .replace("$minutes", minutes).replace("$seconds", seconds1);
                        } else if (showCount) {
                            timerline = ChatColor.RED + "Days:" + String.format("%02d", Integer.valueOf(days)) + " " + hours + ":" + minutes
                                    + ":" + ChatColor.RED + seconds1 + "  #:";
                        } else {
                            timerline = ChatColor.RED + "D:" + String.format("%02d", Integer.valueOf(days)) + " H:" + hours + " M:"
                                    + minutes + " " + ChatColor.RED + "S:";
                        }

                        if (timerline.length() > blahfixlength) {
                            if (timerline.length() > blahfixlength * 2) {
                                timerline = timerline.substring(0, blahfixlength * 2);
                            }

                            spawnsuffix = timerline.substring(blahfixlength);
                            timerline = timerline.substring(0, blahfixlength);
                        }

                        if (!DragonSlayer.TabListPlugin) {
                            team_spawn.setPrefix(timerline);
                            team_spawn.setSuffix(spawnsuffix);
                        } else {
                            sb.getEntries().stream().forEach(str -> sb.resetScores(str));
                            fakePlayScore1 = ScoreObj.getScore(DragonSlayer.ScoreBoardName_TimerDisplay + timerline + spawnsuffix);
                        }

                        ScoreObj.setDisplayName(ChatColor.GREEN + this.configManager.getTimertext().replace("$days", String.valueOf(days))
                                .replace("$hours", String.valueOf(hours)).replace("$minutes", String.valueOf(minutes))
                                .replace("$seconds", String.valueOf(seconds1)).replace('&', '§'));
                        titleset = true;
                    }

                    if (resTimes != null) {
                        fakePlayScore2 = ScoreObj.getScore(DragonSlayer.ScoreBoardName_ResetTimer);
                        final String days = resTimes[0];
                        final String hours = resTimes[1];
                        final String minutes = resTimes[2];
                        seconds2 = resTimes[3];
                        String resetline = this.configManager.getResetline().replace('&', '§');
                        if (!resetline.trim().isEmpty()) {
                            resetline = ChatColor.RED + resetline.replace("$days", days).replace("$hours", hours)
                                    .replace("$minutes", minutes).replace("$seconds", seconds2);
                        } else if (showCount) {
                            resetline = ChatColor.RED + "Days:" + String.format("%02d", Integer.valueOf(days)) + " " + hours + ":" + minutes
                                    + ":" + ChatColor.RED + seconds2 + "  #:";
                        } else {
                            resetline = ChatColor.RED + "D:" + String.format("%02d", Integer.valueOf(days)) + " H:" + hours + " M:"
                                    + minutes + " " + ChatColor.RED + "S:";
                        }

                        if (resetline.length() > blahfixlength) {
                            if (resetline.length() > blahfixlength * 2) {
                                resetline = resetline.substring(0, blahfixlength * 2);
                            }

                            resetsuffix = resetline.substring(blahfixlength);
                            resetline = resetline.substring(0, blahfixlength);
                        }

                        if (!DragonSlayer.TabListPlugin) {
                            team_reset.setPrefix(resetline);
                            team_reset.setSuffix(resetsuffix);
                        } else {
                            fakePlayScore2 = ScoreObj.getScore(DragonSlayer.ScoreBoardName_TimerDisplay + resetline + resetsuffix);
                        }

                        if (!titleset) {
                            ScoreObj.setDisplayName(
                                    ChatColor.GREEN + this.configManager.getTimertext().replace("$days", String.valueOf(days))
                                            .replace("$hours", String.valueOf(hours)).replace("$minutes", String.valueOf(minutes))
                                            .replace("$seconds", String.valueOf(seconds2)).replace('&', '§'));
                        }
                    }

                    if (fakePlayScore1 != null) {
                        fakePlayScore1.setScore(showCount ? this.countRespawnTimers(worldName) : Integer.valueOf(seconds1));
                    }

                    if (fakePlayScore2 != null) {
                        fakePlayScore2.setScore(showCount ? 0 : Integer.valueOf(seconds2));
                    } else {
                        sb.resetScores(DragonSlayer.ScoreBoardName_ResetTimer);
                    }
                } else if (ScoreObj != null) {
                    ScoreObj.setDisplayName(ChatColor.GREEN + "No Spawntimer");
                    sb.getEntries().stream().forEach(str -> sb.resetScores(str));

                    ScoreObj.unregister();

                }

                if (this.getStatueVersion() >= 2 && DragonSlayer.ProtocolLibEnabled) {
                    final World NPCStatue_World = this.getArmorstandWorld();
                    if (NPCStatue_World != null && thisWorld_ != null && thisWorld_.equals(NPCStatue_World)) {
                        Team team2_NPC = sb.getTeam("DSL-NPCs");
                        if (team2_NPC == null) {
                            team2_NPC = sb.registerNewTeam("DSL-NPCs");
                        }

                        if (team2_NPC.getEntries().isEmpty()) {
                            team2_NPC.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);

                            if (DragonSlayer.protLibHandler.team_NPC == null) {
                                DragonSlayer.protLibHandler.team_NPC = this.getServer().getScoreboardManager().getMainScoreboard()
                                        .getTeam("DSL-NPCs");
                            }

                            for (final String ent : DragonSlayer.protLibHandler.team_NPC.getEntries()) {
                                if (!team2_NPC.hasEntry(ent)) {
                                    team2_NPC.addEntry(ent);
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    void setTimerdisplayToPlayer(final Player player) {
        final String thisWorld = player.getWorld().getName().toLowerCase();
        if (this.checkWorld(thisWorld)) {
            if (this.configManager.getTimerfunc(thisWorld) != 0) {
                final Scoreboard timerSB = (Scoreboard) DragonSlayer.timerDisplays.get(thisWorld);
                if (timerSB != null && timerSB.getObjective("DSL") != null) {
                    player.setScoreboard(timerSB);
                }

            }
        }
    }

    void delTimerdisplayFromPlayer(final Player player) {
        final Scoreboard timerSB = player.getScoreboard();
        if (timerSB != null && timerSB.getObjective("DSL") != null) {
            player.setScoreboard(this.getServer().getScoreboardManager().getMainScoreboard());
        }

    }

    void setTimerdisplayForWorld(final String worldName, final Scoreboard timerDisplay) {
        if (this.configManager.getTimerfunc(worldName) != 0) {
            final World world = Bukkit.getWorld(worldName);
            if (world != null) {
                for (final Player player : world.getPlayers()) {
                    player.setScoreboard(timerDisplay);
                }
            }

        }
    }

    private Team addTeamToPlayersScoreBoard(final Player p, final String teamName, final ChatColor color) {
        this.cleanupGlowTeamList();
        final Scoreboard board = p.getScoreboard();
        Team t = board.getTeam(teamName);
        if (t == null) {
            t = board.registerNewTeam(teamName);

            t.setColor(color);

        }

        if (!DragonSlayer.TeamList.contains(t)) {
            DragonSlayer.TeamList.add(t);
        }

        return t;
    }

    void handleGlowTeams(final World world, final int dragonId, final String uuid) {
        final String DragonColor = this.configManager.getGlowColor(world.getName().toLowerCase(), dragonId);
        final ChatColor color = ChatColor.valueOf(DragonColor) != null ? ChatColor.valueOf(DragonColor) : ChatColor.DARK_AQUA;
        final String teamName = "DSL_" + color.name();

        for (final Player p : world.getPlayers()) {
            final Team t = this.addTeamToPlayersScoreBoard(p, teamName, color);
            if (!t.hasEntry(uuid)) {
                t.addEntry(uuid);
            }
        }

    }

    void cleanupGlowTeamList() {

        try {
            final ArrayList<String> tempList = new ArrayList<String>();

            for (final Team t : DragonSlayer.TeamList) {
                for (final String uuid : t.getEntries()) {
                    final Entity dragEnt_ = this.getServer().getEntity(UUID.fromString(uuid));
                    if (dragEnt_ == null || !dragEnt_.isValid()) {
                        tempList.add(uuid);
                    }
                }

                if (!tempList.isEmpty()) {
                    tempList.stream().forEach(uid -> t.removeEntry(uid));
                    tempList.clear();
                }

                if (t.getSize() == 0) {
                    DragonSlayer.TeamList.remove(t);
                    t.unregister();
                }
            }
        } catch (final ConcurrentModificationException var7) {
        }

    }
}
