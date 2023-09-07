package com.acikek.blockreach.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public interface BlockReachPlayer {

    boolean blockreachapi$isReaching();

    Map<BlockPos, RegistryKey<World>> blockreachapi$reaching();
}
