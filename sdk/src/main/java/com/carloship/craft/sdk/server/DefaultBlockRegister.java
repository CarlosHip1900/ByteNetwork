package com.carloship.craft.sdk.server;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class DefaultBlockRegister {

    private static void registerDummyHandlers(String... namespaces) {
        for (String namespace : namespaces) {
            MinecraftServer.getBlockManager().registerHandler(namespace, () -> new BlockHandler() {
                @Override
                public @NotNull Key getKey() {
                    return Key.key(namespace);
                }
            });
        }
    }

    public static void initRegisters(){
        registerDummyHandlers(
                "minecraft:beacon",
                "minecraft:skull",
                "minecraft:sign",
                "minecraft:ender_chest",
                "minecraft:chest",
                "minecraft:hopper",
                "minecraft:dispenser",
                "minecraft:bed",
                "minecraft:furnace",
                "minecraft:mob_spawner"
        );
    }

}
