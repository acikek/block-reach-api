package com.acikek.blockreach.api.position;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public record BlockReachPos(RegistryKey<World> worldKey, BlockPos pos) {

    public static final Codec<BlockReachPos> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("world").forGetter(BlockReachPos::worldKey),
                    BlockPos.CODEC.fieldOf("pos").forGetter(BlockReachPos::pos)
            ).apply(instance, BlockReachPos::new)
    );

    public static final Codec<List<BlockReachPos>> LIST_CODEC = Codec.list(CODEC);

    public boolean matches(BlockPos pos) {
        return this.pos.equals(pos);
    }

    public boolean matches(RegistryKey<World> worldKey, BlockPos pos) {
        return matches(pos) && this.worldKey.getValue().equals(worldKey.getValue());
    }

    public boolean matches(World world, BlockPos pos) {
        return matches(world.getRegistryKey(), pos);
    }

    public static BlockReachPos create(World world, BlockPos pos) {
        return new BlockReachPos(world.getRegistryKey(), pos);
    }
}
