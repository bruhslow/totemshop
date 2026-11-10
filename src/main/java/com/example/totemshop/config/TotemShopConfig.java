package com.example.totemshop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class TotemShopConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "totemshop.json");

    public boolean enabled = true;
    public String keyCombo = "CTRL + O";
    public int delayTicks = 1;
    public boolean autoClose = true;
    public boolean soundAlert = true;
    public String shopCommand = "shop";

    private static TotemShopConfig INSTANCE;

    public static TotemShopConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static TotemShopConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                TotemShopConfig config = GSON.fromJson(reader, TotemShopConfig.class);
                if (config != null) {
                    INSTANCE = config;
                    return config;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TotemShopConfig newConfig = new TotemShopConfig();
        newConfig.save();
        INSTANCE = newConfig;
        return newConfig;
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

