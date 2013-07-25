
package me.heldplayer.mods.recording.client;

import java.util.EnumSet;

import me.heldplayer.util.HeldCore.client.MC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TickHandler implements ITickHandler {

    public TickHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {}

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        if (type.equals(EnumSet.of(TickType.RENDER))) {
            Minecraft mc = MC.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);

            if (mc.currentScreen != null && mc.currentScreen.getClass() == GuiMainMenu.class) {
                ClientProxy.overlay.drawScreen(mc, resolution);
            }
        }
        else if (type.equals(EnumSet.of(TickType.CLIENT))) {
            ClientProxy.overlay.tick();
        }
    }

    @ForgeSubscribe(receiveCanceled = true)
    public void renderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type == ElementType.PORTAL) {
            Minecraft mc = MC.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            ClientProxy.overlay.drawScreen(mc, resolution);
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.RENDER, TickType.CLIENT);
    }

    @Override
    public String getLabel() {
        return "Recording Overlay";
    }

}
