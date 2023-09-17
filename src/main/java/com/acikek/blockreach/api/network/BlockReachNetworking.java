package com.acikek.blockreach.api.network;

import com.acikek.blockreach.api.impl.network.BlockReachNetworkingImpl;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockReachNetworking {
    
    /**
     * Syncs the player's entire position map to their client.
     */
    public static void sync(ServerPlayerEntity player) {
        BlockReachNetworkingImpl.s2cSyncAll(player);
    }

    /**
     * Syncs the specified positions in the player's position map to their client.
     */
    public static void syncPositions(ServerPlayerEntity player, Set<BlockPos> positions) {
        BlockReachNetworkingImpl.s2cSyncDiff(player, positions);
    }

    /**
     * @see BlockReachNetworking#syncPositions(ServerPlayerEntity, Set)
     */
    public static void syncPositions(ServerPlayerEntity player, BlockPos... positions) {
        BlockReachNetworking.syncPositions(player, Arrays.stream(positions).collect(Collectors.toSet()));
    }

    /**
     * @see BlockReachNetworking#syncPositions(ServerPlayerEntity, Set)
     */
    public static void syncPosition(ServerPlayerEntity player, BlockPos pos) {
        BlockReachNetworking.syncPositions(player, Collections.singleton(pos));
    }

    /**
     * Syncs the specified block entities' positions in the player's position map to their client.
     * @see BlockReachNetworking#syncPositions(ServerPlayerEntity, Set)
     */
    public static void syncBlockEntities(ServerPlayerEntity player, Set<BlockEntity> blockEntities) {
        var positions = blockEntities.stream()
                .map(BlockEntity::getPos)
                .collect(Collectors.toSet());
        BlockReachNetworking.syncPositions(player, positions);
    }

    /**
     * @see BlockReachNetworking#syncBlockEntities(ServerPlayerEntity, Set)
     */
    public static void syncBlockEntities(ServerPlayerEntity player, BlockEntity... blockEntities) {
        BlockReachNetworking.syncBlockEntities(player, Arrays.stream(blockEntities).collect(Collectors.toSet()));
    }

    /**
     * @see BlockReachNetworking#syncBlockEntities(ServerPlayerEntity, Set)
     */
    public static void syncBlockEntities(ServerPlayerEntity player, BlockEntity blockEntity) {
        BlockReachNetworking.syncBlockEntities(player, Collections.singleton(blockEntity));
    }
}
