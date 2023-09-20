package com.acikek.blockreach.mixin;

import com.acikek.blockreach.BlockReachMod;
import com.acikek.blockreach.api.position.BlockReachPositions;
import com.acikek.blockreach.util.BlockReachPlayer;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements BlockReachPlayer {

    @Unique
    private static final String NBT_KEY = "blockreachapi$positions";

    @Shadow public abstract float getEyeHeight(EntityPose pose);

    @Shadow public abstract EntityPose getPose();

    @Shadow private World world;

    @Shadow public abstract Text getName();

    // Lazily-loaded to prevent instantiating collection for every entity
    // Never purposefully destroyed here, but doesn't get serialized when empty
    @Unique
    private Multimap<BlockPos, RegistryKey<World>> blockreachapi$reaching = null;

    @Unique
    private Byte blockreachapi$warnTicks = null;

    @Override
    public boolean blockreachapi$isReaching() {
        return blockreachapi$reaching != null && !blockreachapi$reaching.isEmpty();
    }

    @Override
    public Multimap<BlockPos, RegistryKey<World>> blockreachapi$reaching() {
        if (blockreachapi$reaching == null) {
            blockreachapi$reaching = MultimapBuilder.treeKeys().hashSetValues(1).build();
        }
        return blockreachapi$reaching;
    }

    @Override
    public Multimap<BlockPos, RegistryKey<World>> blockreachapi$reachingRaw() {
        return blockreachapi$reaching;
    }

    @Override
    public void blockreachapi$setReaching(Multimap<BlockPos, RegistryKey<World>> multimap) {
        blockreachapi$reaching = multimap;
    }

    /**
     * Compares a reaching position value with a coordinate value passed in by {@link Entity#squaredDistanceTo(double, double, double)}.
     * <p>
     * When validating screen interactions, Vanilla increments each block coordinate by {@code 0.5}, resulting in the block's center.
     * An earlier version of this check was to remove the {@code 0.5} offsets and compare them against the ints of the block position.
     * <p>
     * With <a href="https://modrinth.com/mod/pehkui">Pehkui</a>, however, that method fails; Pehkui in particular fine-tunes the {@code squaredDistanceTo} values
     * so that reach calculations are more precise at smaller scales. Flooring the passed in values and checking against those
     * also fails as the values can sometimes 'bleed into' the next block coordinate, such as {@code 5} becoming {@code 6.0},
     * of course with a floored value of {@code 6.0} instead of {@code 5.0}. It also takes eye level into account for the Y value.
     * <p>
     * As these are two very different sets of values, a broad comparison covering both cases is not sufficient. This method
     * uses a manual compatibility implementation.
     */
    @Unique
    private boolean blockreachapi$compare(int blockPosValue, double providedValue, double eyeOffset) {
        return BlockReachMod.isPehkuiEnabled
                ? Math.abs((blockPosValue + 0.5 - eyeOffset) - providedValue) <= 0.5
                : blockPosValue == (int) (Math.floor(providedValue));
    }

    @Unique
    private void blockreachapi$warn(BlockPos pos) {
        if (blockreachapi$warnTicks != null) {
            return;
        }
        var blockId = Registries.BLOCK.getId(world.getBlockState(pos).getBlock());
        BlockReachMod.LOGGER.debug("Block '{}' ({}) called squaredDistanceTo directly for player {}!", blockId, pos.toShortString(), getName());
    }

    // Screen handlers call this method in some way, just not consistently.
    // Automatic validation - if this call doesn't go through on the server, no slot/GUI actions will be submitted
    @Inject(method = "squaredDistanceTo(DDD)D", cancellable = true, at = @At("HEAD"))
    private void blockreachapi$fakeDistance(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
        if (!blockreachapi$isReaching()) {
            return;
        }
        // Only calculate eye offset if Pehkui is enabled for the relevant check with it
        double eyeOffset = BlockReachMod.isPehkuiEnabled ? getEyeHeight(getPose()) : 0.0;
        for (BlockPos pos : blockreachapi$reaching.keySet()) {
            // This is an important math method that can be used elsewhere, so make sure we're targeting the synced position
            if (blockreachapi$compare(pos.getX(), x, 0.0)
                    && blockreachapi$compare(pos.getY(), y, eyeOffset)
                    && blockreachapi$compare(pos.getZ(), z, 0.0)) {
                cir.setReturnValue(0.0);
                blockreachapi$warn(pos);
                blockreachapi$warnTicks = 2;
                return;
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void blockreachapi$warnTick(CallbackInfo ci) {
        if (blockreachapi$warnTicks != null) {
            blockreachapi$warnTicks--;
            if (blockreachapi$warnTicks == 0) {
                blockreachapi$warnTicks = null;
            }
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void blockreachapi$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (!blockreachapi$isReaching()) {
            return;
        }
        var map = BlockReachPositions.getPositionMap(blockreachapi$reaching);
        var element = BlockReachPositions.POSITIONS_CODEC.encodeStart(NbtOps.INSTANCE, map)
                .getOrThrow(true, BlockReachMod.LOGGER::error);
        nbt.put(NBT_KEY, element);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void blockreachapi$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains(NBT_KEY)) {
            return;
        }
        var map = BlockReachPositions.POSITIONS_CODEC.decode(NbtOps.INSTANCE, nbt.get(NBT_KEY))
                .getOrThrow(true, BlockReachMod.LOGGER::error)
                .getFirst();
        blockreachapi$reaching = BlockReachPositions.createPositions(map);
    }
}
