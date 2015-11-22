package blue.heldplayer.mods.recording.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.specialattack.forge.core.client.MC;

@SideOnly(Side.CLIENT)
public class TickHandler {

    public TickHandler() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientProxy.overlay.tick();
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = MC.getMc();
            ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

            if (mc.currentScreen != null && mc.currentScreen.getClass() == GuiMainMenu.class) {
                ClientProxy.overlay.drawScreen(mc, resolution, true);
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void renderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type == ElementType.PORTAL) {
            Minecraft mc = MC.getMc();
            ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            ClientProxy.overlay.drawScreen(mc, resolution, false);
        }
    }
}
