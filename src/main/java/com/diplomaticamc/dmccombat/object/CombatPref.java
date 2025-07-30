package com.diplomaticamc.dmccombat.object;

public enum CombatPref {
    SAFE,
    UNSAFE;

    public static CombatPref getCombatPrefByName(String name) {
        for (CombatPref combatPref : values()) {
            if (combatPref.toString().equalsIgnoreCase(name)) return combatPref;
        }

        return null;
    }
}

