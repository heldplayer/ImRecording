
package me.heldplayer.mods.recording;

import java.util.logging.Logger;

import me.heldplayer.util.HeldCore.ModInfo;

/**
 * ImRecording mod Objects
 * 
 */
public final class Objects {

    public static final String MOD_ID = "ImRecording";
    public static final String MOD_NAME = "The \"I'm Recording\" Mod";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String MOD_CHANNEL = "ImRecording";
    public static final String CLIENT_PROXY = "me.heldplayer.mods.recording.client.ClientProxy";
    public static final String SERVER_PROXY = "me.heldplayer.mods.recording.CommonProxy";

    public static final ModInfo MOD_INFO = new ModInfo(MOD_ID, MOD_NAME, MOD_VERSION);

    public static Logger log;

}
