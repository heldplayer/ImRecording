
package me.heldplayer.mods.recording;

import java.util.ArrayList;
import java.util.logging.Logger;

import me.heldplayer.util.HeldCore.HeldCoreProxy;
import me.heldplayer.util.HeldCore.event.SyncEvent;
import me.heldplayer.util.HeldCore.sync.SyncHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;

public class CommonProxy extends HeldCoreProxy implements IConnectionHandler {

    public static Logger log;
    public static ArrayList<RecordingInfo> recordingPlayers = new ArrayList<RecordingInfo>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        CommonProxy.log = event.getModLog();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        NetworkRegistry.instance().registerConnectionHandler(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {}

    @ForgeSubscribe
    public void onSyncRequestObject(SyncEvent.RequestObject event) {
        if (event.identifier.startsWith("RecordingInfo_")) {
            String name = event.identifier.substring(14);

            for (RecordingInfo info : recordingPlayers) {
                if (info.name.equalsIgnoreCase(name)) {
                    event.result = info;
                    return;
                }
            }

            RecordingInfo info = new RecordingInfo(name, 0);
            recordingPlayers.add(info);
            event.result = info;
        }
    }

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
        ModRecording.instance.sendPlayersToPlayer((EntityPlayerMP) player);

        for (RecordingInfo info : recordingPlayers) {
            SyncHandler.startTracking(info, (EntityPlayerMP) player);
        }
    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
        return null;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {}

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {}

    @Override
    public void connectionClosed(INetworkManager manager) {
        ServerConfigurationManager configManager = MinecraftServer.getServerConfigurationManager(MinecraftServer.getServer());

        ArrayList<String> players = new ArrayList<String>();

        for (String player : configManager.getAllUsernames()) {
            players.add(player);
        }

        ArrayList<RecordingInfo> playerInfos = CommonProxy.recordingPlayers;

        for (int i = 0; i < playerInfos.size(); i++) {
            RecordingInfo info = playerInfos.get(i);

            if (!players.contains(info.name)) {
                playerInfos.remove(i);

                info.setState(0);

                //ModRecording.instance.sendPlayerToPlayers(info);

                SyncHandler.stopTracking(info);

                i--;
            }
        }

        //ModRecording.instance.broadcastRecorders();
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {}

}
