package me.heldplayer.mods.recording;

public enum ScreenLocation {

    TopLeft("top-left"),
    TopRight("top-right"),
    BottomLeft("bottom-left"),
    BottomRight("bottom-right");
    public static final String[] validValues;

    static {
        validValues = new String[ScreenLocation.values().length];
        for (int i = 0; i < ScreenLocation.validValues.length; i++) {
            ScreenLocation.validValues[i] = ScreenLocation.values()[i].name;
        }
    }

    public final String name;

    ScreenLocation(String name) {
        this.name = name;
    }

    public static ScreenLocation fromString(String name) {
        for (ScreenLocation location : ScreenLocation.values()) {
            if (location.name.equalsIgnoreCase(name)) {
                return location;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
