package de.jeppa.DragonSlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import java.util.Random;
import java.util.Set;

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
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.EconomyResponse;

public class DragonEvents implements Listener {
    DragonSlayer plugin;
    Random random = new Random();

    private static volatile int[] EnderDragonPhases;

    public DragonEvents(final DragonSlayer instance) { this.plugin = instance; }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onDragonDeath(final EntityDeathEvent event) {
        if (event.getEntity() instanceof final Entity entity) {
            final @Nullable EntityDamageEvent e = entity.getLastDamageCause();
            final World world = entity.getWorld();
            final String worldName = world.getName().toLowerCase();
            if (this.plugin.checkWorld(worldName) && entity instanceof final EnderDragon dragon && this.plugin.checkDSLDragon(dragon)) {
                Object damager = null;

                if (e instanceof EntityDamageByEntityEvent) {
                    try {
                        damager = ((EntityDamageByEntityEvent) e).getDamager();
                    } catch (final Exception var30) {
                    }
                } else if (e instanceof EntityDamageByBlockEvent) {
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
                    killer = dragon.getKiller();
                }

                final int dragID = this.plugin.getDragonIDMeta(dragon) == -1 ? 0 : this.plugin.getDragonIDMeta(dragon);
                /*
                 * if (dragID == -1) { dragID = 0; }
                 */
                final int DragonIDPortal = this.plugin.configManager.getMultiPortal() ? dragID : 0;
                if (!this.plugin.configManager.getEggItem(worldName)) {
                    this.dropDragonEgg(entity.getLocation(), DragonIDPortal);
                } else {
                    this.dropDragonEggItem(event, DragonIDPortal);
                }

                if (this.plugin.configManager.getSkullItem(worldName)) {
                    this.dropDragonHeadItem(event);
                }

                if (entity.getWorld().getEnvironment() == Environment.THE_END && dragon.getDragonBattle() == null) {
                    this.plugin.setEDBPreviouslyKilled(dragon, true);
                }

                boolean slayerSet = false;
                if (!this.plugin.configManager.getAlternativeReward(worldName)) {
                    int XPAmount = 12000;
                    if (this.plugin.getEnderDragonPreviouslyKilled(dragon)) {
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
                            final double playersDamageReward = percentage
                                    * (double) this.plugin.configManager.getDragonExp(worldName, dragID);
                            player.giveExp((int) playersDamageReward);
                            String RewardMessage = this.plugin.configManager.getXPRewardMessage(worldName,
                                    String.valueOf((int) playersDamageReward), dragID);
                            player.sendMessage(RewardMessage);
                            if (DragonSlayer.econ != null) {
                                final double reward = percentage * this.plugin.configManager.getReward_double(worldName, dragID);
                                final EconomyResponse r = DragonSlayer.econ.depositPlayer(player, reward);
                                RewardMessage = this.plugin.configManager.getRewardMessage(worldName, String.valueOf((int) reward), dragID);
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
                        final List<String> command = this.plugin.configManager.getRankCommand(worldName, val.intValue(), dragID);
                        if (command != null && !command.isEmpty()) {
                            final String percentage = String.format("%d", (int) (DragonMeta.get(player) / allDamage * 100.0D + 0.5D));
                            command.replaceAll(com -> com.replace("$player", player.getName())
                                    .replace("$rank", String.valueOf(val.intValue())).replace("$percent", percentage));
                            this.plugin.myCommandsHandler(command, world, player);
                        }

                        if (this.plugin.configManager.getSlayerByPercent(worldName) && val.intValue() == 1) {
                            this.setSlayer(player, worldName, dragID);
                            slayerSet = true;
                        }
                    }
                }

                if (killer != null) {
                    if (!slayerSet) {
                        this.setSlayer(killer, worldName, dragID);
                    }

                    if (DragonSlayer.econ != null && !this.plugin.configManager.getAlternativeReward(worldName)) {
                        final double reward = this.plugin.configManager.getReward_double(worldName, dragID);
                        if (reward > 0.0D) {
                            final EconomyResponse r = DragonSlayer.econ.depositPlayer(killer, reward);
                            final String RewardMessage = this.plugin.configManager.getRewardMessage(worldName, String.valueOf(reward),
                                    dragID);
                            if (r.transactionSuccess() && !RewardMessage.equals("")) {
                                killer.sendMessage(RewardMessage);
                            }
                        }
                    }
                } else if (damager != null) {
                    // String dName = "";
                    final Component damagerName = Component.empty();
                    if (damager instanceof Player) {
                        // dName = ((Player) damager).getDisplayName();
                        damagerName.append(((Player) damager).displayName());
                    } else if (damager instanceof Entity) {
                        // dName = ((Entity) damager).getName();
                        damagerName.append((((Entity) damager).customName()) != Component.empty() ? ((Entity) damager).customName()
                                : Component.text().content(((Entity) damager).getName()));
                    } else if (damager instanceof Block) {
                        // dName = ((Block) damager).getType().name();
                        damagerName.append(Component.translatable(((Block) damager)));
                    } else if (damager instanceof Material) {
                        damagerName.append(Component.translatable(((Material) damager)));
                    }

                    Bukkit.getServer()
                            .broadcast(LegacyComponentSerializer.legacyAmpersand()
                                    .deserialize(this.plugin.replaceValues(this.plugin.configManager.getDiedMessage(), worldName))
                                    .appendSpace().append(damagerName));
                } else {
                    if (e != null) {
                        Bukkit.getServer().broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                this.plugin.replaceValues(this.plugin.configManager.getDiedMessage() + " " + e.getCause(), worldName)));
                    } else {
                        Bukkit.getServer().broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                this.plugin.replaceValues(this.plugin.configManager.getDiedMessage() + " " + DamageCause.KILL, worldName)));
                    }
                }

