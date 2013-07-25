
package me.heldplayer.mods.recording.client;

import java.util.ArrayList;
import java.util.EnumSet;

import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.util.HeldCore.client.MC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyHandler extends cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler {

    protected static KeyBinding recordingToggle = new KeyBinding("Recording Toggle", Keyboard.KEY_R);
    protected static KeyBinding pausedToggle = new KeyBinding("Paused Toggle", Keyboard.KEY_P);
    protected static KeyBinding overlayToggle = new KeyBinding("Overlay Toggle", Keyboard.KEY_0);

    public KeyHandler() {
        super(new KeyBinding[] { recordingToggle, pausedToggle, overlayToggle }, new boolean[] { true, true, false });
    }

    @Override
    public String getLabel() {
        return "I'm Recording Mod";
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        if (!tickEnd) {
            return;
        }
        Minecraft mc = MC.getMinecraft();

        if (mc.currentScreen != null && mc.theWorld != null && !(mc.currentScreen instanceof GuiMainMenu)) {
            return;
        }

        if (kb == overlayToggle) {
            ClientProxy.overlayEnabled = !ClientProxy.overlayEnabled;

            ArrayList<RecordingInfo> players = CommonProxy.recordingPlayers;

            for (int i = 0; i < players.size(); i++) {
                RecordingInfo info = players.get(i);

                info.displayTime = 0;
            }
        }

        if (!ClientProxy.overlayEnabled && ModRecording.lockOverlay.getValue()) {
            return;
        }

        if (!isRepeat) {
            if (kb == recordingToggle) {
                kb.pressTime++;
            }
            if (kb == pausedToggle) {
                kb.pressTime++;
            }
        }
        else {
            if (kb == recordingToggle) {
                kb.pressTime = 1;

                RecordingInfo info = ClientProxy.playerInfo;
                byte state = info.getState();
                info.setState((byte) (state > 0 ? 0 : 1));

                if (state != info.getState()) {
                    if (mc.theWorld != null) {
                        ModRecording.instance.sendRecordingToServer();
                    }
                }
            }
            if (kb == pausedToggle) {
                kb.pressTime = 1;
            }
        }
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
        if (!tickEnd) {
            return;
        }
        Minecraft mc = MC.getMinecraft();

        if (mc.currentScreen != null && mc.theWorld != null && !(mc.currentScreen instanceof GuiMainMenu)) {
            return;
        }

        if (!ClientProxy.overlayEnabled && ModRecording.lockOverlay.getValue()) {
            return;
        }

        if (kb == pausedToggle) {
            if (kb.pressTime < 10) {
                RecordingInfo info = ClientProxy.playerInfo;
                byte state = info.getState();
                byte oldState = info.getOldState();
                info.setState((byte) (state == 1 ? 2 : (state == 2 ? 1 : state != 0 ? oldState : 0)));

                if (state != info.getState()) {
                    if (mc.theWorld != null) {
                        ModRecording.instance.sendRecordingToServer();
                    }
                }
            }
            else {
                RecordingInfo info = ClientProxy.playerInfo;
                byte state = info.getState();
                byte oldState = info.getOldState();
                info.setState((byte) (state == 3 ? oldState : 3));

                if (state != info.getState()) {
                    if (mc.theWorld != null) {
                        ModRecording.instance.sendRecordingToServer();
                    }
                }
            }
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }

}
