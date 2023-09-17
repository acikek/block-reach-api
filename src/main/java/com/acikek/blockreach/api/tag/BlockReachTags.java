package com.acikek.blockreach.api.tag;

import com.acikek.blockreach.BlockReachMod;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class BlockReachTags {

    public static final TagKey<Block> ALLOW_BLOCKS = TagKey.of(RegistryKeys.BLOCK, BlockReachMod.id("allow"));

    public static final TagKey<Block> DENY_BLOCKS = TagKey.of(RegistryKeys.BLOCK, BlockReachMod.id("deny"));

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
