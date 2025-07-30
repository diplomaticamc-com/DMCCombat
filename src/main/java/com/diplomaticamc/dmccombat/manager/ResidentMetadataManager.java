package com.diplomaticamc.dmccombat.manager;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.diplomaticamc.dmccombat.object.CombatPref;
import com.diplomaticamc.dmccombat.object.SpawnProtPref;

public class ResidentMetadataManager {
    private final String combatPrefKey = "emccom_combat_pref";
    private final String spawnProtPrefKey = "emccom_spawn_prot_pref";

    public void setResidentCombatPref(Resident resident, CombatPref combatPref) {
        if (!resident.hasMeta(combatPrefKey))
            resident.addMetaData(new StringDataField(combatPrefKey, null));

        StringDataField sdf = (StringDataField) resident.getMetadata(combatPrefKey);
        if (sdf == null) return;

        sdf.setValue(combatPref.toString());
        resident.addMetaData(sdf);
    }

    public CombatPref getResidentCombatPref(Resident resident) {
        if (resident == null) return null;

        StringDataField sdf = (StringDataField) resident.getMetadata(combatPrefKey);
        if (sdf == null) return CombatPref.SAFE;

        return CombatPref.getCombatPrefByName(sdf.getValue());
    }

    public void setResidentSpawnProtPref(Resident resident, SpawnProtPref spawnProtPref) {
        if (!resident.hasMeta(spawnProtPrefKey))
            resident.addMetaData(new StringDataField(spawnProtPrefKey, null));

        StringDataField sdf = (StringDataField) resident.getMetadata(spawnProtPrefKey);
        if (sdf == null) return;

        sdf.setValue(spawnProtPref.toString());
        resident.addMetaData(sdf);
    }

    public SpawnProtPref getResidentSpawnProtPref(Resident resident) {
        if (resident == null) return null;

        StringDataField sdf = (StringDataField) resident.getMetadata(spawnProtPrefKey);
        if (sdf == null) return SpawnProtPref.SHOW;

        return SpawnProtPref.getSpawnProtPrefByName(sdf.getValue());
    }
}
