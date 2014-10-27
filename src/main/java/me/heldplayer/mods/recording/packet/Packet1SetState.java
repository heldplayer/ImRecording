package me.heldplayer.mods.recording.packet;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.util.List;
import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.specialattack.forge.core.packet.Attributes;
import net.specialattack.forge.core.sync.SyncHandler;

public class Packet1SetState extends ImRecordingPacket {

    public int state;

    public Packet1SetState() {
        super(null);
    }

    public Packet1SetState(RecordingInfo info) {
        super(null);
        this.state = info.getState();
    }

    @Override
    public String getDebugInfo() {
        return String.format("PacketSetState[State: %s]", this.state);
    }

    @Override
    public Side getSendingSide() {
        return Side.CLIENT;
    }

    @Override
    public void read(ChannelHandlerContext context, ByteBuf in) throws IOException {
        this.state = in.readUnsignedByte();
    }

    @Override
    public void write(ChannelHandlerContext context, ByteBuf out) throws IOException {
        out.writeByte(this.state);
    }

    @Override
    public void onData(ChannelHandlerContext context) {
        this.requireAttribute(Attributes.SENDING_PLAYER);

        EntityPlayer player = this.attr(Attributes.SENDING_PLAYER).get();
        RecordingInfo info = null;

        List<RecordingInfo> infos = CommonProxy.recordingPlayers;

        for (int i = 0; i < infos.size(); i++) {
            info = infos.get(i);

            if (info.name.equalsIgnoreCase(player.getCommandSenderName())) {
                info.setState(this.state);

                if (this.state == 0) {
                    infos.remove(i);
                    info.setNotValid();
                }

                info.displayTime = 0;

                break;
            }

            info = null;
        }

        if (info == null && this.state != 0) {
            info = new RecordingInfo(player.getCommandSenderName(), player.getUniqueID(), (byte) this.state);

            SyncHandler.Server.startTracking(info);

            infos.add(info);
        }

        if (info != null) {
            ModRecording.instance.sendPlayerToPlayers(info);
        }
    }

}
