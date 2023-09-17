package com.acikek.blockreach.api.position;

import com.acikek.blockreach.BlockReachMod;
import com.acikek.blockreach.api.BlockReachAPI;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains methods and constants for working with the {@link BlockReachAPI} position map structure.
 */
public class BlockReachPositions {

    /**
     * A world key that represents a reaching position not bound to any specific world.
     * @see BlockReachAPI#getPositions(PlayerEntity)
     * @see BlockReachAPI#getPositionView(PlayerEntity)
     */
    public static final RegistryKey<World> GLOBAL_WORLD = RegistryKey.of(RegistryKeys.WORLD, BlockReachMod.id("global"));

    /**
     * A codec for a reaching position map.
     * @see BlockReachPositions#getPositionMap(Multimap)
     * @see BlockReachPositions#createPositions(Map)
     */
    public static final Codec<Map<BlockPos, List<RegistryKey<World>>>> POSITIONS_CODEC = Codec.unboundedMap(
            Codec.STRING.xmap(str -> BlockPos.fromLong(Long.parseLong(str)), pos -> Long.toString(pos.asLong())),
            Codec.list(RegistryKey.createCodec(RegistryKeys.WORLD))
    );

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
     * Converts a position map to a {@link Multimap} for use in {@link BlockReachAPI}.
     * @return the resulting multimap
     */
    public static @NotNull Multimap<BlockPos, RegistryKey<World>> createPositions(@NotNull Map<BlockPos, List<RegistryKey<World>>> map) {
        Multimap<BlockPos, RegistryKey<World>> multimap = MultimapBuilder.treeKeys().hashSetValues(1).build();
        for (var entry : map.entrySet()) {
            multimap.putAll(entry.getKey(), entry.getValue());
        }
        return multimap;
    }
}
