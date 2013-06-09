
package me.heldplayer.mods.recording.client.gui;

import java.util.ArrayList;

import me.heldplayer.mods.recording.client.ClientProxy;
import me.heldplayer.mods.recording.common.CommonProxy;
import me.heldplayer.mods.recording.common.ModRecording;
import me.heldplayer.mods.recording.common.RecordingInfo;
import me.heldplayer.mods.recording.common.ScreenLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOverlay extends Gui {
    public ScreenLocation location;
    private FontRenderer font;

    public GuiOverlay(ScreenLocation location) {
        this.location = location;
        this.font = new FontRenderer(Minecraft.getMinecraft().gameSettings, "/font/default.png", Minecraft.getMinecraft().renderEngine, false);
    }

    public void tick() {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.currentScreen != null && mc.theWorld != null)
            return;

        ArrayList<RecordingInfo> players = CommonProxy.recordingPlayers;

        for (int i = 0; i < players.size(); i++) {
            RecordingInfo info = players.get(i);

            if (!ModRecording.instantHide.getValue() && !ClientProxy.overlayEnabled) {
                if (info.displayTime > 200) {
                    info.displayTime = 200;
                }
                else {
                    info.displayTime++;
                }
            }
            else {
                if (info.getState() == (byte) 3) {
                    info.displayTime++;
                }
                else {
                    info.displayTime = 0;
                }
            }
        }
    }

    public void drawScreen(Minecraft mc, ScaledResolution resolution) {
        if (mc.currentScreen != null && mc.theWorld != null && !(mc.currentScreen instanceof GuiIngameMenu))
            return;

        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();

        boolean alignRight = (this.location == ScreenLocation.TopRight || this.location == ScreenLocation.BottomRight);
        boolean alignBottom = (this.location == ScreenLocation.BottomLeft || this.location == ScreenLocation.BottomRight);

        int x = alignRight ? width - 1 : 10;
        int y = alignBottom ? height - 10 : 1;

        ArrayList<RecordingInfo> players = CommonProxy.recordingPlayers;

        RecordingInfo[] playerArray = new RecordingInfo[players.size()];

        for (int i = 0; i < playerArray.length; i++) {
            playerArray[i] = players.get(i);

            if (playerArray[i].getState() == 0) {
                players.remove(i);
                playerArray[i] = null;
                i--;
                continue;
            }

            if ((playerArray[i].displayTime >= 200 || ModRecording.instantHide.getValue()) && !ClientProxy.overlayEnabled) {
                playerArray[i] = null;
                continue;
            }

            if (alignRight) {
                if (width - 1 - font.getStringWidth(playerArray[i].name) < x) {
                    x = width - 1 - font.getStringWidth(playerArray[i].name);
                }
            }
        }

        for (RecordingInfo player : playerArray) {
            if (player != null && mc.thePlayer != null) {
                int color = player.getColor();

                mc.renderEngine.bindTexture(ClientProxy.texture + "sprites.png");

                GL11.glColor4f(1.0F, 1.0F, 1.0F, ((float) ((color >> 24) & 0xFF) / 255.0F));

                drawTexturedModalRect(x - 9, y, player.getState() * 16, 0, 16, 16);

                font.drawStringWithShadow(player.name, x, y, color);

                if (alignBottom) {
                    y -= 9;
                }
                else {
                    y += 9;
                }
            }
        }

        if (Minecraft.getMinecraft().theWorld == null) {
            RecordingInfo player = ClientProxy.playerInfo;

            if (player.getState() == 0)
                return;

            int color = player.getColor();

            mc.renderEngine.bindTexture(ClientProxy.texture + "sprites.png");

            GL11.glColor4f(1.0F, 1.0F, 1.0F, ((float) ((color >> 24) & 0xFF) / 255.0F));

            x = alignRight ? width - 1 - font.getStringWidth(player.name) : 10;
            y = alignBottom ? height - 10 : 1;

            drawTexturedModalRect(x - 9, y, player.getState() * 16, 0, 16, 16);

            font.drawStringWithShadow(player.name, x, y, color);
        }
    }
}
