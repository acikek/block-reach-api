package com.acikek.blockreach.client;

import com.acikek.blockreach.api.impl.network.BlockReachNetworkingImpl;
import net.fabricmc.api.ClientModInitializer;

public class BlockReachClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockReachNetworkingImpl.registerClient();
    }
}
