package net.earthmc.emccom.object;

public enum SpawnProtPref {
    HIDE,
    SHOW;

    public static SpawnProtPref getSpawnProtPrefByName(String name) {
        for (SpawnProtPref spawnProtPref : values()) {
            if (spawnProtPref.toString().equalsIgnoreCase(name)) return spawnProtPref;
        }

        return null;
    }
}

