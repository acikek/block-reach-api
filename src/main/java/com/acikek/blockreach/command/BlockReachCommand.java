package com.acikek.blockreach.command;

import com.acikek.blockreach.api.BlockReachAPI;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlockReachCommand {

    public static int add(CommandContext<ServerCommandSource> context, boolean useWorld) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        ServerWorld world = useWorld ? DimensionArgumentType.getDimensionArgument(context, "world") : null;
        RegistryKey<World> worldKey = useWorld ? world.getRegistryKey() : null;
        for (var player : targets) {
            if (useWorld) {
                BlockReachAPI.addPositionInWorld(player, pos, worldKey);
            }
            else {
                BlockReachAPI.addPosition(player, pos);
            }
        }
        return targets.size();
    }

    public static int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return add(context, false);
    }

    public static int addToWorld(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return add(context, true);
    }

    public static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        for (var player : targets) {
            BlockReachAPI.removePosition(player, pos);
        }
        return targets.size();
    }

    public static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        for (var player : targets) {
            var positions = BlockReachAPI.getPositions(player);
            if (positions != null) {
                positions.clear();
            }
        }
        return targets.size();
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("blockreachapi")
                        .then(argument("targets", EntityArgumentType.players())
                                .then(literal("add")
                                        .then(argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(BlockReachCommand::add)
                                                .then(argument("world", DimensionArgumentType.dimension())
                                                        .executes(BlockReachCommand::addToWorld))))
                                .then(literal("remove")
                                        .then(argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(BlockReachCommand::remove)))
                                .then(literal("clear")
                                        .executes(BlockReachCommand::clear)))
                        .requires(source -> source.hasPermissionLevel(2)))
        );
    }
}
