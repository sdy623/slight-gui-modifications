package me.shedaniel.slightguimodifications.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.math.MathHelper;

public class ConfigButtonWidget extends ButtonWidget {
    public ConfigButtonWidget(int x, int y, int width, int height, String message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }
    
    @Override
    public void renderButton(int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        minecraftClient.getTextureManager().bindTexture(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        this.blit(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(minecraftClient, mouseX, mouseY);
        int j = this.active ? 16777215 : 10526880;
        RenderSystem.pushMatrix();
        float scale = 1 / 1.3f;
        RenderSystem.scalef(scale, scale, 0);
        textRenderer.drawWithShadow(this.getMessage(), (this.x + this.width / 2) * (1 / scale) - textRenderer.getStringWidth(getMessage()) / 2, (this.y + (this.height - 6) / 2) * (1 / scale), j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        RenderSystem.popMatrix();
    }
}
