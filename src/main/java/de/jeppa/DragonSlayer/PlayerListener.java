package de.jeppa.DragonSlayer;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.craftbukkit.block.data.type.CraftBed;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {
    DragonSlayer plugin;

    public PlayerListener(DragonSlayer instance) { this.plugin = instance; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        World TheWorld = p.getWorld();
        if (this.plugin.checkWorld(TheWorld.getName().toLowerCase())) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                this.setDragsKilledAndTeleport(TheWorld, false);
                this.plugin.handleBossbar(TheWorld);
                this.checkMissingTimers(TheWorld.getName().toLowerCase(), p);
                this.plugin.setTimerdisplayToPlayer(p);
            }, 120L);
        }

        this.plugin.setTabListName(p);
        this.sendNPCClientPackets(TheWorld, p);
    }

    @EventHandler
    public void onPlayerUseTransferportal(PlayerTeleportEvent event) {
        if (event.getCause().compareTo(TeleportCause.END_GATEWAY) == 0) {
            World TheWorld = event.getFrom().getWorld();
            String worldname = TheWorld.getName().toLowerCase();
            if (this.plugin.checkWorld(worldname)) {
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    this.setDragsKilledAndTeleport(TheWorld, false);
                    this.plugin.handleBossbar(TheWorld);
                }, 60L);
                Location getFromLoc = event.getFrom();
                Location teleportTargetLoc = event.getTo();
                Location mapCenter = new Location(TheWorld, 0.0D, (double) teleportTargetLoc.getBlockY(), 0.0D);
                if (teleportTargetLoc.distance(mapCenter) > 127.0D) {
                    boolean found = false;

                    for (int x = -1; x <= 1; ++x) {
                        for (int z = -1; z <= 1; ++z) {
                            Chunk gwChunk = getFromLoc.getBlock().getRelative(x * 16, 0, z * 16).getChunk();
                            BlockState[] tileEnts = gwChunk.getTileEntities();

                            for (BlockState tileEnt : tileEnts) {
                                if (tileEnt instanceof EndGateway) {
                                    Location sourceGWLoc = tileEnt.getLocation();
                                    Location targetLoc = ((EndGateway) tileEnt).getExitLocation();
                                    if (targetLoc != null) {
                                        if (this.plugin.configManager.getFixGateways(worldname)
                                                && mapCenter.distance(sourceGWLoc) > 127.0D) {
                                            Location exitLoc = DragonSlayer.getClosestGateway(TheWorld, sourceGWLoc);
                                            if (exitLoc == null) {
                                                exitLoc = new Location(TheWorld, 20.0D, (double) teleportTargetLoc.getBlockY(), 20.0D);
                                            }

                                            if (targetLoc.getBlock().getType() == Material.END_GATEWAY) {
                                                Location testExitLoc2 = ((EndGateway) targetLoc.getBlock().getState()).getExitLocation();
                                                if (testExitLoc2 != null && testExitLoc2.distance(exitLoc) < 90.0D) {
                                                    exitLoc = testExitLoc2;
                                                }
                                            }

                                            ((EndGateway) tileEnt).setExitLocation(exitLoc);
                                            if (this.plugin.configManager.getVerbosity()) {
                                                this.plugin.logger.info("Gateway target updated/fixed for teleport to center island!");
                                            }

                                            if (this.plugin.configManager.debugOn()) {
                                                this.plugin.logger.info("New target:" + exitLoc);
                                            }

                                            ((EndGateway) tileEnt).update();
                                        }

                                        if (targetLoc.distance(teleportTargetLoc) < 50.0D && !targetLoc.equals(sourceGWLoc)) {
                                            if (targetLoc.getBlock().getType() == Material.END_GATEWAY) {
                                                if (targetLoc.getBlock().getRelative(0, -12, 0).getType() == Material.END_GATEWAY) {
                                                    targetLoc.setY(targetLoc.getY() - 2.0D);
                                                    ((EndGateway) tileEnt)
                                                            .setExitLocation(targetLoc.getBlock().getRelative(0, -10, 0).getLocation());
                                                    ((EndGateway) tileEnt).update();
                                                } else {
                                                    targetLoc.setY(targetLoc.getY() + 10.0D);
                                                }
                                            } else {
                                                targetLoc.setY(targetLoc.getY() + 3.0D);
                                            }

                                            this.removeStackedGateway(targetLoc);
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (found) {
                                break;
                            }
                        }

                        if (found) {
                            break;
                        }
                    }
                }
            }
        }

    }

    private void removeStackedGateway(Location removeArea) {
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (removeArea != null) {
                int y = removeArea.getBlockY();

                for (int y2 = y; y2 <= (y + 6 <= 255 ? y + 6 : 255); ++y2) {
                    removeArea.setY((double) y2);

                    for (int x2 = -1; x2 <= 1; ++x2) {
                        for (int z2 = -1; z2 <= 1; ++z2) {
                            if (x2 == 0 || z2 == 0) {
                                removeArea.getBlock().getRelative(x2, 0, z2).setType(Material.AIR);
                            }
                        }
                    }
                }
            }

        }, 0L);
    }

    private void setDragsKilledAndTeleport(World TheWorld, boolean teleport) {
        Collection<EnderDragon> EntityList = TheWorld.getEntitiesByClass(EnderDragon.class);
        String Mapname = TheWorld.getName().toLowerCase();
        int y = 0;
        int Playercount = teleport ? this.plugin.getPlayerCount(TheWorld.getName().toLowerCase()) : 0;

        for (EnderDragon Dragon : EntityList) {
            if (Dragon.isValid()) {
                if (this.plugin.configManager.getDragonTeleport(Mapname) && Playercount == 1) {
                    int dragonId = this.plugin.getDragonIDMeta(Dragon);
                    if (this.plugin.checkDSLDragon(Dragon)) {
                        Dragon.teleport(new Location(TheWorld, (double) this.plugin.configManager.getPortalX(Mapname, dragonId),
                                (double) (75 + y), (double) this.plugin.configManager.getPortalZ(Mapname, dragonId)));
                        y += 8;
                    }
                }

                this.plugin.OrigEnderDragonSetKilled(Dragon);
            }
        }

    }

    @EventHandler
    public void worldChange(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        String fromWorld = event.getFrom().getName();
        if (this.plugin.checkWorld(fromWorld)) {
            DragonSlayer.deletePlayersBossBars(p);
            this.plugin.delTimerdisplayFromPlayer(p);
        }

        World toWorld = p.getWorld();
        String gotoWorld = toWorld.getName().toLowerCase();
        if (this.plugin.checkWorld(gotoWorld)) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                int existentDragons = this.plugin.getDragonCount(gotoWorld);
                this.setDragsKilledAndTeleport(toWorld, true);
                String ProtectMessage = this.plugin.configManager.getProtectMessage(toWorld.getName());
                if (!ProtectMessage.equals("")) {
                    if (ProtectMessage.contains("$amount")) {
                        p.sendMessage(ChatColor.DARK_PURPLE + ProtectMessage.replace("$amount", String.valueOf(existentDragons)));
                    } else {
                        p.sendMessage("" + ChatColor.DARK_PURPLE + existentDragons + " " + ProtectMessage);
                    }
                }

                this.checkMissingTimers(gotoWorld, p);
            }, 10L);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                this.setDragsKilledAndTeleport(toWorld, false);
                this.plugin.handleBossbar(toWorld);
                this.plugin.setTimerdisplayToPlayer(p);
            }, 120L);
        }

        this.sendNPCClientPackets(toWorld, p);
    }

    private void sendNPCClientPackets(World TheWorld, Player p) {
        if (this.plugin.getStatueVersion() == 2) {
            World statueWorld = this.plugin.getArmorstandWorld();
            if (TheWorld.equals(statueWorld) && DragonSlayer.protLibHandler != null) {
                DragonSlayer.protLibHandler.sendNPCClientPacket(p);
            }

        }
    }

    private void checkMissingTimers(String gotoWorld, Player p) {
        boolean firstJoinPossible = false;
        if (this.plugin.configManager.getfirstjoin(gotoWorld) && !this.plugin.timerManager.checkPlayerOnList(p, gotoWorld)) {
            firstJoinPossible = true;
        }

        if (this.plugin.configManager.getAutofix(gotoWorld) && this.plugin.configManager.getDelay(gotoWorld) > 0 || firstJoinPossible) {
            int missingDragons = this.plugin.missingDragons(gotoWorld);
            if (missingDragons > 0) {
                for (int i = 0; i < missingDragons; ++i) {
                    DragonRespawn Resp = new DragonRespawn(this.plugin);
                    Resp.Mapname = gotoWorld;
                    int runduration = (firstJoinPossible ? 40 : this.plugin.configManager.getDelay(gotoWorld)) + i * 60;
                    Resp.OrigRuntime = (long) runduration;
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, Resp, (long) runduration);
                    this.plugin.getLogger().info("Additional timer for dragonrespawn in world " + gotoWorld + " started...");
                    if (firstJoinPossible) {
                        this.plugin.timerManager.addPlayerToList(p, gotoWorld);
                    }
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void playerChat(AsyncPlayerChatEvent e) {
        if (!DragonSlayer.EssChEnabled) {
            Player p = e.getPlayer();
            if (this.plugin.configManager.getPrefixEnabled() && this.plugin.getSlayerUUIDString().equals(p.getUniqueId().toString())) {
                String prefix = this.plugin.configManager.getPrefix();
                if (!p.getDisplayName().contains(this.plugin.configManager.getPrefix().trim())
                        && (!DragonSlayer.UCenabled && !DragonSlayer.PAPIenabled || this.plugin.configManager.getForcePrefix())) {
                    if (!this.plugin.configManager.getPrefixAsSuffix()) {
                        e.setFormat(ChatColor.translateAlternateColorCodes('&', prefix + "%s: %s"));
                    } else {
                        e.setFormat(ChatColor.translateAlternateColorCodes('&', "%s" + prefix + ": %s"));
                    }
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void playerEssentialsChat(AsyncPlayerChatEvent e) {
        if (DragonSlayer.EssChEnabled) {
            Player p = e.getPlayer();
            if (this.plugin.configManager.getPrefixEnabled() && this.plugin.getSlayerUUIDString().equals(p.getUniqueId().toString())) {
                String prefix = this.plugin.configManager.getPrefix();
                if (!p.getDisplayName().contains(this.plugin.configManager.getPrefix().trim())
                        && (!DragonSlayer.UCenabled && !DragonSlayer.PAPIenabled || this.plugin.configManager.getForcePrefix())) {
                    e.setFormat(e.getFormat().replace("{DRAGONSLAYER}", ChatColor.translateAlternateColorCodes('&', prefix)));
                }
            } else {
                e.setFormat(e.getFormat().replace("{DRAGONSLAYER}", ChatColor.translateAlternateColorCodes('&', "")));
            }
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void ArmorStandInteract1(PlayerArmorStandManipulateEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            ArmorStand armorStand = e.getRightClicked();
            if (armorStand.hasMetadata("DSL-AS")) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onArmorStandDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            if (armorStand.hasMetadata("DSL-AS") && event.getDamage() > 0.0D) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onCrystalDamage1(EntityDamageEvent event) {

        Entity ent = event.getEntity();
        if (ent instanceof EnderCrystal) {
            String world = ent.getWorld().getName().toLowerCase();
            if (this.plugin.checkWorld(world) && this.plugin.configManager.getRefreshWorld(world)) {
                Location loc = ent.getLocation();
                int portX = this.plugin.configManager.getPortalXdef(world);
                int portZ = this.plugin.configManager.getPortalZdef(world);
                if (loc.getX() == (double) portX + 0.5D && (loc.getZ() == (double) portZ - 2.5D || loc.getZ() == (double) portZ + 3.5D)
                        || loc.getZ() == (double) portZ + 0.5D
                                && (loc.getX() == (double) portX - 2.5D || loc.getX() == (double) portX + 3.5D)) {
                    if (this.plugin.isRefreshRunning(ent.getWorld())) {
                        event.setCancelled(true);
                        ent.setInvulnerable(true);
                    } else {
                        ent.setInvulnerable(false);
                    }
                }
            }
        }

    }

    @EventHandler
    public void onCrystalDamage2(ExplosionPrimeEvent event) {
        Entity ent = event.getEntity();
        if (ent.getType().equals(EntityType.TNT)) {
            String world = ent.getWorld().getName().toLowerCase();
            if (this.plugin.checkWorld(world) && this.plugin.configManager.getRefreshWorld(world)
                    && this.plugin.isRefreshRunning(ent.getWorld())) {
                Location loc = ent.getLocation();
                int portX = this.plugin.configManager.getPortalXdef(world);
                int portZ = this.plugin.configManager.getPortalZdef(world);
                if (loc.getX() <= (double) (portX + 5) && loc.getX() >= (double) (portX - 5) && loc.getZ() <= (double) (portZ + 5)
                        && loc.getZ() >= (double) (portZ - 5)) {
                    event.setCancelled(true);
                    ent.setInvulnerable(true);
                }
            }
        }

    }

    @EventHandler
    public void CancelEggTeleport(BlockFromToEvent e) {
        String world = e.getBlock().getWorld().getName().toLowerCase();
        if (this.plugin.checkWorld(world)) {
            boolean DisableEggTeleport = this.plugin.configManager.getEggCancel(world);
            if (DisableEggTeleport && e.getBlock().getType() == Material.DRAGON_EGG) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void CancelEggTeleport(BlockBreakEvent e) {
        String world = e.getBlock().getWorld().getName().toLowerCase();
        if (this.plugin.checkWorld(world)) {
            boolean DisableEggTeleport = this.plugin.configManager.getEggCancel(world);
            if (DisableEggTeleport && e.getBlock().getType() == Material.DRAGON_EGG) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void CancelCrystalPlace(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        World ThisWorld = p.getWorld();
        Block bl = event.getClickedBlock();
        String world = ThisWorld.getName().toLowerCase();
        if (this.plugin.checkWorld(world)) {
            if ((this.plugin.configManager.getCrystalDeny(world) || this.plugin.configManager.getMultiPortal())
                    && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                    && (bl.getType().equals(Material.BEDROCK) || bl.getType().equals(Material.OBSIDIAN))) {
                Material EndCrystal = Material.END_CRYSTAL;

                if (event.getMaterial().equals(EndCrystal)) {
                    Location loc = bl.getLocation();
                    int portX = this.plugin.configManager.getPortalXdef(world);
                    int portZ = this.plugin.configManager.getPortalZdef(world);
                    if (this.plugin.configManager.getCrystalDeny(world) && loc.getX() <= (double) (portX + 5)
                            && loc.getX() >= (double) (portX - 5) && loc.getZ() <= (double) (portZ + 5)
                            && loc.getZ() >= (double) (portZ - 5)) {
                        event.setCancelled(true);
                        p.sendMessage(this.plugin.replaceValues(this.plugin.configManager.getCrystalDenyString(), world));
                    }

                    if (this.plugin.configManager.getMultiPortal()) {
                        for (int i = 1; i <= this.plugin.configManager.getMaxdragons(world); ++i) {
                            int portX2 = this.plugin.configManager.getPortalX(world, i, true, true);
                            int portZ2 = this.plugin.configManager.getPortalZ(world, i, true, true);
                            if (portX2 != portX && portZ2 != portZ && loc.getX() <= (double) (portX2 + 5)
                                    && loc.getX() >= (double) (portX2 - 5) && loc.getZ() <= (double) (portZ2 + 5)
                                    && loc.getZ() >= (double) (portZ2 - 5)) {
                                event.setCancelled(true);
                                p.sendMessage(this.plugin.replaceValues(this.plugin.configManager.getCrystalDenyString(), world) + "!");
                            }
                        }

                        this.plugin.setExitPortalLocation(ThisWorld, portX, (Integer) null, portZ, true, false);
                    }
                }
            }

            if (ThisWorld.getEnvironment() == Environment.THE_END && this.plugin.configManager.getDenyBedExplosion(world)
                    && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                boolean foundBed = false;
                if (bl.getState() != null) {
                    try {
                        if (bl.getState() instanceof Bed) {
                            foundBed = true;
                        }
                    } catch (NoClassDefFoundError | Exception var14) {
                        try {
                            if (bl.getState().getBlockData() instanceof CraftBed) {
                                foundBed = true;
                            }
                        } catch (Exception var13) {
                        }
                    }
                }

                foundBed = DragonSlayer.newRoutines14.getBedTag(bl.getType());

                if (foundBed) {
                    for (EnderDragon drag : this.plugin.getDragonList(ThisWorld, world)) {
                        if (drag.getLocation().distance(bl.getLocation()) < 20.0D) {
                            event.setCancelled(true);
                            bl.breakNaturally();
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void CancelCrystalPlace2a(BlockPistonExtendEvent event) {

        ArrayList<Block> blocklist = new ArrayList<Block>();
        blocklist.addAll(event.getBlocks());
        this.blockPistonEvent(event, blocklist);

    }

    @EventHandler
    public void CancelCrystalPlace2b(BlockPistonRetractEvent event) {

        ArrayList<Block> blocklist = new ArrayList<Block>();
        blocklist.addAll(event.getBlocks());
        this.blockPistonEvent(event, blocklist);
    }

    private void blockPistonEvent(BlockPistonEvent event, ArrayList<Block> blocklist) {
        BlockFace dir = event.getDirection();
        Block movedOne = event.getBlock().getRelative(dir);
        World ThisWorld = movedOne.getWorld();
        String world = ThisWorld.getName().toLowerCase();
        if (this.plugin.checkWorld(world)
                && (this.plugin.configManager.getCrystalDeny(world) || this.plugin.configManager.getMultiPortal())) {
            int portX = this.plugin.configManager.getPortalXdef(world);
            int portZ = this.plugin.configManager.getPortalZdef(world);
            if (blocklist.size() == 0) {
                blocklist.add(movedOne);
            } else {
                Block addOne = event.getBlock().getRelative(dir, blocklist.size() + 1);
                if (addOne.getType() == Material.AIR) {
                    blocklist.add(addOne);
                }
            }

            for (Block block : blocklist) {
                Entity[] entList = block.getChunk().getEntities();
                Entity foundCrystal = null;
                Location loc = block.getLocation();

                for (Entity ent : entList) {
                    if (ent instanceof EnderCrystal) {
                        Location entLoc = ent.getLocation().add(0.1D, 0.0D, 0.1D).getBlock().getLocation();
                        if (loc.distance(entLoc) <= 1.5D) {
                            foundCrystal = ent;
                            loc = block.getRelative(dir).getLocation();
                            break;
                        }
                    }
                }

                if (foundCrystal != null) {
                    if (this.plugin.configManager.getCrystalDeny(world) && loc.getX() <= (double) (portX + 5)
                            && loc.getX() >= (double) (portX - 5) && loc.getZ() <= (double) (portZ + 5)
                            && loc.getZ() >= (double) (portZ - 5)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (this.plugin.configManager.getMultiPortal()) {
                        for (int i = 1; i <= this.plugin.configManager.getMaxdragons(world); ++i) {
                            int portX2 = this.plugin.configManager.getPortalX(world, i, true, true);
                            int portZ2 = this.plugin.configManager.getPortalZ(world, i, true, true);
                            if (portX2 != portX && portZ2 != portZ && loc.getX() <= (double) (portX2 + 5)
                                    && loc.getX() >= (double) (portX2 - 5) && loc.getZ() <= (double) (portZ2 + 5)
                                    && loc.getZ() >= (double) (portZ2 - 5)) {
                                event.setCancelled(true);
                                return;
                            }
                        }

                        this.plugin.setExitPortalLocation(ThisWorld, portX, (Integer) null, portZ, true, false);
                    }
                }
            }
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.plugin.getStatueVersion() == 2 && DragonSlayer.protLibHandler != null) {
            Player p = event.getPlayer();
            Location p_loc = event.getTo();
            Location as_loc = this.plugin.armorStandLoc(true);
            if (as_loc != null && p_loc.getWorld().equals(as_loc.getWorld())) {
                double dist = as_loc.distance(p_loc);
                if (dist <= 10.0D && dist > 9.84D) {
                    DragonSlayer.protLibHandler.sendNPCClientPacket(p);
                }
            }
        }

    }
}
