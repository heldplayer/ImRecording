package me.heldplayer.mods.recording.client;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.mods.recording.client.gui.GuiOverlay;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.Session;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.specialattack.forge.core.SpACore;
import net.specialattack.forge.core.client.MC;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static GuiOverlay overlay;
    public static RecordingInfo playerInfo;
    public static boolean overlayEnabled;
    public static IIcon[] icons = new IIcon[4];

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        ClientProxy.overlayEnabled = true;
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

        Session session = MC.getMinecraft().getSession();

        ClientProxy.playerInfo = new RecordingInfo(session.getUsername(), (byte) 0);
    }

    @SubscribeEvent
    public void onClientDisconnectionFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        CommonProxy.recordingPlayers.clear();
    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        CommonProxy.recordingPlayers.clear();
        //CommonProxy.recordingPlayers.add(playerInfo);
        //PacketHandler.instance.createPacket(new Packet1TrackingStatus(playerInfo, true));
        ModRecording.instance.sendRecordingToServer();
    }

    @SubscribeEvent
    public void onTextureStitchedPost(TextureStitchEvent.Pre event) {
        TextureMap map = event.map;

        if (map.getTextureType() == SpACore.textureMapId.getValue()) {
            String[] types = new String[] { "unknown", "recording", "paused", "request" };

            for (int i = 0; i < types.length; i++) {
                ClientProxy.icons[i] = map.registerIcon("imrecording:" + types[i]);
            }
        }
    }

}
