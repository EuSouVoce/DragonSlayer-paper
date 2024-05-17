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

    public PlayerListener(final DragonSlayer instance) { this.plugin = instance; }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        final World world = p.getWorld();
        if (this.plugin.checkWorld(world.getName().toLowerCase())) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                this.setDragsKilledAndTeleport(world, false);
                this.plugin.handleBossbar(world);
                this.checkMissingTimers(world.getName().toLowerCase(), p);
                this.plugin.setTimerdisplayToPlayer(p);
            }, 120L);
        }

        this.plugin.setTabListName(p);
        this.sendNPCClientPackets(world, p);
    }

    @EventHandler
    public void onPlayerUseTransferportal(final PlayerTeleportEvent event) {
        if (event.getCause().compareTo(TeleportCause.END_GATEWAY) == 0) {
            final World world = event.getFrom().getWorld();
            final String worldName = world.getName().toLowerCase();
            if (this.plugin.checkWorld(worldName)) {
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                    this.setDragsKilledAndTeleport(world, false);
                    this.plugin.handleBossbar(world);
                }, 60L);
                final Location getFromLoc = event.getFrom();
                final Location teleportTargetLoc = event.getTo();
                final Location mapCenter = new Location(world, 0.0D, (double) teleportTargetLoc.getBlockY(), 0.0D);
                if (teleportTargetLoc.distance(mapCenter) > 127.0D) {
                    boolean found = false;

                    for (int x = -1; x <= 1; ++x) {
                        for (int z = -1; z <= 1; ++z) {
                            final Chunk gwChunk = getFromLoc.getBlock().getRelative(x * 16, 0, z * 16).getChunk();
                            final BlockState[] tileEnts = gwChunk.getTileEntities();

                            for (final BlockState tileEnt : tileEnts) {
                                if (tileEnt instanceof EndGateway) {
                                    final Location sourceGWLoc = tileEnt.getLocation();
                                    final Location targetLoc = ((EndGateway) tileEnt).getExitLocation();
                                    if (targetLoc != null) {
                                        if (this.plugin.configManager.getFixGateways(worldName)
                                                && mapCenter.distance(sourceGWLoc) > 127.0D) {
                                            Location exitLoc = DragonSlayer.getClosestGateway(world, sourceGWLoc);
                                            if (exitLoc == null) {
                                                exitLoc = new Location(world, 20.0D, (double) teleportTargetLoc.getBlockY(), 20.0D);
                                            }

                                            if (targetLoc.getBlock().getType() == Material.END_GATEWAY) {
                                                final Location testExitLoc2 = ((EndGateway) targetLoc.getBlock().getState())
                                                        .getExitLocation();
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

    private void removeStackedGateway(final Location removeArea) {
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            if (removeArea != null) {
                final int y = removeArea.getBlockY();

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

    private void setDragsKilledAndTeleport(final World world, final boolean teleport) {
        final Collection<EnderDragon> EntityList = world.getEntitiesByClass(EnderDragon.class);
        final String Mapname = world.getName().toLowerCase();
        int y = 0;
        final int Playercount = teleport ? this.plugin.getPlayerCount(world.getName().toLowerCase()) : 0;

        for (final EnderDragon Dragon : EntityList) {
            if (Dragon.isValid()) {
                if (this.plugin.configManager.getDragonTeleport(Mapname) && Playercount == 1) {
                    final int dragonId = this.plugin.getDragonIDMeta(Dragon);
                    if (this.plugin.checkDSLDragon(Dragon)) {
                        Dragon.teleport(new Location(world, (double) this.plugin.configManager.getPortalX(Mapname, dragonId),
                                (double) (75 + y), (double) this.plugin.configManager.getPortalZ(Mapname, dragonId)));
                        y += 8;
                    }
                }

                this.plugin.OrigEnderDragonSetKilled(Dragon);
            }
        }

    }

    @EventHandler
    public void worldChange(final PlayerChangedWorldEvent event) {
        final Player p = event.getPlayer();
        final String fromWorld = event.getFrom().getName();
        if (this.plugin.checkWorld(fromWorld)) {
            DragonSlayer.deletePlayersBossBars(p);
            this.plugin.delTimerdisplayFromPlayer(p);
        }

        final World toWorld = p.getWorld();
        final String gotoWorld = toWorld.getName().toLowerCase();
        if (this.plugin.checkWorld(gotoWorld)) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                final int existentDragons = this.plugin.getDragonCount(gotoWorld);
                this.setDragsKilledAndTeleport(toWorld, true);
                final String ProtectMessage = this.plugin.configManager.getProtectMessage(toWorld.getName());
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

    private void sendNPCClientPackets(final World world, final Player p) {
        if (this.plugin.getStatueVersion() == 2) {
            final World statueWorld = this.plugin.getArmorstandWorld();
            if (world.equals(statueWorld) && DragonSlayer.protLibHandler != null) {
                DragonSlayer.protLibHandler.sendNPCClientPacket(p);
            }

        }
    }

    private void checkMissingTimers(final String gotoWorld, final Player p) {
        boolean firstJoinPossible = false;
        if (this.plugin.configManager.getfirstjoin(gotoWorld) && !this.plugin.timerManager.checkPlayerOnList(p, gotoWorld)) {
            firstJoinPossible = true;
        }

        if (this.plugin.configManager.getAutofix(gotoWorld) && this.plugin.configManager.getDelay(gotoWorld) > 0 || firstJoinPossible) {
            final int missingDragons = this.plugin.missingDragons(gotoWorld);
            if (missingDragons > 0) {
                for (int i = 0; i < missingDragons; ++i) {
                    final DragonRespawn Resp = new DragonRespawn(this.plugin);
                    Resp.worldName = gotoWorld;
                    final int runduration = (firstJoinPossible ? 40 : this.plugin.configManager.getDelay(gotoWorld)) + i * 60;
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
    public void playerChat(final AsyncPlayerChatEvent e) {
        if (!DragonSlayer.EssChEnabled) {
            final Player p = e.getPlayer();
            if (this.plugin.configManager.getPrefixEnabled() && this.plugin.getSlayerUUIDString().equals(p.getUniqueId().toString())) {
                final String prefix = this.plugin.configManager.getPrefix();
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
    public void playerEssentialsChat(final AsyncPlayerChatEvent e) {
        if (DragonSlayer.EssChEnabled) {
            final Player p = e.getPlayer();
            if (this.plugin.configManager.getPrefixEnabled() && this.plugin.getSlayerUUIDString().equals(p.getUniqueId().toString())) {
                final String prefix = this.plugin.configManager.getPrefix();
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
    public void ArmorStandInteract1(final PlayerArmorStandManipulateEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            final ArmorStand armorStand = e.getRightClicked();
            if (armorStand.hasMetadata("DSL-AS")) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onArmorStandDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            final ArmorStand armorStand = (ArmorStand) event.getEntity();
            if (armorStand.hasMetadata("DSL-AS") && event.getDamage() > 0.0D) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onCrystalDamage1(final EntityDamageEvent event) {

        final Entity ent = event.getEntity();
        if (ent instanceof EnderCrystal) {
            final String world = ent.getWorld().getName().toLowerCase();
            if (this.plugin.checkWorld(world) && this.plugin.configManager.getRefreshWorld(world)) {
                final Location loc = ent.getLocation();
                final int portX = this.plugin.configManager.getPortalXdef(world);
                final int portZ = this.plugin.configManager.getPortalZdef(world);
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
    public void onCrystalDamage2(final ExplosionPrimeEvent event) {
        final Entity ent = event.getEntity();
        if (ent.getType().equals(EntityType.TNT)) {
            final String world = ent.getWorld().getName().toLowerCase();
            if (this.plugin.checkWorld(world) && this.plugin.configManager.getRefreshWorld(world)
                    && this.plugin.isRefreshRunning(ent.getWorld())) {
                final Location loc = ent.getLocation();
                final int portX = this.plugin.configManager.getPortalXdef(world);
                final int portZ = this.plugin.configManager.getPortalZdef(world);
                if (loc.getX() <= (double) (portX + 5) && loc.getX() >= (double) (portX - 5) && loc.getZ() <= (double) (portZ + 5)
                        && loc.getZ() >= (double) (portZ - 5)) {
                    event.setCancelled(true);
                    ent.setInvulnerable(true);
                }
            }
        }

    }

    @EventHandler
    public void CancelEggTeleport(final BlockFromToEvent e) {
        final String world = e.getBlock().getWorld().getName().toLowerCase();
        if (this.plugin.checkWorld(world)) {
            final boolean DisableEggTeleport = this.plugin.configManager.getEggCancel(world);
            if (DisableEggTeleport && e.getBlock().getType() == Material.DRAGON_EGG) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void CancelEggTeleport(final BlockBreakEvent e) {
        final String world = e.getBlock().getWorld().getName().toLowerCase();
        if (this.plugin.checkWorld(world)) {
            final boolean DisableEggTeleport = this.plugin.configManager.getEggCancel(world);
            if (DisableEggTeleport && e.getBlock().getType() == Material.DRAGON_EGG) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void CancelCrystalPlace(final PlayerInteractEvent event) {
        final Player p = event.getPlayer();
        final World ThisWorld = p.getWorld();
        final Block bl = event.getClickedBlock();
        final String world = ThisWorld.getName().toLowerCase();
        if (this.plugin.checkWorld(world)) {
            if ((this.plugin.configManager.getCrystalDeny(world) || this.plugin.configManager.getMultiPortal())
                    && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                    && (bl.getType().equals(Material.BEDROCK) || bl.getType().equals(Material.OBSIDIAN))) {
                final Material EndCrystal = Material.END_CRYSTAL;

                if (event.getMaterial().equals(EndCrystal)) {
                    final Location loc = bl.getLocation();
                    final int portX = this.plugin.configManager.getPortalXdef(world);
                    final int portZ = this.plugin.configManager.getPortalZdef(world);
                    if (this.plugin.configManager.getCrystalDeny(world) && loc.getX() <= (double) (portX + 5)
                            && loc.getX() >= (double) (portX - 5) && loc.getZ() <= (double) (portZ + 5)
                            && loc.getZ() >= (double) (portZ - 5)) {
                        event.setCancelled(true);
                        p.sendMessage(this.plugin.replaceValues(this.plugin.configManager.getCrystalDenyString(), world));
                    }

                    if (this.plugin.configManager.getMultiPortal()) {
                        for (int i = 1; i <= this.plugin.configManager.getMaxdragons(world); ++i) {
                            final int portX2 = this.plugin.configManager.getPortalX(world, i, true, true);
                            final int portZ2 = this.plugin.configManager.getPortalZ(world, i, true, true);
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
                        } catch (final Exception var13) {
                        }
                    }
                }

                foundBed = DragonSlayer.newRoutines14.getBedTag(bl.getType());

                if (foundBed) {
                    for (final EnderDragon drag : this.plugin.getDragonList(ThisWorld, world)) {
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
    public void CancelCrystalPlace2a(final BlockPistonExtendEvent event) {

        final ArrayList<Block> blocklist = new ArrayList<Block>();
        blocklist.addAll(event.getBlocks());
        this.blockPistonEvent(event, blocklist);

    }

    @EventHandler
    public void CancelCrystalPlace2b(final BlockPistonRetractEvent event) {

        final ArrayList<Block> blocklist = new ArrayList<Block>();
        blocklist.addAll(event.getBlocks());
        this.blockPistonEvent(event, blocklist);
    }

    private void blockPistonEvent(final BlockPistonEvent event, final ArrayList<Block> blocklist) {
        final BlockFace dir = event.getDirection();
        final Block movedOne = event.getBlock().getRelative(dir);
        final World ThisWorld = movedOne.getWorld();
        final String world = ThisWorld.getName().toLowerCase();
        if (this.plugin.checkWorld(world)
                && (this.plugin.configManager.getCrystalDeny(world) || this.plugin.configManager.getMultiPortal())) {
            final int portX = this.plugin.configManager.getPortalXdef(world);
            final int portZ = this.plugin.configManager.getPortalZdef(world);
            if (blocklist.size() == 0) {
                blocklist.add(movedOne);
            } else {
                final Block addOne = event.getBlock().getRelative(dir, blocklist.size() + 1);
                if (addOne.getType() == Material.AIR) {
                    blocklist.add(addOne);
                }
            }

            for (final Block block : blocklist) {
                final Entity[] entList = block.getChunk().getEntities();
                Entity foundCrystal = null;
                Location loc = block.getLocation();

                for (final Entity ent : entList) {
                    if (ent instanceof EnderCrystal) {
                        final Location entLoc = ent.getLocation().add(0.1D, 0.0D, 0.1D).getBlock().getLocation();
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
                            final int portX2 = this.plugin.configManager.getPortalX(world, i, true, true);
                            final int portZ2 = this.plugin.configManager.getPortalZ(world, i, true, true);
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
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (this.plugin.getStatueVersion() == 2 && DragonSlayer.protLibHandler != null) {
            final Player p = event.getPlayer();
            final Location p_loc = event.getTo();
            final Location as_loc = this.plugin.armorStandLoc(true);
            if (as_loc != null && p_loc.getWorld().equals(as_loc.getWorld())) {
                final double dist = as_loc.distance(p_loc);
                if (dist <= 10.0D && dist > 9.84D) {
                    DragonSlayer.protLibHandler.sendNPCClientPacket(p);
                }
            }
        }

    }
}
