
package me.heldplayer.mods.recording.common;

import java.io.File;
import java.util.List;

import me.heldplayer.mods.recording.client.ClientProxy;
import me.heldplayer.mods.recording.common.network.PacketHandler;
import me.heldplayer.util.HeldCore.Updater;
import me.heldplayer.util.HeldCore.UsageReporter;
import me.heldplayer.util.HeldCore.config.Config;
import me.heldplayer.util.HeldCore.config.ConfigValue;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = Objects.MOD_ID, name = Objects.MOD_NAME, version = Objects.MOD_VERSION)
@NetworkMod(clientSideRequired = false, serverSideRequired = false, channels = { Objects.MOD_CHANNEL }, packetHandler = PacketHandler.class)
public class ModRecording {

    @Instance(value = Objects.MOD_ID)
    public static ModRecording instance;

    @SidedProxy(clientSide = Objects.CLIENT_PROXY, serverSide = Objects.SERVER_PROXY)
    public static CommonProxy proxy;

    // HeldCore Objects
    private UsageReporter reporter;
    private Config config;
    public static ConfigValue<ScreenLocation> screenLocation;
    public static ConfigValue<Boolean> chatMessages;
    public static ConfigValue<Boolean> lockOverlay;
    public static ConfigValue<Boolean> instantHide;
    // Config values for HeldCore
    public static ConfigValue<Boolean> silentUpdates;
    public static ConfigValue<Boolean> optOut;
    public static ConfigValue<String> modPack;

    @PreInit
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
        silentUpdates = new ConfigValue<Boolean>("silentUpdates", Configuration.CATEGORY_GENERAL, null, Boolean.TRUE, "Set this to true to hide update messages in the main menu");
        optOut = new ConfigValue<Boolean>("optOut", Configuration.CATEGORY_GENERAL, null, Boolean.FALSE, "Set this to true to opt-out from statistics gathering. If you are configuring this mod for a modpack, please leave it set to false");
        modPack = new ConfigValue<String>("modPack", Configuration.CATEGORY_GENERAL, null, "", "If this mod is running in a modpack, please set this config value to the name of the modpack");
        this.config = new Config(event.getSuggestedConfigurationFile());
        this.config.addConfigKey(screenLocation);
        this.config.addConfigKey(chatMessages);
        this.config.addConfigKey(lockOverlay);
        this.config.addConfigKey(instantHide);
        this.config.addConfigKey(silentUpdates);
        this.config.addConfigKey(optOut);
        this.config.addConfigKey(modPack);
        this.config.load();
        this.config.saveOnChange();

        this.reporter = new UsageReporter(Objects.MOD_ID, Objects.MOD_VERSION, modPack.getValue(), FMLCommonHandler.instance().getSide(), file);

        Updater.initializeUpdater(Objects.MOD_ID, Objects.MOD_VERSION, silentUpdates.getValue());

        proxy.preInit(event);
    }

    @Init
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event) {
        if (optOut.getValue()) {
            Thread thread = new Thread(this.reporter, Objects.MOD_ID + " usage reporter");
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

        proxy.postInit(event);
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

        for (int i = 0; i < players.length; i++) {
            players[i] = CommonProxy.recordingPlayers.get(i);

            if (chatMessages.getValue()) {
                String message = players[i].getRecordingString(true);
                if (message != null) {
                    player.sendChatToPlayer(message);
                }
            }
        }

        player.playerNetServerHandler.sendPacketToPlayer(PacketHandler.getPacket(2, players));
    }

    @SuppressWarnings("unchecked")
    public void sendPlayerToPlayers(RecordingInfo info) {
        ServerConfigurationManager configManager = MinecraftServer.getServer().getConfigurationManager();

        List<EntityPlayerMP> players = configManager.playerEntityList;

        for (int i = 0; i < players.size(); i++) {
            EntityPlayerMP player = ((EntityPlayerMP) players.get(i));

            player.playerNetServerHandler.sendPacketToPlayer(PacketHandler.getPacket(1, info));

            if (chatMessages.getValue()) {
                String message = info.getRecordingString(false);
                if (message != null) {
                    player.sendChatToPlayer(message);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendRecordingToServer() {
        FMLClientHandler.instance().sendPacket(PacketHandler.getPacket(0, ClientProxy.playerInfo));
    }

}
