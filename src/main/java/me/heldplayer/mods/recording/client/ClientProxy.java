package me.heldplayer.mods.recording.client;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.heldplayer.mods.recording.Assets;
import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.mods.recording.client.gui.GuiOverlay;
import net.minecraft.util.IIcon;
import net.minecraft.util.Session;
import net.specialattack.forge.core.SpACore;
import net.specialattack.forge.core.client.MC;
import net.specialattack.forge.core.client.texture.IconHolder;
import net.specialattack.forge.core.event.SyncEvent;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static GuiOverlay overlay;
    public static RecordingInfo playerInfo;
    public static boolean overlayEnabled;
    public static IIcon[] icons = new IIcon[4];

    public static Map<UUID, RecordingInfo> recordingPlayers = new HashMap<UUID, RecordingInfo>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        ClientProxy.overlayEnabled = true;

        String[] types = new String[] { "unknown", "recording", "paused", "request" };

        for (int i = 0; i < types.length; i++) {
            SpACore.registerIconHolder(ClientProxy.icons[i] = new IconHolder(Assets.DOMAIN + types[i]));
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        new TickHandler();
        new KeyHandler();

        ClientProxy.overlay = new GuiOverlay();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        Session session = MC.getMc().getSession();

        ClientProxy.playerInfo = new RecordingInfo(session.func_148256_e().getId());
        ClientProxy.playerInfo.name.value = session.getUsername();
    }

    @SubscribeEvent
    public void onClientDisconnectionFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        CommonProxy.recordingPlayers.clear();
    }

    @SubscribeEvent
    public void onClientStartSyncing(SyncEvent.ClientServerInfoReceived event) {
        CommonProxy.recordingPlayers.clear();
        ModRecording.instance.sendRecordingToServer();
    }

    @Override
    public RecordingInfo getClientInfo(UUID uuid) {
        if (ClientProxy.recordingPlayers.containsKey(uuid)) {
            return ClientProxy.recordingPlayers.get(uuid);
        }
        RecordingInfo result = new RecordingInfo(uuid);
        ClientProxy.recordingPlayers.put(uuid, result);
        return result;
    }

    @Override
    public void removeClientInfo(UUID uuid) {
        ClientProxy.recordingPlayers.remove(uuid);
    }
}
