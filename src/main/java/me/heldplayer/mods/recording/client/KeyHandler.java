
package me.heldplayer.mods.recording.client;

import java.util.ArrayList;

import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.specialattack.forge.core.client.MC;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyHandler {

    protected KeyBinding recordingToggle = new KeyBinding("key.recording", Keyboard.KEY_R, "key.categories.imrecording");
    protected KeyBinding pausedToggle = new KeyBinding("key.pause", Keyboard.KEY_P, "key.categories.imrecording");
    protected KeyBinding overlayToggle = new KeyBinding("key.overlay", Keyboard.KEY_0, "key.categories.imrecording");
    protected KeyBinding[] keyBindings;
    protected boolean[] keyDown;
    protected boolean[] repeatings;
    protected int[] ticktimes;

    public KeyHandler() {
        ClientRegistry.registerKeyBinding(this.recordingToggle);
        ClientRegistry.registerKeyBinding(this.pausedToggle);
        ClientRegistry.registerKeyBinding(this.overlayToggle);
        FMLCommonHandler.instance().bus().register(this);
        this.keyBindings = new KeyBinding[] { this.recordingToggle, this.pausedToggle, this.overlayToggle };
        this.keyDown = new boolean[this.keyBindings.length];
        this.repeatings = new boolean[] { true, true, false };
        this.ticktimes = new int[this.keyBindings.length];
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        this.keyTick(event.type, event.phase == Phase.END);
    }

    private void keyTick(TickEvent.Type type, boolean tickEnd) {
        for (int i = 0; i < this.keyBindings.length; i++) {
            KeyBinding keyBinding = this.keyBindings[i];
            int keyCode = keyBinding.getKeyCode();
            boolean state = (keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
            if (state != this.keyDown[i] || (state && this.repeatings[i])) {
                if (state) {
                    this.keyDown(type, i, tickEnd, state != this.keyDown[i]);
                }
                else {
                    this.keyUp(type, i, tickEnd);
                }
                if (tickEnd) {
                    this.keyDown[i] = state;
                }
            }

        }
    }

    public void keyDown(TickEvent.Type type, int binding, boolean tickEnd, boolean isRepeat) {
        if (!tickEnd) {
            return;
        }
        Minecraft mc = MC.getMinecraft();
        KeyBinding kb = this.keyBindings[binding];

        if (mc.currentScreen != null && mc.theWorld != null && !(mc.currentScreen instanceof GuiMainMenu)) {
            return;
        }

        if (kb == this.overlayToggle) {
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
            if (kb == this.recordingToggle || kb == this.pausedToggle) {
                this.ticktimes[binding]++;
            }
        }
        else {
            if (kb == this.recordingToggle) {
                this.ticktimes[binding] = 1;

                RecordingInfo info = ClientProxy.playerInfo;
                int state = info.getState();
                info.setState(state > 0 ? 0 : 1);

                if (state != info.getState()) {
                    if (mc.theWorld != null) {
                        ModRecording.instance.sendRecordingToServer();
                    }
                }
            }
            if (kb == this.pausedToggle) {
                this.ticktimes[binding] = 1;
            }
        }
    }

    public void keyUp(TickEvent.Type type, int binding, boolean tickEnd) {
        if (!tickEnd) {
            return;
        }
        Minecraft mc = MC.getMinecraft();
        KeyBinding kb = this.keyBindings[binding];

        if (mc.currentScreen != null && mc.theWorld != null && !(mc.currentScreen instanceof GuiMainMenu)) {
            return;
        }

        if (!ClientProxy.overlayEnabled && ModRecording.lockOverlay.getValue()) {
            return;
        }

        if (kb == this.pausedToggle) {
            if (this.ticktimes[binding] < 10) {
                RecordingInfo info = ClientProxy.playerInfo;
                int state = info.getState();
                int oldState = info.getOldState();
                info.setState(state == 1 ? 2 : (state == 2 ? 1 : state != 0 ? oldState : 0));

                if (state != info.getState()) {
                    if (mc.theWorld != null) {
                        ModRecording.instance.sendRecordingToServer();
                    }
                }
            }
            else {
                RecordingInfo info = ClientProxy.playerInfo;
                int state = info.getState();
                int oldState = info.getOldState();
                info.setState(state == 3 ? oldState : 3);

                if (state != info.getState()) {
                    if (mc.theWorld != null) {
                        ModRecording.instance.sendRecordingToServer();
                    }
                }
            }
        }
    }

}
