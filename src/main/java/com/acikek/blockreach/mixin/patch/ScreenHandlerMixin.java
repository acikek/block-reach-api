package com.acikek.blockreach.mixin.patch;

import com.acikek.blockreach.api.BlockReachAPI;
import com.acikek.blockreach.api.tag.BlockReachTags;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Unique
    private static BlockPos blockreachapi$capturedBlockPos;

    @Inject(method = "method_17696(Lnet/minecraft/block/Block;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Boolean;",
            cancellable = true, at = @At("HEAD"))
    private static void blockreachapi$captureBE(Block block, PlayerEntity playerEntity, World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var queryResult = BlockReachTags.query(world.getBlockState(pos)).getBoxed();
        if (queryResult != null) {
            cir.setReturnValue(queryResult);
        }
        blockreachapi$capturedBlockPos = pos;
    }

    @Redirect(method = "method_17696(Lnet/minecraft/block/Block;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Boolean;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;squaredDistanceTo(DDD)D"))
    private static double blockreachapi$patchCall(PlayerEntity player, double x, double y, double z) {
        return BlockReachAPI.hasPosition(player, blockreachapi$capturedBlockPos) ? 0 : player.squaredDistanceTo(x, y, z);
    }
}