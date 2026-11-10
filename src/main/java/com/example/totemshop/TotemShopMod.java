package com.example.totemshop;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotemShopMod implements ModInitializer {
    public static final String MOD_ID = "totemshop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("TotemShop Mod initialized for Minecraft 26.2!");
    }
}

