package com.acikek.blockreach.api.tag;

import com.acikek.blockreach.BlockReachMod;
import com.acikek.blockreach.api.BlockReachAPI;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/**
 * Contains constants and methods for working with the {@link BlockReachAPI} tag overrides.
 */
public class BlockReachTags {

    /**
     * Allow players to reach to this block regardless of position map.
     */
    public static final TagKey<Block> ALLOW_BLOCKS = TagKey.of(RegistryKeys.BLOCK, BlockReachMod.id("allow"));

    /**
     * Deny players from reaching this block regardless of position map.
     */
    public static final TagKey<Block> DENY_BLOCKS = TagKey.of(RegistryKeys.BLOCK, BlockReachMod.id("deny"));

    /**
     * @return {@link TriState#TRUE} if the state is in {@link BlockReachTags#ALLOW_BLOCKS},
     * {@link TriState#FALSE} if in {@link BlockReachTags#DENY_BLOCKS}, or {@link TriState#DEFAULT} otherwise.
     */
    public static TriState query(BlockState state) {
        if (state.isIn(ALLOW_BLOCKS)) {
            return TriState.TRUE;
        }
        if (state.isIn(DENY_BLOCKS)) {
            return TriState.FALSE;
        }
        return TriState.DEFAULT;
    }
}
