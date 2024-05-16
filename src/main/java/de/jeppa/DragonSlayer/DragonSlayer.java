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
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftWorld;
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

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.onarandombox.MultiverseCore.MultiverseCore;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;

// @SuppressWarnings("deprecation")
@SuppressWarnings({ "unused", "deprecation" })
public class DragonSlayer extends JavaPlugin {
    static final String Copyright = "Copyright by Jeppa (A.F.)! You are NOT allowed to decompile this plugin and/or use any parts of its code !!!";
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
    private HashMap<String, Integer> healTickCounterList = new HashMap<String, Integer>();
    private static Method db_a = null;

    private static Method db_e = null;
    private static Method db_d = null;
    private static Method endgatewayMethod = null;
    private static Field OrigGateways = null;
    private static Field DragonKilled = null;
    private static Field DragonPreviouslyKilled = null;
    private static Field DragonUUID = null;
    private static Field PortLoc_f = null;
    private static Field naviField = null;
    private static Method fillArray = null;
    private static Method getEDBMethod = null;
    private static Class<org.bukkit.craftbukkit.CraftWorld> CraftWorldClass = null;
    private static Class<org.bukkit.craftbukkit.entity.CraftEnderDragon> CraftEnderDragonClass = null;
    private static Field CrystalAmount_f = null;
    private static Object dId = null;
    private static Constructor<?> newBlockPosition = null;
    private static Constructor<?> newPathPoint = null;
    private static Method pp_geta_func = null;
    private static Field pp_geta_x = null;
    private static Field pp_getb_y = null;
    private static Field pp_getc_z = null;
    private static Method bbp_getX = null;
    private static Method bbp_getY = null;
    private static Method bbp_getZ = null;
    private static String mapVers = null;
    static final double Pi = Math.PI;
    static final double ZwPi = 0.15707963267948966D;
    private Method phase_control_manager_method = null;
    private Method getPathPointFromPathEnt_method = null;
    private Field pathEntList = null;
    private Field respawnPhase_f = null;
    static Method getTileEntity = null;
    static Method saveNBT = null;
    static int RunCounter = 0;
    static int RunCounter2 = 0;
    private static String ScoreBoardName_1 = "" + ChatColor.BLACK + ChatColor.WHITE;
    private static String ScoreBoardName_2 = "" + ChatColor.BLUE + ChatColor.WHITE;
    private static ArrayList<Team> TeamList = new ArrayList<Team>();

    public void onEnable() {
        this.configManager.loadConfiguration();
        this.configManager.checkOldConfig();
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
            spigot = true;
        } catch (ClassNotFoundException | NoClassDefFoundError var8) {
        }

        try {
            newRoutines14 = new FourteenPlusOnlyRoutines(this);
        } catch (Exception var7) {
        }

        this.getServer().getPluginManager().registerEvents(this.playerListener, this);

        if (LegendChatenabled) {
            this.getServer().getPluginManager().registerEvents(this.dragonLChatListener, this);
        }

        if (PAPIenabled) {
            String ver = this.getServer().getPluginManager().getPlugin("PlaceholderAPI").getPluginMeta().getVersion();
            String vers2 = "";

            String[] var6;
            for (String split : var6 = ver.split("\\.")) {
                vers2 = vers2 + (split.length() == 2 ? split : "0" + split);
            }

            if (vers2.contains("-")) {
                vers2 = vers2.substring(0, vers2.indexOf("-"));
            }

            int version2 = Integer.parseInt(vers2);
            if (version2 <= 21006) {
                (new DragonPlaceholderAPIold(this)).register();
            } else {
                (new DragonPlaceholderAPI(this)).register();
            }
        }