                this.plugin.setEndGatewayPortals(entity.getWorld());
                DragonSlayer.resetDragonsBossbar(entity);
                Location DelPortLoc = null;
                double TempX = (double) this.plugin.configManager.getPortalX(worldName, dragID);
                double TempZ = (double) this.plugin.configManager.getPortalZ(worldName, dragID);
                if (this.plugin.configManager.getOldPortal(worldName)) {
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

                if (this.plugin.configManager.getResetWorld(worldName) || this.plugin.configManager.getRespawnPlayer(worldName)
                        || this.plugin.configManager.getRefreshWorld(worldName)) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        final int aktCount = this.checkCount(worldName);
                        if (aktCount <= 0) {
                            this.plugin.StartWorldResetTimer(worldName, (long) this.plugin.configManager.getResetDelay(worldName),
                                    (long) this.plugin.configManager.getWarnTime(worldName));
                        }

                    });
                }

                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    final int delay = this.plugin.configManager.getDelay(worldName);
                    if (delay > 0 && !this.plugin.configManager.getNoAutoRespawn(worldName, dragID)) {
                        this.plugin.timerManager.StartTimer(worldName, (long) delay);
                    }

                    this.plugin.cleanupGlowTeamList();
                });
                this.plugin.replaceArmorStand();
                this.plugin.AtKillCommand(worldName, killer, dragon);
            }
        }

    }

    @SuppressWarnings("deprecation")
    private void setSlayer(final Player slayer, final String worldName, final int dragonId) {
        final String oldSlayer = this.plugin.getSlayer();
        this.plugin.setSlayer(slayer);
        final String slayerName = this.plugin.getSlayer();
        if (!slayerName.equals(oldSlayer)) {
            final String KillMessage = this.plugin.configManager.getDragonKillMessage(worldName, dragonId);
            if (!KillMessage.equals("")) {
                Bukkit.getServer().broadcastMessage(KillMessage);
            }
        } else {
            final String ReKillMessage = this.plugin.configManager.getDragonReKillMessage(worldName, dragonId);
            if (!ReKillMessage.equals("")) {
                Bukkit.getServer().broadcastMessage(ReKillMessage);
            }
        }

    }

    private int checkCount(final String worldName) { return this.plugin.getDragonCount(worldName); }

    /**
     * This function deletes and sets a portal at specified locations based on
     * certain conditions and configurations.
     * 
     * @param position     The `position` parameter is of type Location and
     *                         represents the location where a portal will be
     *                         placed.
     * @param deletePortal `DelPortLoc2` is a `Location` object representing the
     *                         location of a portal that needs to be deleted before
     *                         setting a new portal.
     * @param portalId     The `portalId` parameter in the `PortalDelAndSet` method
     *                         is an integer value that represents the identifier of
     *                         the portal being manipulated. It is used to determine
     *                         the specific portal that needs to be created or
     *                         modified within the plugin's functionality.
     * @param endportal    The `endportal` parameter in the `PortalDelAndSet` method
     *                         is an integer that represents the type of portal to
     *                         be placed at the specified position. It is used to
     *                         determine the type of portal that will be created
     *                         when calling the `placePortal` method within the
     *                         `PortalDelAndSet
     */
    private void PortalDelAndSet(final Location position, final Location deletePortal, final int portalId, final int endportal) {
        final String worldName = position.getWorld().getName().toLowerCase();
        if (deletePortal != null) {
            this.plugin.placePortal(deletePortal, -1);
        }

        if (this.plugin.configManager.getCreatePortal(worldName, portalId)) {
            if (position.getWorld() == Bukkit.getServer().getWorld(worldName)) {
                this.plugin.placePortal(position, endportal);
                if (endportal > 0 && !this.plugin.configManager.checkCreatePortalID(worldName, portalId)) {
                    this.plugin.configManager.setCreatePortal(false, worldName);
                }
            } else if (this.plugin.configManager.getVerbosity()) {
                this.plugin.getLogger().warning("Portal timer was still running while world was new!");
            }
        }

    }

    private void dropDragonEgg(final Location location, final int portalId) {
        final String worldName = location.getWorld().getName().toLowerCase();
        final int i = this.random.nextInt(100);
        if (i < this.plugin.configManager.getDragonEggChance(worldName) && (!this.plugin.configManager.getCreatePortal(worldName, portalId)
                || this.plugin.configManager.getCreatePortal(worldName, portalId)
                        && this.plugin.configManager.getPortalEggChance(worldName) == 0)) {
            location.getBlock().setType(Material.DRAGON_EGG);
        }

    }

    private void dropDragonEggItem(final EntityDeathEvent event, final int portalId) {
        final String worldName = event.getEntity().getWorld().getName().toLowerCase();
        final int random = this.random.nextInt(100);
        if (random < this.plugin.configManager.getDragonEggChance(worldName)
                && (!this.plugin.configManager.getCreatePortal(worldName, portalId)
                        || this.plugin.configManager.getCreatePortal(worldName, portalId)
                                && this.plugin.configManager.getPortalEggChance(worldName) == 0)) {
            final ItemStack dragEggItem = new ItemStack(Material.DRAGON_EGG);
            event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), dragEggItem);
        }

    }

    private void dropDragonHeadItem(final EntityDeathEvent event) {
        final String world = event.getEntity().getWorld().getName().toLowerCase();
        final int random = this.random.nextInt(100);
        if (random < this.plugin.configManager.getSkullChance(world)) {
            final Collection<ItemStack> Reward = new HashSet<ItemStack>();
            ItemStack DragonSkull = null;

            DragonSkull = new ItemStack(Material.DRAGON_HEAD);

            Reward.add(DragonSkull);
            event.getDrops().addAll(Reward);
        }

    }

    private void dropDragonXP(final Entity entity, final int Amount, final int dragonId) {
        final World world = entity.getWorld();
        int PlannedAmount = this.plugin.configManager.getDragonExp(world.getName().toLowerCase(), dragonId);
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
        Entity entitiy = event.getEntity();
        if (entitiy instanceof final ComplexEntityPart complexEntityPart)
            entitiy = complexEntityPart.getParent();

        final Location fromLoc = event.getFrom();
        final World theWorld = fromLoc.getWorld();
        if (entitiy instanceof EnderDragon && this.plugin.checkWorld(theWorld.getName().toLowerCase())) {
            final Block check = DragonSlayer.CheckGatewaysForDragon(theWorld, fromLoc, 14);
            if (check != null) {
                event.setCancelled(true);
                return;
            }
        }

    }

    @EventHandler
    public void onOrbSpawn(final EntityTargetLivingEntityEvent event) {
        final Entity entity = event.getEntity();
        final String worldName = entity.getWorld().getName().toString().toLowerCase();
        if (this.plugin.checkWorld(worldName) && entity.getWorld().getEnvironment() == Environment.THE_END
                && entity instanceof final ExperienceOrb orb && this.plugin.configManager.getAlternativeReward(worldName)
                && orb.getExperience() > 499) {
            event.setCancelled(true);
            orb.remove();
        }

    }

    @EventHandler
    public void onOrbGrab(final PlayerExpChangeEvent event) {
        final Player player = event.getPlayer();
        final String worldName = player.getWorld().getName().toLowerCase();
        if (this.plugin.checkWorld(worldName) && player.getWorld().getEnvironment() == Environment.THE_END) {
            for (final Entity orb : player.getNearbyEntities(2.0D, 2.0D, 2.0D)) {
                if (orb instanceof ExperienceOrb && this.plugin.configManager.getAlternativeReward(worldName)
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
        final Entity entitiy = e.getEntity();
        if (entitiy.getTicksLived() <= 10) {
            final World world = entitiy.getWorld();
            final String worldName = world.getName().toLowerCase();
            if (this.plugin.checkWorld(worldName) && entitiy instanceof final EnderDragon dragon) {
                final SpawnReason commandReason = SpawnReason.COMMAND;

                if (this.plugin.checkDSLDragon(dragon) || world.getEnvironment() == Environment.THE_END
                        && (commandReason != null && e.getSpawnReason().equals(commandReason)
                                || e.getSpawnReason().equals(SpawnReason.DEFAULT))
                        && this.plugin.checkOrigDragon(dragon)) {
                    this.plugin.OrigEnderDragonSetKilled(dragon);
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                        if (dragon != null) {
                            this.plugin.OrigEnderDragonSetKilled(dragon);
                        }

                    }, 2L);
                    if (this.plugin.ProtectResetWorlds.contains(world)) {
                        this.plugin.ProtectResetWorlds.remove(world);
                        if (this.plugin.configManager.getDisableOrigDragonRespawn(worldName)) {
                            e.setCancelled(true);
                            dragon.remove();
                            return;
                        }
                    }

                    this.plugin.findAndUseEndgateways(worldName);
                    final int aktCountByCounting = this.plugin.getDragonCount(worldName);
                    final int maxCount = this.plugin.configManager.getOneByOne(worldName) ? 1
                            : this.plugin.configManager.getMaxdragons(worldName);
                    final int Delay = this.plugin.configManager.getDelay(worldName);
                    if (aktCountByCounting >= maxCount && Delay != 0) {
                        e.setCancelled(true);
                        dragon.remove();
                    } else {
                        dragon.setPhase(Phase.CIRCLING);
                        final String[] dragNameAndID = this.plugin.configManager.getDragonNameAndID(worldName);
                        final String dragName = dragNameAndID[0];
                        final int dragonId = Integer.parseInt(dragNameAndID[1]);
                        if (!dragName.isEmpty()) {
                            dragon.setCustomName(dragName);
                        }

                        dragon.setCustomNameVisible(this.plugin.configManager.getDisplayDragonName(worldName));
                        final boolean glow = this.plugin.configManager.getGlowEffect(worldName);
                        if (glow) {
                            this.plugin.handleGlowTeams(world, dragonId, dragon.getUniqueId().toString());
                        }

                        dragon.setGlowing(glow);
                        this.plugin.setDragonIDMeta(dragon, dragonId);
                        if (DragonSlayer.getDragonPosMeta(dragon) == null) {
                            this.plugin.setDragonPosMeta(dragon, dragon.getLocation());
                        }

                        final int newHealth = this.plugin.configManager.getDragonHealth_n(worldName, dragonId);
                        if (newHealth > 0) {
                            dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue((double) newHealth);
                            dragon.setHealth((double) newHealth);
                        }

                        if (!dragon.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getModifiers().isEmpty()) {
                            dragon.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).removeModifier((AttributeModifier) dragon
                                    .getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getModifiers().iterator().next());
                        }

                        final int range = this.plugin.configManager.getDragonRange(worldName, dragonId);
                        dragon.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue((double) range);
                        final BossBar BossBar = this.plugin.findFreeBar(worldName);
                        if (BossBar != null) {
                            BossBar.setTitle(dragon.getName());
                            this.plugin.setBossBarAmountNOW(dragon, BossBar);
                            DragonSlayer.putBossBarToDragon(dragon, BossBar);
                            this.plugin.FindPlayerAndAddToBossBar(BossBar, dragon);
                        }

                        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.plugin.setDragonNavi(dragon), 2L);
                        this.PlayDragonSound(world);
                        final String RespawnMessage = this.plugin.configManager.getRespawnMessage(worldName, dragonId);
                        if (!RespawnMessage.equals("")) {
                            Bukkit.getServer().broadcastMessage(RespawnMessage);
                        }

                        final List<String> command = this.plugin.configManager.getSpawnCommand(worldName, dragonId);
                        if (!command.isEmpty()) {
                            this.plugin.myCommandsHandler(command, world, (Player) null);
                        }

                        this.plugin.stopResetTimer(worldName);
                    }
                }
            }

        }
    }

    private void PlayDragonSound(final World world) {
        final Sound growl = Sound.ENTITY_ENDER_DRAGON_GROWL;

        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (growl != null && (this.plugin.configManager.getNoSpawnSound() && player.getWorld().equals(world)
                    || !this.plugin.configManager.getNoSpawnSound())) {
                player.playSound(player.getLocation(), growl, 1.0F, 0.0F);
            }
        }

    }

    @EventHandler
    public void stopDragonDamageOrCrystalExplosion(final EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        String worldName = entity.getWorld().getName();
        if (this.plugin.checkWorld(worldName)) {
            worldName = worldName.toLowerCase();
            if (entity instanceof ComplexEntityPart) {
                entity = ((ComplexEntityPart) entity).getParent();
            }
            if (entity instanceof EnderDragon) {
                final boolean blockGrief = !this.plugin.configManager.getBlockGrief(worldName);
                final boolean denyCrystalExplosion = this.plugin.configManager.getDenyCrystalExplosion(worldName);

                if (blockGrief || denyCrystalExplosion) {
                    event.setCancelled(true);
                }
            }

        }

    }

    @EventHandler
    public void onDragonDamage(final EntityDamageByEntityEvent event) {
        final Entity entity = event.getDamager();
        if (entity instanceof final EnderDragon dragon) {
            final String world = entity.getWorld().getName();
            if (this.plugin.checkWorld(world)) {
                final int dragonID = this.plugin.getDragonIDMeta(dragon);
                if (!this.plugin.checkDSLDragon(dragon)) {
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
        final EnderDragon currentDragon = event.getEntity();
        final Phase newPhase = event.getNewPhase();
        final World world = currentDragon.getWorld();
        final String worldName = world.getName().toLowerCase();
        if (this.plugin.checkWorld(worldName)) {
            boolean setEDBforce = false;
            setEDBforce = switch (DragonEvents.EnderDragonPhases()[newPhase.ordinal()]) {
            case 3, 4, 5, 6, 7, 8, 10 -> true;
            case 9 -> false;
            default -> false;
            case 11 -> false;
            };

            final Collection<EnderDragon> dragons = world.getEntitiesByClass(EnderDragon.class);
            if (newPhase != Phase.DYING) {
                if (this.plugin.configManager.getMultiPortal() && !this.plugin.checkServerStarted() && newPhase != Phase.HOVER) {
                    event.setCancelled(true);
                    currentDragon.setPhase(Phase.HOVER);
                    return;
                }
            } else if (currentDragon.isValid()) {
                for (final EnderDragon Dragon : dragons) {
                    if (Dragon.getEntityId() != currentDragon.getEntityId() && Dragon.isValid()) {
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

            if (currentDragon.getTicksLived() > 200) {
                for (final EnderDragon dragon : dragons) {
                    if (dragon.getEntityId() != currentDragon.getEntityId() && dragon.isValid()) {
                        final Phase DragPhase = dragon.getPhase();
                        if (currentDragon.getLocation().distance(dragon.getLocation()) < 128.0D) {
                            if (newPhase == Phase.FLY_TO_PORTAL
                                    && (DragPhase == Phase.LAND_ON_PORTAL || DragPhase == Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
                                            || DragPhase == Phase.ROAR_BEFORE_ATTACK || DragPhase == Phase.BREATH_ATTACK)) {
                                event.setCancelled(true);
                                currentDragon.setPhase(Phase.CIRCLING);
                            } else if (newPhase != Phase.LAND_ON_PORTAL
                                    || DragPhase != Phase.LAND_ON_PORTAL && DragPhase != Phase.SEARCH_FOR_BREATH_ATTACK_TARGET
                                            && DragPhase != Phase.ROAR_BEFORE_ATTACK && DragPhase != Phase.BREATH_ATTACK) {
                                if ((newPhase == Phase.LAND_ON_PORTAL || newPhase == Phase.SEARCH_FOR_BREATH_ATTACK_TARGET)
                                        && DragPhase == Phase.FLY_TO_PORTAL) {
                                    dragon.setPhase(Phase.CIRCLING);
                                }
                            } else {
                                event.setCancelled(true);
                                currentDragon.setPhase(Phase.LEAVE_PORTAL);
                            }
                        }
                    }
                }
            }

            if (this.plugin.configManager.getOldPortal(worldName.toLowerCase())) {
                final int phase = DragonEvents.EnderDragonPhases()[newPhase.ordinal()];
                final Set<Integer> gotoPortalPhases = Set.of(3, 4);
                if (gotoPortalPhases.contains(phase)) {
                    event.setCancelled(true);
                    currentDragon.setPhase(Phase.CIRCLING);
                }
            }

            this.plugin.WorldGenEndTrophyPositionSet(currentDragon, setEDBforce);
        }

    }

    @EventHandler
    public void onDamageTheDragon(final EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        final double damage = event.getFinalDamage();
        if (entity instanceof final EnderDragon dragon && this.plugin.checkDSLDragon(dragon)) {
            this.plugin.setBossBarAmount(dragon);
            if (damager instanceof final Projectile p) {
                if (p.getShooter() instanceof final Player player)
                    damager = player;

            }

            if (damager instanceof final Player player) {
                this.plugin.setDragonDamageMeta(dragon, player, damage);
                final String worldName = dragon.getWorld().getName().toLowerCase();
                if (this.plugin.configManager.getHitEffect(worldName)) {
                    final boolean on = !this.plugin.configManager.getGlowEffect(worldName);
                    this.plugin.handleGlowTeams(dragon.getWorld(), this.plugin.getDragonIDMeta(dragon), dragon.getUniqueId().toString());
                    dragon.setGlowing(on);
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> dragon.setGlowing(!on), 10L);
                }
            }
        }

    }

    @EventHandler
    public void onHealTheDragon(final EntityRegainHealthEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof final EnderDragon dragon && this.plugin.checkDSLDragon(dragon)) {
            this.plugin.setBossBarAmount(dragon);
        }

    }

    static int[] EnderDragonPhases() {
        final int[] Phases = DragonEvents.EnderDragonPhases;
        if (DragonEvents.EnderDragonPhases != null) {
            return Phases;
        } else {
            final int[] allPhases = new int[Phase.values().length];

            allPhases[Phase.CIRCLING.ordinal()] = 1;
            allPhases[Phase.STRAFING.ordinal()] = 2;
            allPhases[Phase.FLY_TO_PORTAL.ordinal()] = 3;
            allPhases[Phase.LAND_ON_PORTAL.ordinal()] = 4;
            allPhases[Phase.LEAVE_PORTAL.ordinal()] = 5;
            allPhases[Phase.BREATH_ATTACK.ordinal()] = 6;
            allPhases[Phase.SEARCH_FOR_BREATH_ATTACK_TARGET.ordinal()] = 7;
            allPhases[Phase.ROAR_BEFORE_ATTACK.ordinal()] = 8;
            allPhases[Phase.CHARGE_PLAYER.ordinal()] = 9;
            allPhases[Phase.DYING.ordinal()] = 10;
            allPhases[Phase.HOVER.ordinal()] = 11;
            allPhases[Phase.HOVER.ordinal()] = 11;

            DragonEvents.EnderDragonPhases = allPhases;
            return allPhases;
        }
    }
}
