package com.acikek.blockreach.api;

import com.acikek.blockreach.BlockReachMod;
import com.mojang.serialization.Codec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BlockReachAPI {

    public static final RegistryKey<World> EMPTY_WORLD = RegistryKey.of(RegistryKeys.WORLD, BlockReachMod.id("empty"));

    public static final Codec<Map<BlockPos, RegistryKey<World>>> POSITIONS_CODEC = Codec.unboundedMap(
            BlockPos.CODEC,
            RegistryKey.createCodec(RegistryKeys.WORLD)
    );

    public static @Nullable Map<BlockPos, RegistryKey<World>> getPositions(PlayerEntity player) {
        return null;
    }

    public static @NotNull Map<BlockPos, RegistryKey<World>> getPositionsView(PlayerEntity player) {
        return null;
    }

    public static boolean hasAnyPositions(PlayerEntity player) {
        return false;
    }

    public static boolean hasPosition(PlayerEntity player, BlockPos pos) {
        return false;
    }

    public static boolean hasPositionInWorld(PlayerEntity player, BlockPos pos, RegistryKey<World> worldKey) {
        return false;
    }

    public static boolean hasPositionInWorld(PlayerEntity player, BlockPos pos, World world) {
        return BlockReachAPI.hasPositionInWorld(player, pos, world.getRegistryKey());
    }

    public static boolean hasBlockEntity(PlayerEntity player, BlockEntity blockEntity, boolean strictWorld) {
        return false;
    }

    public static boolean hasBlockEntity(PlayerEntity player, BlockEntity blockEntity) {
        return BlockReachAPI.hasBlockEntity(player, blockEntity, true);
    }

    public static boolean addPositionInWorld(PlayerEntity player, BlockPos pos, RegistryKey<World> worldKey) {
        return false;
    }

    public static boolean addPositionInWorld(PlayerEntity player, BlockPos pos, World world) {
        return BlockReachAPI.addPositionInWorld(player, pos, world.getRegistryKey());
    }

    public static boolean addPosition(PlayerEntity player, BlockPos pos) {
        return BlockReachAPI.addPositionInWorld(player, pos, BlockReachAPI.EMPTY_WORLD);
    }

    public static boolean addBlockEntity(PlayerEntity player, BlockEntity blockEntity, boolean strictWorld) {
        return false;
    }

    public static boolean addBlockEntity(PlayerEntity player, BlockEntity blockEntity) {
        return BlockReachAPI.addBlockEntity(player, blockEntity, true);
    }

    public static boolean removePosition(PlayerEntity player, BlockPos pos) {
        return false;
    }

    public static boolean removeBlockEntity(PlayerEntity player, BlockEntity blockEntity) {
        return BlockReachAPI.removePosition(player, blockEntity.getPos());
    }
}
