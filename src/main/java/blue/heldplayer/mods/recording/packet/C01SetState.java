package blue.heldplayer.mods.recording.packet;

import blue.heldplayer.mods.recording.CommonProxy;
import blue.heldplayer.mods.recording.RecordingInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class C01SetState extends ImRecordingPacket {

    public int state;

    public C01SetState() {
    }

    public C01SetState(RecordingInfo info) {
        this.state = info.getState();
    }

    @Override
    public String getDebugInfo() {
        return String.format("C01SetState[State: %s]", this.state);
    }

    @Override
    public Side getSendingSide() {
        return Side.CLIENT;
    }

    @Override
    public Side getReceivingSide() {
        return Side.SERVER;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.state = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.state);
    }

    @Override
    public void handle(MessageContext context, EntityPlayer player) {
        RecordingInfo info = CommonProxy.recordingPlayers.get(player.getUniqueID());

        if (info == null) {
            return;
        }

        info.setState(this.state);
    }
}
