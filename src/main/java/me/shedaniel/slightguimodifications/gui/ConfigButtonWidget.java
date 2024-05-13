package me.shedaniel.slightguimodifications.gui;

import me.shedaniel.slightguimodifications.utils.FakeSpriteRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public class ConfigButtonWidget extends Button {
    public ConfigButtonWidget(int i, int j, int k, int l, Component component, OnPress onPress) {
        super(i, j, k, l, component, onPress, Supplier::get);
    }
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Font textRenderer = minecraftClient.font;
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        //graphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
        graphics.blitSprite(WIDGETS_LOCATION, this.getX(), this.getY(), 0, this.getWidth(), this.getHeight());
        //FakeSpriteRenderer.blitNineSlicedSprite(graphics, WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int j = this.active ? 16777215 : 10526880;
        graphics.pose().pushPose();
        float scale = 1 / 1.3f;
        graphics.pose().scale(scale, scale, 0);
        graphics.drawString(textRenderer, this.getMessage(), (int) ((this.getX() + this.width / 2) * (1 / scale) - textRenderer.width(getMessage()) / 2), (int) ((this.getY() + (this.height - 6) / 2) * (1 / scale)), j | Mth.ceil(this.alpha * 255.0F) << 24);
        graphics.pose().popPose();
    }
    
    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }
        
        return 46 + i * 20;
    }
}
