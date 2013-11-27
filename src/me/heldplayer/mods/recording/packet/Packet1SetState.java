
package me.heldplayer.mods.recording.packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.heldplayer.mods.recording.CommonProxy;
import me.heldplayer.mods.recording.ModRecording;
import me.heldplayer.mods.recording.RecordingInfo;
import me.heldplayer.util.HeldCore.packet.HeldCorePacket;
import me.heldplayer.util.HeldCore.sync.SyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;

public class Packet1SetState extends HeldCorePacket {

    public int state;

    public Packet1SetState(int packetId) {
        super(packetId, null);
    }

    public Packet1SetState(RecordingInfo info) {
        super(1, null);
        this.state = info.getState();
    }

    @Override
    public Side getSendingSide() {
        return Side.CLIENT;
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
        RecordingInfo info = null;

        List<RecordingInfo> infos = CommonProxy.recordingPlayers;

        for (int i = 0; i < infos.size(); i++) {
            info = infos.get(i);

            if (info.name.equalsIgnoreCase(player.username)) {
                info.setState(this.state);

                if (this.state == 0) {
                    infos.remove(i);
                }

                info.displayTime = 0;

                break;
            }

            info = null;
        }

        if (info == null && this.state != 0) {
            info = new RecordingInfo(player.username, (byte) this.state);

            SyncHandler.startTracking(info);

            infos.add(info);
        }

        if (info != null) {
            ModRecording.instance.sendPlayerToPlayers(info);
        }
    }

}
