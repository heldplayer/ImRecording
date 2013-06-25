
package me.heldplayer.mods.recording.packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.util.HeldCore.packet.HeldCorePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;

import com.google.common.io.ByteArrayDataInput;

public class Packet1SetState extends HeldCorePacket {

    public int state;

    public Packet1SetState(int packetId) {
        super(packetId);
    }

    public Packet1SetState(RecordingInfo info) {
        super(1);
        this.state = info.getState();
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.state = in.readUnsignedByte();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(this.state);
    }

    @Override
    public void onData(INetworkManager manager, EntityPlayer player) {
        RecordingInfo localInfo = null;

        existanceCheck:
        {
            List<RecordingInfo> infos = CommonProxy.recordingPlayers;

            for (int i = 0; i < infos.size(); i++) {
                localInfo = infos.get(i);

                if (localInfo.name.equalsIgnoreCase(player.username)) {
                    localInfo.setState((byte) this.state);

                    if (this.state == 0) {
                        infos.remove(i);
                    }

                    localInfo.displayTime = 0;

                    break existanceCheck;
                }
            }

            if (this.state != 0) {
                localInfo = new RecordingInfo(player.username, (byte) this.state);

                infos.add(localInfo);
            }
        }

        if (localInfo != null) {
            ModRecording.instance.sendPlayerToPlayers(localInfo);
        }
    }

}
