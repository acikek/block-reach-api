package com.acikek.blockreach.network;

import com.acikek.blockreach.BlockReachMod;
import com.acikek.blockreach.api.BlockReachAPI;
import com.acikek.blockreach.util.BlockReachPlayer;
import com.google.common.collect.Multimap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlockReachNetworking {

    public static final Identifier ALL = BlockReachMod.id("all");
    public static final Identifier DIFF = BlockReachMod.id("diff");

    private static void s2cSyncMap(ServerPlayerEntity player, Identifier channel, Map<BlockPos, List<RegistryKey<World>>> map) {
        var buf = PacketByteBufs.create();
        buf.encodeAsJson(BlockReachAPI.POSITIONS_CODEC, BlockReachAPI.getPositionMap(player));
        ServerPlayNetworking.send(player, channel, buf);
    }

    public static void s2cSyncAll(ServerPlayerEntity player) {
        s2cSyncMap(player, ALL, BlockReachAPI.getPositionMap(player));
    }

    public static void s2cSyncDiff(ServerPlayerEntity player, Set<BlockPos> positions) {
        var positionMap = BlockReachAPI.getPositionMap(player);
        var syncMap = positions.stream()
                .collect(Collectors.toMap(pos -> pos, positionMap::get));
        s2cSyncMap(player, DIFF, syncMap);
    }

    @Environment(EnvType.CLIENT)
    private static void executeWithMap(MinecraftClient client, PacketByteBuf buf, Consumer<Multimap<BlockPos, RegistryKey<World>>> callback) {
        final var map = buf.decodeAsJson(BlockReachAPI.POSITIONS_CODEC);
        client.execute(() -> {
            var multimap = BlockReachAPI.createPositions(map);
            callback.accept(multimap);
        });
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ALL, (client, handler, buf, responseSender) -> {
            executeWithMap(client, buf, multimap -> ((BlockReachPlayer) client.player).blockreachapi$setReaching(multimap));
        });
        ClientPlayNetworking.registerGlobalReceiver(DIFF, (client, handler, buf, responseSender) -> {
            executeWithMap(client, buf, multimap -> ((BlockReachPlayer) client.player).blockreachapi$reaching().putAll(multimap));
        });
    }
}
