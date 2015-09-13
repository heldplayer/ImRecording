package me.heldplayer.mods.recording;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.heldplayer.mods.recording.client.ClientProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.*;
import net.specialattack.forge.core.client.MC;
import net.specialattack.forge.core.sync.ISyncable;
import net.specialattack.forge.core.sync.ISyncableOwner;
import net.specialattack.forge.core.sync.SyncObjectProvider;
import net.specialattack.forge.core.sync.SyncTrackingStorage;
import net.specialattack.forge.core.sync.object.SyncInt;
import net.specialattack.forge.core.sync.object.SyncString;

public class RecordingInfo implements ISyncableOwner {

    private UUID uuid;

    public int displayTime;
    public Side side;
    public final UUID playerUUID;

    public SyncInt state, oldState;
    public SyncString name;
    private Map<String, ISyncable> syncables = new HashMap<String, ISyncable>();

    // States:
    // 0: Not recording
    // 1: Recording
    // 2: Recording paused
    // 3: Request stop/pause recording

    public RecordingInfo(UUID uuid) {
        this.playerUUID = uuid;
        this.syncables.put("State", this.state = new SyncInt(0, this, "State"));
        this.syncables.put("OldState", this.oldState = new SyncInt(0, this, "OldState"));
        this.syncables.put("Name", this.name = new SyncString(null, this, "Name"));
    }

    @Override
    public Map<String, ISyncable> getSyncables() {
        return this.syncables;
    }

    @Override
    public void register(SyncTrackingStorage tracker) {
        if (tracker != null) {
            this.side = tracker.side;
            if (this.side == Side.CLIENT) {
                IChatComponent message = this.getRecordingString(true);
                if (message != null) {
                    MC.getPlayer().addChatMessage(message);
                }
            }
        } else if (this.side == Side.CLIENT) {
            ModRecording.proxy.removeClientInfo(this.playerUUID);
        }
    }

    @Override
    public boolean canPlayerTrack(EntityPlayerMP player) {
        return true;
    }

    @Override
    public SyncObjectProvider getProvider() {
        return ModRecording.syncProvider;
    }

    @Override
    public UUID getSyncUUID() {
        return this.uuid;
    }

    @Override
    public void setSyncUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean canStartTracking() {
        return true;
    }

    @Override
    public String getDebugDisplay() {
        return "Recording: " + this.name.value;
    }

    @Override
    public void syncableChanged(ISyncable syncable) {
        if (syncable == this.state && this.side == Side.CLIENT) {
            IChatComponent message = this.getRecordingString(false);
            if (message != null) {
                MC.getPlayer().addChatMessage(message);
            }
        }
    }

    public int getState() {
        return this.state.value;
    }

    public void setState(int newState) {
        this.displayTime = 0;
        this.oldState.value = this.state.value;
        this.state.value = newState;
    }

    public int getOldState() {
        return this.oldState.value;
    }

    @SideOnly(Side.CLIENT)
    public int getColor(boolean disableOpcaity) {
        int opacity = 0xFF;

        if (!disableOpcaity) {
            if (!ClientProxy.overlayEnabled) {
                opacity = (int) (255.0F - ((this.displayTime) * 255.0F / 205.0F));
            }
        }

        if (this.state.value == 1) {
            return 0xFF4444 | (opacity << 24);
        }
        if (this.state.value == 2) {
            return 0x0080FF | (opacity << 24);
        }
        if (this.state.value == 3) {
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
        return 0xFFFFFF | (opacity << 24);
    }

    public IChatComponent getRecordingString(boolean onConnect) {
        String key = null;

        if (onConnect) {
            if (this.state.value == 1) {
                key = "imrecording:state.recording.connect";
            }
            if (this.state.value == 2) {
                key = "imrecording:state.paused.connect";
            }
            if (this.state.value == 3) {
                key = "imrecording:state.halt";
            }
        } else {
            if (this.state.value == 1 && this.oldState.value == 0) {
                key = "imrecording:state.recording";
            }
            if (this.state.value == 1 && this.oldState.value == 2) {
                key = "imrecording:state.recording.resume";
            }
            if (this.state.value == 2 && this.oldState.value == 1) {
                key = "imrecording:state.paused";
            }
            if (this.state.value == 3) {
                key = "imrecording:state.halt";
            }
            if (this.state.value == 0 && (this.oldState.value == 1 || this.oldState.value == 2)) {
                key = "imrecording:state.recording.stopped";
            }
            if (this.oldState.value == 3) {
                key = "imrecording:state.halt.stopped";
            }
        }

        if (key == null) {
            return null;
        }

        ChatComponentText name = new ChatComponentText(this.name.value);
        name.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + this.name.value + " ")).setColor(this.getChatColor());

        ChatComponentTranslation result = new ChatComponentTranslation(key, name);
        result.getChatStyle().setItalic(true).setColor(EnumChatFormatting.GRAY);
        return result;
    }

    public EnumChatFormatting getChatColor() {
        switch (this.state.value) {
            case 1:
                return EnumChatFormatting.RED;
            case 2:
                return EnumChatFormatting.BLUE;
            case 3:
                return EnumChatFormatting.GREEN;
            default:
                return EnumChatFormatting.GRAY;
        }
    }
}
