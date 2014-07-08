package me.heldplayer.mods.recording.client.gui;

import cpw.mods.fml.client.config.GuiConfig;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.Objects;
import net.minecraft.client.gui.GuiScreen;

public class GuiConfiguration extends GuiConfig {

    public GuiConfiguration(GuiScreen parent) {
        super(parent, ModRecording.instance.config.getConfigElements(), Objects.MOD_ID, false, false, "I'm Recording Mod Configuration");
    }

}
