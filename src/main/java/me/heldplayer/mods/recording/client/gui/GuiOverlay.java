
package me.heldplayer.mods.recording.client.gui;

import java.util.ArrayList;

import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.mods.recording.ScreenLocation;
import me.heldplayer.mods.recording.client.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.specialattack.forge.core.client.MC;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOverlay extends Gui {

    public ScreenLocation location;
    private FontRenderer font;

    public GuiOverlay(ScreenLocation location) {
        this.location = location;
        this.font = MC.getFontRenderer();
    }

    public void tick() {
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

    public void drawScreen(Minecraft mc, ScaledResolution resolution, boolean disableOpcaity) {
        GL11.glEnable(GL11.GL_BLEND);

        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();

        boolean alignRight = (this.location == ScreenLocation.TopRight || this.location == ScreenLocation.BottomRight);
        boolean alignBottom = (this.location == ScreenLocation.BottomLeft || this.location == ScreenLocation.BottomRight);

        int x = alignRight ? width - 1 : 10;
        int y = alignBottom ? height - 10 : 1;

        ArrayList<RecordingInfo> players = CommonProxy.recordingPlayers;

        RecordingInfo[] playerArray = new RecordingInfo[players.size()];

        for (int i = 0; i < playerArray.length && i < players.size(); i++) {
            playerArray[i] = players.get(i);

            if (playerArray[i] == null) {
                continue;
            }

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
                if (width - 1 - this.font.getStringWidth(playerArray[i].name) < x) {
                    x = width - 1 - this.font.getStringWidth(playerArray[i].name);
                }
            }
        }

        for (RecordingInfo player : playerArray) {
            if (player != null && mc.thePlayer != null) {
                int color = player.getColor(disableOpcaity);

                mc.renderEngine.bindTexture(net.specialattack.forge.core.Assets.TEXTURE_MAP);

                GL11.glColor4f(1.0F, 1.0F, 1.0F, (((color >> 24) & 0xFF) / 255.0F));

                IIcon icon = ClientProxy.icons[player.getState()];

                if (icon != null) {
                    Tessellator tes = Tessellator.instance;
                    tes.startDrawingQuads();
                    tes.addVertexWithUV(x - 9.0D, y + 8.0D, 0.0D, icon.getMinU(), icon.getMaxV());
                    tes.addVertexWithUV(x - 1.0D, y + 8.0D, 0.0D, icon.getMaxU(), icon.getMaxV());
                    tes.addVertexWithUV(x - 1.0D, y, 0.0D, icon.getMaxU(), icon.getMinV());
                    tes.addVertexWithUV(x - 9.0D, y, 0.0D, icon.getMinU(), icon.getMinV());
                    tes.draw();
                }

                this.font.drawStringWithShadow(player.name, x, y, color);

                if (alignBottom) {
                    y -= 9;
                }
                else {
                    y += 9;
                }
            }
        }

        if (MC.getWorld() == null) {
            RecordingInfo player = ClientProxy.playerInfo;

            if (player.getState() == 0) {
                return;
            }

            int color = player.getColor(disableOpcaity);

            mc.renderEngine.bindTexture(net.specialattack.forge.core.Assets.TEXTURE_MAP);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, (((color >> 24) & 0xFF) / 255.0F));

            x = alignRight ? width - 1 - this.font.getStringWidth(player.name) : 10;
            y = alignBottom ? height - 10 : 1;

            IIcon icon = ClientProxy.icons[player.getState()];

            if (icon != null) {
                Tessellator tes = Tessellator.instance;
                tes.startDrawingQuads();
                tes.addVertexWithUV(x - 9.0D, y + 8.0D, 0.0D, icon.getMinU(), icon.getMaxV());
                tes.addVertexWithUV(x - 1.0D, y + 8.0D, 0.0D, icon.getMaxU(), icon.getMaxV());
                tes.addVertexWithUV(x - 1.0D, y, 0.0D, icon.getMaxU(), icon.getMinV());
                tes.addVertexWithUV(x - 9.0D, y, 0.0D, icon.getMinU(), icon.getMinV());
                tes.draw();
            }

            this.font.drawStringWithShadow(player.name, x, y, color);
        }
    }
}
