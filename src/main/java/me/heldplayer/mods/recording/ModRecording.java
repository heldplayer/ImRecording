package me.heldplayer.mods.recording;

import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.heldplayer.mods.recording.client.ClientProxy;
import me.heldplayer.mods.recording.packet.C01SetState;
import me.heldplayer.mods.recording.packet.ImRecordingPacket;
import net.minecraftforge.common.config.Configuration;
import net.specialattack.forge.core.ModInfo;
import net.specialattack.forge.core.SpACoreMod;
import net.specialattack.forge.core.SpACoreProxy;
import net.specialattack.forge.core.config.Config;
import net.specialattack.forge.core.config.ConfigCategory;
import net.specialattack.forge.core.config.ConfigValue;
import net.specialattack.forge.core.packet.SpAPacketHandler;
import net.specialattack.forge.core.sync.SyncObjectProvider;
import net.specialattack.forge.core.sync.SyncServerAPI;

@Mod(modid = Objects.MOD_ID, name = Objects.MOD_NAME, dependencies = Objects.MOD_DEPENCIES, guiFactory = Objects.GUI_FACTORY)
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

    public static SpAPacketHandler<ImRecordingPacket> packetHandler;

    public static SyncObjectProvider<RecordingInfo> syncProvider = new SyncProvider();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Objects.log = event.getModLog();

        ModRecording.packetHandler = new SpAPacketHandler<ImRecordingPacket>(Objects.MOD_CHANNEL, C01SetState.class);

        // Config
        ConfigCategory<?> category = new ConfigCategory(Configuration.CATEGORY_GENERAL, "imrecording:config.general", null);
        ModRecording.screenLocation = new ConfigValue<ScreenLocation>("screenLocation", "imrecording:config.general.screenLocation", Side.CLIENT, ScreenLocation.TopRight);
        ModRecording.chatMessages = new ConfigValue<Boolean>("chatMessages", "imrecording:config.general.chatMessages", null, Boolean.TRUE);
        ModRecording.lockOverlay = new ConfigValue<Boolean>("lockOverlay", "imrecording:config.general.lockOverlay", Side.CLIENT, Boolean.TRUE);
        ModRecording.instantHide = new ConfigValue<Boolean>("instantHide", "imrecording:config.general.instantHide", Side.CLIENT, Boolean.FALSE);
        this.config = new Config(event.getSuggestedConfigurationFile());
        this.config.addCategory(category);
        category.addValue(ModRecording.screenLocation);
        category.addValue(ModRecording.chatMessages);
        category.addValue(ModRecording.lockOverlay);
        category.addValue(ModRecording.instantHide);

        SyncServerAPI.registerProvider(ModRecording.syncProvider);

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
    public boolean configChanged(OnConfigChangedEvent event) {
        return true;
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        CommonProxy.recordingPlayers.clear();
    }

    @SideOnly(Side.CLIENT)
    public void sendRecordingToServer() {
        C01SetState packet = new C01SetState(ClientProxy.playerInfo);
        ModRecording.packetHandler.sendToServer(packet);
    }

}
