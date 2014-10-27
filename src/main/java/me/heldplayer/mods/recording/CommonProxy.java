package me.heldplayer.mods.recording;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.specialattack.forge.core.SpACoreProxy;
import net.specialattack.forge.core.event.SyncEvent;
import net.specialattack.forge.core.sync.SyncHandler;

public class CommonProxy extends SpACoreProxy {

    public static ArrayList<RecordingInfo> recordingPlayers = new ArrayList<RecordingInfo>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @Override
    public void init(FMLInitializationEvent event) {
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }

    @SubscribeEvent
    public void onSyncRequestObject(SyncEvent.RequestObject event) {
        if (event.identifier.startsWith("RecordingInfo_")) {
            String name = event.identifier.substring(14);

            for (RecordingInfo info : CommonProxy.recordingPlayers) {
                if (info.name.equalsIgnoreCase(name)) {
                    event.result = info;
                    return;
                }
            }

            RecordingInfo info = new RecordingInfo(name, null, 0);
            CommonProxy.recordingPlayers.add(info);
            event.result = info;
        }
    }

    @SubscribeEvent
    public void onSyncStartTracking(SyncEvent.StartTracking event) {
        EntityPlayerMP player = event.tracker.getPlayer();

        ModRecording.instance.sendPlayersToPlayer(player);

        for (RecordingInfo info : CommonProxy.recordingPlayers) {
            SyncHandler.Server.startTracking(info, player);
        }
    }

    @SubscribeEvent
    public void onSyncStopTracking(SyncEvent.StopTracking event) {
        ArrayList<RecordingInfo> playerInfos = CommonProxy.recordingPlayers;

        for (RecordingInfo info : playerInfos) {
            if (event.tracker.uuid.equals(info.uuid)) {
                info.setNotValid();
            }
        }
    }

}
