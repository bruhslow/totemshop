package com.example.totemshop.client;

import com.example.totemshop.config.TotemShopConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TotemShopConfigScreen extends Screen {
    private final Screen parent;
    private Button keybindBtn;
    private Button delayBtn;
    private Button autoCloseBtn;
    private Button soundAlertBtn;
    private boolean isRecording = false;
    private final Set<Integer> recordedKeys = new LinkedHashSet<>();

    public TotemShopConfigScreen(Screen parent) {
        super(Component.literal("Totem Shop Settings"));
        this.parent = parent;
    }

    public static Screen create(Screen parent) {
        return new TotemShopConfigScreen(parent);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 60;

        // Keybind recorder button
        TotemShopConfig config = TotemShopConfig.get();
        this.keybindBtn = Button.builder(
                getKeybindButtonText(),
                btn -> startRecording()
        ).bounds(centerX - 120, startY, 240, 24).build();
        this.addRenderableWidget(this.keybindBtn);

        // Click Delay toggle button (0, 1, 2, 3, 5 ticks)
        this.delayBtn = Button.builder(
                getDelayButtonText(),
                btn -> cycleDelay()
        ).bounds(centerX - 120, startY + 32, 240, 20).build();
        this.addRenderableWidget(this.delayBtn);

        // Auto Close Shop toggle button
        this.autoCloseBtn = Button.builder(
                getAutoCloseButtonText(),
                btn -> toggleAutoClose()
        ).bounds(centerX - 120, startY + 58, 240, 20).build();
        this.addRenderableWidget(this.autoCloseBtn);

        // Sound alert toggle button
        this.soundAlertBtn = Button.builder(
                getSoundAlertButtonText(),
                btn -> toggleSoundAlert()
        ).bounds(centerX - 120, startY + 84, 240, 20).build();
        this.addRenderableWidget(this.soundAlertBtn);

        // Done button
        this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                btn -> onClose()
        ).bounds(centerX - 80, this.height - 35, 160, 20).build());
    }

    private void startRecording() {
        this.isRecording = true;
        this.recordedKeys.clear();
        this.keybindBtn.setMessage(Component.literal("§e> Press keys (e.g. CTRL + O)... <"));
    }

    private void finishRecording() {
        if (!this.isRecording) return;
        this.isRecording = false;

        TotemShopConfig config = TotemShopConfig.get();
        if (this.recordedKeys.isEmpty()) {
            config.keyCombo = "None";
        } else {
            List<String> keyNames = new ArrayList<>();
            for (int k : this.recordedKeys) {
                String name = KeyCombo.getKeyName(k);
                if (!keyNames.contains(name)) {
                    keyNames.add(name);
                }
            }
            config.keyCombo = KeyCombo.format(String.join(" + ", keyNames));
        }
        config.save();

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        }

        this.keybindBtn.setMessage(getKeybindButtonText());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.isRecording) {
            return super.keyPressed(event);
        }

        int key = event.key();

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.isRecording = false;
            this.recordedKeys.clear();
            this.keybindBtn.setMessage(getKeybindButtonText());
            return true;
        }

        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            finishRecording();
            return true;
        }

        this.recordedKeys.add(key);

        boolean isModifier = (key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL
                || key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT
                || key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT);

        if (!isModifier) {
            finishRecording();
        } else {
            List<String> current = new ArrayList<>();
            for (int k : this.recordedKeys) {
                current.add(KeyCombo.getKeyName(k));
            }
            this.keybindBtn.setMessage(Component.literal("§e> " + String.join(" + ", current) + " + [Press Key]... <"));
        }

        return true;
    }

    private Component getKeybindButtonText() {
        return Component.literal("§7Keybind: §b[ " + TotemShopConfig.get().keyCombo + " ] §8(Click to change)");
    }

    private Component getDelayButtonText() {
        int ticks = TotemShopConfig.get().delayTicks;
        String desc = (ticks == 0) ? "0 ticks (Instant)" : ticks + " ticks (" + (ticks * 50) + "ms)";
        return Component.literal("§7Click Delay: §e" + desc);
    }

    private Component getAutoCloseButtonText() {
        boolean autoClose = TotemShopConfig.get().autoClose;
        return Component.literal("§7Auto-Close Shop: " + (autoClose ? "§aON" : "§cOFF"));
    }

    private Component getSoundAlertButtonText() {
        boolean sound = TotemShopConfig.get().soundAlert;
        return Component.literal("§7Sound Alert: " + (sound ? "§aON" : "§cOFF"));
    }

    private void cycleDelay() {
        TotemShopConfig config = TotemShopConfig.get();
        int[] delays = {0, 1, 2, 3, 5};
        int next = 1;
        for (int i = 0; i < delays.length; i++) {
            if (delays[i] == config.delayTicks) {
                next = delays[(i + 1) % delays.length];
                break;
            }
        }
        config.delayTicks = next;
        config.save();
        this.delayBtn.setMessage(getDelayButtonText());
    }

    private void toggleAutoClose() {
        TotemShopConfig config = TotemShopConfig.get();
        config.autoClose = !config.autoClose;
        config.save();
        this.autoCloseBtn.setMessage(getAutoCloseButtonText());
    }

    private void toggleSoundAlert() {
        TotemShopConfig config = TotemShopConfig.get();
        config.soundAlert = !config.soundAlert;
        config.save();
        this.soundAlertBtn.setMessage(getSoundAlertButtonText());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        extractor.centeredText(this.font, Component.literal("§6§lTotem Shop §f- Settings"), this.width / 2, 20, 0xFFFFFF);
        extractor.centeredText(this.font, Component.literal("§7Click the button below to record your keybind (e.g. §eCTRL + O§7)"), this.width / 2, 38, 0xAAAAAA);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}

