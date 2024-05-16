package de.jeppa.DragonSlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import net.milkbowl.vault.economy.EconomyResponse;

public class DragonEvents implements Listener {
    DragonSlayer plugin;
    Random random = new Random();
    // $FF: synthetic field
    private static volatile int[] $SWITCH_TABLE$org$bukkit$entity$EnderDragon$Phase;

    public DragonEvents(final DragonSlayer instance) { this.plugin = instance; }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @EventHandler
    public void onDragonDeath(final EntityDeathEvent event) {
        if (event.getEntity() instanceof Entity) {
            final Entity entity = event.getEntity();
            final EntityDamageEvent e = entity.getLastDamageCause();
            final World ThisWorld = entity.getWorld();
            final String w = ThisWorld.getName().toLowerCase();
            if (this.plugin.checkWorld(w) && entity instanceof EnderDragon && this.plugin.checkDSLDragon((EnderDragon) entity)) {
                Object damager = null;
                final String damageEventName = e.getEventName();
                if (damageEventName.equals("EntityDamageByEntityEvent")) {
                    try {
                        damager = ((EntityDamageByEntityEvent) e).getDamager();
                    } catch (final Exception var30) {
                    }
                } else if (damageEventName.equals("EntityDamageByBlockEvent")) {
                    try {
                        final Block damager_ = ((EntityDamageByBlockEvent) e).getDamager();
                        if (damager_ != null) {
                            damager = damager_.getType();
                        }

                        if (damager == null) {
                            damager = damager_;
                        }
                    } catch (ClassCastException | NullPointerException var29) {
                    }
                }

                Player killer = null;
                if (damager != null) {
                    if (damager instanceof Player) {
                        killer = (Player) damager;
                    } else if (damager instanceof final Projectile p) {
                        if (p.getShooter() instanceof Player) {
                            killer = (Player) p.getShooter();
                        }
                    } else if (damager instanceof TNTPrimed && ((TNTPrimed) damager).getSource() instanceof Player) {
                        killer = (Player) ((TNTPrimed) damager).getSource();
                    }
                }

                if (killer == null) {
                    killer = ((EnderDragon) entity).getKiller();
                }

                final int dragID = this.plugin.getDragonIDMeta((EnderDragon) entity) == -1 ? 0
                        : this.plugin.getDragonIDMeta((EnderDragon) entity);
                /*
                 * if (dragID == -1) { dragID = 0; }
                 */
                final int DragonIDPortal = this.plugin.configManager.getMultiPortal() ? dragID : 0;
                if (!this.plugin.configManager.getEggItem(w)) {
                    this.dropDragonEgg(entity.getLocation(), DragonIDPortal);
                } else {
                    this.dropDragonEggItem(event, DragonIDPortal);
                }

                if (this.plugin.configManager.getSkullItem(w)) {
                    this.dropDragonHeadItem(event);
                }

                if (entity.getWorld().getEnvironment() == Environment.THE_END && ((EnderDragon) entity).getDragonBattle() == null) {
                    this.plugin.setEDBPreviouslyKilled((EnderDragon) entity, true);
                }

                boolean slayerSet = false;
                if (!this.plugin.configManager.getAlternativeReward(w)) {
                    int XPAmount = 12000;
                    if (this.plugin.getEnderDragonPreviouslyKilled((EnderDragon) entity)) {
                        XPAmount = 500;
                    }

                    this.dropDragonXP(entity, XPAmount, dragID);
                } else if (entity.hasMetadata("DSL-Damage")) {
                    double allDamage = 0.0D;
                    HashMap<Player, Double> DragonMeta = new HashMap<Player, Double>();
                    HashMap<Double, Player> orderList = new HashMap<Double, Player>();
                    final List<MetadataValue> list = entity.getMetadata("DSL-Damage");
                    if (list != null && list.size() != 0) {
                        try {
                            DragonMeta = (HashMap<Player, Double>) ((MetadataValue) list.get(0)).value();
                        } catch (final Exception var28) {
                        }
                    }

                    final Collection<Player> PlayerList = entity.getWorld().getPlayers();

                    for (final Player player : DragonMeta.keySet()) {
                        allDamage += PlayerList.contains(player) && DragonMeta.get(player) > 0.0D ? DragonMeta.get(player) : 0.0D;
                    }

                    for (final Player player : PlayerList) {
                        if (DragonMeta.get(player) != null && DragonMeta.get(player) > 0.0D) {
                            final double percentage = DragonMeta.get(player) / allDamage;
                            orderList.put(percentage, player);
                            final double playersDamageReward = percentage * (double) this.plugin.configManager.getDragonExp(w, dragID);
                            player.giveExp((int) playersDamageReward);
                            String RewardMessage = this.plugin.configManager.getXPRewardMessage(w,
                                    String.valueOf((int) playersDamageReward), dragID);
                            player.sendMessage(RewardMessage);
                            if (DragonSlayer.econ != null) {
                                final double reward = percentage * this.plugin.configManager.getReward_double(w, dragID);
                                final EconomyResponse r = DragonSlayer.econ.depositPlayer(player, reward);
                                RewardMessage = this.plugin.configManager.getRewardMessage(w, String.valueOf((int) reward), dragID);
                                if (r.transactionSuccess() && !RewardMessage.equals("")) {
                                    player.sendMessage(RewardMessage);
                                }
                            }
                        }
                    }

                    orderList = this.plugin.sortDamagersRanks(orderList);

                    for (final Entry<Double, Player> es : orderList.entrySet()) {
                        final Player player = (Player) es.getValue();
                        final Double val = (Double) es.getKey();
                        final List<String> command = this.plugin.configManager.getRankCommand(w, val.intValue(), dragID);
                        if (command != null && !command.isEmpty()) {
                            final String percentage = String.format("%d", (int) (DragonMeta.get(player) / allDamage * 100.0D + 0.5D));
                            command.replaceAll(com -> com.replace("$player", player.getName())
                                    .replace("$rank", String.valueOf(val.intValue())).replace("$percent", percentage));
                            this.plugin.myCommandsHandler(command, ThisWorld, player);
                        }

                        if (this.plugin.configManager.getSlayerByPercent(w) && val.intValue() == 1) {
                            this.setSlayer(player, w, dragID);
                            slayerSet = true;
                        }
                    }
                }

                if (killer != null) {
                    if (!slayerSet) {
                        this.setSlayer(killer, w, dragID);
                    }

                    if (DragonSlayer.econ != null && !this.plugin.configManager.getAlternativeReward(w)) {
                        final double reward = this.plugin.configManager.getReward_double(w, dragID);
                        if (reward > 0.0D) {
                            final EconomyResponse r = DragonSlayer.econ.depositPlayer(killer, reward);
                            final String RewardMessage = this.plugin.configManager.getRewardMessage(w, String.valueOf(reward), dragID);
                            if (r.transactionSuccess() && !RewardMessage.equals("")) {
                                killer.sendMessage(RewardMessage);
                            }
                        }
                    }
                } else if (damager != null) {
                    String dName = "";
                    if (damager instanceof Entity) {
                        dName = ((Entity) damager).getName();
                    } else if (damager instanceof Block) {
                        dName = ((Block) damager).getType().name();
                    } else if (damager instanceof Material) {
                        dName = ((Material) damager).name();
                    }

                    Bukkit.getServer()
                            .broadcastMessage(this.plugin.replaceValues(this.plugin.configManager.getDiedMessage() + " " + dName, w));
                } else {
                    Bukkit.getServer().broadcastMessage(
                            this.plugin.replaceValues(this.plugin.configManager.getDiedMessage() + " " + e.getCause(), w));
                }

                this.plugin.setEndGatewayPortals(entity.getWorld());
                DragonSlayer.resetDragonsBossbar(entity);
                Location DelPortLoc = null;
                double TempX = (double) this.plugin.configManager.getPortalX(w, dragID);
                double TempZ = (double) this.plugin.configManager.getPortalZ(w, dragID);
                if (this.plugin.configManager.getOldPortal(w)) {
                    DelPortLoc = DragonSlayer.findPosForPortal(TempX, TempZ, entity.getWorld(), Material.BEDROCK);
                    TempX = entity.getLocation().getX();
                    TempZ = entity.getLocation().getZ();
                }

                final Location PortLoc2 = DragonSlayer.findPosForPortal(TempX, TempZ, entity.getWorld(), Material.BEDROCK);
                final Location DelPortLoc2 = DelPortLoc;

                for (int i = 46; i <= 52; ++i) {
                    final int i1 = i;
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                        int endportal = 0;
                        if (i1 == 52) {
                            endportal = 1;
                        }

                        this.PortalDelAndSet(PortLoc2, DelPortLoc2, DragonIDPortal, endportal);
                    }, (long) i * 10L);
                }

                if (this.plugin.configManager.getResetWorld(w) || this.plugin.configManager.getRespawnPlayer(w)
                        || this.plugin.configManager.getRefreshWorld(w)) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        final int aktCount = this.checkCount(w);
                        if (aktCount <= 0) {
                            this.plugin.StartWorldResetTimer(w, (long) this.plugin.configManager.getResetDelay(w),
                                    (long) this.plugin.configManager.getWarnTime(w));
                        }

                    });
                }

                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    final int delay = this.plugin.configManager.getDelay(w);
                    if (delay > 0 && !this.plugin.configManager.getNoAutoRespawn(w, dragID)) {
                        this.plugin.timerManager.StartTimer(w, (long) delay);
                    }

                    this.plugin.cleanupGlowTeamList();
                });
                this.plugin.replaceArmorStand();
                this.plugin.AtKillCommand(w, killer, (EnderDragon) entity);
            }
        }

    }

    @SuppressWarnings("deprecation")
    private void setSlayer(final Player slayer, final String w, final int dragID) {
        final String oldSlayer = this.plugin.getSlayer();
        this.plugin.setSlayer(slayer);
        final String slayerName = this.plugin.getSlayer();
        if (!slayerName.equals(oldSlayer)) {
            final String KillMessage = this.plugin.configManager.getDragonKillMessage(w, dragID);
            if (!KillMessage.equals("")) {
                Bukkit.getServer().broadcastMessage(KillMessage);
            }
        } else {
            final String ReKillMessage = this.plugin.configManager.getDragonReKillMessage(w, dragID);
            if (!ReKillMessage.equals("")) {
                Bukkit.getServer().broadcastMessage(ReKillMessage);
            }
        }

    }

    private int checkCount(final String w) { return this.plugin.getDragonCount(w); }

    private void PortalDelAndSet(final Location PortLoc2, final Location DelPortLoc2, final int DragonID, final int endportal) {
        final String w = PortLoc2.getWorld().getName().toLowerCase();
        if (DelPortLoc2 != null) {
            this.plugin.placePortal(DelPortLoc2, -1);
        }

        if (this.plugin.configManager.getCreatePortal(w, DragonID)) {
            if (PortLoc2.getWorld() == Bukkit.getServer().getWorld(w)) {
                this.plugin.placePortal(PortLoc2, endportal);
                if (endportal > 0 && !this.plugin.configManager.checkCreatePortalID(w, DragonID)) {
                    this.plugin.configManager.setCreatePortal(false, w);
                }
            } else if (this.plugin.configManager.getVerbosity()) {
                this.plugin.getLogger().warning("Portal timer was still running while world was new!");
            }
        }

    }

    private void dropDragonEgg(final Location l, final int DragonIDPortal) {
        final String world = l.getWorld().getName().toLowerCase();
        final int i = this.random.nextInt(100);
        if (i < this.plugin.configManager.getDragonEggChance(world) && (!this.plugin.configManager.getCreatePortal(world, DragonIDPortal)
                || this.plugin.configManager.getCreatePortal(world, DragonIDPortal)
                        && this.plugin.configManager.getPortalEggChance(world) == 0)) {
            l.getBlock().setType(Material.DRAGON_EGG);
        }

    }

    private void dropDragonEggItem(final EntityDeathEvent event, final int DragonIDPortal) {
        final String world = event.getEntity().getWorld().getName().toLowerCase();
        final int i = this.random.nextInt(100);
        if (i < this.plugin.configManager.getDragonEggChance(world) && (!this.plugin.configManager.getCreatePortal(world, DragonIDPortal)
                || this.plugin.configManager.getCreatePortal(world, DragonIDPortal)
                        && this.plugin.configManager.getPortalEggChance(world) == 0)) {
            final ItemStack dragEggItem = new ItemStack(Material.DRAGON_EGG);
            event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), dragEggItem);
        }

    }

    private void dropDragonHeadItem(final EntityDeathEvent event) {
        final String world = event.getEntity().getWorld().getName().toLowerCase();
        final int i = this.random.nextInt(100);
        if (i < this.plugin.configManager.getSkullChance(world)) {
            final Collection<ItemStack> Reward = new HashSet<ItemStack>();
            ItemStack DragonSkull = null;

            DragonSkull = new ItemStack(Material.DRAGON_HEAD);

            Reward.add(DragonSkull);
            event.getDrops().addAll(Reward);
        }

    }

    private void dropDragonXP(final Entity entity, final int Amount, final int dragID) {
        final World world = entity.getWorld();
        int PlannedAmount = this.plugin.configManager.getDragonExp(world.getName().toLowerCase(), dragID);
        if (PlannedAmount > Amount) {
            PlannedAmount -= Amount;
            final int div = DragonSlayer.spigot && Bukkit.spigot().getConfig().getDouble("world-settings.default.merge-radius.exp") == 0.0D
                    ? 40
                    : 1;

            for (int i = 1; i <= div; ++i) {
                ((ExperienceOrb) world.spawn(entity.getLocation(), ExperienceOrb.class)).setExperience(PlannedAmount / div);
            }
        }

    }

    @EventHandler
    public void onDragonUseTransferportal(final EntityTeleportEvent event) {
        Entity e = event.getEntity();
        if (e instanceof ComplexEntityPart) {
            e = ((ComplexEntityPart) e).getParent();
        }

        final Location fromLoc = event.getFrom();
        final World theWorld = fromLoc.getWorld();
        if (e instanceof EnderDragon && this.plugin.checkWorld(theWorld.getName().toLowerCase())) {
            final Block check = DragonSlayer.CheckGatewaysForDragon(theWorld, fromLoc, 14);
            if (check != null) {
                event.setCancelled(true);
                return;
            }
        }

    }

    @EventHandler
    public void onOrbSpawn(final EntityTargetLivingEntityEvent event) {
        final Entity orb = event.getEntity();
        final String w = orb.getWorld().getName().toString().toLowerCase();
        if (this.plugin.checkWorld(w) && orb.getWorld().getEnvironment() == Environment.THE_END && orb instanceof ExperienceOrb
                && this.plugin.configManager.getAlternativeReward(w) && ((ExperienceOrb) orb).getExperience() > 499) {
            event.setCancelled(true);
            orb.remove();
        }

    }

    @EventHandler
    public void onOrbGrab(final PlayerExpChangeEvent event) {
        final Player player = event.getPlayer();
        final String w = player.getWorld().getName().toLowerCase();
        if (this.plugin.checkWorld(w) && player.getWorld().getEnvironment() == Environment.THE_END) {
            for (final Entity orb : player.getNearbyEntities(2.0D, 2.0D, 2.0D)) {
                if (orb instanceof ExperienceOrb && this.plugin.configManager.getAlternativeReward(w)
                        && ((ExperienceOrb) orb).getExperience() > 499 && event.getAmount() == ((ExperienceOrb) orb).getExperience()) {
                    event.setAmount(0);
                    orb.remove();
                }
            }
        }

    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onDragonSpawn(final CreatureSpawnEvent e) {
        final Entity ent = e.getEntity();
        if (ent.getTicksLived() <= 10) {
            final World ThisWorld = ent.getWorld();
            final String w = ThisWorld.getName().toLowerCase();
            if (this.plugin.checkWorld(w) && e.getEntityType() == EntityType.ENDER_DRAGON) {
                final EnderDragon ThisDrag = (EnderDragon) ent;
                SpawnReason commandReason = null;

                try {
                    commandReason = SpawnReason.COMMAND;
                } catch (final NoSuchFieldError var19) {
                }

                if (this.plugin.checkDSLDragon(ThisDrag) || ThisWorld.getEnvironment() == Environment.THE_END
                        && (commandReason != null && e.getSpawnReason().equals(commandReason)
                                || e.getSpawnReason().equals(SpawnReason.DEFAULT))
                        && this.plugin.checkOrigDragon(ThisDrag)) {
                    this.plugin.OrigEnderDragonSetKilled(ThisDrag);
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                        if (ThisDrag != null) {
                            this.plugin.OrigEnderDragonSetKilled(ThisDrag);
                        }

                    }, 2L);
                    if (this.plugin.ProtectResetWorlds.contains(ThisWorld)) {
                        this.plugin.ProtectResetWorlds.remove(ThisWorld);
                        if (this.plugin.configManager.getDisableOrigDragonRespawn(w)) {
                            e.setCancelled(true);
                            ThisDrag.remove();
                            return;
                        }
                    }

                    this.plugin.findAndUseEndgateways(w);
                    final int aktCountByCounting = this.plugin.getDragonCount(w);
                    final int maxCount = this.plugin.configManager.getOneByOne(w) ? 1 : this.plugin.configManager.getMaxdragons(w);
                    final int Delay = this.plugin.configManager.getDelay(w);
                    if (aktCountByCounting >= maxCount && Delay != 0) {
                        e.setCancelled(true);
                        ThisDrag.remove();
                    } else {
                        ThisDrag.setPhase(Phase.CIRCLING);
                        final String[] dragNameAndID = this.plugin.configManager.getDragonNameAndID(w);
                        final String dragName = dragNameAndID[0];
                        final int dragonId = Integer.parseInt(dragNameAndID[1]);
                        if (!dragName.isEmpty()) {
                            ThisDrag.setCustomName(dragName);
                        }

                        ThisDrag.setCustomNameVisible(this.plugin.configManager.getDisplayDragonName(w));
                        final boolean glow = this.plugin.configManager.getGlowEffect(w);
                        if (glow) {
                            this.plugin.handleGlowTeams(ThisWorld, dragonId, ThisDrag.getUniqueId().toString());
                        }

                        ThisDrag.setGlowing(glow);
                        this.plugin.setDragonIDMeta(ThisDrag, dragonId);
                        if (DragonSlayer.getDragonPosMeta(ThisDrag) == null) {
                            this.plugin.setDragonPosMeta(ThisDrag, ThisDrag.getLocation());
                        }

                        final int newHealth = this.plugin.configManager.getDragonHealth_n(w, dragonId);
                        if (newHealth > 0) {
                            ThisDrag.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue((double) newHealth);
                            ThisDrag.setHealth((double) newHealth);
                        }

                        if (!ThisDrag.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getModifiers().isEmpty()) {
                            ThisDrag.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).removeModifier((AttributeModifier) ThisDrag
                                    .getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getModifiers().iterator().next());
                        }

                        final int range = this.plugin.configManager.getDragonRange(w, dragonId);
                        ThisDrag.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue((double) range);
                        final BossBar BossBar = this.plugin.findFreeBar(w);
                        if (BossBar != null) {
                            BossBar.setTitle(ThisDrag.getName());
                            this.plugin.setBossBarAmountNOW(ThisDrag, BossBar);
                            DragonSlayer.putBossBarToDragon(ThisDrag, BossBar);
                            this.plugin.FindPlayerAndAddToBossBar(BossBar, ThisDrag);
                        }

                        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.plugin.setDragonNavi(ThisDrag), 2L);
                        this.PlayDragonSound(ThisWorld);
                        final String RespawnMessage = this.plugin.configManager.getRespawnMessage(w, dragonId);
                        if (!RespawnMessage.equals("")) {
                            Bukkit.getServer().broadcastMessage(RespawnMessage);
                        }

                        final List<String> command = this.plugin.configManager.getSpawnCommand(w, dragonId);
                        if (!command.isEmpty()) {
                            this.plugin.myCommandsHandler(command, ThisWorld, (Player) null);
                        }

                        this.plugin.stopResetTimer(w);
                    }
                }
            }

        }
    }

    private void PlayDragonSound(final World ThisWorld) {
        Sound Ton = null;

        try {
            Ton = Sound.valueOf("ENDERDRAGON_GROWL");
        } catch (final IllegalArgumentException var8) {
            try {
                Ton = Sound.valueOf("ENTITY_ENDERDRAGON_GROWL");
            } catch (final IllegalArgumentException var7) {
                try {
                    Ton = Sound.valueOf("ENTITY_ENDER_DRAGON_GROWL");
                } catch (final IllegalArgumentException var6) {
                }
            }
        }

        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (Ton != null && (this.plugin.configManager.getNoSpawnSound() && p.getWorld().equals(ThisWorld)
                    || !this.plugin.configManager.getNoSpawnSound())) {
                p.playSound(p.getLocation(), Ton, 1.0F, 0.0F);
            }
        }

    }

    @EventHandler
    public void stopDragonDamageOrCrystalExplosion(final EntityExplodeEvent event) {
        Entity e = event.getEntity();
        final String w = e.getWorld().getName();
        if (this.plugin.checkWorld(w)) {
            if (e instanceof ComplexEntityPart) {
                e = ((ComplexEntityPart) e).getParent();
            }

            if (e instanceof EnderDragon && !this.plugin.configManager.getBlockGrief(w.toLowerCase())) {
                event.setCancelled(true);
            }

            if (e instanceof EnderCrystal && this.plugin.configManager.getDenyCrystalExplosion(w.toLowerCase())) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onDragonDamage(final EntityDamageByEntityEvent event) {
        final Entity e = event.getDamager();
        if (e instanceof EnderDragon) {
            final String world = e.getWorld().getName();
            if (this.plugin.checkWorld(world)) {
                final int dragonID = this.plugin.getDragonIDMeta((EnderDragon) e);
                if (!this.plugin.checkDSLDragon((EnderDragon) e)) {
                    return;
                }

                final double dam = this.plugin.configManager.getDragonDamage(world.toLowerCase(), dragonID);
                if (dam > 0.0D) {
                    event.setDamage(dam);
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDragonMoves(final EnderDragonChangePhaseEvent event) {
        final EnderDragon ThisDrag = event.getEntity();
        final Phase NextPhase = event.getNewPhase();
        final World ThisWorld = ThisDrag.getWorld();
        final String w = ThisWorld.getName().toLowerCase();
        if (this.plugin.checkWorld(w)) {
            boolean setEDBforce = false;
            setEDBforce = switch (DragonEvents.$SWITCH_TABLE$org$bukkit$entity$EnderDragon$Phase()[NextPhase.ordinal()]) {
            case 3, 4, 5, 6, 7, 8, 10 -> true;
            case 9 -> false;
            default -> false;
            case 11 -> false;
            };

            final Collection<EnderDragon> DragonList = ThisWorld.getEntitiesByClass(EnderDragon.class);
            if (NextPhase != Phase.DYING) {
                if (this.plugin.configManager.getMultiPortal() && !this.plugin.checkServerStarted() && NextPhase != Phase.HOVER) {
                    event.setCancelled(true);
                    ThisDrag.setPhase(Phase.HOVER);
                    return;
                }
            } else if (ThisDrag.isValid()) {
                for (final EnderDragon Dragon : DragonList) {
                    if (Dragon.getEntityId() != ThisDrag.getEntityId() && Dragon.isValid()) {
                        final Phase DragPhase = Dragon.getPhase();
                        if (DragPhase != Phase.SEARCH_FOR_BREATH_ATTACK_TARGET && DragPhase != Phase.ROAR_BEFORE_ATTACK
                                && DragPhase != Phase.BREATH_ATTACK) {
                            if (DragPhase == Phase.FLY_TO_PORTAL || DragPhase == Phase.LAND_ON_PORTAL || DragPhase == Phase.HOVER) {
                                Dragon.setPhase(Phase.CIRCLING);
                            }
                        } else {
                            Dragon.setPhase(Phase.LEAVE_PORTAL);
                        }
                    }
                }
            }

            if (ThisDrag.getTicksLived() > 200) {
                for (final EnderDragon Dragon : DragonList) {
                    if (Dragon.getEntityId() != ThisDrag.getEntityId() && Dragon.isValid()) {
                        final Phase DragPhase = Dragon.getPhase();
                        if (ThisDrag.getLocation().distance(Dragon.getLocation()) < 128.0D) {
                            if (NextPhase == Phase.FLY_TO_PORTAL
                                    && (DragPhase == Phase.LAND_ON_PORTAL || DragPhase == Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
                                            || DragPhase == Phase.ROAR_BEFORE_ATTACK || DragPhase == Phase.BREATH_ATTACK)) {
                                event.setCancelled(true);
                                ThisDrag.setPhase(Phase.CIRCLING);
                            } else if (NextPhase != Phase.LAND_ON_PORTAL
                                    || DragPhase != Phase.LAND_ON_PORTAL && DragPhase != Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
                                            && DragPhase != Phase.ROAR_BEFORE_ATTACK && DragPhase != Phase.BREATH_ATTACK) {
                                if ((NextPhase == Phase.LAND_ON_PORTAL || NextPhase == Phase.SEARCH_FOR_BREATH_ATTACK_TARGET)
                                        && DragPhase == Phase.FLY_TO_PORTAL) {
                                    Dragon.setPhase(Phase.CIRCLING);
                                }
                            } else {
                                event.setCancelled(true);
                                ThisDrag.setPhase(Phase.LEAVE_PORTAL);
                            }
                        }
                    }
                }
            }

            if (this.plugin.configManager.getOldPortal(w.toLowerCase())) {
                switch (DragonEvents.$SWITCH_TABLE$org$bukkit$entity$EnderDragon$Phase()[NextPhase.ordinal()]) {
                case 1:
                case 2:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                default:
                    break;
                case 3:
                case 4:
                    event.setCancelled(true);
                    ThisDrag.setPhase(Phase.CIRCLING);
                }
            }

            this.plugin.WorldGenEndTrophyPositionSet(ThisDrag, setEDBforce);
        }

    }

    @EventHandler
    public void onDamageTheDragon(final EntityDamageByEntityEvent event) {
        final Entity e = event.getEntity();
        Entity damager = event.getDamager();
        final double schaden = event.getFinalDamage();
        if (e instanceof EnderDragon && this.plugin.checkDSLDragon((EnderDragon) e)) {
            this.plugin.setBossBarAmount((EnderDragon) e);
            if (damager instanceof final Projectile p) {
                if (p.getShooter() instanceof Player) {
                    damager = (Player) p.getShooter();
                }
            }

            if (damager instanceof Player) {
                this.plugin.setDragonDamageMeta((EnderDragon) e, (Player) damager, schaden);
                final String w = e.getWorld().getName().toLowerCase();
                if (this.plugin.configManager.getHitEffect(w)) {
                    final boolean on = !this.plugin.configManager.getGlowEffect(w);
                    this.plugin.handleGlowTeams(e.getWorld(), this.plugin.getDragonIDMeta((EnderDragon) e), e.getUniqueId().toString());
                    e.setGlowing(on);
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> e.setGlowing(!on), 10L);
                }
            }
        }

    }

    @EventHandler
    public void onHealTheDragon(final EntityRegainHealthEvent event) {
        final Entity e = event.getEntity();
        if (e instanceof EnderDragon && this.plugin.checkDSLDragon((EnderDragon) e)) {
            this.plugin.setBossBarAmount((EnderDragon) e);
        }

    }

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$org$bukkit$entity$EnderDragon$Phase() {
        final int[] var10000 = DragonEvents.$SWITCH_TABLE$org$bukkit$entity$EnderDragon$Phase;
        if (DragonEvents.$SWITCH_TABLE$org$bukkit$entity$EnderDragon$Phase != null) {
            return var10000;
        } else {
            final int[] var0 = new int[Phase.values().length];

            try {
                var0[Phase.BREATH_ATTACK.ordinal()] = 6;
            } catch (final NoSuchFieldError var11) {
            }

            try {
                var0[Phase.CHARGE_PLAYER.ordinal()] = 9;
            } catch (final NoSuchFieldError var10) {
            }

            try {
                var0[Phase.CIRCLING.ordinal()] = 1;
            } catch (final NoSuchFieldError var9) {
            }

            try {
                var0[Phase.DYING.ordinal()] = 10;
            } catch (final NoSuchFieldError var8) {
            }

            try {
                var0[Phase.FLY_TO_PORTAL.ordinal()] = 3;
            } catch (final NoSuchFieldError var7) {
            }

            try {
                var0[Phase.HOVER.ordinal()] = 11;
            } catch (final NoSuchFieldError var6) {
            }

            try {
                var0[Phase.LAND_ON_PORTAL.ordinal()] = 4;
            } catch (final NoSuchFieldError var5) {
            }

            try {
                var0[Phase.LEAVE_PORTAL.ordinal()] = 5;
            } catch (final NoSuchFieldError var4) {
            }

            try {
                var0[Phase.ROAR_BEFORE_ATTACK.ordinal()] = 8;
            } catch (final NoSuchFieldError var3) {
            }

            try {
                var0[Phase.SEARCH_FOR_BREATH_ATTACK_TARGET.ordinal()] = 7;
            } catch (final NoSuchFieldError var2) {
            }

            try {
                var0[Phase.STRAFING.ordinal()] = 2;
            } catch (final NoSuchFieldError var1) {
            }

            DragonEvents.$SWITCH_TABLE$org$bukkit$entity$EnderDragon$Phase = var0;
            return var0;
        }
    }
}
