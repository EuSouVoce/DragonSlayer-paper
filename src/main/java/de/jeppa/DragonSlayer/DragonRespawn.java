package de.jeppa.DragonSlayer;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class DragonRespawn implements Runnable {
    DragonSlayer plugin;
    public String Mapname = null;
    public long StartTime = System.currentTimeMillis() / 50L;
    public long OrigRuntime = 0L;
    public int taskId;

    public DragonRespawn(DragonSlayer instance) {
        this.plugin = instance;
        this.plugin.timerManager.RespawnList.add(this);
    }

    public void run() {
        if (this.Mapname != null && this.plugin.getDragonCount(this.Mapname) < this.plugin.configManager.getMaxdragons(this.Mapname)) {
            Location DragSpawnPos = this.plugin.configManager.getDragonSpawn(this.Mapname);
            World MyWorld = this.plugin.getDragonWorldFromString(this.Mapname);
            if (MyWorld == null) {
                return;
            }

            Chunk MyChunk = MyWorld.getChunkAt(DragSpawnPos);
            if (!MyChunk.isLoaded()) {
                boolean load = MyChunk.load();
                if (!load) {
                    this.plugin.getLogger().warning("Failed to load Chunk: " + MyChunk.toString());
                }
            }

            try {
                MyWorld.spawn(DragSpawnPos, EnderDragon.class, (drag) -> this.setWerte(drag));
            } catch (NoSuchMethodError var7) {
                if (this.plugin.configManager.debugOn()) {
                    this.plugin.getLogger().info("Dragonspawn: New spawn method with consumer not possible, using NMS method...");
                }

                try {
                    this.BukkitNMSSpawn(DragSpawnPos);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException | ClassNotFoundException var6) {
                }
            }
        }

        this.plugin.timerManager.RespawnList.remove(this);
    }

    private EnderDragon BukkitNMSSpawn(Location dragonSpawn) throws ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        SpawnReason spawnReason = SpawnReason.DEFAULT;
        Object CraftWorld_o = this.plugin.getCraftWorld(dragonSpawn.getWorld());
        Object entity = CraftWorld_o.getClass().getMethod("createEntity", Location.class, Class.class).invoke(CraftWorld_o, dragonSpawn,
                EntityType.ENDER_DRAGON.getEntityClass());
        EnderDragon dragon = (EnderDragon) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
        this.setWerte(dragon);

        Class<?> entClass;
        try {
            entClass = Class.forName("net.minecraft.world.entity.Entity");
        } catch (ClassNotFoundException var8) {
            entClass = Class.forName("net.minecraft.server." + DragonSlayer.getVersion() + ".Entity");
        }

        CraftWorld_o.getClass().getMethod("addEntity", entClass, spawnReason.getClass()).invoke(CraftWorld_o, entity, spawnReason);
        return dragon;
    }

    private void setWerte(EnderDragon drag) {
        drag.setPhase(Phase.CIRCLING);
        this.plugin.setDragonPosMeta(drag, drag.getLocation());
    }
}