        if (ProtocolLibEnabled) {
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

        for (String DragonWorld : this.configManager.getMaplist()) {
            World MyWorld = this.getDragonWorldFromString(DragonWorld);
            this.activateChunksAroundPosition(new Location(MyWorld, 0.0D, 75.0D, 0.0D), MyWorld, 7, false);
        }

        if (UCenabled) {
            this.logger.info("UChat found, tag will be used!");
        }

        if (LegendChatenabled) {
            this.logger.info("LegendChat found, tag will be used!");
        }

        if (PAPIenabled) {
            this.logger.info("PlaceholderAPI found, will be used!");
        }

        if (this.configManager.getVerbosity() && EssentialsEnabled) {
            this.logger.info("Essentials found, will be used!");
        }

        if (this.configManager.getVerbosity() && EssChEnabled) {
            this.logger.info("EssentialsChat found, will be used!");
        }

        if (TabListPlugin) {
            Plugin tl = this.getServer().getPluginManager().getPlugin("TabList");
            if (tl.getPluginMeta().getMainClass().contains("montlikadani")) {
                String[] vers1 = tl.getPluginMeta().getVersion().split("\\.");
                if (vers1.length >= 3) {
                    if (Integer.valueOf(String.format("%03d%03d%03d", Integer.valueOf(vers1[0]), Integer.valueOf(vers1[1]),
                            Integer.valueOf(vers1[2]))) <= 5007004) {
                        if (this.configManager.getVerbosity()) {
                            this.logger.info("Plugin 'TabList' <= 5.7.4 found, using fallback mode for Timer-Scoreboard !");
                        }
                    } else {
                        TabListPlugin = false;
                    }
                }
            } else {
                TabListPlugin = false;
            }
        }

        serverStarted = !this.configManager.getMultiPortal();
        this.countEndGatewaysAndContinue();
        this.setTestPortal();
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.configManager.setDragonDefaults();
            this.timerManager.getTimerlist();
            this.timerManager.RestartTimers();
            this.leaderManager.getLeaderlist();
            this.leaderManager.sortLeaderList();
            this.leaderManager.saveLeaderlist();
            if (ProtocolLibEnabled) {
                this.getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
                    if (this.getStatueVersion() >= 2) {
                        protLibHandler.getNewTextureArray((Player) null, true, true);
                    }

                }, 0L);
            }

            this.getServer().getScheduler().runTaskLater(this, () -> this.resetArmorStand(), 30L);
            this.StartRepeatingTimer();
            this.StartSecondRepeatingTimer();
            for (String DragonWorld : this.configManager.getMaplist()) {
                this.getDragonCount(DragonWorld);
            }

        }, 20L);
        dId = new NamespacedKey(this, "DragonID");

    }

    public void onDisable() {
        this.timerManager.getTimerlist();
        for (String DragonWorld : this.configManager.getMaplist()) {
            World world = this.getDragonWorldFromString(DragonWorld);

            try {
                boolean pendingRefresh = this.isRefreshRunning(world);
                long resetTimerNotRunning = this.getResetTime(DragonWorld);
                long timerValue = 60L;
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
            } catch (Exception var9) {
            }
        }

        this.timerManager.updateTimerList();
        this.timerManager.saveTimerlist();
        this.leaderManager.saveLeaderlist();
        this.saveConfig();
    }

    private void setupDependPlugins() {
        UCenabled = this.checkPlugin("UltimateChat");
        LegendChatenabled = this.checkPlugin("Legendchat");
        PAPIenabled = this.checkPlugin("PlaceholderAPI");
        EssentialsEnabled = this.checkPlugin("Essentials");
        EssChEnabled = this.checkPlugin("EssentialsChat");
        SkinsRestorerEnabled = this.checkPlugin("SkinsRestorer");
        TabListPlugin = this.checkPlugin("TabList");
    }

    private boolean checkPlugin(String pluginName) { return this.getServer().getPluginManager().getPlugin(pluginName) != null; }

    private void Protocollib() {
        if (this.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                protLibHandler = new ProtLibHandler(this);
                ProtocolLibEnabled = true;
            } catch (Exception var2) {
            }
        }

    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                econ = (Economy) rsp.getProvider();
                return true;
            }
        }
    }

    boolean checkServerStarted() { return serverStarted; }

    private int countRespawnTimers(String ThisWorld) {
        int Count = 0;

        for (DragonRespawn Resp : this.timerManager.RespawnList) {
            if (Resp.Mapname.toLowerCase().equals(ThisWorld)) {
                ++Count;
            }
        }

        return Count;
    }

    public int missingDragons(String ThisWorld) {
        int MaxDragons = this.configManager.getMaxdragons(ThisWorld);
        int Count = this.getDragonCount(ThisWorld);
        int runningTimers = this.countRespawnTimers(ThisWorld);
        return runningTimers + Count < MaxDragons ? MaxDragons - (runningTimers + Count) : 0;
    }

    private long getNextRespawn(String ThisWorld) {
        long Resttime = -1L;

        for (DragonRespawn Resp : this.timerManager.RespawnList) {
            if (Resp.Mapname.equals(ThisWorld)) {
                Long check = this.remainingTimerDuration(Resp);
                if (check != null && (check < Resttime || Resttime == -1L)) {
                    Resttime = check;
                }
            }
        }

        return Resttime;
    }

    Long remainingTimerDuration(DragonRespawn Resp) {
        long Runtime = System.currentTimeMillis() / 50L - Resp.StartTime;
        long Resttime = Resp.OrigRuntime - Runtime;
        return Resttime > 0L ? Resttime : null;
    }

    private long getResetTime(String ThisWorld) {
        long Resttime = -1L;

        for (WorldRefreshOrReset Res : ResetimerList) {
            if (Res.Mapname.equals(ThisWorld)) {
                Long check = this.remainingResetDuration(Res);
                if (check != null && (check < Resttime || Resttime == -1L)) {
                    Resttime = check;
                }
            }
        }

        return Resttime;
    }

    Long remainingResetDuration(WorldRefreshOrReset Res) {
        long Runtime = System.currentTimeMillis() / 50L - Res.StartTime;
        long Resttime = Res.OrigRuntime - Runtime;
        return Resttime > 0L ? Resttime : null;
    }

    void stopResetTimer(String ThisWorld) {
        List<WorldRefreshOrReset> killList = new ArrayList<WorldRefreshOrReset>();

        for (WorldRefreshOrReset Res_ : ResetimerList) {
            if (Res_.Mapname.toLowerCase().equals(ThisWorld)) {
                this.getServer().getScheduler().cancelTask(Res_.taskId);
                killList.add(Res_);
            }
        }

        for (WorldRefreshOrReset Res_ : killList) {
            this.getServer().getScheduler().cancelTask(Res_.taskId);
            ResetimerList.remove(Res_);
            Res_ = null;
        }

        killList.clear();
    }

    public void StartWorldResetTimer(String Mapname, long Runtime, long Warntime) {
        WorldRefreshOrReset Res = this.createWorldResetTimer(Mapname, Runtime, Warntime);
        if (Runtime < Warntime) {
            Res.Warntime = Runtime;
            Runtime = 0L;
        } else {
            Runtime -= Warntime;
        }

        Res.taskId = this.getServer().getScheduler().runTaskLater(this, Res, Runtime).getTaskId();
    }

    private WorldRefreshOrReset createWorldResetTimer(String Mapname, long Runtime, long Warntime) {
        WorldRefreshOrReset Res = new WorldRefreshOrReset(this);
        Res.Mapname = Mapname;
        Res.OrigRuntime = Runtime;
        Res.Warntime = Warntime;
        return Res;
    }

    public HashMap<Double, Player> sortDamagersRanks(HashMap<Double, Player> orderList) {
        List<Double> percentList = new ArrayList<Double>(orderList.keySet());
        Collections.sort(percentList);
        Collections.reverse(percentList);
        HashMap<Double, Player> newOrderList = new HashMap<Double, Player>();
        double r = 1.0D;

        for (Double val : percentList) {
            newOrderList.put(r++, (Player) orderList.get(val));
        }

        return newOrderList;
    }

    Integer getPlayerCount(String Mapname) {
        World MyWorld = this.getDragonWorldFromString(Mapname);
        return MyWorld == null ? 0 : MyWorld.getPlayers().size();
    }

    Integer getDragonCount(String Mapname) {
        World MyWorld = this.getDragonWorldFromString(Mapname);
        int Counter = 0;
        if (MyWorld == null) {
            return 0;
        } else {
            Collection<EnderDragon> dragons = this.getDragonList(MyWorld, Mapname);
            Counter = dragons.size();

            for (EnderDragon dr : dragons) {
                if (!dr.isValid() || dr.isDead() || dr.getPhase() == Phase.DYING || !this.checkDSLDragon(dr)) {
                    --Counter;
                }
            }

            return Counter;
        }
    }

    public Collection<EnderDragon> getDragonList(World myWorld, String mapname) {
        if (this.checkWorld(mapname)) {
            Location DragSpawnPos = findPosForPortal((double) this.configManager.getPortalXdef(mapname),
                    (double) this.configManager.getPortalZdef(mapname), myWorld, Material.BEDROCK);
            this.activateChunksAroundPosition(DragSpawnPos, myWorld, 12, false);

            for (int i = 1; i <= this.configManager.getMaxdragons(mapname); ++i) {
                Location PortLoc = findPosForPortal((double) this.configManager.getPortalX(mapname, i, true, true),
                        (double) this.configManager.getPortalZ(mapname, i, true, true), myWorld, Material.BEDROCK);
                if (!DragSpawnPos.equals(PortLoc)) {
                    this.activateChunksAroundPosition(PortLoc, myWorld, 6, false);
                }
            }
        }

        return myWorld.getEntitiesByClass(EnderDragon.class);
    }

    void activateChunksAroundPosition(Location StartPos, World World, int Radius, boolean forceTicket) {
        if (World != null) {
            int baseX = (int) (StartPos.getX() / 16.0D);
            int baseZ = (int) (StartPos.getZ() / 16.0D);

            for (int x = -1 * Radius; x <= Radius; ++x) {
                int ChunkX = baseX + x;

                for (int z = -1 * Radius; z <= Radius; ++z) {
                    int ChunkZ = baseZ + z;
                    if ((this.configManager.keepChunksLoaded() || forceTicket) && (World.isChunkGenerated(ChunkX, ChunkZ))) {
                        try {
                            World.addPluginChunkTicket(ChunkX, ChunkZ, this);
                        } catch (NoSuchMethodError var17) {
                            try {
                                World.setChunkForceLoaded(ChunkX, ChunkZ, true);
                            } catch (NoSuchMethodError var16) {
                            }
                        }
                    }

                    Chunk testChunk = World.getChunkAt(ChunkX, ChunkZ);
                    if (!World.isChunkLoaded(ChunkX, ChunkZ)) {
                        boolean load;
                        try {
                            load = World.loadChunk(ChunkX, ChunkZ, true);
                        } catch (RuntimeException var15) {
                            load = false;
                        }

                        if (!load && this.configManager.getVerbosity()) {
                            this.logger.warning("Failed to load and activate Chunk at X: " + ChunkX * 16 + " Z: " + ChunkZ * 16 + " in "
                                    + World.getName());
                        }
                    }

                    try {
                        testChunk.getEntities();
                    } catch (Exception var14) {
                    }
                }
            }

        }
    }

    public void RemoveDragons(World ThisWorld, boolean deadOnly, boolean forceAll) {
        String Mapname = ThisWorld.getName().toLowerCase();

        for (EnderDragon Dragon : this.getDragonList(ThisWorld, Mapname)) {
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

    boolean SpawnForceDragon(String w) {
        if (this.checkWorld(w)) {
            int ExistentDragons = this.getDragonCount(w);
            int maxDragons = this.configManager.getMaxdragons(w);
            if (ExistentDragons < maxDragons) {
                this.SpawnXDragons(maxDragons - ExistentDragons, w);
                return true;
            }
        }

        return false;
    }

    void SpawnForceAllDragons() {
        for (String DragonWorld : this.configManager.getMaplist()) {
            int ExistentDragons = this.getDragonCount(DragonWorld);
            int MaxDragons = this.configManager.getMaxdragons(DragonWorld);
            if (ExistentDragons < MaxDragons) {
                this.SpawnXDragons(MaxDragons - ExistentDragons, DragonWorld);
            }
        }

    }

    void SpawnXDragons(int x, String World) {
        for (int i = 0; i < x; ++i) {
            DragonRespawn Resp = new DragonRespawn(this);
            Resp.Mapname = World;
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

    World getDragonWorldFromString(String Mapname) { return Bukkit.getServer().getWorld(Mapname); }

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
            String topKiller = this.leaderManager.getUUIDforRank(1);
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

        if (ProtocolLibEnabled) {
            Player pl = p;
            this.getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
                if (this.getStatueVersion() >= 2) {
                    protLibHandler.getNewTextureArray(pl, false, true);
                }

            }, 0L);
        }

    }

    private void resetTabListName() {
        if (this.configManager.getTabListEnable()) {
            OfflinePlayer oldSlayer = this.getOfflineSlayer();
            if (oldSlayer != null && oldSlayer.isOnline()) {
                Player oldSlayer_ = (Player) oldSlayer;
                oldSlayer_.setPlayerListName(oldSlayer_.getDisplayName());
            }
        }

    }

    void setTabListName(Player p) {
        if (this.configManager.getTabListEnable()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                if (p.getUniqueId().toString().equals(this.getSlayerUUIDString())) {
                    p.setPlayerListName((!this.configManager.getPrefixAsSuffix() ? this.configManager.getPrefix() : "") + p.getDisplayName()
                            + (this.configManager.getPrefixAsSuffix() ? this.configManager.getPrefix() : ""));
                }

            }, 10L);
        }

    }

    String replaceValues(String s, String world, Integer id) {
        s = s.replace('&', '§');
        if (this.configManager.getSlayerPAPINick() != null) {
            s = s.replace("$slayername", this.configManager.getSlayerPAPINick());
        }

        if (this.getSlayer() != null) {
            s = s.replace("$slayer", this.getSlayer());
        }

        if (world != null) {
            String baseworld = world.replace("_the_end", "");
            int dragonID = id != null ? id : 0;
            s = s.replace("$world", world).replace("$baseworld", baseworld)
                    .replace("$dragon", this.configManager.getDragonDefaultName(world.toLowerCase(), dragonID) + "§r")
                    .replace("$reward", String.valueOf(this.configManager.getReward_double(world.toLowerCase(), dragonID)));
        } else {
            s = s.replace("$world", "-No World-").replace("$dragon", this.getConfig().getString("dragon._default.name") + "§r")
                    .replace("$reward", this.getConfig().getString("dragon._default.reward"));
        }

        return s;
    }

    String replaceValues(String s, String world) { return this.replaceValues(s, world, (Integer) null); }

    public boolean checkWorld(String world) { return this.configManager.getMaplist().contains(world.toLowerCase()); }

    boolean checkDSLDragon(EnderDragon dragon) {
        boolean checkDSLDragon = dragon.hasMetadata("DSL-Location");
        if (!checkDSLDragon) {
            checkDSLDragon = this.getDragonIDMeta(dragon) > 0;
        }

        return checkDSLDragon;
    }

    boolean checkOrigDragon(EnderDragon dragon) {
        Object newDragEnt = this.getEntityEnderDragon(dragon);
        return newDragEnt.getClass().getSimpleName().equals("EntityEnderDragon")
                && newDragEnt.getClass().getTypeName().contains("net.minecraft");
    }

    void FindPlayerAndAddToBossBar(BossBar BossBar, Entity ThisDrag) {
        List<Player> AddedPlayers = BossBar.getPlayers();
        int distancePlayer = this.configManager.getBossbarDistance(ThisDrag.getWorld().getName().toLowerCase());

        for (Player player : ThisDrag.getWorld().getPlayers()) {
            if (!AddedPlayers.contains(player)) {
                if (player.getLocation().distance(ThisDrag.getLocation()) < (double) distancePlayer) {
                    BossBar.addPlayer(player);
                }
            } else if (player.getLocation().distance(ThisDrag.getLocation()) >= (double) distancePlayer) {
                BossBar.removePlayer(player);
            }
        }

    }

    BossBar findFreeBar(String worldname) {
        for (BossBar BB : BossBars) {
            if (!BB.isVisible()) {
                BB.setVisible(true);
                this.setBBdark(BB, worldname);
                BB.removeAll();
                return BB;
            }
        }

        BossBar BossBar = Bukkit.getServer().createBossBar("EnderDragon", BarColor.PURPLE, BarStyle.SOLID,
                new BarFlag[] { BarFlag.PLAY_BOSS_MUSIC });
        this.setBBdark(BossBar, worldname);
        BossBar.setVisible(true);
        BossBars.add(BossBar);
        return BossBar;
    }

    private void setBBdark(BossBar BossBar, String worldname) {
        if (this.configManager.getDark(worldname)) {
            BossBar.addFlag(BarFlag.CREATE_FOG);
            BossBar.addFlag(BarFlag.DARKEN_SKY);
        } else {
            BossBar.removeFlag(BarFlag.CREATE_FOG);
            BossBar.removeFlag(BarFlag.DARKEN_SKY);
        }

    }

    void handleBossbar(World TheWorld) {
        for (EnderDragon ThisDrag : TheWorld.getEntitiesByClass(EnderDragon.class)) {
            if (this.checkDSLDragon(ThisDrag)) {
                BossBar BossBar = getBossBarFromDragon(ThisDrag);
                if (BossBar != null) {
                    this.FindPlayerAndAddToBossBar(BossBar, ThisDrag);
                    this.setBossBarAmountNOW(ThisDrag, BossBar);
                } else if (ThisDrag.isValid() && !ThisDrag.isDead() && ThisDrag.getPhase() != Phase.DYING) {
                    BossBar = this.findFreeBar(TheWorld.getName().toLowerCase());
                    if (BossBar != null) {
                        BossBar.setTitle(ThisDrag.getName());
                        this.setBossBarAmountNOW(ThisDrag, BossBar);
                        putBossBarToDragon(ThisDrag, BossBar);
                        this.FindPlayerAndAddToBossBar(BossBar, ThisDrag);
                    }
                }

                this.OrigEnderDragonSetKilled(ThisDrag);
                if (this.configManager.getGlowEffect(TheWorld.getName().toLowerCase())) {
                    this.handleGlowTeams(TheWorld, this.getDragonIDMeta(ThisDrag), ThisDrag.getUniqueId().toString());
                }
            }
        }

    }

    int findDragonID(String Mapname, String dragonName) {
        int maxD = this.configManager.getMaxdragons(Mapname);

        for (int i = 0; i <= maxD; ++i) {
            String TestName = this.getConfig().getString(i == 0 ? "dragon." + Mapname + ".name" : "dragon." + Mapname + ".name_" + i);
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

    static void putBossBarToDragon(EnderDragon ThisDragon, BossBar BossBar) { DragonBarList.put(ThisDragon, BossBar); }

    static void delBossBarFromDragon(EnderDragon ThisDragon) { DragonBarList.remove(ThisDragon); }

    static BossBar getBossBarFromDragon(EnderDragon ThisDragon) {
        BossBar BossBar = null;

        try {
            BossBar = (BossBar) DragonBarList.get(ThisDragon);
        } catch (Exception var3) {
        }

        return BossBar;
    }

    public static void resetDragonsBossbar(Entity Dragon) {
        BossBar BossBar = getBossBarFromDragon((EnderDragon) Dragon);
        if (BossBar != null) {
            BossBar.setProgress(0.0D);
            BossBar.removeAll();
            delBossBarFromDragon((EnderDragon) Dragon);
            BossBar.setVisible(false);
        }

    }

    public static void cleanupDragons() {
        Set<EnderDragon> testdrags = DragonBarList.keySet();
        Set<EnderDragon> listDelDrags = new HashSet<EnderDragon>();

        for (EnderDragon testdrag : testdrags) {
            if (!testdrag.isValid()) {
                BossBar tobedeleted = getBossBarFromDragon(testdrag);
                tobedeleted.setVisible(false);
                listDelDrags.add(testdrag);
            }
        }

        for (EnderDragon testdrag : listDelDrags) {
            DragonBarList.remove(testdrag);
        }

    }

    public static void deletePlayersBossBars(Player player) {
        for (BossBar BB : BossBars) {
            if (BB.getPlayers().contains(player)) {
                BB.removePlayer(player);
            }
        }

    }

    void setBossBarAmount(EnderDragon e) {
        String w = e.getWorld().getName();
        if (this.checkWorld(w)) {
            BossBar bossBar = getBossBarFromDragon(e);
            if (bossBar != null) {
                this.setBossBarAmountNOW(e, bossBar);
            }
        }

    }

    void setBossBarAmountNOW(EnderDragon e, BossBar bossBar) {
        this.getServer().getScheduler().runTaskLater(this, () -> {
            double maxHealth = e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double dragHealth = e.getHealth();
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

    static Location findPosForPortal(double tempX, double tempZ, World world, Material checkMat) {
        Location portLoc = new Location(world, tempX, 60.0, tempZ);
        double y = 200.0;
        while (y > 35.0) {
            portLoc.setY(y);
            Material testMat = portLoc.getBlock().getType();
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

    private void PlaceEmptyPortal(World ThisWorld) {
        String worldname = ThisWorld.getName().toLowerCase();
        if (!this.configManager.getOldPortal(worldname) && this.configManager.getDisableOrigDragonRespawn(worldname)) {
            Location PortLoc = findPosForPortal((double) this.configManager.getPortalXdef(worldname),
                    (double) this.configManager.getPortalZdef(worldname), ThisWorld, Material.BEDROCK);
            this.placePortal(PortLoc, 0);
        }

    }

    private void PlaceEmptyPortals(World ThisWorld, boolean place) {
        if (this.configManager.getMultiPortal()) {
            String worldname = ThisWorld.getName().toLowerCase();
            if (!this.configManager.getOldPortal(worldname)) {
                Location defPortLoc = findPosForPortal((double) this.configManager.getPortalXdef(worldname),
                        (double) this.configManager.getPortalZdef(worldname), ThisWorld, Material.BEDROCK);

                for (int i = 1; i <= this.configManager.getMaxdragons(worldname); ++i) {
                    Location PortLoc = findPosForPortal((double) this.configManager.getPortalX(worldname, i),
                            (double) this.configManager.getPortalZ(worldname, i), ThisWorld, Material.BEDROCK);
                    if (!defPortLoc.equals(PortLoc)) {
                        this.placePortal(PortLoc, place ? 0 : -1);
                    }
                }
            }
        }

    }

    void placePortal(Location position, int endPortal) {
        Material portalStone = (endPortal < 0) ? Material.END_STONE : Material.BEDROCK;
        World world = position.getWorld();

        for (int y = 0; y <= 10; ++y) {
            int maxXZ = Math.min(y < 2 ? y + 1 : 3, 3);

            for (int x = -maxXZ; x <= maxXZ; ++x) {
                for (int z = -maxXZ; z <= maxXZ; ++z) {
                    if (Math.abs(x * z) < maxXZ * 2) {
                        Material blockType = Material.AIR;
                        if (y <= 2) {
                            if (Math.abs(x * z) < (maxXZ - 1) * 2 && Math.abs(x) <= maxXZ - 1 && Math.abs(z) <= maxXZ - 1) {
                                blockType = (y == 2 && endPortal > 0) ? Material.valueOf("ENDER_PORTAL") : portalStone;
                            } else {
                                blockType = portalStone;
                            }
                        }
                        position.getBlock().getRelative(x, y - 2, z).setType(blockType);
                    }
                }
            }
        }

        position.getBlock().setType(portalStone);
        position = position.getBlock().getRelative(0, 1, 0).getLocation();
        position.getBlock().setType((endPortal < 0) ? Material.AIR : portalStone);
        position.getBlock().getRelative(0, 1, 0).setType((endPortal < 0) ? Material.AIR : portalStone);
        position.getBlock().getRelative(0, 2, 0).setType((endPortal < 0) ? Material.AIR : portalStone);

        if (endPortal > 0 && new Random().nextInt(100) < this.configManager.getPortalEggChance(world.getName().toLowerCase())) {
            position.getBlock().getRelative(0, 3, 0).setType(Material.DRAGON_EGG);
        }

        if (endPortal >= 0) {
            Block block = position.getBlock();
            block.getRelative(BlockFace.NORTH).setType(Material.BEDROCK);
            block.getRelative(BlockFace.SOUTH).setType(Material.BEDROCK);
            block.getRelative(BlockFace.EAST).setType(Material.BEDROCK);
            block.getRelative(BlockFace.WEST).setType(Material.BEDROCK);

            Location torchLocation = block.getRelative(0, 1, 0).getLocation();
            for (BlockFace face : BlockFace.values()) {
                if (face != BlockFace.UP && face != BlockFace.DOWN) {
                    DragonSlayer.setTorch(torchLocation, face);
                }
            }

            position = torchLocation.getBlock().getRelative(0, -1, 0).getLocation();
            delSurroundingBlocks(position);
        }

        for (Item item : world.getEntitiesByClass(Item.class)) {
            if (item.getType() == EntityType.ITEM && item.getLocation().distance(position) <= 3.0D
                    && item.getItemStack().getType().equals(Material.TORCH)) {
                item.remove();
            }
        }
    }

    private static void delSurroundingBlocks(Location Position2) {
        Position2.getBlock().getRelative(BlockFace.NORTH).setType(Material.AIR);
        Position2.getBlock().getRelative(BlockFace.SOUTH).setType(Material.AIR);
        Position2.getBlock().getRelative(BlockFace.EAST).setType(Material.AIR);
        Position2.getBlock().getRelative(BlockFace.WEST).setType(Material.AIR);
    }

    private static void setTorch(Location Baseblock, BlockFace Face) {
        Block TorchBlock = Baseblock.getBlock().getRelative(Face);
        TorchBlock.setType(Material.WALL_TORCH);
        BlockState state = TorchBlock.getState();
        ((Directional) state.getBlockData()).setFacing(TorchBlock.getFace(Baseblock.getBlock()).getOppositeFace());
        state.update(true);

    }

    private void WorldRefresh2(String ThisWorldsName) {
        World ThisWorld = this.getDragonWorldFromString(ThisWorldsName);
        try {
            Object edb = this.getEnderDragonBattle(ThisWorld);
            if (db_a == null) {
                db_a = edb.getClass().getDeclaredMethod("a", List.class);
            }

            if (db_a != null) {
                db_a.setAccessible(true);
                db_a.invoke(edb, Collections.emptyList());
            }
        } catch (SecurityException | NullPointerException | InvocationTargetException | IllegalAccessException
                | NoSuchMethodException var4) {
            this.logger.info("Can not handle world refresh..");
        }

        this.PlaceEmptyPortals(ThisWorld, true);
        this.getServer().getScheduler().runTaskLater(this, () -> {
            Location PortLoc = findPosForPortal((double) this.configManager.getPortalXdef(ThisWorldsName),
                    (double) this.configManager.getPortalZdef(ThisWorldsName), ThisWorld, Material.BEDROCK);
            this.activateChunksAroundPosition(PortLoc, ThisWorld, 2, false);

            for (Entity ent : PortLoc.getWorld().getEntitiesByClass(EnderCrystal.class)) {
                if (PortLoc.distance(ent.getLocation()) <= 4.0D) {
                    ent.remove();
                }
            }

        }, 1200L);
    }

    public void WorldRefresh(String ThisWorldsName) {
        if (ThisWorldsName != null && this.checkWorld(ThisWorldsName.toLowerCase())) {
            World ThisWorld = this.getDragonWorldFromString(ThisWorldsName);
            if (ThisWorld.getEnvironment() != Environment.THE_END) {
                this.logger.info("World refresh not possible! " + ThisWorldsName + " is not an END-World!");
                return;
            }

            this.setWorldRefreshRun(ThisWorldsName);
            Location PortLoc = findPosForPortal((double) this.configManager.getPortalXdef(ThisWorldsName),
                    (double) this.configManager.getPortalZdef(ThisWorldsName), ThisWorld, Material.BEDROCK);

            for (double x = -3.0D; x <= 3.0D; x += 3.0D) {
                for (double z = -3.0D; z <= 3.0D; z += 6.0D) {
                    if (x != 0.0D) {
                        z = 0.0D;
                    }

                    Entity crystal = ThisWorld.spawnEntity(PortLoc.clone().add(x + 0.5D, 1.0D, z + 0.5D), EntityType.END_CRYSTAL);
                    crystal.setInvulnerable(true);
                }
            }

            if (this.configManager.getMultiPortal()) {
                this.setExitPortalLocation(ThisWorld, PortLoc.getBlockX(), PortLoc.getBlockY(), PortLoc.getBlockZ(), true, false);
            }

            this.PlaceEmptyPortals(ThisWorld, false);

            ThisWorld.getEnderDragonBattle().initiateRespawn();

            this.configManager.setCreatePortal(true, ThisWorldsName.toLowerCase());
            this.WorldRefresh2(ThisWorldsName);
        }

    }

    private void setWorldRefreshRun(String ThisWorldsName) {
        World ThisWorld = this.getDragonWorldFromString(ThisWorldsName);
        this.ProtectResetWorlds.add(ThisWorld);
        this.getServer().getScheduler().runTaskLater(this, () -> {
            if (this.configManager.getRespawnPlayer(ThisWorldsName.toLowerCase())) {
                this.WorldReset(ThisWorldsName, false);
            }

        }, 2400L);
    }

    void WorldReset(String ThisWorldsName, boolean force) {
        if (this.getServer().getPluginManager().getPlugin("Multiverse-Core") != null) {
            if (ThisWorldsName != null && this.checkWorld(ThisWorldsName.toLowerCase())) {
                if (this.configManager.getResetWorld(ThisWorldsName.toLowerCase())) {
                    force = true;
                }

                World ThisWorld = this.getDragonWorldFromString(ThisWorldsName);
                Collection<Player> PlayerList = ThisWorld.getPlayers();
                World BaseWorld = this.getDragonWorldFromString(ThisWorldsName.replace("_the_end", ""));
                if (BaseWorld == null) {
                    BaseWorld = this.getMultiverseCore().getMVWorldManager().getMVWorld(ThisWorld).getRespawnToWorld();
                }

                List<String> command = this.configManager.getRespawnCommand(ThisWorldsName.toLowerCase());
                if (command != null && !command.isEmpty()) {
                    for (String command2 : command) {
                        if (!command2.contains("$player")) {
                            this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command2);
                        }
                    }
                }

                for (Player player : PlayerList) {
                    deletePlayersBossBars(player);
                    if (command != null && !command.isEmpty()) {
                        for (String command2 : command) {
                            if (command2.contains("$player")) {
                                command2 = command2.replace("$player", player.getName());
                                this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command2);
                            }
                        }
                    } else if (BaseWorld != null) {
                        Location Spawn = this.getMultiverseCore().getMVWorldManager().getMVWorld(BaseWorld).getSpawnLocation();
                        Spawn.setWorld(BaseWorld);
                        player.teleport(Spawn);
                    }
                }

                if (force) {
                    this.RemoveDragons(ThisWorld, false, false);
                    this.getServer().getScheduler().runTaskLater(this, () -> {
                        Long WorldsOldSeed = ThisWorld.getSeed();
                        this.getMultiverseCore().getMVWorldManager().regenWorld(ThisWorldsName, true, false, WorldsOldSeed.toString());
                        if (this.configManager.getVerbosity()) {
                            this.logger.info(ChatColor.GREEN + "The world " + ThisWorld.getName() + " will get recreated!");
                        }

                        World NewWorld = this.getDragonWorldFromString(ThisWorldsName);
                        if (NewWorld != null) {
                            this.UpdateEndgatewayPosList(NewWorld);
                            if (!this.configManager.getCreateGateways(ThisWorldsName.toLowerCase())) {
                                this.prepareEndGateways(NewWorld, new ArrayList<Integer>());
                            }

                            Location Spawn = null;
                            Spawn = this.getMultiverseCore().getMVWorldManager().getMVWorld(NewWorld).getSpawnLocation();

                            Material endstone = Material.END_STONE;

                            Spawn.setY(findPosForPortal(Spawn.getX(), Spawn.getZ(), NewWorld, endstone).getY() + 4.0D);
                            Spawn.setWorld(NewWorld);
                            this.getMultiverseCore().getMVWorldManager().getMVWorld(NewWorld).setSpawnLocation(Spawn);
                            if (!this.configManager.getRespawnPlayer(ThisWorldsName.toLowerCase())) {
                                for (Player player : PlayerList) {
                                    player.teleport(Spawn);
                                }
                            }

                            this.getServer().getScheduler().runTaskLater(this, () -> {
                                this.PlaceEmptyPortal(NewWorld);
                                this.PlaceEmptyPortals(NewWorld, true);
                            }, 1L);
                        }

                        this.configManager.setCreatePortal(true, ThisWorldsName.toLowerCase());
                        if (this.configManager.getDelay(ThisWorldsName.toLowerCase()) > 0) {
                            if (NewWorld != null) {
                                Location Position = new Location(NewWorld, 0.0D, 1.0D, 0.0D);
                                if (this.configManager.getDisableOrigDragonRespawn(ThisWorldsName.toLowerCase())) {
                                    this.setTestPortal2(Position);
                                }
                            }

                            this.SpawnXDragons(this.missingDragons(ThisWorldsName.toLowerCase()), ThisWorldsName.toLowerCase());
                        } else if (NewWorld != null) {
                            Location Position = new Location(NewWorld, 0.0D, 1.0D, 0.0D);
                            this.setTestPortal2(Position);
                            int ExistDrags = this.getDragonCount(ThisWorldsName);
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
        Collection<EnderDragon> entityList = new HashSet<EnderDragon>();

        for (String mapname : this.configManager.getMaplist()) {
            World theWorld = this.getServer().getWorld(mapname);
            if (theWorld != null) {
                Collection<EnderDragon> entityList_ = theWorld.getEntitiesByClass(EnderDragon.class);
                entityList.addAll(entityList_);
            }
        }

        for (EnderDragon ent : DragonBarList.keySet()) {
            if (!entityList.contains(ent)) {
                entityList.add(ent);
            }
        }

        for (EnderDragon dragon : entityList) {
            if (dragon.isValid()) {
                int dragonId = this.getDragonIDMeta(dragon);
                if (dragonId >= 0) {
                    Location actLoc = dragon.getLocation();
                    Location oldLoc = getDragonPosMeta(dragon);
                    if (oldLoc != null && oldLoc.equals(actLoc)) {
                        String worldName = dragon.getWorld().getName().toLowerCase();

                        Phase nowPhase = dragon.getPhase();
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

    void AtKillCommand(String ThisWorldsName, Player player, EnderDragon ThisDrag) {
        if (ThisWorldsName != null && this.checkWorld(ThisWorldsName.toLowerCase())) {
            int dragonId = this.getDragonIDMeta(ThisDrag);
            if (dragonId >= 0) {
                this.getServer().getScheduler().runTaskLater(this, () -> {
                    World ThisWorld = this.getDragonWorldFromString(ThisWorldsName);
                    List<String> commands = this.configManager.getDragonCommand(ThisWorldsName.toLowerCase(), dragonId);
                    if (!commands.isEmpty()) {
                        this.myCommandsHandler(commands, ThisWorld, player);
                    }

                }, 60L);
            }
        }

    }

    void myCommandsHandler(List<String> commands, World ThisWorld, Player player1) {
        if (commands != null && !commands.isEmpty()) {
            String ThisWorldName = ThisWorld.getName();
            commands.forEach((command) -> {
                if (!command.isEmpty()) {
                    List<Player> pj = new ArrayList<Player>();
                    if (command.contains("$player") && player1 == null) {
                        pj.addAll(ThisWorld.getPlayers());
                    } else {
                        pj.add(player1);
                    }

                    pj.forEach((player) -> {
                        String pn = player == null ? "" : player.getName();
                        String command3 = command.replace("$player", pn).replace(ThisWorldName.toLowerCase(), ThisWorldName);
                        if (PAPIenabled) {
                            try {
                                command3 = PlaceholderAPI.setPlaceholders(
                                        player == null ? this.getOfflineSlayer() : this.getServer().getOfflinePlayer(player.getUniqueId()),
                                        command3);
                            } catch (NullPointerException var7) {
                            }
                        }

                        this.commandPercentageCall(command3, ThisWorldName);
                    });
                }

            });
        }

    }

    private void commandPercentageCall(String command, String ThisWorldName) {
        int perc_ = 100;
        String percAdd = "";
        if (command.startsWith("{") && command.contains("}")) {
            String perc = command.substring(1, command.indexOf("}")).replaceAll("[ _a-zA-Z%-]", "");
            if (!perc.isEmpty()) {
                perc_ = Integer.parseInt(perc);
                command = command.substring(command.indexOf("}") + 1);
                percAdd = " on a " + perc + "% chance!";
            }
        }

        int i = (new Random()).nextInt(100);
        if (i < perc_) {
            this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command);
            if (this.configManager.getVerbosity()) {
                this.logger
                        .info(ChatColor.GREEN + "In the world " + ThisWorldName + " Command: '" + command + "' was executed..." + percAdd);
            }
        }

    }

    void setEndGatewayPortals(World ThisWorld) {
        if (this.configManager.getCreateGateways(ThisWorld.getName().toLowerCase())) {
            Object drb = this.getEnderDragonBattle(ThisWorld);
            if (drb != null) {
                try {
                    if (endgatewayMethod == null) {
                        String methodeName = null;
                        methodeName = "spawnNewGateway";

                        endgatewayMethod = drb.getClass().getDeclaredMethod(methodeName);
                    }

                    if (endgatewayMethod != null) {
                        endgatewayMethod.setAccessible(true);
                        endgatewayMethod.invoke(drb);
                    }
                } catch (SecurityException | NullPointerException | InvocationTargetException | IllegalAccessException
                        | NoSuchMethodException var5) {
                    this.logger.warning("Unknown or unsupported Version :" + getVersion() + ", can't handle end-gateways...(yet?)");
                }
            }
        }

    }

    private void prepareEndGateways(World ThisWorld, ArrayList<Integer> remainingGateways) {
        Object DrBatt = this.getEnderDragonBattle(ThisWorld);
        if (DrBatt != null) {
            try {
                if (OrigGateways == null) {
                    OrigGateways = this.getFieldByName(DrBatt.getClass(), "gateways");
                }

                if (OrigGateways != null) {
                    OrigGateways.setAccessible(true);
                    ObjectArrayList<Integer> newObjectArray = new ObjectArrayList<Integer>();
                    newObjectArray.addAll(remainingGateways);
                    OrigGateways.set(DrBatt, newObjectArray);

                }
            } catch (NullPointerException | IllegalAccessException | SecurityException var5) {
                this.logger.warning("Unknown or unsupported Version :" + getVersion() + ", can't handle end-gateways...(yet?)");
            }
        }

    }

    static String getVersion() { return "1.20.6"; }

    private void setTestPortal() {
        for (String DragonWorld : this.configManager.getMaplist()) {
            if (Bukkit.getWorld(DragonWorld) != null) {
                World W = this.getDragonWorldFromString(DragonWorld);
                Location Position = new Location(W, 0.0D, 1.0D, 0.0D);
                Location Position2 = new Location(W, -2.0D, 1.0D, 2.0D);
                Position.getBlock().setType(Material.AIR);
                Position2.getBlock().setType(Material.AIR);
                if (this.configManager.getDisableOrigDragonRespawn(DragonWorld)) {
                    this.setTestPortal2(Position);
                    this.setTestPortal2(Position2);
                }
            }
        }

    }

    private void setTestPortal2(Location Position) {
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            Position.getBlock().getRelative(BlockFace.NORTH).setType(Material.BEDROCK);
            Position.getBlock().getRelative(BlockFace.SOUTH).setType(Material.BEDROCK);
            Position.getBlock().getRelative(BlockFace.WEST).setType(Material.BEDROCK);
            Position.getBlock().getRelative(BlockFace.EAST).setType(Material.BEDROCK);
            Position.getBlock().getRelative(BlockFace.UP).setType(Material.BEDROCK);

            Position.getBlock().setType(Material.END_PORTAL);

        });
    }

    private void countEndGatewaysAndContinue() {
        for (String DragonWorld : this.configManager.getMaplist()) {
            this.findAndUseEndgateways(DragonWorld);
        }

    }

    void findAndUseEndgateways(String DragonWorld) {

        World ThisWorld = this.getDragonWorldFromString(DragonWorld);
        if (ThisWorld != null) {
            int Counter = 0;
            long oldSeed = ThisWorld.getSeed();
            ArrayList<Integer> shuffledList = new ArrayList<Integer>();
            shuffledList.addAll(ContiguousSet.create(Range.closedOpen(0, 20), DiscreteDomain.integers()));
            Collections.shuffle(shuffledList, new Random(oldSeed));
            String Slots = " free slots...";

            for (int x = 1; x <= 20; ++x) {
                double x2 = 96.0D * Math.cos(2.0D * (-Math.PI + 0.15707963267948966D * (double) x));
                double z2 = 96.0D * Math.sin(2.0D * (-Math.PI + 0.15707963267948966D * (double) x));
                int x1 = (int) x2;
                int z1 = (int) z2;

                if (x2 < (double) x1) {
                    --x1;
                }

                if (z2 < (double) z1) {
                    --z1;
                }

                Location Testlocation = new Location(ThisWorld, (double) x1, 75.0D, (double) z1);
                if (!Endgateways.contains(Testlocation)) {
                    Endgateways.add(Testlocation);
                }

                Block TestBlock = Testlocation.getBlock();
                if (TestBlock.getType() == Material.END_GATEWAY) {
                    ++Counter;
                    shuffledList.remove(shuffledList.indexOf(x == 20 ? 0 : x));
                }
            }

            if (!this.configManager.getCreateGateways(DragonWorld.toLowerCase())) {
                shuffledList = new ArrayList<Integer>();
            }

            this.prepareEndGateways(ThisWorld, shuffledList);
            if (this.configManager.getVerbosity()) {
                this.logger.info(Counter + " endgateways found on " + ThisWorld.getName() + " with " + shuffledList.size() + Slots);
            }
        }

    }

    void UpdateEndgatewayPosList(World ThisWorld) {
        for (int x = 1; x <= 20; ++x) {
            double x2 = 96.0D * Math.cos(2.0D * (-Math.PI + 0.15707963267948966D * (double) x));
            double z2 = 96.0D * Math.sin(2.0D * (-Math.PI + 0.15707963267948966D * (double) x));
            int x1 = (int) x2;
            int z1 = (int) z2;
            if (x2 < (double) x1) {
                --x1;
            }

            if (z2 < (double) z1) {
                --z1;
            }

            Location Testlocation = new Location(ThisWorld, (double) x1, 75.0D, (double) z1);
            if (!Endgateways.contains(Testlocation)) {
                Endgateways.add(Testlocation);
            }
        }

        ArrayList<Location> newEndgateways = new ArrayList<Location>();

        for (Location Loc : Endgateways) {
            boolean worldExists = false;

            try {
                if (Loc.isWorldLoaded()) {
                    worldExists = true;
                }
            } catch (NoSuchMethodError var11) {
                try {
                    Loc.getBlock().getWorld().getChunkAt(0, 0);
                    worldExists = true;
                } catch (Exception var10) {
                }
            }

            if (worldExists && this.checkWorld(Loc.getWorld().getName().toLowerCase())) {
                newEndgateways.add(Loc);
            }
        }

        Endgateways = newEndgateways;
    }

    void OrigEnderDragonSetKilled(EnderDragon ThisDragon) {

        Object DrBatt = this.getEnderDragonBattle(ThisDragon);
        if (DrBatt != null) {
            try {
                if (DragonKilled == null) {
                    DragonKilled = this.getFieldByName(DrBatt.getClass(), "dragonKilled");
                }
                if (DragonUUID == null) {
                    DragonUUID = this.getFieldByType(DrBatt.getClass(), "UUID");
                }

                if (DragonKilled != null && DragonUUID != null) {
                    DragonKilled.setAccessible(true);
                    DragonUUID.setAccessible(true);
                    DragonKilled.setBoolean(DrBatt, true);
                    DragonUUID.set(DrBatt, (Object) null);
                }
            } catch (SecurityException | NullPointerException | IllegalAccessException var5) {
                this.logger.warning("Unknown or unsupported Version :" + getVersion() + " ,can't handle this here... (yet?)");
                if (this.configManager.debugOn()) {
                    var5.printStackTrace();
                }
            }
        }

    }

    void setEDBPreviouslyKilled(EnderDragon ThisDragon, boolean pk) {

        Object DrBatt = this.getEnderDragonBattle(ThisDragon);

        if (DrBatt != null) {
            try {
                if (DragonPreviouslyKilled == null) {
                    DragonPreviouslyKilled = this.getFieldByName(DrBatt.getClass(), "previouslyKilled");
                }

                if (DragonPreviouslyKilled != null) {
                    DragonPreviouslyKilled.setAccessible(true);
                    DragonPreviouslyKilled.setBoolean(DrBatt, pk);
                }
            } catch (SecurityException | NullPointerException | IllegalAccessException var7) {
                this.logger.warning("Unknown or unsupported Version :" + getVersion() + " ,can't handle this here... (yet?)");
                if (this.configManager.debugOn()) {
                    var7.printStackTrace();
                }
            }
        }

    }

    boolean getEnderDragonPreviouslyKilled(EnderDragon ThisDragon) {

        try {
            if (ThisDragon.getWorld().getEnvironment() == Environment.THE_END) {
                return ThisDragon.getDragonBattle() != null ? ThisDragon.getDragonBattle().hasBeenPreviouslyKilled()
                        : ThisDragon.getWorld().getEnderDragonBattle().hasBeenPreviouslyKilled();
            }
        } catch (NullPointerException var5) {
            if (this.configManager.debugOn()) {
                this.logger.warning("NullPointerException in EnderDragonPreviouslyKilled... no API DragonBattle?:");
                var5.printStackTrace();
            }
        }

        return true;
    }

    void WorldGenEndTrophyPositionSet(EnderDragon ThisDrag, boolean setEDBforce) {
        if (this.configManager.getMultiPortal() || serverStarted) {
            if (!this.configManager.getMultiPortal() || !serverStarted || this.checkDSLDragon(ThisDrag)) {
                World ThisWorld = ThisDrag.getWorld();
                int dragonId = this.getDragonIDMeta(ThisDrag);
                String Worldname = ThisWorld.getName().toLowerCase();
                boolean errored = false;

                try {
                    if (!this.configManager.getOldPortal(Worldname)) {
                        this.getServer().getScheduler().runTaskLater(this, () -> {
                            String phaseController_m_c_name = "c";
                            Object newDragEnt = this.getEntityEnderDragon(ThisDrag);
                            Object phase_control = null;
                            String newPhase = "";
                            int newX = this.configManager.getPortalX(Worldname, dragonId);
                            int newZ = this.configManager.getPortalZ(Worldname, dragonId);
                            int newY = findPosForPortal((double) newX, (double) newZ, ThisWorld, Material.BEDROCK).getBlockY() + 4;

                            try {
                                if (this.phase_control_manager_method == null) {
                                    this.phase_control_manager_method = this.getMethodByReturntype(newDragEnt.getClass(),
                                            "EnderDragonPhaseManager", (Class<?>[]) null);
                                }

                                Object phase_control_manager = this.phase_control_manager_method.invoke(newDragEnt);
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
                                    Field pathEnt_f = this.getFieldByName(phase_control.getClass(), "currentPath");
                                    pathEnt_f.setAccessible(true);
                                    Object pathEnt = pathEnt_f.get(phase_control);
                                    if (pathEnt != null) {
                                        if (this.getPathPointFromPathEnt_method == null) {
                                            this.getPathPointFromPathEnt_method = this.getMethodByReturntype(pathEnt.getClass(), "Node",
                                                    new Class[] { Integer.TYPE });
                                        }

                                        int pathEntListSize;
                                        if (this.pathEntList == null) {
                                            Object pathPoint = this.getPathPointFromPathEnt_method.invoke(pathEnt, 0);
                                            this.pathEntList = this.getFieldByType(pathEnt.getClass(),
                                                    "List<" + pathPoint.getClass().getName() + ">");
                                            this.pathEntList.setAccessible(true);
                                        }

                                        pathEntListSize = ((List<?>) this.pathEntList.get(pathEnt)).size();

                                        for (int j = 0; j < pathEntListSize; ++j) {
                                            Object pathPointAusPathEnt = this.getPathPointFromPathEnt_method.invoke(pathEnt, j);
                                            if (this.checkPositionRange(pathPointAusPathEnt, newX, newZ, 90)) {
                                                Object newPathPoint = pathPointAusPathEnt.getClass()
                                                        .getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE)
                                                        .newInstance(newX, newY, newZ);
                                                this.getMethodByReturntype(pathEnt.getClass(), "void",
                                                        new Class[] { Integer.TYPE, newPathPoint.getClass() })
                                                        .invoke(pathEnt, j, newPathPoint);
                                            }
                                        }
                                    }
                                } catch (Exception var23) {
                                    if (this.configManager.debugOn()) {
                                        this.logger.warning("Can not handle landing flight (PathEntity)!");
                                        var23.printStackTrace();
                                    }
                                }
                            } else if (newPhase.startsWith("Landing")
                                    || newPhase.startsWith("Dying") && this.configManager.getDragonDeathFix(Worldname)) {
                                try {
                                    Object vec3 = this.getMethodByReturntype(phase_control.getClass(), "Vec3", (Class<?>[]) null)
                                            .invoke(phase_control);
                                    if (vec3 != null) {
                                        String ved3d_add_name = "add";
                                        double v_x = (double) vec3.getClass().getMethod("x").invoke(vec3);
                                        double v_z = (double) vec3.getClass().getMethod("z").invoke(vec3);

                                        if (Math.abs(v_x - (double) newX) > 1.0D && Math.abs(v_z - (double) newZ) > 1.0D) {
                                            Object vec3d_new = null;

                                            try {
                                                vec3d_new = vec3.getClass().getConstructor(Double.TYPE, Double.TYPE, Double.TYPE)
                                                        .newInstance(newX, newY, newZ);
                                            } catch (InstantiationException var21) {
                                                if (this.configManager.debugOn()) {
                                                    this.logger
                                                            .warning("Debug: create Vec3D with dynamic reflection NMS... not directly...!");
                                                }

                                                Object vec3d_new1 = this
                                                        .getMethodByReturntype(vec3.getClass(), "Vec3", new Class[] { Double.TYPE })
                                                        .invoke(vec3, 0);
                                                vec3d_new = vec3d_new1.getClass()
                                                        .getMethod(ved3d_add_name, Double.TYPE, Double.TYPE, Double.TYPE)
                                                        .invoke(vec3d_new1, newX, newY, newZ);
                                            }

                                            Field vec3c_f = this.getFieldByType(phase_control.getClass(), "Vec3");
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
                    } else if (ThisDrag.isValid()) {
                        this.setDragonNavi(ThisDrag);
                    }

                    if (this.checkDSLDragon(ThisDrag)) {
                        this.setExitPortalLocation(ThisWorld, this.configManager.getPortalX(Worldname, dragonId), (Integer) null,
                                this.configManager.getPortalZ(Worldname, dragonId), setEDBforce, useAll);
                    }

                }, 2L);
            }
        }
    }

    void setDragonNavi(EnderDragon drag) {
        Object entDrag;
        int dragonId;
        String world;

        if (!this.checkDSLDragon(drag)) {
            return;
        }
        String funcName = "";
        world = drag.getWorld().getName().toLowerCase();
        dragonId = this.getDragonIDMeta(drag);
        entDrag = this.getEntityEnderDragon(drag);
        if (naviField == null) {
            funcName = "findClosestNode";

            try {
                naviField = this.getFieldByName(entDrag.getClass(), "nodes");
                fillArray = entDrag.getClass().getDeclaredMethod(funcName, new Class[0]);
            } catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
                if (this.configManager.debugOn()) {
                    e.printStackTrace();
                }
                this.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion()
                        + ", can't handle EnderDragon's pathpoints...(yet?)");

                this.logger.warning("Dragon is:" + drag.getName());
            }
        }

        if (naviField != null && fillArray != null) {
            try {
                naviField.setAccessible(true);
                Object pathArray = naviField.get(entDrag);
                int newX = this.configManager.getPortalX(world, dragonId);
                int newZ = this.configManager.getPortalZ(world, dragonId);
                int newY = DragonSlayer.findPosForPortal(newX, newZ, drag.getWorld(), Material.BEDROCK).getBlockY();
                newY = newY < 48 || newY > 80 ? newY - 64 : 0;
                int n = 20;
                while (n <= 23) {
                    Object path_n = ((Object[]) pathArray)[n];
                    if (path_n == null || path_n != null && this.checkPositionRange(path_n, newX, newZ, 20)) {
                        ((Object[]) pathArray)[0] = null;
                        naviField.set(entDrag, pathArray);
                        fillArray.setAccessible(true);
                        fillArray.invoke(entDrag, new Object[0]);
                        pathArray = naviField.get(entDrag);
                        int i = 0;
                        while (i < ((Object[]) pathArray).length) {
                            ((Object[]) pathArray)[i] = this.makeMovedPathpointObject(((Object[]) pathArray)[i], newX, newY, newZ);
                            ++i;
                        }
                        naviField.set(entDrag, pathArray);
                        Location teleLoc = drag.getLocation();
                        if (path_n == null) {
                            path_n = "x=" + teleLoc.getBlockX() + ",y=" + teleLoc.getBlockY() + ",z=" + teleLoc.getBlockZ();
                        }
                        if (this.checkPositionRange(path_n, newX, newZ, 100)) {
                            teleLoc.setX(newX);
                            teleLoc.setZ(newZ);
                            teleLoc.setY(teleLoc.getBlockY() + new Random().nextInt(50));
                            drag.teleport(teleLoc);
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
        serverStarted = !this.checkServerStarted();

        for (String Mapname : this.configManager.getMaplist()) {
            World World = this.getDragonWorldFromString(Mapname);
            if (World != null) {
                Collection<EnderDragon> dragList = this.getDragonList(World, Mapname);
                if (dragList != null) {
                    for (EnderDragon drag : dragList) {
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

    void setExitPortalLocation(World ThisWorld, int newX, Integer newY, int newZ, boolean setEDBforce, boolean useAll) {
        Object DragonBattle = this.getEnderDragonBattle(ThisWorld);
        HashMap<Object, World> BattleList = new HashMap<Object, World>();
        if (!useAll) {
            if (DragonBattle != null) {
                BattleList.put(DragonBattle, ThisWorld);
            }
        } else {
            setEDBforce = true;

            for (String Mapname : this.configManager.getMaplist()) {
                World World = this.getDragonWorldFromString(Mapname);
                if (World != null && World.getEnvironment() == Environment.THE_END) {
                    BattleList.put(this.getEnderDragonBattle(World), World);
                }
            }
        }

        try {
            if (setEDBforce) {
                for (Object Battle : BattleList.keySet()) {
                    if (PortLoc_f == null) {
                        PortLoc_f = this.getFieldByType(Battle.getClass(), "BlockPosition", true);
                    }

                    PortLoc_f.setAccessible(true);
                    Object EDB_PortLoc = PortLoc_f.get(Battle);
                    if (EDB_PortLoc != null && (this.checkPositionRange(EDB_PortLoc, newX, newZ, 0) || newY != null)
                            || EDB_PortLoc == null) {
                        if (newY == null) {
                            newY = findPosForPortal((double) newX, (double) newZ, (World) BattleList.get(Battle), Material.BEDROCK)
                                    .getBlockY();
                        }

                        Object BlockPosN = this.makeBlockPositionObject(newX, newY, newZ);
                        PortLoc_f.set(Battle, BlockPosN);
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
            this.setCrystalAmount(DragonBattle, this.configManager.getOldPortal(ThisWorld.getName().toLowerCase()));
        }

    }

    private boolean checkPositionRange(Object loc, int newX, int newZ, int dist) {
        String[] locArray = loc.toString().replace("BlockPos", "").replace("Node", "").replaceAll("[_ }{=]", "").split(",");
        int newX2 = 0;
        int newZ2 = 0;
        if (locArray != null && locArray.length == 3) {
            for (String n : locArray) {
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

    private Object makeBlockPositionObject(int newX, int newY, int newZ) {

        try {
            if (newBlockPosition == null) {
                Class<?> BlockPosition_c = Class.forName("net.minecraft.core.BlockPos");

                newBlockPosition = BlockPosition_c.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
            }

            if (newBlockPosition != null) {
                return newBlockPosition.newInstance(newX, newY, newZ);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | NullPointerException
                | IllegalArgumentException | InstantiationException | ClassNotFoundException var5) {
            this.logger.warning("Unknown or unsupported Version :" + getVersion() + " ,can't handle BlockPosition...(yet?)");
            if (this.configManager.debugOn()) {
                var5.printStackTrace();
            }
        }

        return null;

    }

    private void setCrystalAmount(Object DragonBattle, boolean forceOff) {
        try {
            if (CrystalAmount_f == null) {
                CrystalAmount_f = DragonBattle.getClass().getDeclaredField("crystalsAlive");
            }

            CrystalAmount_f.setAccessible(true);
            Object CrystalAmount = CrystalAmount_f.get(DragonBattle);
            if (CrystalAmount != null && this.configManager.getMultiPortal()) {
                int newCrystalAmount = this.configManager.getPortalAggression(forceOff);
                if ((Integer) CrystalAmount != newCrystalAmount) {
                    CrystalAmount_f.set(DragonBattle, newCrystalAmount);
                }
            }
        } catch (NoSuchFieldException | NullPointerException | IllegalAccessException var5) {
            if (this.configManager.debugOn()) {
                this.logger.warning("Error while trying to set new crystal amount !!");
                var5.printStackTrace();
            }
        }

    }

    boolean isRefreshRunning(World ThisWorld) {

        return !ThisWorld.getEnderDragonBattle().getRespawnPhase().toString().equalsIgnoreCase("NONE");

    }

    /** {@link net.minecraft.world.entity.boss.enderdragon.EnderDragon} */
    @SuppressWarnings("unchecked")
    Object getEntityEnderDragon(EnderDragon dragon) {
        Object returnwert = null;

        try {
            if (CraftEnderDragonClass == null) {
                CraftEnderDragonClass = (Class<CraftEnderDragon>) Class.forName("org.bukkit.craftbukkit.entity.CraftEnderDragon");
            }

            if (CraftEnderDragonClass.isInstance(dragon)) {
                Object craftDragon = CraftEnderDragonClass.cast(dragon);
                returnwert = CraftEnderDragonClass.getDeclaredMethod("getHandle").invoke(craftDragon);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | ClassNotFoundException var4) {
            this.logger.warning("Unknown or unsupported Version :" + getVersion() + ", can't handle EntityEnderDragon...(yet?)");
            if (this.configManager.debugOn()) {
                var4.printStackTrace();
            }
        }

        return returnwert;
    }

    /**
     * {@link net.minecraft.world.level.pathfinder.Node}
     */
    private Object makeMovedPathpointObject(Object point, int newX, int newY, int newZ) {
        try {
            if (newPathPoint == null) {
                Class<?> pathPoint_cl = null;
                pathPoint_cl = Class.forName("net.minecraft.world.level.pathfinder.Node");

                newPathPoint = pathPoint_cl.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);

                pp_geta_func = this.getMethodByReturntype(pathPoint_cl, "BlockPos", new Class[0]);
                Class<?> baseBlockPosition_cl = null;

                baseBlockPosition_cl = Class.forName("net.minecraft.core.BlockPos");

                bbp_getX = baseBlockPosition_cl.getDeclaredMethod("getX");
                bbp_getY = baseBlockPosition_cl.getDeclaredMethod("getY");
                bbp_getZ = baseBlockPosition_cl.getDeclaredMethod("getZ");

            }

            Object baselockPos = pp_geta_func.invoke(point);
            return newPathPoint.newInstance(((int) bbp_getX.invoke(baselockPos)) + newX, (int) bbp_getY.invoke(baselockPos) + newY,
                    (int) bbp_getZ.invoke(baselockPos) + newZ);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException | NoSuchMethodException | SecurityException var7) {
            this.logger.warning("Unknown or unsupported Version :" + getVersion() + ", can't handle Pathpoints...(yet?)");
            return null;
        }
    }

    private Field getFieldByName(Class<?> _class, String name) { return this.getFieldByName(_class, name, false); }

    private Field getFieldByName(Class<?> _class, String name, boolean onlyPublics) {
        Field[] allFields = onlyPublics ? _class.getFields() : _class.getDeclaredFields();

        for (Field field : allFields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }

        if (this.configManager.debugOn()) {
            this.logger.warning("No Field found with name " + name + " on class '" + _class.getName() + "'");
        }

        return null;
    }

    private Field getFieldByType(Class<?> _class, String returnType) { return this.getFieldByType(_class, returnType, false); }

    private Field getFieldByType(Class<?> _class, String returnType, boolean onlyPublics) {
        Field[] allFields = onlyPublics ? _class.getFields() : _class.getDeclaredFields();

        for (Field field : allFields) {
            if (field.getGenericType().getTypeName().endsWith(returnType)) {
                return field;
            }
        }

        if (this.configManager.debugOn()) {
            this.logger.warning("No Field found for " + returnType);
        }

        return null;
    }

    Method getMethodByReturntype(Class<?> _class, String returnType, Class<?>[] parameters) {
        return this.getMethodByReturntype(_class, returnType, parameters, false);
    }

    Method getMethodByReturntype(Class<?> _class, String returnType, Class<?>[] parameters, boolean noRaw) {
        Method[] allMeth = _class.getMethods();

        label47: for (Method meth : allMeth) {
            if (parameters != null) {
                int paramLength = parameters.length;
                Class<?>[] methParameters = meth.getParameterTypes();
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

    private Object getEnderDragonBattle(EnderDragon ThisDragon) { return this.getEnderDragonBattle(ThisDragon.getWorld()); }

    /** {@link net.minecraft.world.level.dimension.end.EndDragonFight} */
    private Object getEnderDragonBattle(World ThisWorld) {
        try {
            Object worldServer = this.getWorldServer(ThisWorld);
            if (getEDBMethod == null) {
                getEDBMethod = this.getMethodByReturntype(worldServer.getClass(), "EndDragonFight", (Class<?>[]) null);
            }

            Object edb = getEDBMethod.invoke(worldServer);
            if (edb == null && ThisWorld.getEnvironment() == Environment.THE_END) {
                try {
                    long ws_long = ThisWorld.getSeed();
                    Object emptyNBT = Class.forName("net.minecraft.nbt.CompoundTag").newInstance();
                    edb = Class.forName("net.minecraft.world.level.dimension.end.EndDragonFight")
                            .getConstructor(worldServer.getClass(), Long.TYPE, emptyNBT.getClass())
                            .newInstance(worldServer, ws_long, emptyNBT);

                    Field ws_edb_f = this.getFieldByType(worldServer.getClass(), "EndDragonFight");
                    ws_edb_f.setAccessible(true);
                    ws_edb_f.set(worldServer, edb);
                    if (this.configManager.getVerbosity()) {
                        this.logger.warning("Started Hot-Fix for DragonBattle in world " + ThisWorld.getName());
                    }
                } catch (InstantiationException var7) {
                    this.logger.warning("unsupported Version :" + getVersion() + ", can't create own dragonbattle for this version...");
                    if (this.configManager.debugOn()) {
                        var7.printStackTrace();
                    }
                }
            }

            return edb;
        } catch (SecurityException | NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | ClassNotFoundException var8) {
            this.logger.warning("Unknown or unsupported Version :" + getVersion() + ", can't handle dragonbattle...(yet?)");
            if (this.configManager.debugOn()) {
                var8.printStackTrace();
            }
        }
        return null;
    }

    void PlaceArmorStand(String w, double x, double y, double z, float yaw) {
        Location as_loc = new Location(this.getDragonWorldFromString(w), x, y, z);
        as_loc.setYaw(yaw);
        ArmorStand armorStand = this.spawnArmorStand1(as_loc);
        this.setArmorstandMeta(armorStand);
        if (this.getSlayer() != null && this.getStatueVersion() == 1) {
            this.setArmorStandNameETC(armorStand, as_loc.clone());
        }

        if (ProtocolLibEnabled) {
            if (this.getStatueVersion() >= 2) {
                this.changeArmorStand1_NPCValues(armorStand);

                this.getServer().getScheduler().runTaskLater(this, () -> {
                    Location target_l = as_loc.clone().add(0.0D, 1.8D, 0.0D);
                    armorStand.teleport(target_l);
                    this.getServer().getScheduler().runTaskLater(this, () -> {
                        if (as_loc.equals(armorStand.getLocation())) {
                            try {
                                as_loc.getChunk().load();
                                as_loc.getChunk().getEntities();
                                armorStand.teleport(target_l);
                            } catch (Exception var5) {
                            }

                            this.getServer().getScheduler().runTaskLater(this, () -> {
                                if (as_loc.equals(armorStand.getLocation())) {
                                    this.RemoveArmorStand();
                                    ArmorStand armorStand_ = this.spawnArmorStand1(target_l);
                                    this.changeArmorStand1_NPCValues(armorStand_);
                                    this.setArmorstandMeta(armorStand_);
                                }

                            }, 10L);
                        }

                    }, 10L);
                }, 10L);

                protLibHandler.replaceNPCStatue();
            } else {
                protLibHandler.removeNPCStatue();
            }
        }

    }

    private ArmorStand spawnArmorStand1(Location as_loc) {
        ArmorStand armorStand = (ArmorStand) as_loc.getWorld().spawnEntity(as_loc, EntityType.ARMOR_STAND);
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

        double pitch = -20.0D;
        float headYaw = 20.0F;
        setHeadDirection(armorStand, headYaw, pitch);
        double armPitch = Math.toRadians(-100.0D);
        double armYaw = Math.toRadians(12.0D);
        double armRoll = Math.toRadians(-10.0D);
        EulerAngle eangle = new EulerAngle(armPitch, armYaw, armRoll);
        armorStand.setRightArmPose(eangle);
        return armorStand;
    }

    private void changeArmorStand1_NPCValues(ArmorStand armorStand) {
        String playername = this.configManager.getSlayerPAPIFormatNickString();
        if (playername != null) {
            armorStand.setCustomName(playername);
        }

        armorStand.setVisible(false);
        if (this.getSlayer() != null) {
            armorStand.setCustomNameVisible(true);
        }

        armorStand.setCollidable(true);

    }

    private void setArmorStandNameETC(ArmorStand armorStand, Location as_loc) {
        Material[] matList = this.getArmorMat();
        EntityEquipment Equip = armorStand.getEquipment();
        Equip.setHelmet(this.getPlayerHead());
        Equip.setBoots(new ItemStack(matList[0]));
        Equip.setChestplate(new ItemStack(matList[1]));
        Equip.setLeggings(new ItemStack(matList[2]));
        Equip.setItemInMainHand(new ItemStack(matList[3]));
        Equip.setItemInOffHand(getDragonSkull());

        ArmorStand armorStand2 = (ArmorStand) as_loc.getWorld().spawnEntity(as_loc, EntityType.ARMOR_STAND);
        armorStand2.setGravity(false);
        armorStand2.setMarker(false);
        String playername = this.configManager.getSlayerPAPIFormatNickString();
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
        Material[] matList = new Material[5];
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

    private void setArmorstandMeta(ArmorStand armorstand) {
        MetadataValue MdV_Armorstand = new FixedMetadataValue(this, true);
        armorstand.setMetadata("DSL-AS", MdV_Armorstand);
    }

    void setNPCStatueMeta(Entity statue, String value, String metaAdd) {
        MetadataValue mdV_Statue = new FixedMetadataValue(this, value);
        statue.setMetadata("DSL-AS" + (metaAdd != null ? metaAdd : ""), mdV_Statue);
    }

    String getNPCStatueMeta(Entity statue, String metaAdd) {
        String value = "";
        if (statue.hasMetadata("DSL-AS" + (metaAdd != null ? metaAdd : ""))) {
            List<MetadataValue> list = statue.getMetadata("DSL-AS" + (metaAdd != null ? metaAdd : ""));
            if (list != null && list.size() != 0) {
                try {
                    value = (String) ((MetadataValue) list.get(0)).value();
                } catch (Exception var6) {
                }
            }
        }

        return value;
    }

    int getStatueVersion() {
        int i = Integer.parseInt(this.getConfig().getString("global.statue_version", "1"));
        return i >= 1 && i <= 2 ? i : 1;
    }

    World getArmorstandWorld() {
        Location theArmorStandLoc = this.armorStandLoc(false);
        return theArmorStandLoc != null ? theArmorStandLoc.getWorld() : null;
    }

    ItemStack getPlayerHead() {
        String offlineslayer = this.getSlayer();
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);

        SkullMeta HeadMetadata = (SkullMeta) skull.getItemMeta();
        if (this.getSlayer() != null) {
            HeadMetadata.setOwningPlayer(this.getOfflineSlayer());
            HeadMetadata.setDisplayName(offlineslayer);
        }

        skull.setItemMeta(HeadMetadata);
        return skull;
    }

    private static void setHeadDirection(ArmorStand armorStand, float yaw, double pitch) {
        double xint = Math.toRadians(pitch);
        double zint = 0.0D;
        double yint = Math.toRadians((double) yaw);
        EulerAngle eangle = new EulerAngle(xint, yint, zint);
        armorStand.setHeadPose(eangle);
    }

    boolean RemoveArmorStand() {
        Location as_loc = this.armorStandLoc(true);
        if (as_loc == null) {
            return false;
        } else {
            World w = as_loc.getWorld();
            double as_x = as_loc.getX();
            double as_z = as_loc.getZ();
            double as_y = as_loc.getY();
            Chunk MyChunk = w.getChunkAt(as_loc);
            if (!MyChunk.isLoaded()) {
                MyChunk.load();
            }

            Entity[] chunkEntities = MyChunk.getEntities();
            Collection<ArmorStand> ArmorStands = w.getEntitiesByClass(ArmorStand.class);
            Collection<Entity> allEntities = w.getEntities();
            Collection<Entity> newEntities = new ArrayList<Entity>();

            for (Entity ent : chunkEntities) {
                if (ent instanceof ArmorStand && !newEntities.contains(ent)) {
                    newEntities.add(ent);
                }
            }

            for (ArmorStand ent : ArmorStands) {
                if (!newEntities.contains(ent)) {
                    newEntities.add(ent);
                }
            }

            for (Entity ent : allEntities) {
                if (ent instanceof ArmorStand && !newEntities.contains(ent)) {
                    newEntities.add(ent);
                }
            }

            boolean done = false;

            for (Entity armorstand : newEntities) {
                Location checkLoc = armorstand.getLocation();
                double check_x = checkLoc.getX();
                double check_z = checkLoc.getZ();
                double check_y = checkLoc.getY();
                if (armorstand.hasMetadata("DSL-AS")
                        || Math.abs(check_x - as_x) <= 1.0D && Math.abs(check_z - as_z) <= 1.0D && Math.abs(check_y - as_y) <= 2.5D) {
                    armorstand.remove();
                    done = true;
                }
            }

            return done;
        }
    }

    Location armorStandLoc(boolean simple) {
        if (this.getConfig().getConfigurationSection("armorstand") != null) {
            ConfigurationSection oldSec = this.getConfig().getConfigurationSection("armorstand");
            ConfigurationSection newSec = this.getConfig().createSection("statue");

            for (Entry<String, Object> mapEntry : oldSec.getValues(true).entrySet()) {
                newSec.set((String) mapEntry.getKey(), mapEntry.getValue());
            }

            this.getConfig().set("armorstand", (Object) null);
            this.saveConfig();
        }

        if (this.getConfig().getString("statue.world") != null) {
            String w = this.getConfig().getString("statue.world");
            double x = this.getConfig().getDouble("statue.x");
            double y = this.getConfig().getDouble("statue.y");
            double z = this.getConfig().getDouble("statue.z");
            float yaw = (float) this.getConfig().getDouble("statue.yaw");
            World W = this.getDragonWorldFromString(w);
            Location as_loc = new Location(W, x, y, z);
            if (!simple) {
                as_loc.setYaw(yaw);
            }

            return as_loc;
        } else {
            return null;
        }
    }

    private void resetArmorStand() {
        Location as_loc = this.armorStandLoc(false);
        if (as_loc != null) {
            World W = as_loc.getWorld();
            if (W != null) {
                Chunk MyChunk = W.getChunkAt(as_loc);
                if (!MyChunk.isLoaded()) {
                    MyChunk.load();
                }

                MyChunk.getEntities();
                int delay = 10;

                this.getServer().getScheduler().runTaskLater(this, () -> {
                    Entity[] List = MyChunk.getEntities();
                    int as_loc_y = as_loc.getBlockY();
                    as_loc.setY(0.0D);
                    int amount = 0;

                    for (Entity armorstand : List) {
                        if (armorstand instanceof ArmorStand) {
                            Location armstloc = armorstand.getLocation();
                            int armorstand_y = armstloc.getBlockY();
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
                        if (amount == 0 || amount <= 1 && this.getStatueVersion() == 1
                                || this.getStatueVersion() >= 2 && protLibHandler != null && protLibHandler.NPCStatue == null) {
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
        Location theArmorStandLoc = this.armorStandLoc(false);
        if (theArmorStandLoc != null) {
            try {
                theArmorStandLoc.getChunk().load();
                theArmorStandLoc.getChunk().getEntities();
            } catch (Exception var3) {
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
            this.getServer().getScheduler().runTaskAsynchronously(this, () -> this.checkGWReverse(15));

        }, 15L, 3L);
    }

    private void checkGWReverse(int distance) {
        boolean bypassON = Boolean.parseBoolean(this.getConfig().getString("global.bypassdragongateway"));
        int bypassFunc = Integer.parseInt(this.getConfig().getString("global.bypassfunc", "1"));
        bypassFunc = bypassFunc >= 1 && bypassFunc <= 2 ? bypassFunc : 1;
        if (bypassON) {
            for (String mapname : this.configManager.getMaplist()) {
                World thisWorld = this.getDragonWorldFromString(mapname);
                if (thisWorld != null) {
                    Collection<EnderDragon> dragons = null;

                    try {
                        dragons = thisWorld.getEntitiesByClass(EnderDragon.class);
                    } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException var19) {
                    }

                    if (dragons != null) {
                        for (EnderDragon dragon : dragons) {
                            Location dragLoc = dragon.getLocation();
                            Block foundGateway = CheckGatewaysForDragon(thisWorld, dragLoc, distance);
                            if (foundGateway != null) {
                                if (bypassFunc == 1) {
                                    double dragY = dragLoc.getY();
                                    double gateY = (double) foundGateway.getY();
                                    double diffY = gateY - dragY;
                                    diffY = diffY < 0.0D ? diffY - (double) distance * 1.2D : diffY + (double) distance * 1.2D;
                                    Location target = dragLoc.clone().add(0.0D, diffY, 0.0D);
                                    this.syncTP(dragon, target);
                                } else if (bypassFunc == 2) {
                                    this.getServer().getScheduler().runTaskLater(this, () -> {
                                        if (foundGateway.getType() == Material.END_GATEWAY) {
                                            try {
                                                Object worldServer = this.getWorldServer(thisWorld);
                                                Object blockPos = this.makeBlockPositionObject(foundGateway.getX(), foundGateway.getY(),
                                                        foundGateway.getZ());
                                                Object tileEnt = null;
                                                if (getTileEntity == null) {
                                                    String funcName = "getBlockEntity";

                                                    try {
                                                        getTileEntity = Class.forName("net.minecraft.world.level.Level")
                                                                .getDeclaredMethod(funcName, blockPos.getClass(), Boolean.TYPE);
                                                    } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                                                    }
                                                }

                                                try {
                                                    getTileEntity.setAccessible(true);
                                                    tileEnt = getTileEntity.invoke(worldServer, blockPos, false);
                                                } catch (IllegalArgumentException var10) {
                                                    tileEnt = getTileEntity.invoke(worldServer, blockPos);
                                                }

                                                // Object nbt_1 = null;
                                                if (tileEnt != null) {

                                                    if (saveNBT == null) {
                                                        // Until I learn how to get HolderLookup$Provider this will be skipped
                                                        // saveNBT = tileEnt.getClass().getDeclaredMethod("saveWithFullMetadata");

                                                        if (saveNBT != null) {
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
                                                                getTileEntity.setAccessible(true);
                                                                tileEnt2 = getTileEntity.invoke(worldServer, blockPos, false);
                                                            } catch (IllegalArgumentException var7) {
                                                                tileEnt2 = getTileEntity.invoke(worldServer, blockPos);
                                                            }

                                                            if (tileEnt2 != null) {
                                                                tileEnt2.getClass().getDeclaredMethod("loadAdditional", nbt_1.getClass())
                                                                        .invoke(tileEnt2, nbt_1);
                                                            }
                                                        } catch (IllegalAccessException | NoSuchMethodException
                                                                | InvocationTargetException var8) {
                                                            if (this.configManager.debugOn()) {
                                                                this.logger.warning("Can not handle TileEntity/NBT recreate)");
                                                                var8.printStackTrace();
                                                            }
                                                        }

                                                    }, 40L);
                                                }
                                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                                    | SecurityException | ClassNotFoundException | InstantiationException
                                                    | NullPointerException var13) {
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

    }

    private void syncTP(EnderDragon ThisDragon, Location target) {
        this.getServer().getScheduler().runTaskLater(this, () -> ThisDragon.teleport(target), 0L);
    }

    static Block CheckGatewaysForDragon(World ThisWorld, Location DragLoc, int distance) {
        for (Location TestLoc : Endgateways) {
            if (TestLoc.getWorld() == ThisWorld) {
                Block testBlock = TestLoc.getBlock();
                if (testBlock != null && testBlock.getChunk().isLoaded() && testBlock.getType() == Material.END_GATEWAY
                        && DragLoc.distance(TestLoc) < (double) distance) {
                    return testBlock;
                }
            }
        }

        return null;
    }

    static Location getClosestGateway(World thisWorld, Location testLoc) {
        double distance = -1.0D;
        Location returnLoc = null;

        for (Location checkLoc : Endgateways) {
            if (checkLoc.getWorld() == thisWorld) {
                Block testBlock = checkLoc.getBlock();
                if (testBlock != null && testBlock.getChunk().isLoaded() && testBlock.getType() == Material.END_GATEWAY) {
                    double dist_ = testLoc.distance(checkLoc);
                    if (distance == -1.0D || dist_ < distance) {
                        distance = dist_;
                        returnLoc = checkLoc;
                    }
                }
            }
        }

        return returnLoc;
    }

    @SuppressWarnings("unchecked")
    Object getCraftWorld(World ThisWorld) {
        try {
            if (CraftWorldClass == null) {
                CraftWorldClass = (Class<CraftWorld>) Class.forName("org.bukkit.craftbukkit.CraftWorld");
            }

            if (CraftWorldClass.isInstance(ThisWorld)) {
                return CraftWorldClass.cast(ThisWorld);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException | ClassNotFoundException var3) {
            if (this.configManager.debugOn()) {
                var3.printStackTrace();
            }
        }

        return null;
    }

    /** {@link net.minecraft.server.level.ServerLevel} */
    Object getWorldServer(World ThisWorld) {
        try {
            Object castClass = this.getCraftWorld(ThisWorld);
            return CraftWorldClass.getDeclaredMethod("getHandle").invoke(castClass);
        } catch (NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException var3) {
            if (this.configManager.debugOn()) {
                var3.printStackTrace();
            }

            return null;
        }
    }

    /** {@link net.minecraft.server.MinecraftServer} */
    Object getMinecraftServer(World world) {
        Object worldServer = this.getWorldServer(world);

        try {
            return this.getMethodByReturntype(worldServer.getClass(), "MinecraftServer", (Class<?>[]) null).invoke(worldServer);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException var4) {
            return null;
        }
    }

    void setDragonPosMeta(Entity dragon, Location location) {
        MetadataValue MdV_DragonLocation = new FixedMetadataValue(this, location);
        dragon.setMetadata("DSL-Location", MdV_DragonLocation);
    }

    static Location getDragonPosMeta(Entity dragon) {
        Location location = null;
        if (dragon.hasMetadata("DSL-Location")) {
            List<MetadataValue> list = dragon.getMetadata("DSL-Location");
            if (list != null && list.size() != 0) {
                try {
                    location = (Location) ((MetadataValue) list.get(0)).value();
                } catch (Exception var4) {
                }
            }
        }

        return location;
    }

    void setDragonIDMeta(EnderDragon dragon, int dragonId) {
        MetadataValue MdV_DragonID = new FixedMetadataValue(this, dragonId);
        dragon.setMetadata("DSL-dragID", MdV_DragonID);
        if (dId != null && dragonId >= 0 && !dragon.getPersistentDataContainer().has((NamespacedKey) dId, PersistentDataType.INTEGER)) {
            dragon.getPersistentDataContainer().set((NamespacedKey) dId, PersistentDataType.INTEGER, dragonId);
        }

    }

    int getDragonIDMeta(EnderDragon dragon) {
        int dragonId = -1;
        if (dId != null && dragon.getPersistentDataContainer().has((NamespacedKey) dId, PersistentDataType.INTEGER)) {
            dragonId = dragon.getPersistentDataContainer().get((NamespacedKey) dId, PersistentDataType.INTEGER);
            if (!dragon.hasMetadata("DSL-Location") && dragonId >= 0) {
                this.setDragonPosMeta(dragon, dragon.getEyeLocation());
            }

            return dragonId;
        } else {
            if (dragon.hasMetadata("DSL-dragID")) {
                List<MetadataValue> list = dragon.getMetadata("DSL-dragID");
                if (list != null && list.size() != 0) {
                    try {
                        dragonId = (int) ((MetadataValue) list.get(0)).value();
                    } catch (Exception var5) {
                    }
                }
            } else if (dragon.isCustomNameVisible()) {
                String world = dragon.getWorld().getName().toLowerCase();
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
    void setDragonDamageMeta(EnderDragon dragon, Player player, double damage) {
        String w = dragon.getWorld().getName().toLowerCase();
        if (this.checkWorld(w)) {
            HashMap<Player, Double> DragonMeta = new HashMap<Player, Double>();
            DragonMeta.put(player, damage);
            if (dragon.hasMetadata("DSL-Damage")) {
                List<MetadataValue> list = dragon.getMetadata("DSL-Damage");
                if (list != null && list.size() != 0) {
                    try {
                        DragonMeta = (HashMap<Player, Double>) ((MetadataValue) list.get(0)).value();
                        double oldDamage = DragonMeta.get(player) != null ? DragonMeta.get(player) : 0.0D;
                        damage += oldDamage;
                        DragonMeta.put(player, damage);
                    } catch (Exception var10) {
                    }
                }
            }

            MetadataValue MdV_DragonDamage = new FixedMetadataValue(this, DragonMeta);
            dragon.setMetadata("DSL-Damage", MdV_DragonDamage);
        }

    }

    private void StartSecondRepeatingTimer() {
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (String mapname : this.configManager.getMaplist()) {
                this.addTimerDisplay(mapname);
                this.updateTimerDisplay(mapname);
                if (!this.healTickCounterList.containsKey(mapname)) {
                    this.healTickCounterList.put(mapname, 0);
                }

                int secs = this.configManager.getRegenSecs(mapname);
                int healAmount = this.configManager.getRegenAmount(mapname);
                int mapCounter = this.healTickCounterList.get(mapname);
                boolean startHeal = secs > 0 ? mapCounter >= secs : false;
                if (startHeal) {
                    World thisWorld = this.getDragonWorldFromString(mapname);
                    if (thisWorld != null) {
                        Collection<EnderDragon> dragons = thisWorld.getEntitiesByClass(EnderDragon.class);
                        if (dragons != null) {
                            for (EnderDragon dragon : dragons) {
                                if (dragon.isValid() && this.checkDSLDragon(dragon)) {
                                    double maxHealth;

                                    maxHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

                                    double health = dragon.getHealth();
                                    if (health < maxHealth) {
                                        double newHealth = health + (double) healAmount;
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

            switch (RunCounter) {
            case 2:
                cleanupDragons();

                for (String Mapname : this.configManager.getMaplist()) {
                    World world = this.getServer().getWorld(Mapname);
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
                RunCounter = 0;
            }

            ++RunCounter;
            if (++RunCounter2 > this.configManager.getTabListTime()) {
                RunCounter2 = 1;

                for (Player p : this.getServer().getOnlinePlayers()) {
                    this.setTabListName(p);
                }
            }

        }, 600L, 20L);
    }

    String[] getWorldsNextSpawnsOrReset(String Mapname, boolean spawn, boolean reset) {
        long checkTimer = -1L;
        if (spawn) {
            checkTimer = this.getNextRespawn(Mapname);
        }

        if (reset) {
            checkTimer = this.getResetTime(Mapname);
        }

        if (checkTimer >= 0L) {
            checkTimer /= 20L;
            int rest = (int) checkTimer % 86400;
            int days = (int) checkTimer / 86400;
            int hours = rest / 3600;
            int minutes = rest / 60 - hours * 60;
            int seconds = rest % 60;
            return String.format("%d,%02d,%02d,%02d", days, hours, minutes, seconds).split(",");
        } else {
            return null;
        }
    }

    private void addTimerDisplay(String ThisWorld) {
        if (!timerDisplays.containsKey(ThisWorld)) {
            Scoreboard timerDisplay = this.getServer().getScoreboardManager().getNewScoreboard();
            this.setDisplayBasics(timerDisplay);
            timerDisplays.put(ThisWorld, timerDisplay);
            this.setTimerdisplayForWorld(ThisWorld, timerDisplay);
        }
    }

    void setDisplayBasics(Scoreboard timerDisplay) {
        Objective ScoreObj = timerDisplay.getObjective("DSL");
        if (ScoreObj == null) {
            try {
                Criteria dslTimer = Criteria.create("DSLTimer");
                ScoreObj = timerDisplay.registerNewObjective("DSL", dslTimer, ChatColor.GREEN + "Next Spawn Time");
            } catch (NoSuchMethodError | NoClassDefFoundError var6) {
                try {
                    ScoreObj = timerDisplay.registerNewObjective("DSL", "DSLTimer", ChatColor.GREEN + "Next Spawn Time");
                } catch (NoSuchMethodError var5) {
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

        if (!team_spawn.hasEntry(ScoreBoardName_1)) {
            team_spawn.addEntry(ScoreBoardName_1);
        }

        if (!team_reset.hasEntry(ScoreBoardName_2)) {
            team_reset.addEntry(ScoreBoardName_2);
        }

    }

    private void updateTimerDisplay(String ThisWorld) {
        World thisWorld_ = this.getDragonWorldFromString(ThisWorld);
        if (thisWorld_ != null) {
            if (timerDisplays.containsKey(ThisWorld)) {
                Scoreboard sb = (Scoreboard) timerDisplays.get(ThisWorld);
                Objective ScoreObj = sb.getObjective("DSL");
                int timerFunc = this.configManager.getTimerfunc(ThisWorld);
                String[] times = this.getWorldsNextSpawnsOrReset(ThisWorld, true, false);
                String[] resTimes = this.getWorldsNextSpawnsOrReset(ThisWorld, false, true);
                if ((times != null || resTimes != null) && timerFunc > 0) {
                    if (ScoreObj == null) {
                        this.setDisplayBasics(sb);
                        ScoreObj = sb.getObjective("DSL");
                        this.setTimerdisplayForWorld(ThisWorld, sb);
                    }

                    Team team_spawn = sb.getTeam("T-Spawn");
                    Team team_reset = sb.getTeam("T-Reset");
                    Score fakePlayScore1 = null;
                    Score fakePlayScore2 = null;
                    String seconds1 = "";
                    String seconds2 = "";
                    String spawnsuffix = "";
                    String resetsuffix = "";
                    int blahfixlength = 64;
                    boolean titleset = false;
                    boolean showCount = timerFunc == 2;
                    if (times != null) {
                        fakePlayScore1 = ScoreObj.getScore(ScoreBoardName_1);
                        String days = times[0];
                        String hours = times[1];
                        String minutes = times[2];
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

                        if (!TabListPlugin) {
                            team_spawn.setPrefix(timerline);
                            team_spawn.setSuffix(spawnsuffix);
                        } else {
                            sb.getEntries().stream().forEach((str) -> sb.resetScores(str));
                            fakePlayScore1 = ScoreObj.getScore(ScoreBoardName_1 + timerline + spawnsuffix);
                        }

                        ScoreObj.setDisplayName(ChatColor.GREEN + this.configManager.getTimertext().replace("$days", String.valueOf(days))
                                .replace("$hours", String.valueOf(hours)).replace("$minutes", String.valueOf(minutes))
                                .replace("$seconds", String.valueOf(seconds1)).replace('&', '§'));
                        titleset = true;
                    }

                    if (resTimes != null) {
                        fakePlayScore2 = ScoreObj.getScore(ScoreBoardName_2);
                        String days = resTimes[0];
                        String hours = resTimes[1];
                        String minutes = resTimes[2];
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

                        if (!TabListPlugin) {
                            team_reset.setPrefix(resetline);
                            team_reset.setSuffix(resetsuffix);
                        } else {
                            fakePlayScore2 = ScoreObj.getScore(ScoreBoardName_1 + resetline + resetsuffix);
                        }

                        if (!titleset) {
                            ScoreObj.setDisplayName(
                                    ChatColor.GREEN + this.configManager.getTimertext().replace("$days", String.valueOf(days))
                                            .replace("$hours", String.valueOf(hours)).replace("$minutes", String.valueOf(minutes))
                                            .replace("$seconds", String.valueOf(seconds2)).replace('&', '§'));
                        }
                    }

                    if (fakePlayScore1 != null) {
                        fakePlayScore1.setScore(showCount ? this.countRespawnTimers(ThisWorld) : Integer.valueOf(seconds1));
                    }

                    if (fakePlayScore2 != null) {
                        fakePlayScore2.setScore(showCount ? 0 : Integer.valueOf(seconds2));
                    } else {
                        sb.resetScores(ScoreBoardName_2);
                    }
                } else if (ScoreObj != null) {
                    ScoreObj.setDisplayName(ChatColor.GREEN + "No Spawntimer");
                    sb.getEntries().stream().forEach((str) -> sb.resetScores(str));

                    ScoreObj.unregister();

                }

                if (this.getStatueVersion() >= 2 && ProtocolLibEnabled) {
                    World NPCStatue_World = this.getArmorstandWorld();
                    if (NPCStatue_World != null && thisWorld_ != null && thisWorld_.equals(NPCStatue_World)) {
                        Team team2_NPC = sb.getTeam("DSL-NPCs");
                        if (team2_NPC == null) {
                            team2_NPC = sb.registerNewTeam("DSL-NPCs");
                        }

                        if (team2_NPC.getEntries().isEmpty()) {
                            team2_NPC.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);

                            for (String ent : protLibHandler.team_NPC.getEntries()) {
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

    void setTimerdisplayToPlayer(Player player) {
        String thisWorld = player.getWorld().getName().toLowerCase();
        if (this.checkWorld(thisWorld)) {
            if (this.configManager.getTimerfunc(thisWorld) != 0) {
                Scoreboard timerSB = (Scoreboard) timerDisplays.get(thisWorld);
                if (timerSB != null && timerSB.getObjective("DSL") != null) {
                    player.setScoreboard(timerSB);
                }

            }
        }
    }

    void delTimerdisplayFromPlayer(Player player) {
        Scoreboard timerSB = player.getScoreboard();
        if (timerSB != null && timerSB.getObjective("DSL") != null) {
            player.setScoreboard(this.getServer().getScoreboardManager().getMainScoreboard());
        }

    }

    void setTimerdisplayForWorld(String thisWorld, Scoreboard timerDisplay) {
        if (this.configManager.getTimerfunc(thisWorld) != 0) {
            World world = Bukkit.getWorld(thisWorld);
            if (world != null) {
                for (Player player : world.getPlayers()) {
                    player.setScoreboard(timerDisplay);
                }
            }

        }
    }

    private Team addTeamToPlayersScoreBoard(Player p, String teamName, ChatColor color) {
        this.cleanupGlowTeamList();
        Scoreboard board = p.getScoreboard();
        Team t = board.getTeam(teamName);
        if (t == null) {
            t = board.registerNewTeam(teamName);

            t.setColor(color);

        }

        if (!TeamList.contains(t)) {
            TeamList.add(t);
        }

        return t;
    }

    void handleGlowTeams(World w, int dragonId, String uuid) {
        String DragonColor = this.configManager.getGlowColor(w.getName().toLowerCase(), dragonId);
        ChatColor color = ChatColor.valueOf(DragonColor) != null ? ChatColor.valueOf(DragonColor) : ChatColor.DARK_AQUA;
        String teamName = "DSL_" + color.name();

        for (Player p : w.getPlayers()) {
            Team t = this.addTeamToPlayersScoreBoard(p, teamName, color);
            if (!t.hasEntry(uuid)) {
                t.addEntry(uuid);
            }
        }

    }

    void cleanupGlowTeamList() {

        try {
            ArrayList<String> tempList = new ArrayList<String>();

            for (Team t : TeamList) {
                for (String uuid : t.getEntries()) {
                    Entity dragEnt_ = this.getServer().getEntity(UUID.fromString(uuid));
                    if (dragEnt_ == null || !dragEnt_.isValid()) {
                        tempList.add(uuid);
                    }
                }

                if (!tempList.isEmpty()) {
                    tempList.stream().forEach((uid) -> t.removeEntry(uid));
                    tempList.clear();
                }

                if (t.getSize() == 0) {
                    TeamList.remove(t);
                    t.unregister();
                }
            }
        } catch (ConcurrentModificationException var7) {
        }

    }
}
