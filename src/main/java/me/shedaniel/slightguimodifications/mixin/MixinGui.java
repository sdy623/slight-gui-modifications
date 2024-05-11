package me.shedaniel.slightguimodifications.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import me.shedaniel.slightguimodifications.SlightGuiModifications;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(Gui.class)
public class MixinGui {
    @Shadow @Final private Minecraft minecraft;
    private int screenWidth;
    public double offsetBeneficial;
    public double offsetTargetBeneficial;
    
    public double offsetNonBeneficial;
    public double offsetTargetNonBeneficial;

    @Inject(method = "renderEffects", at = @At("HEAD"))
    private void preRenderStatusEffectOverlay(CallbackInfo ci) {
        double widthBeneficial = 0;
        double widthNonBeneficial = 0;
        for (MobEffectInstance effect : minecraft.player.getActiveEffects()) {
            if (!effect.showIcon()) continue;
            if (effect.getEffect().value().isBeneficial()) {
                widthBeneficial += 25;
            } else {
                widthNonBeneficial += 25;
            }
        }
        offsetTargetBeneficial = widthBeneficial;
        offsetTargetNonBeneficial = widthNonBeneficial;
    }
    
    /**
     * fuck mod compatibility
     *
     * @author shedaniel
     */
    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    protected void renderStatusEffectOverlay(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        this.screenWidth = this.minecraft.getWindow().getScreenWidth();
        if (!SlightGuiModifications.getGuiConfig().fluidStatusEffects) return;
        ci.cancel();
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty()) {
            RenderSystem.enableBlend();
            
            double beneficialOffset = 0;
            double nonBeneficialOffset = 0;
            MobEffectTextureManager statusEffectSpriteManager = this.minecraft.getMobEffectTextures();
            List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());
            RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);
            
            for (MobEffectInstance statusEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
                MobEffect statusEffect = statusEffectInstance.getEffect().value();
                if (statusEffectInstance.showIcon()) {
                    double[] x = {this.screenWidth};
                    int[] y = {1};
                    if (this.minecraft.isDemo()) {
                        y[0] += 15;
                    }
                    
                    if (statusEffect.isBeneficial()) {
                        beneficialOffset += 25 * Math.min(1.0, EasingMethod.EasingMethodImpl.EXPO.apply(statusEffectInstance.getDuration() / 10.0));
                        x[0] -= beneficialOffset;
                    } else {
                        nonBeneficialOffset += 25 * Math.min(1.0, EasingMethod.EasingMethodImpl.EXPO.apply(statusEffectInstance.getDuration() / 10.0));
                        x[0] -= nonBeneficialOffset;
                        y[0] += 26;
                    }
                    
                    graphics.pose().pushPose();
                    float alphaOffset = (float) Math.min(1.0, 1 - EasingMethod.EasingMethodImpl.LINEAR.apply(1 - Mth.clamp(statusEffectInstance.getDuration() / 10.0, 0, 1)));
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alphaOffset);
                    graphics.pose().translate(x[0] - this.screenWidth, 0, 0);
                    float[] alpha = {alphaOffset};
                    if (statusEffectInstance.isAmbient()) {
                        graphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, this.screenWidth, y[0], 165, 166, 24, 24);
                    } else {
                        graphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, this.screenWidth, y[0], 141, 166, 24, 24);
                        if (statusEffectInstance.getDuration() <= 200) {
                            int m = 10 - statusEffectInstance.getDuration() / 20;
                            alpha[0] = alphaOffset * Mth.clamp((float) statusEffectInstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float) statusEffectInstance.getDuration() * 3.1415927F / 5.0F) * Mth.clamp((float) m / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }
                    graphics.pose().popPose();
                    
                    TextureAtlasSprite sprite = statusEffectSpriteManager.get((Holder<MobEffect>) statusEffect);
                    list.add(() -> {
                        if (alpha[0] <= 0.01) return;
                        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
                        graphics.pose().pushPose();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha[0]);
                        graphics.pose().translate(x[0] - this.screenWidth, 0, 0);
                        graphics.blit(this.screenWidth + 3, y[0] + 3, 0, 18, 18, sprite);
                        graphics.pose().popPose();
                    });
                }
            }
            
            list.forEach(Runnable::run);
            
            RenderSystem.clear(256, Minecraft.ON_OSX);
            Window window = minecraft.getWindow();
            Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float) (window.getWidth() / window.getGuiScale()), 0.0F, (float) (window.getHeight() / window.getGuiScale()), 1000.0F, 3000.0F);
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
            Matrix4fStack poseStack = RenderSystem.getModelViewStack();
            poseStack.identity();
            poseStack.translate(0.0F, 0.0F, -2000.0F);
            RenderSystem.applyModelViewMatrix ();
            Lighting.setupFor3DItems();
        }
    }
}
