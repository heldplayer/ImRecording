
package me.heldplayer.mods.recording.packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.util.HeldCore.packet.HeldCorePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;

public class Packet3BroadcastRecorders extends HeldCorePacket {

    public RecordingInfo[] infos;

    public Packet3BroadcastRecorders(int packetId) {
        super(packetId, null);
    }

    public Packet3BroadcastRecorders(RecordingInfo[] infos) {
        super(3, null);
        this.infos = infos;
    }

    @Override
    public Side getSendingSide() {
        return Side.SERVER;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        int size = in.readInt();

        this.infos = new RecordingInfo[size];

        for (int i = 0; i < size; i++) {
            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.readFully(bytes);
            String username = new String(bytes);

            int state = in.readByte();

            if (state != 0) {
                RecordingInfo info = new RecordingInfo(username, (byte) state);
                this.infos[i] = info;
            }
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(this.infos.length);

        for (RecordingInfo info : this.infos) {
            byte[] bytes = info.name.getBytes();
            out.writeInt(bytes.length);
            out.write(bytes);

            out.writeByte(info.getState());
        }
    }

    @Override
    public void onData(INetworkManager manager, EntityPlayer player) {
        List<RecordingInfo> infos = CommonProxy.recordingPlayers;

        infos.clear();

        for (RecordingInfo info : infos) {
            if (info != null) {
                infos.add(info);
            }
        }
    }
}
