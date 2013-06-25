
package me.heldplayer.mods.recording.common.packet;

import me.heldplayer.mods.recording.common.Objects;

public class PacketHandler extends me.heldplayer.util.HeldCore.packet.PacketHandler {

    public static PacketHandler instance;

    public PacketHandler() {
        super(Objects.MOD_CHANNEL);
        this.registerPacket(1, Packet1SetState.class);
        this.registerPacket(2, Packet2UpdatePlayerState.class);
        this.registerPacket(3, Packet3BroadcastRecorders.class);
        instance = this;
    }

}
