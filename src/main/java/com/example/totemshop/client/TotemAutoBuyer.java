package com.example.totemshop.client;

import com.example.totemshop.config.TotemShopConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

public class TotemAutoBuyer {
    public enum State {
        IDLE,
        WAIT_FOR_SHOP_GUI,
        WAIT_FOR_GEAR_GUI,
        WAIT_FOR_CONFIRM_GUI,
        FINISHING
    }

    private static State state = State.IDLE;
    private static int ticksWaited = 0;
    private static int timeoutTicks = 0;

    public static State getState() {
        return state;
    }

    public static void trigger(Minecraft client) {
        if (!TotemShopConfig.get().enabled) {
            return;
        }

        if (client.player == null || client.getConnection() == null) {
            return;
        }

        if (state != State.IDLE) {
            return;
        }

        state = State.WAIT_FOR_SHOP_GUI;
        ticksWaited = 0;
        timeoutTicks = 100; // 5 seconds max timeout

        String cmd = TotemShopConfig.get().shopCommand.trim();
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        client.getConnection().sendCommand(cmd);
    }

    public static void onTick(Minecraft client) {
        if (state == State.IDLE) {
            return;
        }

        timeoutTicks--;
        if (timeoutTicks <= 0) {
            state = State.IDLE;
            ticksWaited = 0;
            return;
        }

        if (client.gui == null) return;
        Screen currentScreen = client.gui.screen();

        // Finishing phase: handle auto-closing cleanly and reliably
        if (state == State.FINISHING) {
            ticksWaited++;
            TotemShopConfig config = TotemShopConfig.get();

            // Optional sound alert at tick 2
            if (ticksWaited == 2) {
                if (config.soundAlert && client.player != null) {
                    client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                }
            }

            // Reliably close screen over ticks 2-8 so server packet delay doesn't keep it open
            if (config.autoClose && ticksWaited >= 2) {
                forceCloseCurrentScreen(client);
            }

            if (ticksWaited >= 8) {
                if (config.autoClose) {
                    forceCloseCurrentScreen(client);
                }
                state = State.IDLE;
                ticksWaited = 0;
            }
            return;
        }

        if (!(currentScreen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        String title = currentScreen.getTitle().getString().toLowerCase(Locale.ROOT);
        AbstractContainerMenu menu = containerScreen.getMenu();
        int containerId = menu.containerId;
        int targetDelay = Math.max(0, TotemShopConfig.get().delayTicks);

        switch (state) {
            case WAIT_FOR_SHOP_GUI -> {
                if (title.contains("shop") && !title.contains("gear") && !title.contains("confirm")) {
                    if (!hasContainerLoadedItems(menu)) return;

                    ticksWaited++;
                    if (ticksWaited <= targetDelay) return;

                    int targetSlot = findGearSlot(menu);
                    if (targetSlot != -1 && client.gameMode != null && client.player != null) {
                        client.gameMode.handleContainerInput(containerId, targetSlot, 0, ContainerInput.PICKUP, client.player);
                        state = State.WAIT_FOR_GEAR_GUI;
                        ticksWaited = 0;
                    }
                }
            }
            case WAIT_FOR_GEAR_GUI -> {
                if (title.contains("gear")) {
                    if (!hasContainerLoadedItems(menu)) return;

                    ticksWaited++;
                    if (ticksWaited <= targetDelay) return;

                    int targetSlot = findTotemSlot(menu);
                    if (targetSlot != -1 && client.gameMode != null && client.player != null) {
                        client.gameMode.handleContainerInput(containerId, targetSlot, 0, ContainerInput.PICKUP, client.player);
                        state = State.WAIT_FOR_CONFIRM_GUI;
                        ticksWaited = 0;
                    }
                }
            }
            case WAIT_FOR_CONFIRM_GUI -> {
                if (title.contains("confirm")) {
                    if (!hasContainerLoadedItems(menu)) return;

                    ticksWaited++;
                    if (ticksWaited <= targetDelay) return;

                    int targetSlot = findConfirmSlot(menu);
                    if (targetSlot != -1 && client.gameMode != null && client.player != null) {
                        client.gameMode.handleContainerInput(containerId, targetSlot, 0, ContainerInput.PICKUP, client.player);
                        state = State.FINISHING;
                        ticksWaited = 0;
                    }
                }
            }
            default -> {}
        }
    }

    public static void forceCloseCurrentScreen(Minecraft client) {
        if (client == null) return;
        Screen screen = (client.gui != null) ? client.gui.screen() : null;
        if (screen != null) {
            screen.onClose();
        }
        if (client.player != null) {
            client.player.closeContainer();
        }
        if (client.gui != null) {
            client.gui.setScreen(null);
        }
        if (client.mouseHandler != null && !client.mouseHandler.isMouseGrabbed()) {
            client.mouseHandler.grabMouse();
        }
    }

    private static boolean hasContainerLoadedItems(AbstractContainerMenu menu) {
        if (menu == null || menu.slots == null) return false;
        for (Slot slot : menu.slots) {
            if (slot.index >= 54) break;
            if (!slot.getItem().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static int findGearSlot(AbstractContainerMenu menu) {
        // Priority 1: Item name contains "gear"
        for (Slot slot : menu.slots) {
            if (slot.index >= 54) break;
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
            if (name.contains("gear")) {
                return slot.index;
            }
        }
        // Priority 2: Armor / Chestplate icon
        for (Slot slot : menu.slots) {
            if (slot.index >= 54) break;
            ItemStack stack = slot.getItem();
            if (stack.is(Items.NETHERITE_CHESTPLATE) || stack.is(Items.DIAMOND_CHESTPLATE) || stack.is(Items.IRON_CHESTPLATE)) {
                return slot.index;
            }
        }
        // Priority 3: Fallback slot 10 (seen in video)
        if (menu.slots.size() > 10) {
            return 10;
        }
        return -1;
    }

    private static int findTotemSlot(AbstractContainerMenu menu) {
        // Priority 1: Item is Totem of Undying or name contains "totem"
        for (Slot slot : menu.slots) {
            if (slot.index >= 54) break;
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            if (stack.is(Items.TOTEM_OF_UNDYING) || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("totem")) {
                return slot.index;
            }
        }
        // Priority 2: Fallback slot 12 (seen in video)
        if (menu.slots.size() > 12) {
            return 12;
        }
        return -1;
    }

    private static int findConfirmSlot(AbstractContainerMenu menu) {
        // Priority 1: Name contains "confirm"
        for (Slot slot : menu.slots) {
            if (slot.index >= 54) break;
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
            if (name.contains("confirm")) {
                return slot.index;
            }
        }
        // Priority 2: Green or Lime stained glass pane
        for (Slot slot : menu.slots) {
            if (slot.index >= 54) break;
            ItemStack stack = slot.getItem();
            if (stack.is(Items.STAINED_GLASS_PANE.lime()) 
                    || stack.is(Items.STAINED_GLASS_PANE.green())
                    || stack.getItem().getDescriptionId().contains("lime")
                    || stack.getItem().getDescriptionId().contains("green")) {
                return slot.index;
            }
        }
        // Priority 3: Fallback slot 11 (seen in video)
        if (menu.slots.size() > 11) {
            return 11;
        }
        return -1;
    }
}
