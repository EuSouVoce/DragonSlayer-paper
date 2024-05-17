package de.jeppa.DragonSlayer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;

public class ProtLibHandler {
    DragonSlayer plugin;
    @SuppressWarnings("unused")
    private Constructor<?> pim_const = null;
    private Constructor<?> entp_const = null;

    private Class<?> ENT_Class = null;
    Object NPCStatue = null;
    Team team_NPC = null;
    private Object[] showNPCPackets = null;
    private Object[] showNPCPackets2 = null;
    boolean headHelmet = false;
    static String[] NPCtexture;
    Method enth_getProfile = null;
    Class<?> CraftPlayerClass = null;

    public ProtLibHandler(DragonSlayer instance) { this.plugin = instance; }

    /** {@link net.minecraft.world.entity.player.ProfilePublicKey} */

    @SuppressWarnings("deprecation")
    private Object setNPC_Stand(World world, double x, double y, double z, float yaw) {
        Object nmsWorld = this.plugin.getWorldServer(world);
        Object nmsServer = this.plugin.getMinecraftServer(world);
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
        Scoreboard _NPC_scoreboard = this.plugin.getServer().getScoreboardManager().getMainScoreboard();
        this.team_NPC = _NPC_scoreboard.getTeam("DSL-NPCs");
        if (this.team_NPC == null) {
            this.team_NPC = _NPC_scoreboard.registerNewTeam("DSL-NPCs");
        }

        this.team_NPC.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);

        if (!this.team_NPC.hasEntry(gameProfile.getName())) {
            for (String ent : this.team_NPC.getEntries()) {
                this.team_NPC.removeEntry(ent);
            }

            this.team_NPC.addEntry(gameProfile.getName());
        }

        if (NPCtexture != null && NPCtexture[0] != null && NPCtexture[1] != null) {
            this.headHelmet = false;
            gameProfile.getProperties().put("textures", new Property("textures", NPCtexture[0], NPCtexture[1]));
        } else {
            this.headHelmet = true;
        }

        Object pim = null;

        try {

            pim = true;
            if (this.ENT_Class == null) {
                this.ENT_Class = Class.forName("net.minecraft.world.entity.Entity");
            }

            if (pim != null) {

                try {
                    Class<?> clientInfo = Class.forName("net.minecraft.server.level.ClientInformation");
                    if (this.entp_const == null) {
                        this.entp_const = Class.forName("net.minecraft.server.level.ServerPlayer").getConstructor(
                                Class.forName("net.minecraft.server.MinecraftServer"), nmsWorld.getClass(), gameProfile.getClass(),
                                clientInfo);
                    }

                    Method createDefault = this.plugin.getMethodByName(clientInfo, "createDefault");

                    this.NPCStatue = this.entp_const.newInstance(nmsServer, nmsWorld, gameProfile, createDefault.invoke(clientInfo));
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException ignored) {
                    if (DragonSlayer.debugOn)
                        ignored.printStackTrace();
                }

                if (this.NPCStatue != null) {
                    try {
                        ((ServerPlayer) this.NPCStatue).forceSetPositionRotation(x, y, z, yaw, -10.0F);
                    } catch (NullPointerException var17) {
                    }
                }

                Entity ent_ = this.getBukkitEntity(this.NPCStatue);
                if (ent_ != null) {
                    ent_.setCustomName(this.plugin.configManager.getSlayerPAPIFormatNickString());
                    ent_.setCustomNameVisible(true);
                    this.plugin.setNPCStatueMeta(ent_, this.plugin.getSlayerUUIDString(), (String) null);
                    this.saveTextureToMeta(ent_, NPCtexture);
                    this.plugin.setNPCStatueMeta(ent_, String.valueOf(yaw), "_yaw");
                }
            }
        } catch (IllegalArgumentException | SecurityException | ClassNotFoundException var20) {
            if (this.plugin.configManager.debugOn()) {
                var20.printStackTrace();
            }
        }

