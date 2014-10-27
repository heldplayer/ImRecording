package me.heldplayer.mods.recording;

import com.google.common.io.ByteArrayDataInput;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import me.heldplayer.mods.recording.client.ClientProxy;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.specialattack.forge.core.sync.ISyncable;
import net.specialattack.forge.core.sync.ISyncableObjectOwner;
import net.specialattack.forge.core.sync.SInteger;
import net.specialattack.forge.core.sync.SString;

public class RecordingInfo implements ISyncableObjectOwner {

    public String name;
    public UUID uuid;
    public int displayTime;
    private boolean isInvalid;
    private SInteger state;
    private SString uuidStr;
    private int oldState;
    private List<ISyncable> syncables;

    // States:
    // 0: Not recording
    // 1: Recording
    // 2: Recording paused
    // 3: Request stop/pause recording

    public RecordingInfo(String name, UUID uuid, int state) {
        this.name = name;
        this.uuid = uuid;
        this.state = new SInteger(this, state);
        this.uuidStr = new SString(this, "");
        this.syncables = Arrays.asList((ISyncable) this.state, this.uuidStr);
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
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[RecordingInfo: name=" + this.name + ", state=" + this.state + "]";
    }

    public int getState() {
        return this.state.getValue();
    }

    public void setState(int newState) {
        this.displayTime = 0;
        this.oldState = this.state.getValue();
        this.state.setValue(newState);
    }

    public int getOldState() {
        return this.oldState;
    }

    @SideOnly(Side.CLIENT)
    public int getColor(boolean disableOpcaity) {
        int opacity = 0xFF;

        if (!disableOpcaity) {
            if (!ClientProxy.overlayEnabled) {
                opacity = (int) (255.0F - ((this.displayTime) * 255.0F / 205.0F));
            }
        }

        if (this.state.getValue() == 1) {
            return 0xFF4444 | (opacity << 24);
        }
        if (this.state.getValue() == 2) {
            return 0x0080FF | (opacity << 24);
        }
        if (this.state.getValue() == 3) {
            if (!disableOpcaity) {
                opacity = 0x90 + (int) MathHelper.abs(0x60 * MathHelper.sin(this.displayTime / 6.28F));
                if (!ClientProxy.overlayEnabled) {
                    if (this.displayTime < 200) {
                        opacity *= (205 - this.displayTime) / 205.0F;
                    }
                }
            }

            return 0x10C91C | (opacity << 24);
        }
        return 0xFFFFF | (opacity << 24);
    }

    public String getRecordingString(boolean onConnect) {
        String base = this.getChatColor() + "" + EnumChatFormatting.ITALIC + this.name + EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC + " ";

        if (onConnect) {
            if (this.state.getValue() == 1) {
                return base + "is recording";
            }
            if (this.state.getValue() == 2) {
                return base + "was recording but paused";
            }
            if (this.state.getValue() == 3) {
                return base + "is requesting to halt recording";
            }
        } else {
            if (this.state.getValue() == 1 && this.oldState == 0) {
                return base + "has started recording";
            }
            if (this.state.getValue() == 1 && this.oldState == 2) {
                return base + "has resumed recording";
            }
            if (this.state.getValue() == 2 && this.oldState == 1) {
                return base + "has paused recording";
            }
            if (this.state.getValue() == 3) {
                return base + "is requesting to halt recording";
            }
            if (this.state.getValue() == 0 && (this.oldState == 1 || this.oldState == 2)) {
                return base + "has stopped recording";
            }
            if (this.oldState == 3) {
                return base + "is no longer requesting to halt recording";
            }
        }

        return null;
    }

    public EnumChatFormatting getChatColor() {
        if (this.state.getValue() == 1) {
            return EnumChatFormatting.RED;
        }
        if (this.state.getValue() == 2) {
            return EnumChatFormatting.BLUE;
        }
        if (this.state.getValue() == 3) {
            return EnumChatFormatting.GREEN;
        }

        return EnumChatFormatting.GRAY;
    }

    @Override
    public boolean isNotValid() {
        return this.isInvalid;
    }

    @Override
    public void setNotValid() {
        this.isInvalid = true;
    }

    @Override
    public List<ISyncable> getSyncables() {
        return this.syncables;
    }

    @Override
    public void readSetup(ByteArrayDataInput in) throws IOException {
        for (ISyncable syncable : this.syncables) {
            syncable.setId(in.readInt());
            syncable.read(in);
        }
    }

    @Override
    public void writeSetup(DataOutputStream out) throws IOException {
        for (ISyncable syncable : this.syncables) {
            out.writeInt(syncable.getId());
            syncable.write(out);
        }
    }

    @Override
    public String getIdentifier() {
        return "RecordingInfo_" + this.name;
    }

    @Override
    public boolean isWorldBound() {
        return false;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public int getPosX() {
        return 0;
    }

    @Override
    public int getPosY() {
        return 0;
    }

    @Override
    public int getPosZ() {
        return 0;
    }

    @Override
    public void onDataChanged(ISyncable syncable) {
        this.displayTime = 0;
    }

}
