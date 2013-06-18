
package me.heldplayer.mods.recording.common;

import me.heldplayer.mods.recording.client.ClientProxy;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RecordingInfo {
    public String name;
    private byte state;
    private byte oldState;
    public int displayTime;

    // States:
    // 0: Not recording
    // 1: Recording
    // 2: Recording paused
    // 3: Request stop/pause recording

    public RecordingInfo(String name, byte state) {
        this.name = name;
        this.state = state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        RecordingInfo other = (RecordingInfo) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public byte getState() {
        return this.state;
    }

    public byte getOldState() {
        return this.oldState;
    }

    public void setState(byte newState) {
        this.displayTime = 0;
        this.oldState = this.state;
        this.state = newState;
    }

    @SideOnly(Side.CLIENT)
    public int getColor() {
        int opacity = 0xFF;

        if (!ClientProxy.overlayEnabled) {
            opacity = (int) (255.0F - (((float) this.displayTime) * 255.0F / 205.0F));
        }

        if (this.state == (byte) 1) {
            return 0xFF4444 | (opacity << 24);
        }
        if (this.state == (byte) 2) {
            return 0x0080FF | (opacity << 24);
        }
        if (this.state == (byte) 3) {
            opacity = 0x90 + (int) MathHelper.abs(0x60 * MathHelper.sin((float) this.displayTime / 6.28F));
            if (!ClientProxy.overlayEnabled) {
                if (this.displayTime < 200) {
                    opacity *= (float) (205 - this.displayTime) / 205.0F;
                }
            }

            return 0x10C91C | (opacity << 24);
        }
        return 0xFFFFF | (opacity << 24);
    }

    public ChatColor getChatColor() {
        if (this.state == (byte) 1) {
            return ChatColor.RED;
        }
        if (this.state == (byte) 2) {
            return ChatColor.BLUE;
        }
        if (this.state == (byte) 3) {
            return ChatColor.GREEN;
        }

        return ChatColor.GRAY;
    }

    public String getRecordingString(boolean onConnect) {
        String base = this.getChatColor() + "" + ChatColor.ITALIC + this.name + ChatColor.GRAY + "" + ChatColor.ITALIC + " ";

        if (onConnect) {
            if (this.state == (byte) 1) {
                return base + "is recording";
            }
            if (this.state == (byte) 2) {
                return base + "was recording but paused";
            }
            if (this.state == (byte) 3) {
                return base + "is requesting to halt recording";
            }
        }
        else {
            if (this.state == (byte) 1 && this.oldState == (byte) 0) {
                return base + "has started recording";
            }
            if (this.state == (byte) 1 && this.oldState == (byte) 2) {
                return base + "has resumed recording";
            }
            if (this.state == (byte) 2 && this.oldState == (byte) 1) {
                return base + "has paused recording";
            }
            if (this.state == (byte) 3) {
                return base + "is requesting to halt recording";
            }
            if (this.state == (byte) 0 && (this.oldState == (byte) 1 || this.oldState == (byte) 2)) {
                return base + "has stopped recording";
            }
            if (this.oldState == (byte) 3) {
                return base + "is no longer requesting to halt recording";
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "[RecordingPair: name=" + this.name + ", state=" + this.state + " ]";
    }

}
