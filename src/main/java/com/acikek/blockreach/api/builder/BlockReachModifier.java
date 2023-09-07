package com.acikek.blockreach.api.builder;

import com.acikek.blockreach.api.position.BlockReachPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public interface BlockReachModifier {

    boolean isAdd();

    BlockReachModifier positions(Set<BlockReachPos> positions);

    default BlockReachModifier positions(BlockReachPos... positions) {
        return positions(Arrays.stream(positions).collect(Collectors.toSet()));
    }

    default BlockReachModifier position(BlockReachPos position) {
        return positions(Collections.singleton(position));
    }

    BlockReachModifier positions(World world, Set<BlockPos> positions) {

    }
}
