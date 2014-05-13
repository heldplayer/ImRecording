
package me.heldplayer.mods.recording.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.specialattack.forge.core.client.MC;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TickHandler {

    public TickHandler() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END) {
            ClientProxy.overlay.tick();
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == Phase.END) {
            Minecraft mc = MC.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);

            if (mc.currentScreen != null && mc.currentScreen.getClass() == GuiMainMenu.class) {
                ClientProxy.overlay.drawScreen(mc, resolution, true);
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void renderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type == ElementType.PORTAL) {
            Minecraft mc = MC.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            ClientProxy.overlay.drawScreen(mc, resolution, false);
        }
    }

}
