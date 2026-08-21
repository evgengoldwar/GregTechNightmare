package com.EvgenWarGold.GregTechNightmare.GregTech.Wildcard;

public enum WildcardBlacklistMode {

    INPUT,
    OUTPUT;

    public static WildcardBlacklistMode fromName(String name) {
        if (name != null) {
            for (WildcardBlacklistMode mode : values()) {
                if (mode.name()
                    .equalsIgnoreCase(name)) {
                    return mode;
                }
            }
        }
        return OUTPUT;
    }
}
