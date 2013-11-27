
package me.heldplayer.mods.recording.packet;

import me.heldplayer.mods.recording.Objects;

// TODO: make this obsolete
public class PacketHandler extends me.heldplayer.util.HeldCore.packet.PacketHandler {

    public static PacketHandler instance;

    public PacketHandler() {
        super(Objects.MOD_CHANNEL);
        this.registerPacket(1, Packet1SetState.class);
        instance = this;
    }

}
