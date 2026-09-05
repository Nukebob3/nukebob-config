package net.nukebob.nbconfig.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.nukebob.nbconfig.NukebobConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(NukebobConfig.MOD_ID + "/main_config.json").toFile();
    private static MainConfig config;

    public boolean showLivesInNametag = true;

    public static synchronized MainConfig loadConfig() {
        if (config!=null) return config;

        if (!CONFIG_FILE.exists()) {
            config = new MainConfig();
            saveConfig();
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, MainConfig.class);
            } catch (IOException e) {
                NukebobConfig.LOGGER.error("Could not load main config file", e);
            }
        }
        if (config == null) {
            config = new MainConfig();
            saveConfig();
        }
        return config;
    }

    public static synchronized void saveConfig() {
        if (config == null) {
            config = new MainConfig();
        }

        File parent = CONFIG_FILE.getParentFile();
        if (parent!=null && !parent.exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            NukebobConfig.LOGGER.error("Could not save main config file", e);
        }
    }
}
