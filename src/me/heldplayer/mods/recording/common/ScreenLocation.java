
package me.heldplayer.mods.recording.common;

import me.heldplayer.util.HeldCore.config.IConfigurable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum ScreenLocation implements IConfigurable {

    TopLeft("top-left"),
    TopRight("top-right"),
    BottomLeft("bottom-left"),
    BottomRight("bottom-right");

    public final String name;

    private ScreenLocation(String name) {
        this.name = name;
    }

    public static ScreenLocation fromString(String name) {
        for (ScreenLocation location : values()) {
            if (location.name.equalsIgnoreCase(name)) {
                return location;
            }
        }

        return null;
    }

    @Override
    public String serialize() {
        return this.name;
    }

    @Override
    public IConfigurable load(String serialized) {
        for (ScreenLocation loc : values()) {
            if (loc.name.equalsIgnoreCase(serialized)) {
                return loc;
            }
        }

        return null;
    }

}
