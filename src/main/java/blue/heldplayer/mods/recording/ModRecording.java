package blue.heldplayer.mods.recording;

import blue.heldplayer.mods.recording.client.ClientProxy;
import blue.heldplayer.mods.recording.packet.C01SetState;
import blue.heldplayer.mods.recording.packet.ImRecordingPacket;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.specialattack.forge.core.ModInfo;
import net.specialattack.forge.core.SpACoreMod;
import net.specialattack.forge.core.SpACoreProxy;
import net.specialattack.forge.core.config.ConfigManager;
import net.specialattack.forge.core.config.Configuration;
import net.specialattack.forge.core.packet.SpAPacketHandler;
import net.specialattack.forge.core.sync.SyncObjectProvider;
import net.specialattack.forge.core.sync.SyncServerAPI;

@Mod(modid = Objects.MOD_ID, name = Objects.MOD_NAME, dependencies = Objects.MOD_DEPENCIES, guiFactory = Objects.GUI_FACTORY)
public class ModRecording extends SpACoreMod {

    @Mod.Instance(value = Objects.MOD_ID)
    public static ModRecording instance;

    @SidedProxy(clientSide = Objects.CLIENT_PROXY, serverSide = Objects.SERVER_PROXY)
    public static CommonProxy proxy;

    public static Config config;
    public static ConfigManager configManager;

    @Configuration("imrecording.cfg")
    public static class Config {

        @Configuration.Option(category = "general")
        @Configuration.StringOptions({ "top-left", "top-right", "bottom-right", "bottom-left" })
        public String screenLocation = "top-right";

        @Configuration.Option(category = "general")
        public boolean chatMessages = true;

        @Configuration.Option(category = "general")
        public boolean lockOverlay = true;

        @Configuration.Option(category = "general")
        public boolean instantHide = false;
    }

    public static SpAPacketHandler<ImRecordingPacket> packetHandler;

    public static SyncObjectProvider<RecordingInfo> syncProvider = new SyncProvider();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Objects.log = event.getModLog();

        ModRecording.configManager = ConfigManager.registerConfig(ModRecording.config = new Config());

        ModRecording.packetHandler = new SpAPacketHandler<ImRecordingPacket>(Objects.MOD_CHANNEL, C01SetState.class);

        SyncServerAPI.registerProvider(ModRecording.syncProvider);

        super.preInit(event);
    }

    @Override
    public ModInfo getModInfo() {
        return Objects.MOD_INFO;
    }

    @Override
    public SpACoreProxy[] getProxies() {
        return new SpACoreProxy[] { ModRecording.proxy };
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        CommonProxy.recordingPlayers.clear();
    }

    @SideOnly(Side.CLIENT)
    public void sendRecordingToServer() {
        C01SetState packet = new C01SetState(ClientProxy.playerInfo);
        ModRecording.packetHandler.sendToServer(packet);
    }
}
