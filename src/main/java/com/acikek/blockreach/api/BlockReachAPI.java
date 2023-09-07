package com.acikek.blockreach.api;

import com.acikek.blockreach.BlockReachMod;
import com.acikek.blockreach.util.BlockReachPlayer;
import com.mojang.serialization.Codec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class BlockReachAPI {

    /**
     * A world key used to represent empty values in block reach maps.
     * @see BlockReachAPI#getPositions(PlayerEntity)
     * @see BlockReachAPI#getPositionView(PlayerEntity)
     */
    public static final RegistryKey<World> EMPTY_WORLD = RegistryKey.of(RegistryKeys.WORLD, BlockReachMod.id("empty"));

    /**
     * A codec for a reaching position map.
     */
    public static final Codec<Map<BlockPos, RegistryKey<World>>> POSITIONS_CODEC = Codec.unboundedMap(
            BlockPos.CODEC,
            RegistryKey.createCodec(RegistryKeys.WORLD)
    );

    /**
     * @return a <em>modifiable</em> map of the player's reaching positions.
     * May be {@code null} if the player has no reaching positions.
     * The {@link RegistryKey<World>} values should never be {@code null}. Instead, empty world values
     * are represented with {@link BlockReachAPI#EMPTY_WORLD}.
     */
    public static @Nullable Map<BlockPos, RegistryKey<World>> getPositions(PlayerEntity player) {
        return ((BlockReachPlayer) player).blockreachapi$reachingRaw();
    }

    /**
     * @return an <em>unmodifiable</em> map of the player's reaching positions
     * @see BlockReachAPI#getPositions(PlayerEntity)
     */
    public static @NotNull Map<BlockPos, RegistryKey<World>> getPositionView(PlayerEntity player) {
        var positions = BlockReachAPI.getPositions(player);
        if (positions == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(positions);
    }

    /**
     * @return whether the player has any reaching positions
     */
    public static boolean hasAnyPositions(PlayerEntity player) {
        return ((BlockReachPlayer) player).blockreachapi$isReaching();
    }

    /**
     * @return whether the player has the specified reaching position, regardless of attached world
     */
    public static boolean hasPosition(PlayerEntity player, BlockPos pos) {
        var positions = BlockReachAPI.getPositions(player);
        return positions != null && positions.containsKey(pos);
    }

    /**
     * @return whether the player has the specified reaching position in the specified world
     */
    public static boolean hasPositionInWorld(PlayerEntity player, BlockPos pos, RegistryKey<World> worldKey) {
        var positions = BlockReachAPI.getPositions(player);
        if (positions == null) {
            return false;
        }
        var world = positions.get(pos);
        return world != null && world.equals(worldKey);
    }

    /**
     * @see BlockReachAPI#hasPositionInWorld(PlayerEntity, BlockPos, RegistryKey)
     */
    public static boolean hasPositionInWorld(PlayerEntity player, BlockPos pos, World world) {
        return BlockReachAPI.hasPositionInWorld(player, pos, world.getRegistryKey());
    }

    /**
     * @param strictWorld whether to check against the specified block entity's world
     * @see BlockReachAPI#hasPositionInWorld(PlayerEntity, BlockPos, World)
     * @see BlockReachAPI#hasPosition(PlayerEntity, BlockPos)
     */
    public static boolean hasBlockEntity(PlayerEntity player, BlockEntity blockEntity, boolean strictWorld) {
        return strictWorld && blockEntity.getWorld() != null
                ? BlockReachAPI.hasPositionInWorld(player, blockEntity.getPos(), blockEntity.getWorld())
                : BlockReachAPI.hasPosition(player, blockEntity.getPos());
    }

    /**
     * @see BlockReachAPI#hasBlockEntity(PlayerEntity, BlockEntity, boolean)
     */
    public static boolean hasBlockEntity(PlayerEntity player, BlockEntity blockEntity) {
        return BlockReachAPI.hasBlockEntity(player, blockEntity, true);
    }

    /**
     * Adds a reaching position attached to the specified world to the specified player's map.
     * @see BlockReachAPI#addPosition(PlayerEntity, BlockPos)
     */
    public static RegistryKey<World> addPositionInWorld(PlayerEntity player, BlockPos pos, RegistryKey<World> worldKey) {
        return ((BlockReachPlayer) player).blockreachapi$reaching().put(pos, worldKey);
    }

    /**
     * @see BlockReachAPI#addPositionInWorld(PlayerEntity, BlockPos, RegistryKey)
     */
    public static RegistryKey<World> addPositionInWorld(PlayerEntity player, BlockPos pos, World world) {
        return BlockReachAPI.addPositionInWorld(player, pos, world.getRegistryKey());
    }

    /**
     * Adds a reaching position to the specified player's map.
     * @return the replaced world key, if any
     */
    public static RegistryKey<World> addPosition(PlayerEntity player, BlockPos pos) {
        return BlockReachAPI.addPositionInWorld(player, pos, BlockReachAPI.EMPTY_WORLD);
    }

    /**
     * Adds a block entity's reaching position to the specified player's map.
     * @param strictWorld whether to also add the block entity's world
     * @see BlockReachAPI#addPositionInWorld(PlayerEntity, BlockPos, World)
     * @see BlockReachAPI#addPosition(PlayerEntity, BlockPos)
     */
    public static RegistryKey<World> addBlockEntity(PlayerEntity player, BlockEntity blockEntity, boolean strictWorld) {
        return strictWorld && blockEntity.getWorld() != null
                ? BlockReachAPI.addPositionInWorld(player, blockEntity.getPos(), blockEntity.getWorld())
                : BlockReachAPI.addPosition(player, blockEntity.getPos());
    }

    /**
     * @see BlockReachAPI#addBlockEntity(PlayerEntity, BlockEntity)
     */
    public static RegistryKey<World> addBlockEntity(PlayerEntity player, BlockEntity blockEntity) {
        return BlockReachAPI.addBlockEntity(player, blockEntity, true);
    }

    /**
     * Removes a reaching position from the specified player's map.
     * @return the removed world key, if any
     */
    public static RegistryKey<World> removePosition(PlayerEntity player, BlockPos pos) {
        var positions = BlockReachAPI.getPositions(player);
        if (positions == null) {
            return null;
        }
        return positions.remove(pos);
    }

    /**
     * Removes a block entity's position from the specified player's map.
     * @see BlockReachAPI#removePosition(PlayerEntity, BlockPos)
     */
    public static RegistryKey<World> removeBlockEntity(PlayerEntity player, BlockEntity blockEntity) {
        return BlockReachAPI.removePosition(player, blockEntity.getPos());
    }
}
