package me.shedaniel.slightguimodifications.mixin;

import com.google.common.collect.Lists;
import dev.architectury.hooks.client.screen.ScreenHooks;
import me.shedaniel.slightguimodifications.SlightGuiModifications;
import me.shedaniel.slightguimodifications.config.Cts;
import me.shedaniel.slightguimodifications.gui.cts.elements.WidgetElement;
import me.shedaniel.slightguimodifications.listener.AnimationListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Mixin(PanoramaRenderer.class)
public class MixinPanoramaRenderer {
    @Shadow @Final private Minecraft minecraft;
    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"))
    private void thing(GuiGraphics graphics, ResourceLocation los, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int texWidth, int texHeight) {
        Minecraft mc = minecraft.getInstance(); // 获取 Minecraft 实例
        Screen currentScreen = mc.screen;       // 获取当前屏幕
        if (currentScreen instanceof TitleScreen) {
            TitleScreen titleScreen = (TitleScreen) currentScreen;
        if (!SlightGuiModifications.getCtsConfig().enabled || SlightGuiModifications.getCtsConfig().renderGradientShade) {
            int tmp = ((AnimationListener) titleScreen).slightguimodifications_getAnimationState();
            ((AnimationListener) titleScreen).slightguimodifications_setAnimationState(0);
            graphics.blit(los, x, y, width, height, u, v, uWidth, vHeight, texWidth, texHeight);
            ((AnimationListener) titleScreen).slightguimodifications_setAnimationState(tmp);
            }
        }
    }
}
