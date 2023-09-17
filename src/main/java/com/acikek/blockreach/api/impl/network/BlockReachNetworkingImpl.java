package com.acikek.blockreach.api.impl.network;

import com.acikek.blockreach.BlockReachMod;
import com.acikek.blockreach.api.BlockReachAPI;
import com.acikek.blockreach.api.position.BlockReachPositions;
import com.acikek.blockreach.util.BlockReachPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockReachNetworkingImpl {

    public static final Identifier ALL = BlockReachMod.id("all");
    public static final Identifier DIFF = BlockReachMod.id("diff");

    private static void s2cSyncMap(ServerPlayerEntity player, Identifier channel, Map<BlockPos, List<RegistryKey<World>>> map) {
        var buf = PacketByteBufs.create();
        buf.encodeAsJson(BlockReachPositions.POSITIONS_CODEC, map);
        ServerPlayNetworking.send(player, channel, buf);
    }

    public static void s2cSyncAll(ServerPlayerEntity player) {
        s2cSyncMap(player, ALL, BlockReachAPI.getPositionMap(player));
    }

    public static void s2cSyncDiff(ServerPlayerEntity player, Set<BlockPos> positions) {
        var positionMap = BlockReachAPI.getPositionView(player);
        var syncMap = positions.stream()
                .collect(Collectors.toMap(pos -> pos, pos -> positionMap.get(pos).stream().toList()));
        s2cSyncMap(player, DIFF, syncMap);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ALL, (client, handler, buf, responseSender) -> {
            final var map = buf.decodeAsJson(BlockReachPositions.POSITIONS_CODEC);
            client.execute(() -> {
                var multimap = BlockReachPositions.createPositions(map);
                ((BlockReachPlayer) client.player).blockreachapi$setReaching(multimap);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DIFF, (client, handler, buf, responseSender) -> {
            final var map = buf.decodeAsJson(BlockReachPositions.POSITIONS_CODEC);
            client.execute(() -> {
                var reaching = ((BlockReachPlayer) client.player).blockreachapi$reaching();
                // Cannot call BlockReachAPI.createPositions here,
                // as Multimap#putAll is a no-op and we need to preserve empty list information
                for (var entry : map.entrySet()) {
                    if (entry.getValue().isEmpty()) {
                        reaching.removeAll(entry.getKey());
                    }
                    else {
                        reaching.putAll(entry.getKey(), entry.getValue());
                    }
                }
            });
        });
    }
}
