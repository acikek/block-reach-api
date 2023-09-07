package com.acikek.blockreach.util;

import com.acikek.blockreach.api.position.BlockReachPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BlockReachPlayer {

    boolean blockreachapi$isReaching();

    Map<BlockPos, RegistryKey<World>> blockreachapi$reaching();
}
