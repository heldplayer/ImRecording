
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

public class Packet2UpdatePlayerState extends HeldCorePacket {

    public int state;
    public String username;

    public Packet2UpdatePlayerState(int packetId) {
        super(packetId);
    }

    public Packet2UpdatePlayerState(RecordingInfo info) {
        super(1);
        this.state = info.getState();
        this.username = info.name;
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        int nameLength = in.readInt();
        byte[] nameBytes = new byte[nameLength];
        in.readFully(nameBytes);
        this.username = new String(nameBytes);

        this.state = in.readUnsignedByte();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        byte[] nameBytes = this.username.getBytes();
        out.writeInt(nameBytes.length);
        out.write(nameBytes);

        out.writeByte(this.state);
    }

    @Override
    public void onData(INetworkManager manager, EntityPlayer player) {
        existanceCheck:
        {
            List<RecordingInfo> infos = CommonProxy.recordingPlayers;

            for (int i = 0; i < infos.size(); i++) {
                RecordingInfo info = infos.get(i);

                if (info.name.equalsIgnoreCase(this.username)) {
                    info.setState((byte) this.state);

                    if (this.state == 0) {
                        infos.remove(i);
                    }

                    info.displayTime = 0;

                    break existanceCheck;
                }
            }

            if (this.state != 0) {
                RecordingInfo info = new RecordingInfo(this.username, (byte) this.state);

                infos.add(info);
            }
        }
    }
}
