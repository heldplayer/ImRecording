package me.heldplayer.mods.recording.packet;

import net.minecraft.world.World;
import net.specialattack.forge.core.packet.SpACorePacket;

public abstract class ImRecordingPacket extends SpACorePacket {

    public ImRecordingPacket(World world) {
        super(world);
    }

}
