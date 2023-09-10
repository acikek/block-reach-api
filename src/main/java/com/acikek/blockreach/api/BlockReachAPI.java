package com.acikek.blockreach.api;

import com.acikek.blockreach.BlockReachMod;
import com.acikek.blockreach.util.BlockReachPlayer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.serialization.Codec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockReachAPI {

    /**
     * A world key that represents a reaching position not bound to any specific world.
     * @see BlockReachAPI#getPositions(PlayerEntity)
     * @see BlockReachAPI#getPositionView(PlayerEntity)
     */
    public static final RegistryKey<World> GLOBAL_WORLD = RegistryKey.of(RegistryKeys.WORLD, BlockReachMod.id("global"));

    /**
     * A codec for a reaching position map.
     * @see BlockReachAPI#getPositionMap(Multimap)
     * @see BlockReachAPI#createPositions(Map)
     */
    public static final Codec<Map<BlockPos, List<RegistryKey<World>>>> POSITIONS_CODEC = Codec.unboundedMap(
            BlockPos.CODEC,
            Codec.list(RegistryKey.createCodec(RegistryKeys.WORLD))
    );

    /**
     * @return a <em>modifiable</em> map of the player's reaching positions.
     * May be {@code null} if the player has no reaching positions.
     * The present {@link RegistryKey<World>} collections should never be empty. Instead, positions
     * not attached to a world should be treated as 'global' and must contain {@link BlockReachAPI#GLOBAL_WORLD}.
     * Global position values may be attached to other worlds, but to no effect.
     * @see Multimap#get(Object)
     */
    public static @Nullable Multimap<BlockPos, RegistryKey<World>> getPositions(PlayerEntity player) {
        return ((BlockReachPlayer) player).blockreachapi$reachingRaw();
    }

    /**
     * Converts a position multimap to a {@link Map} better suited for serialization.
     * @return the resulting map
     */
    public static @NotNull Map<BlockPos, List<RegistryKey<World>>> getPositionMap(@NotNull Multimap<BlockPos, RegistryKey<World>> multimap) {
        return multimap.asMap().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, pair -> List.copyOf(pair.getValue())));
    }

    /**
     * @see BlockReachAPI#getPositionMap(Multimap)
     */
    public static @NotNull Map<BlockPos, List<RegistryKey<World>>> getPositionMap(PlayerEntity player) {
        var positions = BlockReachAPI.getPositions(player);
        return positions != null
                ? BlockReachAPI.getPositionMap(positions)
                : Collections.emptyMap();
    }

    /**
     * Converts a position map to a {@link Multimap} for use in {@link BlockReachAPI}.
     * @return the resulting multimap
     */
    public static @NotNull Multimap<BlockPos, RegistryKey<World>> createPositions(@NotNull Map<BlockPos, List<RegistryKey<World>>> map) {
        var collMap = map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        pair -> (Collection<RegistryKey<World>>) pair.getValue()
                ));
        return Multimaps.newSetMultimap(collMap, HashSet::new);
    }

    /**
     * @return an <em>unmodifiable</em> map of the player's reaching positions
     * @see BlockReachAPI#getPositions(PlayerEntity)
     */
    public static @NotNull Multimap<BlockPos, RegistryKey<World>> getPositionView(PlayerEntity player) {
        var positions = BlockReachAPI.getPositions(player);
        return positions != null
                ? Multimaps.unmodifiableMultimap(positions)
                : HashMultimap.create();
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
     * @return whether the player has the specified reaching position attached to the {@link BlockReachAPI#GLOBAL_WORLD}
     */
    public static boolean hasPositionGlobally(PlayerEntity player, BlockPos pos) {
        var positions = BlockReachAPI.getPositions(player);
        return positions != null && positions.get(pos).contains(BlockReachAPI.GLOBAL_WORLD);
    }

    /**
     * @param strict whether {@link BlockReachAPI#GLOBAL_WORLD} is not sufficient for the world check
     * @return whether the player has the specified reaching position in the specified world
     */
    public static boolean hasPositionInWorld(PlayerEntity player, BlockPos pos, RegistryKey<World> worldKey, boolean strict) {
        var positions = BlockReachAPI.getPositions(player);
        if (positions == null) {
            return false;
        }
        var worldKeys = positions.get(pos);
        if (worldKeys.isEmpty()) {
            return false; // Position not present in multimap
        }
        return worldKeys.contains(worldKey) || (!strict && worldKeys.contains(GLOBAL_WORLD));
    }

    /**
     * @see BlockReachAPI#hasPositionInWorld(PlayerEntity, BlockPos, RegistryKey, boolean)
     */
    public static boolean hasPositionInWorld(PlayerEntity player, BlockPos pos, RegistryKey<World> worldKey) {
        return hasPositionInWorld(player, pos, worldKey, false);
    }

    /**
     * @see BlockReachAPI#hasPositionInWorld(PlayerEntity, BlockPos, RegistryKey, boolean)
     */
    public static boolean hasPositionInWorld(PlayerEntity player, BlockPos pos, World world, boolean strict) {
        return BlockReachAPI.hasPositionInWorld(player, pos, world.getRegistryKey(), strict);
    }

    /**
     * @see BlockReachAPI#hasPositionInWorld(PlayerEntity, BlockPos, World, boolean)
     */
    public static boolean hasPositionInWorld(PlayerEntity player, BlockPos pos, World world) {
        return BlockReachAPI.hasPositionInWorld(player, pos, world, false);
    }

    /**
     * @param checkWorld whether to check against the specified block entity's world (non-strictly)
     * @see BlockReachAPI#hasPositionInWorld(PlayerEntity, BlockPos, World)
     * @see BlockReachAPI#hasPosition(PlayerEntity, BlockPos)
     */
    public static boolean hasBlockEntity(PlayerEntity player, BlockEntity blockEntity, boolean checkWorld) {
        return checkWorld && blockEntity.getWorld() != null
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
    public static boolean addPositionInWorld(PlayerEntity player, BlockPos pos, RegistryKey<World> worldKey) {
        return ((BlockReachPlayer) player).blockreachapi$reaching().put(pos, worldKey);
    }

    /**
     * @see BlockReachAPI#addPositionInWorld(PlayerEntity, BlockPos, RegistryKey)
     */
    public static boolean addPositionInWorld(PlayerEntity player, BlockPos pos, World world) {
        return BlockReachAPI.addPositionInWorld(player, pos, world.getRegistryKey());
    }

    /**
     * Adds a reaching position to the specified player's map.
     * @return whether the position was added successfully
     */
    public static boolean addPosition(PlayerEntity player, BlockPos pos) {
        return BlockReachAPI.addPositionInWorld(player, pos, BlockReachAPI.GLOBAL_WORLD);
    }

    /**
     * Adds a block entity's reaching position to the specified player's map.
     * @param strictWorld whether to also add the block entity's world
     * @see BlockReachAPI#addPositionInWorld(PlayerEntity, BlockPos, World)
     * @see BlockReachAPI#addPosition(PlayerEntity, BlockPos)
     */
    public static boolean addBlockEntity(PlayerEntity player, BlockEntity blockEntity, boolean strictWorld) {
        return strictWorld && blockEntity.getWorld() != null
                ? BlockReachAPI.addPositionInWorld(player, blockEntity.getPos(), blockEntity.getWorld())
                : BlockReachAPI.addPosition(player, blockEntity.getPos());
    }

    /**
     * @see BlockReachAPI#addBlockEntity(PlayerEntity, BlockEntity)
     */
    public static boolean addBlockEntity(PlayerEntity player, BlockEntity blockEntity) {
        return BlockReachAPI.addBlockEntity(player, blockEntity, true);
    }

    /**
     * Removes a reaching position from the specified player's map.
     * @return the removed world keys, if any
     */
    public static Collection<RegistryKey<World>> removePosition(PlayerEntity player, BlockPos pos) {
        var positions = BlockReachAPI.getPositions(player);
        if (positions == null) {
            return null;
        }
        return positions.removeAll(pos);
    }

    /**
     * Removes a reaching position from the specified world in the player's map.
     * Other worlds may still be attached to the position.
     * @return whether the entry was removed successfully
     */
    public static boolean removePositionFromWorld(PlayerEntity player, BlockPos pos, RegistryKey<World> worldKey) {
        var positions = BlockReachAPI.getPositions(player);
        if (positions == null) {
            return false;
        }
        return positions.remove(pos, worldKey);
    }

    /**
     * @see BlockReachAPI#removePositionFromWorld(PlayerEntity, BlockPos, RegistryKey)
     */
    public static boolean removePositionFromWorld(PlayerEntity player, BlockPos pos, World world) {
        return BlockReachAPI.removePositionFromWorld(player, pos, world.getRegistryKey());
    }

    /**
     * Removes a reaching position from the {@link BlockReachAPI#GLOBAL_WORLD} in the player's map.
     * @see BlockReachAPI#removePositionFromWorld(PlayerEntity, BlockPos, RegistryKey)
     */
    public static boolean removePositionGlobally(PlayerEntity player, BlockPos pos) {
        return BlockReachAPI.removePositionFromWorld(player, pos, BlockReachAPI.GLOBAL_WORLD);
    }

    /**
     * Removes a block entity's position from the specified player's map.
     * @see BlockReachAPI#removePosition(PlayerEntity, BlockPos)
     */
    public static Collection<RegistryKey<World>> removeBlockEntity(PlayerEntity player, BlockEntity blockEntity) {
        return BlockReachAPI.removePosition(player, blockEntity.getPos());
    }

    public static void sync(ServerPlayerEntity player) {

    }

    public static void sync(ServerPlayerEntity player, Set<BlockPos> positions) {

    }
}
