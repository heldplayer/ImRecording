package blue.heldplayer.mods.recording;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.specialattack.forge.core.sync.SyncObjectProvider;
import net.specialattack.forge.core.sync.SyncTrackingStorage;

public class SyncProvider extends SyncObjectProvider<RecordingInfo> {

    public SyncProvider() {
        super("ImRecording:RecordingInfo");
    }

    @Override
    public NBTTagCompound writeDescriptorClient(RecordingInfo info) {
        NBTTagCompound result = new NBTTagCompound();
        result.setString("UUID", info.playerUUID.toString());
        return result;
    }

    @Override
    public RecordingInfo readDescriptorClient(NBTTagCompound tag) {
        UUID uuid = UUID.fromString(tag.getString("UUID"));
        return ModRecording.proxy.getClientInfo(uuid);
    }

    @Override
    public NBTTagCompound writeDescriptorServer(RecordingInfo info) {
        return null;
    }

    @Override
    public RecordingInfo readDescriptorServer(NBTTagCompound tag, SyncTrackingStorage storage) {
        return null;
    }
}
