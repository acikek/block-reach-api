package com.acikek.blockreach.client;

import com.acikek.blockreach.network.BlockReachNetworking;
import net.fabricmc.api.ClientModInitializer;

public class BlockReachClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockReachNetworking.registerClient();
    }
}
