package com.example.totemshop.client;

import com.example.totemshop.config.TotemShopConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

public class TotemShopClient implements ClientModInitializer {
    private static boolean wasKeyPressed = false;

    @Override
    public void onInitializeClient() {
        TotemShopConfig.load();

        // Register client commands: /totemshop (aliases /ts and /tm)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var root = ClientCommands.literal("totemshop")
                .executes(context -> {
                    openGui(context.getSource().getClient());
                    return 1;
                })
                .then(ClientCommands.literal("gui")
                    .executes(context -> {
                        openGui(context.getSource().getClient());
                        return 1;
                    }))
                .then(ClientCommands.literal("config")
                    .executes(context -> {
                        openGui(context.getSource().getClient());
                        return 1;
                    }))
                .then(ClientCommands.literal("status")
                    .executes(context -> {
                        printStatus(context.getSource().getClient());
                        return 1;
                    }))
                .then(ClientCommands.literal("toggle")
                    .executes(context -> {
                        TotemShopConfig cfg = TotemShopConfig.get();
                        cfg.enabled = !cfg.enabled;
                        cfg.save();
                        context.getSource().sendFeedback(Component.literal("§6[TotemShop] Enabled: §f" + cfg.enabled));
                        return 1;
                    }))
                .then(ClientCommands.literal("autoclose")
                    .executes(context -> {
                        TotemShopConfig cfg = TotemShopConfig.get();
                        cfg.autoClose = !cfg.autoClose;
                        cfg.save();
                        context.getSource().sendFeedback(Component.literal("§6[TotemShop] Auto-close: §f" + cfg.autoClose));
                        return 1;
                    }))
                .then(ClientCommands.literal("delay")
                    .then(ClientCommands.argument("ticks", IntegerArgumentType.integer(0, 20))
                        .executes(context -> {
                            int ticks = IntegerArgumentType.getInteger(context, "ticks");
                            TotemShopConfig cfg = TotemShopConfig.get();
                            cfg.delayTicks = ticks;
                            cfg.save();
                            context.getSource().sendFeedback(Component.literal("§a[TotemShop] Click delay set to: §e" + ticks + " ticks (" + (ticks * 50) + "ms)"));
                            return 1;
                        })))
                .then(ClientCommands.literal("key")
                    .executes(context -> {
                        openGui(context.getSource().getClient());
                        return 1;
                    })
                    .then(ClientCommands.argument("combo", StringArgumentType.greedyString())
                        .executes(context -> {
                            String raw = StringArgumentType.getString(context, "combo");
                            String formatted = KeyCombo.format(raw);
                            TotemShopConfig cfg = TotemShopConfig.get();
                            cfg.keyCombo = formatted;
                            cfg.save();
                            context.getSource().sendFeedback(Component.literal("§a[TotemShop] Keybind set to: §e" + formatted));
                            return 1;
                        })));

            dispatcher.register(root);

            // Aliases: /ts and /tm
            dispatcher.register(ClientCommands.literal("ts")
                .redirect(dispatcher.getRoot().getChild("totemshop")));
            dispatcher.register(ClientCommands.literal("tm")
                .redirect(dispatcher.getRoot().getChild("totemshop")));
        });

        // Register client tick handler for keybind detection & auto-buyer progression
        ClientTickEvents.END_CLIENT_TICK.register(TotemShopClient::onTick);
    }

    private static void openGui(Minecraft client) {
        client.execute(() -> {
            if (client.gui != null) {
                var current = client.gui.screen();
                var parent = (current instanceof ChatScreen) ? null : current;
                client.gui.setScreen(new TotemShopConfigScreen(parent));
            }
        });
    }

    private static void onTick(Minecraft client) {
        if (client.player == null) return;

        TotemShopConfig config = TotemShopConfig.get();

        // Only check key press if we are not in an active screen (e.g. Chat or GUI)
        if (client.gui != null && client.gui.screen() == null) {
            boolean isPressed = config.enabled && KeyCombo.isComboPressed(config.keyCombo);
            if (isPressed && !wasKeyPressed) {
                TotemAutoBuyer.trigger(client);
            }
            wasKeyPressed = isPressed;
        } else {
            wasKeyPressed = false;
        }

        // Advance auto buyer state machine
        TotemAutoBuyer.onTick(client);
    }

    private static void printStatus(Minecraft client) {
        if (client.player == null) return;
        TotemShopConfig cfg = TotemShopConfig.get();
        client.player.sendSystemMessage(Component.literal("§8§m----------------------------------------"));
        client.player.sendSystemMessage(Component.literal("§6§lTotem Shop §7- Settings"));
        client.player.sendSystemMessage(Component.literal("§7Status: " + (cfg.enabled ? "§aEnabled" : "§cDisabled")));
        client.player.sendSystemMessage(Component.literal("§7Keybind: §e" + cfg.keyCombo));
        client.player.sendSystemMessage(Component.literal("§7Delay: §e" + cfg.delayTicks + " ticks (" + (cfg.delayTicks * 50) + "ms)"));
        client.player.sendSystemMessage(Component.literal("§7Auto Close: §e" + cfg.autoClose));
        client.player.sendSystemMessage(Component.literal("§7Shop Command: §e/" + cfg.shopCommand));
        client.player.sendSystemMessage(Component.literal("§7Commands:"));
        client.player.sendSystemMessage(Component.literal("  §e/ts §7- Open GUI Settings"));
        client.player.sendSystemMessage(Component.literal("  §e/ts key <combo> §7- e.g. §b/ts key CTRL+O"));
        client.player.sendSystemMessage(Component.literal("  §e/ts delay <ticks> §7- e.g. §b/ts delay 1"));
        client.player.sendSystemMessage(Component.literal("  §e/ts toggle §7- Enable/disable"));
        client.player.sendSystemMessage(Component.literal("§8§m----------------------------------------"));
    }
}

