package com.awakenedredstone.optionsreload.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow
    protected abstract void debugLog(String key, Object... args);

    @Unique
    private boolean optionsReload$shouldCloseScreen() {
        Screen screen = client.currentScreen;
        return screen instanceof GameOptionsScreen || screen instanceof OptionsScreen;
    }

    @Unique
    private void optionsReload$closeUntilSafe() {
        Screen screen = client.currentScreen;
        int i = 5;
        while (i-- > 0 && screen != null) {
            if (screen instanceof GameOptionsScreen scr) {
                screen = ((GameOptionsScreenAccessor) scr).getParent();
                continue;
            }
            if (screen instanceof OptionsScreen scr) {
                screen = ((OptionsScreenAccessor) scr).getParent();
                continue;
            }
            break;
        }
        client.setScreen(screen);
    }

    @Unique
    private void optionsReload$reloadOptions() {
        client.options.load();
        boolean closeScreen = optionsReload$shouldCloseScreen();
        if (closeScreen) optionsReload$closeUntilSafe();
        debugLog("debug.reload_options.message");
        if (closeScreen) debugLog("debug.reload_options.description");
        if (client.world == null) {
            SystemToast.add(client.getToastManager(), SystemToast.Type.NARRATOR_TOGGLE, Text.translatable("debug.reload_options.message"),
                closeScreen ? Text.translatable("debug.reload_options.description") : Text.empty());
        }
    }

    @Inject(method = "processF3", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V",
            ordinal = 6, shift = At.Shift.AFTER))
    private void onProcessF3$addHelp(int key, CallbackInfoReturnable<Boolean> cir) {
        client.inGameHud.getChatHud().addMessage(Text.translatable("debug.reload_options.help"));
    }

    @Inject(method = "processF3", at = @At("RETURN"), cancellable = true)
    private void onProcessF3(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_O) {
            optionsReload$reloadOptions();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onKey", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"),
            cancellable = true)
    private void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3) && key == GLFW.GLFW_KEY_O) {
            if (action != 0) {
                optionsReload$reloadOptions();
            }
            ci.cancel();
        }
    }

    @Inject(method = "onChar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V",
            ordinal = 0), cancellable = true)
    private void onOnChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_F3) && InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_O)) {
            ci.cancel();
        }
    }
}