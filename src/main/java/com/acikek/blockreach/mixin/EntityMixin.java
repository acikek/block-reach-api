package com.acikek.blockreach.mixin;

import com.acikek.blockreach.BlockReachMod;
import com.acikek.blockreach.api.position.BlockReachPos;
import com.acikek.blockreach.util.BlockReachPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements BlockReachPlayer {

    @Unique
    private static final String NBT_KEY = "blockreachapi$positions";

    @Shadow public abstract float getEyeHeight(EntityPose pose);

    @Shadow public abstract EntityPose getPose();

    @Unique
    private Set<BlockReachPos> blockreachapi$reaching = null;

    @Override
    public boolean blockreachapi$isReaching() {
        return blockreachapi$reaching != null && !blockreachapi$reaching.isEmpty();
    }

    @Override
    public Set<BlockReachPos> blockreachapi$reaching() {
        if (blockreachapi$reaching == null) {
            blockreachapi$reaching = new HashSet<>();
        }
        return blockreachapi$reaching;
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

    // Screen handlers call this method in some way, just not consistently.
    // Automatic validation - if this call doesn't go through on the server, no slot/GUI actions will be submitted
    @Inject(method = "squaredDistanceTo(DDD)D", cancellable = true, at = @At("HEAD"))
    private void blockreachapi$fakeDistance(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
        if (!blockreachapi$isReaching()) {
            return;
        }
        // Only calculate eye offset if Pehkui is enabled for the relevant check with it
        double eyeOffset = BlockReachMod.isPehkuiEnabled ? getEyeHeight(getPose()) : 0.0;
        for (BlockReachPos data : blockreachapi$reaching) {
            // This is an important math method that can be used elsewhere, so make sure we're targeting the synced position
            BlockPos pos = data.pos();
            if (blockreachapi$compare(pos.getX(), x, 0.0)
                    && blockreachapi$compare(pos.getY(), y, eyeOffset)
                    && blockreachapi$compare(pos.getZ(), z, 0.0)) {
                cir.setReturnValue(0.0);
            }
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void blockreachapi$writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (!blockreachapi$isReaching()) {
            return;
        }
        List<BlockReachPos> positions = new ArrayList<>(blockreachapi$reaching);
        var element = BlockReachPos.LIST_CODEC.encodeStart(NbtOps.INSTANCE, positions)
                .getOrThrow(true, BlockReachMod.LOGGER::error);
        nbt.put(NBT_KEY, element);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void blockreachapi$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains(NBT_KEY)) {
            return;
        }
        List<BlockReachPos> positions = BlockReachPos.LIST_CODEC.decode(NbtOps.INSTANCE, nbt.get(NBT_KEY))
                .getOrThrow(true, BlockReachMod.LOGGER::error)
                .getFirst();
        blockreachapi$reaching = new HashSet<>(positions);
    }
}
