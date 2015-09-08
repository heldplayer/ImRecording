package me.heldplayer.mods.recording;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.specialattack.forge.core.sync.SyncObjectProvider;

public class SyncProvider extends SyncObjectProvider<RecordingInfo> {

    public SyncProvider() {
        super("ImRecording:RecordingInfo");
    }

    @Override
    public NBTTagCompound writeDescriptor(RecordingInfo owner) {
        NBTTagCompound result = new NBTTagCompound();
        result.setString("UUID", owner.playerUUID.toString());
        return result;
    }

    @Override
    public RecordingInfo readDescriptor(NBTTagCompound tag) {
        UUID uuid = UUID.fromString(tag.getString("UUID"));
        return ModRecording.proxy.getClientInfo(uuid);
    }
}