        NPCtexture = null;
        return this.NPCStatue;
    }

    @SuppressWarnings("deprecation")
    Object[] createNPCStatuePackets(Object NPCStatue) {
        Object[] packets = new Object[12];
        if (NPCStatue != null) {
            if (this.showNPCPackets != null) {
                return this.showNPCPackets;
            }

            try {
                Entity ent_ = this.getBukkitEntity(NPCStatue);
                int NPCStatue_id = ent_.getEntityId();
                PacketContainer addPlayerPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.PLAYER_INFO);
                boolean isNewProt = false;
                if (MinecraftProtocolVersion.getCurrentVersion() >= 761) {
                    isNewProt = true;
                    addPlayerPacket.getPlayerInfoActions().write(0, EnumSet.of(PlayerInfoAction.ADD_PLAYER));
                } else {
                    addPlayerPacket.getPlayerInfoAction().writeSafely(0, PlayerInfoAction.ADD_PLAYER);
                }

                WrappedGameProfile wrappedGameprofile = WrappedGameProfile.fromPlayer((Player) ent_);
                GameMode gM = ((Player) ent_).getGameMode() != null ? ((Player) ent_).getGameMode() : GameMode.SURVIVAL;

                WrappedChatComponent wrappedChatComp;
                try {
                    wrappedChatComp = WrappedChatComponent.fromText(ent_.getCustomName());
                } catch (IllegalStateException var23) {
                    wrappedChatComp = WrappedChatComponent.fromLegacyText(ent_.getCustomName());
                }

                PlayerInfoData playerInfoData = new PlayerInfoData(wrappedGameprofile, 0, NativeGameMode.fromBukkit(gM), wrappedChatComp);
                addPlayerPacket.getPlayerInfoDataLists().write(isNewProt ? 1 : 0, Collections.singletonList(playerInfoData));
                packets[0] = addPlayerPacket;
                float yaw_2 = this.getEntityYaw(NPCStatue);
                byte ent_rot_1 = (byte) ((int) (yaw_2 * 256.0F / 360.0F));
                byte ent_rot_2 = (byte) ((int) (ent_.getLocation().getPitch() * 256.0F / 360.0F));
                PacketContainer spawnPacket = null;

                spawnPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.SPAWN_ENTITY);
                spawnPacket.getIntegers().write(0, NPCStatue_id);
                spawnPacket.getUUIDs().write(0, ent_.getUniqueId());
                spawnPacket.getEntityTypeModifier().write(0, ent_.getType());
                spawnPacket.getDoubles().write(0, ent_.getLocation().getX()).write(1, ent_.getLocation().getY()).write(2,
                        ent_.getLocation().getZ());
                spawnPacket.getBytes().write(0, ent_rot_2).write(1, ent_rot_1).write(2, ent_rot_1);
                spawnPacket.getDataWatcherModifier().writeSafely(0, WrappedDataWatcher.getEntityWatcher((Player) ent_));
                packets[1] = spawnPacket;

                PacketContainer headRotationPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.ENTITY_HEAD_ROTATION);
                headRotationPacket.getIntegers().write(0, NPCStatue_id);
                headRotationPacket.getBytes().write(0, ent_rot_1);
                packets[3] = headRotationPacket;
                Material[] matList = this.plugin.getArmorMat();

                List<Pair<ItemSlot, ItemStack>> equipItems = new ArrayList<Pair<ItemSlot, ItemStack>>();
                equipItems.add(new Pair<ItemSlot, ItemStack>(ItemSlot.FEET, new ItemStack(matList[0])));
                equipItems.add(new Pair<ItemSlot, ItemStack>(ItemSlot.LEGS, new ItemStack(matList[2])));
                equipItems.add(new Pair<ItemSlot, ItemStack>(ItemSlot.CHEST, new ItemStack(matList[1])));
                if (this.headHelmet) {
                    equipItems.add(new Pair<ItemSlot, ItemStack>(ItemSlot.HEAD, this.plugin.getPlayerHead()));
                } else {
                    equipItems.add(new Pair<ItemSlot, ItemStack>(ItemSlot.HEAD, new ItemStack(matList[4])));
                }

                equipItems.add(new Pair<ItemSlot, ItemStack>(ItemSlot.MAINHAND, new ItemStack(matList[3])));
                if (DragonSlayer.getDragonSkull() != null) {
                    equipItems.add(new Pair<ItemSlot, ItemStack>(ItemSlot.OFFHAND, DragonSlayer.getDragonSkull()));
                }

                PacketContainer equipmentPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.ENTITY_EQUIPMENT);
                equipmentPacket.getModifier().writeDefaults();
                equipmentPacket.getIntegers().write(0, NPCStatue_id);
                equipmentPacket.getSlotStackPairLists().write(0, equipItems);
                packets[6] = equipmentPacket;

                this.showNPCPackets = packets;
            } catch (SecurityException | NullPointerException | IllegalArgumentException var24) {
                if (this.plugin.configManager.debugOn()) {
                    var24.printStackTrace();
                }
            }
        }

        return packets;
    }

    private float getEntityYaw(Object nms_ent) {
        try {
            return Float.parseFloat(this.plugin.getNPCStatueMeta(this.getBukkitEntity(nms_ent), "_yaw"));
        } catch (NumberFormatException | NullPointerException var3) {
            return this.getBukkitEntity(nms_ent).getLocation().getYaw();
        }
    }

    Object[] createNPCStatuePackets_2(Object NPCStatue) {
        Object[] packets = new Object[3];
        if (NPCStatue != null) {
            if (this.showNPCPackets2 != null) {
                return this.showNPCPackets2;
            }

            try {
                Entity ent_ = this.getBukkitEntity(NPCStatue);
                int NPCStatue_id = ent_.getEntityId();
                float yaw_2 = this.getEntityYaw(NPCStatue);
                byte ent_rot_1 = (byte) ((int) (yaw_2 * 256.0F / 360.0F));
                byte ent_rot_2 = (byte) ((int) (ent_.getLocation().getPitch() * 256.0F / 360.0F));
                PacketContainer headRotationPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.ENTITY_HEAD_ROTATION);
                headRotationPacket.getIntegers().write(0, NPCStatue_id);
                headRotationPacket.getBytes().write(0, ent_rot_1);
                packets[0] = headRotationPacket;

                PacketContainer lookPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.ENTITY_LOOK);
                lookPacket.getIntegers().write(0, NPCStatue_id);
                lookPacket.getBytes().write(0, ent_rot_1).write(1, ent_rot_2);
                lookPacket.getBooleans().write(0, true);
                packets[1] = lookPacket;

                PacketContainer animationPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.ANIMATION);
                animationPacket.getIntegers().write(0, NPCStatue_id).write(1, 0);
                packets[2] = animationPacket;
                this.showNPCPackets2 = packets;
            } catch (SecurityException | NullPointerException | IllegalArgumentException var10) {
                if (this.plugin.configManager.debugOn()) {
                    var10.printStackTrace();
                }
            }
        }

        return packets;
    }

    private Object[] createNPCStatueDestroyPacket(Object NPCStatue) {
        Object[] packets = new Object[1];
        if (NPCStatue != null) {
            try {
                Entity ent_ = this.getBukkitEntity(NPCStatue);
                int NPCStatue_id = ent_.getEntityId();
                int[] idArray = new int[] { NPCStatue_id };
                PacketContainer destroyEntityPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.ENTITY_DESTROY);

                try {
                    Class<?> intArrayClass = null;

                    try {
                        intArrayClass = Class.forName("it.unimi.dsi.fastutil.ints.IntArrayList");
                    } catch (ClassNotFoundException | NoClassDefFoundError var9) {
                        intArrayClass = Class.forName("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.IntArrayList");
                    }

                    if (intArrayClass != null) {
                        Object newArrayList = intArrayClass.getConstructor().newInstance();
                        intArrayClass.getDeclaredMethod("add", Integer.TYPE).invoke(newArrayList, NPCStatue_id);
                        if (newArrayList != null) {
                            destroyEntityPacket.getIntegers().writeSafely(0, idArray.length);
                            destroyEntityPacket.getModifier().write(0, newArrayList);
                        }
                    }
                } catch (RuntimeException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                        | ClassNotFoundException | InstantiationException | NoClassDefFoundError var10) {
                    if (this.plugin.configManager.debugOn()) {
                        var10.printStackTrace();
                    }
                }

                packets[0] = destroyEntityPacket;
            } catch (SecurityException | NullPointerException | IllegalArgumentException var11) {
                if (this.plugin.configManager.debugOn()) {
                    var11.printStackTrace();
                }
            }
        }

        return packets;
    }

    @SuppressWarnings({ "deprecation", "unused" })
    Object[] createNPCStatueRemovePacket(Object NPCStatue) {
        Object[] packets = new Object[1];
        if (NPCStatue != null) {
            try {
                Entity ent_ = this.getBukkitEntity(NPCStatue);
                WrappedGameProfile wrappedGameprofile = WrappedGameProfile.fromPlayer((Player) ent_);
                GameMode gM = ((Player) ent_).getGameMode() != null ? ((Player) ent_).getGameMode() : GameMode.SURVIVAL;

                WrappedChatComponent wrappedChatComp;
                try {
                    wrappedChatComp = WrappedChatComponent.fromText(ent_.getCustomName());
                } catch (IllegalStateException var9) {
                    wrappedChatComp = WrappedChatComponent.fromLegacyText(ent_.getCustomName());
                }

                PlayerInfoData playerInfoData = new PlayerInfoData(wrappedGameprofile, 0, NativeGameMode.fromBukkit(gM), wrappedChatComp);
                PacketContainer removePlayerPacket;

                removePlayerPacket = ProtocolLibrary.getProtocolManager().createPacket(Server.PLAYER_INFO_REMOVE);
                removePlayerPacket.getModifier().writeDefaults();
                removePlayerPacket.getLists(Converters.passthrough(UUID.class)).write(0, Arrays.asList(ent_.getUniqueId()));

                packets[0] = removePlayerPacket;
            } catch (SecurityException | IllegalArgumentException var10) {
                if (this.plugin.configManager.debugOn()) {
                    var10.printStackTrace();
                }
            }
        }

        return packets;
    }

    void sendNPCClientPacket(Player p) { this.sendNPCClientPackets(new Player[] { p }); }

    private void sendNPCClientPackets(Player[] p) {
        Object[] packets = this.createNPCStatuePackets(this.NPCStatue);

        for (Player pl : p) {
            this.sendNPCStatue_PL_PacketsToOnePlayer(pl, packets);
        }

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            Object[] packets2 = this.createNPCStatuePackets_2(this.NPCStatue);

            for (Player pl : p) {
                this.sendNPCStatue_PL_PacketsToOnePlayer(pl, packets2);
            }

            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                Object[] packets3 = this.createNPCStatueRemovePacket(this.NPCStatue);

                for (Player pl : p) {
                    this.sendNPCStatue_PL_PacketsToOnePlayer(pl, packets3);
                }

            }, 2L);
        }, 5L);
    }

    private void sendNPCStatue_PL_PacketsToOnePlayer(Player player, Object[] packets) {
        if (player != null) {
            for (Object packet : packets) {
                if (packet != null && packet instanceof PacketContainer) {
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, (PacketContainer) packet);
                    } catch (Exception var8) {
                        if (this.plugin.configManager.debugOn()) {
                            var8.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    void getNewTextureArray(Player player, boolean useConfig, boolean useAPI) {
        String[] texture = null;
        if (player != null) {
            texture = this.getPlayerTexture(player);
            if (texture[0] != null) {
                NPCtexture = texture;
                this.saveTextureToConfig(NPCtexture);
                return;
            }
        }

        if (useConfig) {
            texture = this.getTextureFromConfig();
            if (texture[0] != null) {
                NPCtexture = texture;
                return;
            }
        }

        if (useAPI) {
            texture = this.getApiTexture();
            if (texture[0] != null) {
                NPCtexture = texture;
                this.saveTextureToConfig(NPCtexture);
                return;
            }
        }

        this.saveTextureToConfig(NPCtexture);
    }

    private String[] getPlayerTexture(Player player) {
        String[] texture_and_sig = new String[2];
        Object entityPlayer = this.getEntityPlayer(player);

        try {
            if (this.enth_getProfile == null) {
                this.enth_getProfile = this.plugin.getMethodByName(Class.forName("net.minecraft.world.entity.player.Player"),
                        "getGameProfile");
            }

            GameProfile profile = (GameProfile) this.enth_getProfile.invoke(entityPlayer);
            Collection<Property> property_col = profile.getProperties().get("textures");
            if (property_col != null && !property_col.isEmpty()) {
                Property property = (Property) property_col.iterator().next();
                String texture = "";
                String sig = "";

                try {
                    texture = (String) property.getClass().getMethod("getValue").invoke(property);
                    sig = (String) property.getClass().getMethod("getSignature").invoke(property);
                } catch (NoSuchMethodException var12) {
                    try {
                        texture = (String) property.getClass().getMethod("value").invoke(property);
                        sig = (String) property.getClass().getMethod("signature").invoke(property);
                    } catch (NoSuchMethodException var11) {
                        if (this.plugin.configManager.debugOn()) {
                            var11.printStackTrace();
                        }
                    }
                }

                texture_and_sig[0] = texture;
                texture_and_sig[1] = sig;
            }
        } catch (IllegalArgumentException | InvocationTargetException | SecurityException | ClassNotFoundException
                | IllegalAccessException var13) {
            if (this.plugin.configManager.debugOn()) {
                var13.printStackTrace();
            }
        }

        return texture_and_sig;
    }

    private String[] getApiTexture() {
        String[] texture_and_sig = new String[2];
        UUID uuid = this.plugin.getSlayerUUID();
        if (uuid != null) {
            try {
                URL url_1 = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").toURL();
                InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());

                JsonElement textureProperty_;

                textureProperty_ = JsonParser.parseReader(reader_1);

                if (textureProperty_ != null && textureProperty_.isJsonObject()) {
                    JsonObject textureProperty = textureProperty_.getAsJsonObject().get("properties").getAsJsonArray().get(0)
                            .getAsJsonObject();
                    String texture = textureProperty.get("value").getAsString();
                    String sig = textureProperty.get("signature").getAsString();
                    texture_and_sig[0] = texture;
                    texture_and_sig[1] = sig;
                }
            } catch (IOException var10) {
                if (this.plugin.configManager.debugOn()) {
                    var10.printStackTrace();
                }
            }
        }

        return texture_and_sig;
    }

    private void saveTextureToConfig(String[] texture_and_sig) {
        String summ = null;
        if (texture_and_sig != null && texture_and_sig[0] != null && texture_and_sig[1] != null) {
            summ = Base64Coder.encodeString(texture_and_sig[0] + "_SIG_" + texture_and_sig[1]);
        }

        this.plugin.getConfig().set("statue.texture", summ);
        this.plugin.saveConfig();
    }

    private void saveTextureToMeta(Entity ent_, String[] texture_and_sig) {
        String summ = null;
        if (texture_and_sig != null && texture_and_sig[0] != null && texture_and_sig[1] != null) {
            summ = Base64Coder.encodeString(texture_and_sig[0] + "_SIG_" + texture_and_sig[1]);
        }

        this.plugin.setNPCStatueMeta(ent_, summ, "_tex");
    }

    private String[] getTextureFromConfig() {
        String texture = this.plugin.getConfig().getString("statue.texture");
        return this.decodeTextureFromString(texture);
    }

    private String[] getTextureFromMeta(Entity ent_) {
        String texture = this.plugin.getNPCStatueMeta(ent_, "_tex");
        return this.decodeTextureFromString(texture);
    }

    private String[] decodeTextureFromString(String texture) {
        String[] texture_and_sig = new String[2];
        if (texture != null) {
            String tex_uuid = null;
            String dec_tex = null;
            String dec_sig = null;

            try {
                texture = Base64Coder.decodeString(texture);
                int si = texture.indexOf("_SIG_");
                dec_tex = texture.substring(0, si);
                dec_sig = texture.substring(si + 5, texture.length());
                String dec = Base64Coder.decodeString(dec_tex);

                tex_uuid = JsonParser.parseString(dec).getAsJsonObject().get("profileId").getAsString();
            } catch (Exception var10) {
            }

            if ((tex_uuid == null || !tex_uuid.equalsIgnoreCase(this.plugin.getSlayerUUIDString().replaceAll("-", "")))
                    && !DragonSlayer.SkinsRestorerEnabled) {
                this.plugin.getConfig().set("statue.texture", (Object) null);
                this.plugin.saveConfig();
            } else {
                texture_and_sig[0] = dec_tex;
                texture_and_sig[1] = dec_sig;
            }
        }

        return texture_and_sig;
    }

    void removeNPCStatue() {
        if (this.NPCStatue != null) {
            try {
                Object[] packets = this.createNPCStatueDestroyPacket(this.NPCStatue);

                for (Player pl : this.plugin.getServer().getOnlinePlayers()) {
                    this.sendNPCStatue_PL_PacketsToOnePlayer(pl, packets);
                }
            } catch (SecurityException | IllegalArgumentException var4) {
                if (this.plugin.configManager.debugOn()) {
                    var4.printStackTrace();
                }
            }
        }

    }

    void replaceNPCStatue() {
        if (this.NPCStatue != null) {
            try {
                Entity ent_ = this.getBukkitEntity(this.NPCStatue);
                if (ent_ != null) {
                    String oldUUID = this.plugin.getNPCStatueMeta(ent_, (String) null);
                    if (!oldUUID.equals(this.plugin.getSlayerUUIDString())) {
                        if (NPCtexture == null) {
                            this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin,
                                    () -> this.getNewTextureArray((Player) null, false, true), 0L);
                        }
                    } else {
                        NPCtexture = this.getTextureFromMeta(ent_);
                    }

                    this.showNPCPackets = null;
                    this.showNPCPackets2 = null;
                }
            } catch (SecurityException | IllegalArgumentException var3) {
                if (this.plugin.configManager.debugOn()) {
                    var3.printStackTrace();
                }
            }
        }

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            this.removeNPCStatue();
            Location theArmorStandLoc = this.plugin.armorStandLoc(false);
            if (theArmorStandLoc != null) {
                this.setNPC_Stand(this.plugin.getDragonWorldFromString(theArmorStandLoc.getWorld().getName()), theArmorStandLoc.getX(),
                        theArmorStandLoc.getY(), theArmorStandLoc.getZ(), theArmorStandLoc.getYaw());
                Player[] p = (Player[]) this.plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
                this.sendNPCClientPackets(p);
            }

        }, 2L);
    }

    Object getEntityPlayer(Player player) {
        Object returnwert = null;

        try {
            if (this.CraftPlayerClass == null) {
                this.CraftPlayerClass = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            }

            if (this.CraftPlayerClass.isInstance(player)) {
                Object craftPlayer = this.CraftPlayerClass.cast(player);
                returnwert = this.CraftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayer);
            }
        } catch (SecurityException | NullPointerException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | ClassNotFoundException var4) {
            this.plugin.logger.warning("Unknown or unsupported Version :" + DragonSlayer.getVersion() + ", can't handle EntityPlayer...?");
            if (this.plugin.configManager.debugOn()) {
                var4.printStackTrace();
            }
        }

        return returnwert;
    }

    Entity getBukkitEntity(Object NPCStatue) {
        Entity ent_ = null;

        try {
            Method getCraftEnt = this.plugin.getMethodByReturntype(this.ENT_Class, "CraftEntity", new Class[0], true);
            ent_ = (Entity) getCraftEnt.invoke(NPCStatue);
        } catch (IllegalArgumentException | InvocationTargetException | SecurityException | IllegalAccessException var4) {
            if (this.plugin.configManager.debugOn()) {
                var4.printStackTrace();
            }
        }

        return ent_;
    }
}
