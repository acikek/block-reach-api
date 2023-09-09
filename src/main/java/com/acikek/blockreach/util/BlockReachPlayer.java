package com.acikek.blockreach.util;

import com.google.common.collect.Multimap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockReachPlayer {

    boolean blockreachapi$isReaching();

    Multimap<BlockPos, RegistryKey<World>> blockreachapi$reaching();

    Multimap<BlockPos, RegistryKey<World>> blockreachapi$reachingRaw();
}
