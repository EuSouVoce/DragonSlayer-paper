package de.jeppa.DragonSlayer;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.Nullable;

public class DragonRespawn implements Runnable {
    DragonSlayer plugin;
    public String worldName = null;
    public long StartTime = System.currentTimeMillis() / 50L;
    public long OrigRuntime = 0L;
    public int taskId;

    public DragonRespawn(final DragonSlayer instance) {
        this.plugin = instance;
        this.plugin.timerManager.RespawnList.add(this);
    }

    @Override
    public void run() {
        if (this.worldName != null
                && this.plugin.getDragonCount(this.worldName) < this.plugin.configManager.getMaxdragons(this.worldName)) {
            final Location dragonSpawn = this.plugin.configManager.getDragonSpawn(this.worldName);
            final World world = this.plugin.getDragonWorldFromString(this.worldName);
            if (world == null) {
                return;
            }

            final Chunk chunk = world.getChunkAt(dragonSpawn);
            if (!chunk.isLoaded()) {
                final boolean load = chunk.load();
                if (!load) {
                    this.plugin.getLogger().warning("Failed to load Chunk: " + chunk.toString());
                }
            }

            try {
                world.spawn(dragonSpawn, EnderDragon.class, (@Nullable Consumer<? super EnderDragon>) this::setWerte);
            } catch (final NoSuchMethodError var7) {
                if (this.plugin.configManager.debugOn()) {
                    this.plugin.getLogger().info("Dragonspawn: New spawn method with consumer not possible, using NMS method...");
                }

                try {
                    this.BukkitNMSSpawn(dragonSpawn);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException | ClassNotFoundException var6) {
                }
            }
        }

        this.plugin.timerManager.RespawnList.remove(this);
    }

    private EnderDragon BukkitNMSSpawn(final Location dragonSpawn) throws ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        final SpawnReason spawnReason = SpawnReason.DEFAULT;
        final Object CraftWorld_o = this.plugin.getCraftWorld(dragonSpawn.getWorld());
        final Object entity = CraftWorld_o.getClass().getMethod("createEntity", Location.class, Class.class).invoke(CraftWorld_o,
                dragonSpawn, EntityType.ENDER_DRAGON.getEntityClass());
        final EnderDragon dragon = (EnderDragon) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
        this.setWerte(dragon);

        final Class<?> entClass = Class.forName("net.minecraft.world.entity.Entity");

        CraftWorld_o.getClass().getMethod("addEntityToWorld", entClass, spawnReason.getClass()).invoke(CraftWorld_o, entity, spawnReason);
        return dragon;
    }

    private void setWerte(final EnderDragon drag) {
        drag.setPhase(Phase.CIRCLING);
        this.plugin.setDragonPosMeta(drag, drag.getLocation());
    }
}
