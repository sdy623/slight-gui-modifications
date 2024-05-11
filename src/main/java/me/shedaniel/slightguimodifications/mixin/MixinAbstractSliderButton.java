package me.shedaniel.slightguimodifications.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.slightguimodifications.SlightGuiModifications;
import me.shedaniel.slightguimodifications.config.SlightGuiModificationsConfig;
import me.shedaniel.slightguimodifications.utils.FakeSpriteRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSliderButton.class)
public abstract class MixinAbstractSliderButton extends AbstractWidget {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    public MixinAbstractSliderButton(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }
    
    @Shadow
    protected abstract void setValue(double mouseX);
    
    @Shadow protected double value;
    
    @Inject(method = "setValueFromMouse", at = @At("HEAD"), cancellable = true)
    private void setValueFromMouse(double mouseX, CallbackInfo ci) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            this.setValue((mouseX - (double) (this.getX() + grabberWidth / 2)) / (double) (this.width - grabberWidth));
            ci.cancel();
        }
    }
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void preKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            this.width += 8;
            this.width -= grabberWidth;
        }
    }
    
    @Inject(method = "keyPressed", at = @At("RETURN"), cancellable = true)
    private void postKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            this.width += grabberWidth;
            this.width -= 8;
        }
    }
    
    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void renderBg(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            if (config.sliderModifications.customBackgroundTexture) {
                ResourceLocation location = new ResourceLocation("slightguimodifications:textures/gui/slider" + (this.isHoveredOrFocused() ? "_hovered.png" : ".png"));
                RenderSystem.setShaderTexture(0, location);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int grabberX = this.getX() + (int) (this.value * (double) (this.width - grabberWidth));
                graphics.innerBlit(location, grabberX, grabberX + grabberWidth, getY(), getY() + 20, 0, 0F, 1F, 0F, 1F);
            } else if (grabberWidth != 8) {
                RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int i = (this.isHoveredOrFocused() ? 2 : 1) * 20;
                graphics.blit(WIDGETS_LOCATION, this.getX() + (int) (this.value * (double) (this.width - grabberWidth)), this.getY(), 0, 46 + i, grabberWidth / 2, 20);
                graphics.blit(WIDGETS_LOCATION, this.getX() + (int) (this.value * (double) (this.width - grabberWidth)) + (grabberWidth / 2), this.getY(), 200 - (grabberWidth / 2), 46 + i, grabberWidth / 2, 20);
            }
        }
    }
    
    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void renderBg(GuiGraphics graphics, ResourceLocation los, int x, int y, int w, int h) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            if (config.sliderModifications.customBackgroundTexture) {
                return;
            } else if (grabberWidth != 8) {
                return;
            }
        }
        graphics.blitSprite(los, x, y, w, h);
    }
}
