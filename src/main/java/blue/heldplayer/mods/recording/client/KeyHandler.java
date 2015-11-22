package blue.heldplayer.mods.recording.client;

import blue.heldplayer.mods.recording.CommonProxy;
import blue.heldplayer.mods.recording.ModRecording;
import blue.heldplayer.mods.recording.RecordingInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.specialattack.forge.core.client.MC;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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
        this.keyTick(event.type, event.phase == TickEvent.Phase.END);
    }

    private void keyTick(TickEvent.Type type, boolean tickEnd) {
        for (int i = 0; i < this.keyBindings.length; i++) {
            KeyBinding keyBinding = this.keyBindings[i];
            int keyCode = keyBinding.getKeyCode();
            boolean state = (keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
            if (state != this.keyDown[i] || (state && this.repeatings[i])) {
                if (state) {
                    this.keyDown(type, i, tickEnd, !this.keyDown[i]);
                } else {
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
        Minecraft mc = MC.getMc();
        KeyBinding kb = this.keyBindings[binding];

        if (mc.currentScreen != null && mc.theWorld != null && !(mc.currentScreen instanceof GuiMainMenu)) {
            return;
        }

        if (kb == this.overlayToggle) {
            ClientProxy.overlayEnabled = !ClientProxy.overlayEnabled;

            for (RecordingInfo info : CommonProxy.recordingPlayers.values()) {
                info.displayTime = 0;
            }
        }

        if (!ClientProxy.overlayEnabled && ModRecording.config.lockOverlay) {
            return;
        }

        if (!isRepeat) {
            if (kb == this.recordingToggle || kb == this.pausedToggle) {
                this.ticktimes[binding]++;
            }
        } else {
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
        Minecraft mc = MC.getMc();
        KeyBinding kb = this.keyBindings[binding];

        if (mc.currentScreen != null && mc.theWorld != null && !(mc.currentScreen instanceof GuiMainMenu)) {
            return;
        }

        if (!ClientProxy.overlayEnabled && ModRecording.config.lockOverlay) {
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
            } else {
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
