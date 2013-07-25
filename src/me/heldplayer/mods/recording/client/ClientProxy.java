
package me.heldplayer.mods.recording.client;

import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.mods.recording.client.gui.GuiOverlay;
import me.heldplayer.util.HeldCore.client.MC;
import me.heldplayer.util.HeldCore.reflection.RClass;
import me.heldplayer.util.HeldCore.reflection.RField;
import me.heldplayer.util.HeldCore.reflection.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.util.Session;
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

        playerInfo = new RecordingInfo(session.func_111285_a(), (byte) 0);
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void connectionClosed(INetworkManager manager) {
        CommonProxy.recordingPlayers.clear();
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
        CommonProxy.recordingPlayers.clear();
        ModRecording.instance.sendRecordingToServer();
    }

}
