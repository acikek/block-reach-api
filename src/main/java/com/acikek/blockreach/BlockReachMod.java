package com.acikek.blockreach;

import com.acikek.blockreach.api.BlockReachAPI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockReachMod implements ModInitializer {

    public static final String ID = "blockreachapi";

    public static final Logger LOGGER = LogManager.getLogger("Block Reach API");

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }

    public static boolean isPehkuiEnabled = false;

    @Override
    public void onInitialize() {
        isPehkuiEnabled = FabricLoader.getInstance().isModLoaded("pehkui");
    }
}
