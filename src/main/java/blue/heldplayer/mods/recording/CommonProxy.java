package blue.heldplayer.mods.recording;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.specialattack.forge.core.Objects;
import net.specialattack.forge.core.SpACoreProxy;
import net.specialattack.forge.core.event.SyncEvent;
import net.specialattack.forge.core.sync.SyncHandler;
import net.specialattack.util.PlayerUtils;

public class CommonProxy extends SpACoreProxy {

    public static Map<UUID, RecordingInfo> recordingPlayers = new LinkedHashMap<UUID, RecordingInfo>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        Objects.SYNC_EVENT_BUS.register(this);
    }

    @Override
    public void init(FMLInitializationEvent event) {
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }

    @SubscribeEvent
    public void onSyncServerConnectionSetup(SyncEvent.ServerConnectionSetup event) {
        UUID uuid = event.connection.getUuid();
        EntityPlayerMP player = PlayerUtils.getServerPlayer(uuid);

        RecordingInfo info = new RecordingInfo(uuid);
        info.name.value = player.getName();
        CommonProxy.recordingPlayers.put(uuid, info);

        SyncHandler.globalStorage.registerSyncableOwner(info);
    }

    @SubscribeEvent
    public void onSyncServerClientDisconnected(SyncEvent.ServerClientDisconnected event) {
        SyncHandler.globalStorage.unregisterSyncableOwner(CommonProxy.recordingPlayers.remove(event.connection.getUuid()));
    }

    @SubscribeEvent
    public void onSyncServerStopped(SyncEvent.ServerStopped event) {
        CommonProxy.recordingPlayers.clear();
    }

    public RecordingInfo getClientInfo(UUID uuid) {
        return null;
    }

    public void removeClientInfo(UUID uuid) {
    }
}
