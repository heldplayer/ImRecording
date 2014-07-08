package me.heldplayer.mods.recording;

import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.heldplayer.mods.recording.client.ClientProxy;
import me.heldplayer.mods.recording.packet.Packet1SetState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.config.Configuration;
import net.specialattack.forge.core.ModInfo;
import net.specialattack.forge.core.SpACoreMod;
import net.specialattack.forge.core.SpACoreProxy;
import net.specialattack.forge.core.config.Config;
import net.specialattack.forge.core.config.ConfigCategory;
import net.specialattack.forge.core.config.ConfigValue;
import net.specialattack.forge.core.packet.PacketHandler;

import java.util.List;

@Mod(modid = Objects.MOD_ID, name = Objects.MOD_NAME, guiFactory = Objects.GUI_FACTORY)
public class ModRecording extends SpACoreMod {

    @Instance(value = Objects.MOD_ID)
    public static ModRecording instance;

    @SidedProxy(clientSide = Objects.CLIENT_PROXY, serverSide = Objects.SERVER_PROXY)
    public static CommonProxy proxy;

    // HeldCore Objects
    public static ConfigValue<ScreenLocation> screenLocation;
    public static ConfigValue<Boolean> chatMessages;
    public static ConfigValue<Boolean> lockOverlay;
    public static ConfigValue<Boolean> instantHide;

    public static PacketHandler packetHandler;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Objects.log = event.getModLog();

        ModRecording.packetHandler = new PacketHandler(Objects.MOD_CHANNEL, Packet1SetState.class);

        // Config
        ConfigCategory<?> category = new ConfigCategory(Configuration.CATEGORY_GENERAL, "config.imrecording.category.general", null, "General mod settings");
        ModRecording.screenLocation = new ConfigValue<ScreenLocation>("screenLocation", "config.imrecording.key.screenLocation", Side.CLIENT, ScreenLocation.TopRight, "Determines the location the GUI part of this mod is located in");
        ModRecording.chatMessages = new ConfigValue<Boolean>("chatMessages", "config.imrecording.key.chatMessages", null, Boolean.TRUE, "Set this to true to broadcast a chat message to every player when a player starts recording");
        ModRecording.lockOverlay = new ConfigValue<Boolean>("lockOverlay", "config.imrecording.key.lockOverlay", Side.CLIENT, Boolean.TRUE, "Set this to true to disable being able to change recording state when the overlay is hidden");
        ModRecording.instantHide = new ConfigValue<Boolean>("instantHide", "config.imrecording.key.instantHide", Side.CLIENT, Boolean.FALSE, "Set this to true to instantly hide the overlay instead of fading out slowly when toggling the GUI");
        this.config = new Config(event.getSuggestedConfigurationFile());
        this.config.addCategory(category);
        category.addValue(ModRecording.screenLocation);
        category.addValue(ModRecording.chatMessages);
        category.addValue(ModRecording.lockOverlay);
        category.addValue(ModRecording.instantHide);

        super.preInit(event);
    }

    @Override
    public ModInfo getModInfo() {
        return Objects.MOD_INFO;
    }

    @Override
    public SpACoreProxy getProxy() {
        return ModRecording.proxy;
    }

    @Override
    @EventHandler
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public boolean configChanged(OnConfigChangedEvent event) {
        return true;
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        CommonProxy.recordingPlayers.clear();
    }

    @SuppressWarnings("unchecked")
    public void broadcastRecorders() {
        ServerConfigurationManager configManager = MinecraftServer.getServer().getConfigurationManager();

        List<EntityPlayerMP> players = configManager.playerEntityList;

        for (int i = 0; i < players.size(); i++) {
            this.sendPlayersToPlayer(players.get(i));
        }
    }

    public void sendPlayersToPlayer(EntityPlayerMP player) {
        RecordingInfo[] players = new RecordingInfo[CommonProxy.recordingPlayers.size()];

        if (ModRecording.chatMessages.getValue()) {
            for (int i = 0; i < players.length; i++) {
                players[i] = CommonProxy.recordingPlayers.get(i);

                String message = players[i].getRecordingString(true);
                if (message != null) {
                    player.addChatComponentMessage(new ChatComponentText(message));
                }

                //SyncHandler.startTracking(players[i], player);
            }

        }
    }

    @SuppressWarnings("unchecked")
    public void sendPlayerToPlayers(RecordingInfo info) {
        ServerConfigurationManager configManager = MinecraftServer.getServer().getConfigurationManager();

        List<EntityPlayerMP> players = configManager.playerEntityList;

        if (ModRecording.chatMessages.getValue()) {
            String message = info.getRecordingString(false);

            if (message != null) {
                for (int i = 0; i < players.size(); i++) {
                    EntityPlayerMP player = players.get(i);

                    player.addChatComponentMessage(new ChatComponentText(message));
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendRecordingToServer() {
        Packet1SetState packet = new Packet1SetState(ClientProxy.playerInfo);
        ModRecording.packetHandler.sendPacketToServer(packet);
    }

}
