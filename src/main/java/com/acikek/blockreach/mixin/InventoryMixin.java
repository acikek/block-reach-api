package com.acikek.blockreach.mixin;

import com.acikek.blockreach.api.BlockReachAPI;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public interface InventoryMixin {

    @Inject(method = "canPlayerUse(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/player/PlayerEntity;I)Z", cancellable = true, at = @At("HEAD"))
    private static void blockreachapi$fakeUsable(BlockEntity blockEntity, PlayerEntity player, int range, CallbackInfoReturnable<Boolean> cir) {
        if (BlockReachAPI.hasBlockEntity(player, blockEntity)) {
            cir.setReturnValue(true);
        }
    }
}
