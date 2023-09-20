package com.acikek.blockreach.mixin.debug;

import com.acikek.blockreach.BlockReachMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeHandledScreen()V"))
    private void blockreachapi$catchScreenClose(CallbackInfo ci) {
        var player = (PlayerEntity) (Object) this;
        var screenType = Registries.SCREEN_HANDLER.getId(player.currentScreenHandler.getType());
        BlockReachMod.LOGGER.debug(
                "Screen '{}' ({}) forcefully closed for player {}!",
                screenType, player.currentScreenHandler.getClass(), player.getName().getString()
        );
    }
}
