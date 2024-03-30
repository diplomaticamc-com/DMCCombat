package net.earthmc.emccom.manager;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import net.earthmc.emccom.object.CombatPref;

public class ResidentMetadataManager {
    private final String combatPrefKey = "emccom_combat_pref";

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
}
