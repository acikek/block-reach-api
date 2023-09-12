package com.acikek.blockreach.command;

import com.acikek.blockreach.api.BlockReachAPI;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlockReachCommand {

    @FunctionalInterface
    private interface Callback {
        int call(Collection<ServerPlayerEntity> targets, BlockPos pos, ServerWorld world, RegistryKey<World> worldKey);
    }

    @FunctionalInterface
    private interface SubCommand {
        int run(CommandContext<ServerCommandSource> context, boolean useWorld) throws CommandSyntaxException;
    }

    private static int run(CommandContext<ServerCommandSource> context, boolean useWorld, Callback callback) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        ServerWorld world = useWorld ? DimensionArgumentType.getDimensionArgument(context, "world") : null;
        RegistryKey<World> worldKey = useWorld ? world.getRegistryKey() : null;
        return callback.call(targets, pos, world, worldKey);
    }

    private static int modify(CommandContext<ServerCommandSource> context, boolean useWorld, boolean add) throws CommandSyntaxException {
        return run(context, useWorld, (targets, pos, world, worldKey) -> {
            for (var player : targets) {
                if (useWorld) {
                    if (add) {
                        BlockReachAPI.addPositionInWorld(player, pos, worldKey);
                    }
                    else {
                        BlockReachAPI.removePositionFromWorld(player, pos, worldKey);
                    }
                }
                else {
                    if (add) {
                        BlockReachAPI.addPosition(player, pos);
                    }
                    else {
                        BlockReachAPI.removePosition(player, pos);
                    }
                }
                BlockReachAPI.syncPosition(player, pos);
            }
            return targets.size();
        });
    }

    private static int add(CommandContext<ServerCommandSource> context, boolean useWorld) throws CommandSyntaxException {
        return modify(context, useWorld, true);
    }

    private static int remove(CommandContext<ServerCommandSource> context, boolean useWorld) throws CommandSyntaxException {
        return modify(context, useWorld, false);
    }

    private static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        for (var player : targets) {
            var positions = BlockReachAPI.getPositions(player);
            if (positions != null) {
                positions.clear();
            }
            BlockReachAPI.sync(player);
        }
        return targets.size();
    }

    private static int open(CommandContext<ServerCommandSource> context, boolean useWorld) throws CommandSyntaxException {
        return run(context, useWorld, (targets, pos, world, worldKey) -> {
            World targetWorld = useWorld ? world : context.getSource().getWorld();
            if (targetWorld.getBlockEntity(pos) instanceof NamedScreenHandlerFactory factory) {
                for (var target : targets) {
                    target.openHandledScreen(factory);
                }
            }
            return targets.size();
        });
    }

    private static RequiredArgumentBuilder<ServerCommandSource, PosArgument> subcommand(SubCommand command) {
        return argument("pos", BlockPosArgumentType.blockPos())
                .executes(context -> command.run(context, false))
                .then(argument("world", DimensionArgumentType.dimension())
                        .executes(context -> command.run(context, true)));
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("blockreachapi")
                        .then(argument("targets", EntityArgumentType.players())
                                .then(literal("add")
                                        .then(subcommand(BlockReachCommand::add)))
                                .then(literal("remove")
                                        .then(subcommand(BlockReachCommand::remove)))
                                .then(literal("clear")
                                        .executes(BlockReachCommand::clear))
                                .then(literal("open")
                                        .then(subcommand(BlockReachCommand::open))))
                        .requires(source -> source.hasPermissionLevel(2)))
        );
    }
}
