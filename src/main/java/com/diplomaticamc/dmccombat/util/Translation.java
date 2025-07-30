package com.diplomaticamc.dmccombat.util;

import com.diplomaticamc.dmccombat.DMCCombat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Translation {
    private Translation() {}

    private static HashMap<String, String> translations;
    public static boolean loadStrings() {
        File languageDirectory = new File(DMCCombat.getInstance().getDataFolder(), "languages/");

        // Check if the languages directory exists
        if (!languageDirectory.exists()) {
            languageDirectory.mkdir();

            // Get the plugin jar file
            Method getFile;
            try {
                getFile = DMCCombat.class.getSuperclass().getDeclaredMethod("getFile");
            } catch(NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            }

            getFile.setAccessible(true);

            // Open the jar file
            File jarFile;
            try {
                jarFile = (File)getFile.invoke(DMCCombat.getInstance());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }

            // Unpack the jar file
            JarFile jar;
            try {
                jar = new JarFile(jarFile);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            // Loop over all files in the jar and find the languages
            final Enumeration<JarEntry> entries = jar.entries();
            while(entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith("languages/")) {
                    DMCCombat.getInstance().saveResource(name, false);
                }
            }

            // Close the jar file
            try {
                jar.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // Get the language from the config
        String locale = DMCCombat.getInstance().getConfig().getString("locale");
        if(locale == null) {
            locale = "en_US";
        }

        // Load the language file
        File languageFile = new File(DMCCombat.getInstance().getDataFolder(), "languages/" + locale + ".yml");
        if(!languageFile.exists()) {
            return false;
        }
        FileConfiguration translationsYaml = YamlConfiguration.loadConfiguration(languageFile);

        // Load the translations
        translations = new HashMap<String, String>();
        for (Map.Entry<String, Object> translation : translationsYaml.getValues(false).entrySet()) {
            translations.put(translation.getKey(), translation.getValue().toString());
        }

        return true;
    }

    public static String get(String text) {
        if(translations == null) return "";
        return translations.get(text).toString();
    }
}
