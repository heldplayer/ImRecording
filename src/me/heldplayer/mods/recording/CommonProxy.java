
package me.heldplayer.mods.recording;

import java.util.ArrayList;
import java.util.logging.Logger;

import me.heldplayer.util.HeldCore.HeldCoreProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.server.FMLServerHandler;

public class CommonProxy extends HeldCoreProxy implements IConnectionHandler {

    public static Logger log;
    public static ArrayList<RecordingInfo> recordingPlayers = new ArrayList<RecordingInfo>();

    public void preInit(FMLPreInitializationEvent event) {
        CommonProxy.log = event.getModLog();
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.instance().registerConnectionHandler(this);
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public Side getSide() {
        return Side.SERVER;
    }

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
        ModRecording.instance.sendPlayersToPlayer((EntityPlayerMP) player);
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
        ServerConfigurationManager configManager = MinecraftServer.getServerConfigurationManager(FMLServerHandler.instance().getServer());

        ArrayList<String> players = new ArrayList<String>();

        for (String player : configManager.getAllUsernames()) {
            players.add(player);
        }

        ArrayList<RecordingInfo> playerInfos = CommonProxy.recordingPlayers;

        for (int i = 0; i < playerInfos.size(); i++) {
            RecordingInfo info = playerInfos.get(i);

            if (!players.contains(info.name)) {
                playerInfos.remove(i);

                info.setState((byte) 0);

                ModRecording.instance.sendPlayerToPlayers(info);

                i--;
            }
        }

        //ModRecording.instance.broadcastRecorders();
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {}

}
