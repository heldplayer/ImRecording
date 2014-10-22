package me.heldplayer.mods.recording;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.specialattack.forge.core.SpACoreProxy;
import net.specialattack.forge.core.event.SyncEvent;
import net.specialattack.forge.core.sync.SyncHandler;

public class CommonProxy extends SpACoreProxy {

    public static ArrayList<RecordingInfo> recordingPlayers = new ArrayList<RecordingInfo>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
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

            RecordingInfo info = new RecordingInfo(name, 0);
            CommonProxy.recordingPlayers.add(info);
            event.result = info;
        }
    }

    @SubscribeEvent
    public void onServerConnectionFromClient(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        if (!(event.handler instanceof NetHandlerPlayServer)) {
            return;
        }
        EntityPlayerMP player = ((NetHandlerPlayServer) event.handler).playerEntity;

        ModRecording.instance.sendPlayersToPlayer(player);

        for (RecordingInfo info : CommonProxy.recordingPlayers) {
            SyncHandler.startTracking(info, player);
        }
    }

    // FIXME: Unregister syncable from players that disconnected
    @SubscribeEvent
    public void onServerDisconnectionFromClient(FMLNetworkEvent.ServerDisconnectionFromClientEvent event) {
        ArrayList<String> players = new ArrayList<String>();

        Collections.addAll(players, MinecraftServer.getServer().getAllUsernames());

        ArrayList<RecordingInfo> playerInfos = CommonProxy.recordingPlayers;

        for (int i = 0; i < playerInfos.size(); i++) {
            RecordingInfo info = playerInfos.get(i);

            if (!players.contains(info.name)) {
                playerInfos.remove(i);

                info.setState(0);

                SyncHandler.stopTracking(info);

                i--;
            }
        }
    }

}
