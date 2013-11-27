
package me.heldplayer.mods.recording;

import java.io.File;
import java.util.List;

import me.heldplayer.mods.recording.client.ClientProxy;
import me.heldplayer.mods.recording.packet.Packet1SetState;
import me.heldplayer.mods.recording.packet.PacketHandler;
import me.heldplayer.util.HeldCore.HeldCoreMod;
import me.heldplayer.util.HeldCore.HeldCoreProxy;
import me.heldplayer.util.HeldCore.ModInfo;
import me.heldplayer.util.HeldCore.config.Config;
import me.heldplayer.util.HeldCore.config.ConfigValue;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = Objects.MOD_ID, name = Objects.MOD_NAME, version = Objects.MOD_VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false, channels = { Objects.MOD_CHANNEL }, packetHandler = PacketHandler.class)
public class ModRecording extends HeldCoreMod {

    @Instance(value = Objects.MOD_ID)
    public static ModRecording instance;

    @SidedProxy(clientSide = Objects.CLIENT_PROXY, serverSide = Objects.SERVER_PROXY)
    public static CommonProxy proxy;

    // HeldCore Objects
    public static ConfigValue<ScreenLocation> screenLocation;
    public static ConfigValue<Boolean> chatMessages;
    public static ConfigValue<Boolean> lockOverlay;
    public static ConfigValue<Boolean> instantHide;

    @Override
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File file = new File(event.getModConfigurationDirectory(), "HeldCore");

        if (!file.exists()) {
            file.mkdirs();
        }

        Objects.log = event.getModLog();

        // Config
        screenLocation = new ConfigValue<ScreenLocation>("screenLocation", Configuration.CATEGORY_GENERAL, Side.CLIENT, ScreenLocation.TopRight, "Determines the location the GUI part of this mod is located in");
        chatMessages = new ConfigValue<Boolean>("chatMessages", Configuration.CATEGORY_GENERAL, null, Boolean.TRUE, "Set this to true to broadcast a chat message to every player when a player starts recording");
        lockOverlay = new ConfigValue<Boolean>("lockOverlay", Configuration.CATEGORY_GENERAL, Side.CLIENT, Boolean.TRUE, "Set this to true to disable being able to change recording state when the overlay is hidden");
        instantHide = new ConfigValue<Boolean>("instantHide", Configuration.CATEGORY_GENERAL, Side.CLIENT, Boolean.FALSE, "Set this to true to instantly hide the overlay instead of fading out slowly when toggling the GUI");
        this.config = new Config(event.getSuggestedConfigurationFile());
        this.config.addConfigKey(screenLocation);
        this.config.addConfigKey(chatMessages);
        this.config.addConfigKey(lockOverlay);
        this.config.addConfigKey(instantHide);

        super.preInit(event);
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

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        CommonProxy.recordingPlayers.clear();
    }

    @SuppressWarnings("unchecked")
    public void broadcastRecorders() {
        ServerConfigurationManager configManager = MinecraftServer.getServer().getConfigurationManager();

        List<EntityPlayerMP> players = configManager.playerEntityList;

        for (int i = 0; i < players.size(); i++) {
            this.sendPlayersToPlayer((EntityPlayerMP) players.get(i));
        }
    }

    public void sendPlayersToPlayer(EntityPlayerMP player) {
        RecordingInfo[] players = new RecordingInfo[CommonProxy.recordingPlayers.size()];

        if (chatMessages.getValue()) {
            for (int i = 0; i < players.length; i++) {
                players[i] = CommonProxy.recordingPlayers.get(i);

                String message = players[i].getRecordingString(true);
                if (message != null) {
                    player.addChatMessage(message);
                }

                //SyncHandler.startTracking(players[i], player);
            }

        }
    }

    @SuppressWarnings("unchecked")
    public void sendPlayerToPlayers(RecordingInfo info) {
        ServerConfigurationManager configManager = MinecraftServer.getServer().getConfigurationManager();

        List<EntityPlayerMP> players = configManager.playerEntityList;

        if (chatMessages.getValue()) {
            String message = info.getRecordingString(false);

            if (message != null) {
                for (int i = 0; i < players.size(); i++) {
                    EntityPlayerMP player = players.get(i);

                    player.addChatMessage(message);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendRecordingToServer() {
        Packet1SetState packet = new Packet1SetState(ClientProxy.playerInfo);
        FMLClientHandler.instance().sendPacket(PacketHandler.instance.createPacket(packet));
    }

    @Override
    public ModInfo getModInfo() {
        return Objects.MOD_INFO;
    }

    @Override
    public HeldCoreProxy getProxy() {
        return proxy;
    }

}
