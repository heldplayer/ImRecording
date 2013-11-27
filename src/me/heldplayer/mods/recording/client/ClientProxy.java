
package me.heldplayer.mods.recording.client;

import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.mods.recording.client.gui.GuiOverlay;
import me.heldplayer.util.HeldCore.HeldCore;
import me.heldplayer.util.HeldCore.client.MC;
import me.heldplayer.util.HeldCore.reflection.RClass;
import me.heldplayer.util.HeldCore.reflection.RField;
import me.heldplayer.util.HeldCore.reflection.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.MemoryConnection;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.TcpConnection;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.util.Icon;
import net.minecraft.util.Session;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static GuiOverlay overlay;
    public static RecordingInfo playerInfo;
    public static boolean overlayEnabled;
    public static Icon[] icons = new Icon[4];

    public static String texture = "/me/heldplayer/textures/recording/";

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        ClientProxy.overlayEnabled = true;
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);

        KeyBindingRegistry.registerKeyBinding(new KeyHandler());

        ClientProxy.overlay = new GuiOverlay(ModRecording.screenLocation.getValue());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        RClass<Minecraft> minecraftClass = ReflectionHelper.getClass(Minecraft.class);
        RField<Minecraft, Session> sessionField = minecraftClass.getField("session");
        Session session = sessionField.get(MC.getMinecraft());

        playerInfo = new RecordingInfo(session.getUsername(), (byte) 0);
    }

    @Override
    public void connectionClosed(INetworkManager manager) {
        NetHandler handler = null;
        if (manager instanceof TcpConnection) {
            handler = ((TcpConnection) manager).theNetHandler;
        }
        else if (manager instanceof MemoryConnection) {
            handler = ((MemoryConnection) manager).myNetHandler;
        }
        else {
            return;
        }

        if (handler instanceof NetServerHandler) {
            super.connectionClosed(manager);
        }
        else {
            CommonProxy.recordingPlayers.clear();
        }
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
        CommonProxy.recordingPlayers.clear();
        //CommonProxy.recordingPlayers.add(playerInfo);
        //PacketHandler.instance.createPacket(new Packet1TrackingStatus(playerInfo, true));
        ModRecording.instance.sendRecordingToServer();
    }

    @ForgeSubscribe
    public void onTextureStitchedPost(TextureStitchEvent.Pre event) {
        TextureMap map = event.map;

        if (map.textureType == HeldCore.textureMapId.getValue()) {
            String[] types = new String[] { "unknown", "recording", "paused", "request" };

            for (int i = 0; i < types.length; i++) {
                icons[i] = map.registerIcon("imrecording:" + types[i]);
            }
        }
    }

}
