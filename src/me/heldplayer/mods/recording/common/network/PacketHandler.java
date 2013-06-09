
package me.heldplayer.mods.recording.common.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.heldplayer.mods.recording.common.CommonProxy;
import me.heldplayer.mods.recording.common.ModRecording;
import me.heldplayer.mods.recording.common.RecordingInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

    /*
     * Packet Types
     * 
     * Id Side Data Description
     * 0 CLIENT byte:state Sends the state of the client
     * 1 (C) SERVER int:length String:player byte:state Sends an update to a
     * player in the list
     * 2 SERVER int:size [int:length String:name byte:state]:players Sends a
     * complete list of active players to add to the list
     */

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        ByteArrayDataInput dat = ByteStreams.newDataInput(packet.data);

        int type = dat.readUnsignedByte();

        String username = ((EntityPlayer) player).username;

        int state;
        List<RecordingInfo> infos = CommonProxy.recordingPlayers;
        String name;

        switch (type) {
        case 0:
            state = dat.readByte();

            RecordingInfo localInfo = null;

            existanceCheck:
            {
                for (int i = 0; i < infos.size(); i++) {
                    localInfo = infos.get(i);

                    if (localInfo.name.equalsIgnoreCase(username)) {
                        localInfo.setState((byte) state);

                        if (state == 0) {
                            infos.remove(i);
                        }

                        localInfo.displayTime = 0;

                        break existanceCheck;
                    }
                }

                if (state != 0) {
                    localInfo = new RecordingInfo(username, (byte) state);

                    infos.add(localInfo);
                }
            }

            if (localInfo != null)
                ModRecording.instance.sendPlayerToPlayers(localInfo);
        break;
        case 1:
            int nameLength = dat.readInt();

            byte[] nameBytes = new byte[nameLength];

            dat.readFully(nameBytes);

            name = new String(nameBytes);

            state = dat.readByte();

            existanceCheck:
            {
                for (int i = 0; i < infos.size(); i++) {
                    RecordingInfo info = infos.get(i);

                    if (info.name.equalsIgnoreCase(name)) {
                        info.setState((byte) state);

                        if (state == 0) {
                            infos.remove(i);
                        }

                        info.displayTime = 0;

                        break existanceCheck;
                    }
                }

                if (state != 0) {
                    RecordingInfo info = new RecordingInfo(name, (byte) state);

                    infos.add(info);
                }
            }
        break;
        case 2:
            int size = dat.readInt();

            infos.clear();

            for (int i = 0; i < size; i++) {
                int length = dat.readInt();

                byte[] bytes = new byte[length];

                dat.readFully(bytes);

                name = new String(bytes);

                state = dat.readByte();

                if (state != 0) {
                    RecordingInfo info = new RecordingInfo(name, (byte) state);

                    infos.add(info);
                }
            }
        break;
        }
    }

    public static Packet getPacket(int type, Object arg) {
        int size = 1;

        switch (type) {
        case 0:
            size++;
        break;
        case 1:
            RecordingInfo info = (RecordingInfo) arg;
            byte[] nameBytes = info.name.getBytes();
            size++;
            size += nameBytes.length;
        break;
        case 2:
            RecordingInfo[] infos = (RecordingInfo[]) arg;

            for (RecordingInfo localInfo : infos) {
                size++;
                size += localInfo.name.getBytes().length;
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            dos.writeByte(type);
            switch (type) {
            case 0:
                dos.writeByte(((RecordingInfo) arg).getState());
            break;
            case 1:
                RecordingInfo info = (RecordingInfo) arg;
                byte[] nameBytes = info.name.getBytes();
                dos.writeInt(nameBytes.length);

                dos.write(nameBytes);

                dos.writeByte(info.getState());

            break;
            case 2:
                RecordingInfo[] infos = (RecordingInfo[]) arg;

                dos.writeInt(infos.length);

                for (RecordingInfo localInfo : infos) {
                    byte[] bytes = localInfo.name.getBytes();
                    dos.writeInt(bytes.length);
                    dos.write(bytes);
                    dos.writeByte(localInfo.getState());
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();

        packet.channel = "ImRecording";
        packet.data = bos.toByteArray();
        packet.length = packet.data.length;

        return packet;
    }
}
