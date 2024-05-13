package me.shedaniel.slightguimodifications.mixin;

import me.shedaniel.slightguimodifications.SlightGuiModifications;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "keyPress", at = @At(value = "INVOKE",
                                          target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"))
    private void preSaveScreenshot(long window, int key, int scancode, int i, int j, CallbackInfo ci) {
        SlightGuiModifications.startPrettyScreenshot(null);
        if (SlightGuiModifications.getGuiConfig().enhancedScreenshots.satisfyingScreenshots) {
            SlightGuiModifications.prettyScreenshots = true;
            if(SlightGuiModifications.getGuiConfig().enhancedScreenshots.withcorner == true){
                SlightGuiModifications.withRoundCorner = true;
                SlightGuiModifications.roundedcornerRadius = SlightGuiModifications.getGuiConfig().enhancedScreenshots.cornerRadius;
                SlightGuiModifications.roundedcornerBorderWidth = SlightGuiModifications.getGuiConfig().enhancedScreenshots.borderWidth;
            }
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_IN, 10f, 3f));
        }
    }
}
