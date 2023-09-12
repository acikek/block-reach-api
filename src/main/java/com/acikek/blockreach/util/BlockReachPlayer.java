package com.acikek.blockreach.util;

import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockReachPlayer {

    boolean blockreachapi$isReaching();

    Multimap<BlockPos, RegistryKey<World>> blockreachapi$reaching();

    Multimap<BlockPos, RegistryKey<World>> blockreachapi$reachingRaw();

    void blockreachapi$setReaching(Multimap<BlockPos, RegistryKey<World>> multimap);

    static void registerCopier() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            var positions = ((BlockReachPlayer) oldPlayer).blockreachapi$reachingRaw();
            ((BlockReachPlayer) newPlayer).blockreachapi$setReaching(positions);
        });
    }
}
