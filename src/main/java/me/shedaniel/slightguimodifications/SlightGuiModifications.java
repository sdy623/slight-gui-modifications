package me.shedaniel.slightguimodifications;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientScreenInputEvent;
import dev.architectury.hooks.client.screen.ScreenHooks;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.LazyResettable;
import me.shedaniel.math.Point;
import me.shedaniel.slightguimodifications.config.Cts;
import me.shedaniel.slightguimodifications.config.SlightGuiModificationsConfig;
import me.shedaniel.slightguimodifications.gui.MenuWidget;
import me.shedaniel.slightguimodifications.gui.TextMenuEntry;
import me.shedaniel.slightguimodifications.gui.cts.CtsRegistry;
import me.shedaniel.slightguimodifications.listener.AnimationListener;
import me.shedaniel.slightguimodifications.listener.MenuWidgetListener;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public class SlightGuiModifications implements ClientModInitializer {
    public static float backgroundTint = 0;
    public static final ResourceLocation TEXT_FIELD_TEXTURE = new ResourceLocation("textures/gui/text_field.png");
    public static float lastAlpha = -1;
    public static boolean prettyScreenshots = false;
    public static boolean withRoundCorner = false;
    public static int roundedcornerRadius = 120;
    public static int roundedcornerBorderWidth = 35;
    public static DynamicTexture prettyScreenshotTexture = null;
    public static DynamicTexture lastPrettyScreenshotTexture = null;
    public static ResourceLocation prettyScreenshotTextureId = null;
    public static ResourceLocation lastPrettyScreenshotTextureId = null;
    public static long prettyScreenshotTime = -1;
    public static long backgroundTime = -1;
    public static final Logger LOGGER = LogManager.getLogger("SlightGuiModifications");
    
    public static float[] getColorObj() {
        return RenderSystem.getShaderColor();
    }
    
    public static float getColorRed(float[] colorObj) {
        return colorObj[0];
    }
    
    public static float getColorGreen(float[] colorObj) {
        return colorObj[1];
    }
    
    public static float getColorBlue(float[] colorObj) {
        return colorObj[2];
    }
    
    public static float getColorAlpha(float[] colorObj) {
        return colorObj[3];
    }
    
    public static void setAlpha(float alpha) {
        if (lastAlpha >= 0) new IllegalStateException().printStackTrace();
        float[] colorObj = getColorObj();
        float colorRed = getColorRed(colorObj);
        float colorGreen = getColorGreen(colorObj);
        float colorBlue = getColorBlue(colorObj);
        float colorAlpha = getColorAlpha(colorObj);
        lastAlpha = colorAlpha == -1 ? 1 : Mth.clamp(colorAlpha, 0, 1);
        RenderSystem.setShaderColor(colorRed == -1 ? 1 : colorRed,
                colorGreen == -1 ? 1 : colorGreen,
                colorBlue == -1 ? 1 : colorBlue,
                lastAlpha * alpha);
    }
    
    public static void restoreAlpha() {
        if (lastAlpha < 0) return;
        float[] colorObj = getColorObj();
        float colorRed = getColorRed(colorObj);
        float colorGreen = getColorGreen(colorObj);
        float colorBlue = getColorBlue(colorObj);
        RenderSystem.setShaderColor(colorRed == -1 ? 1 : colorRed,
                colorGreen == -1 ? 1 : colorGreen,
                colorBlue == -1 ? 1 : colorBlue,
                lastAlpha);
        lastAlpha = -1;
    }
    
    public static float ease(float t) {
        return (float) (1f * (-Math.pow(2, -10 * t / 1f) + 1));
    }
    
    public static int reverseYAnimation(int y) {
        return y - applyYAnimation(y) + y;
    }
    
    public static int applyYAnimation(int y) {
        if (!RenderSystem.isOnRenderThread()) return y;
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AnimationListener) {
            float alpha = ((AnimationListener) screen).slightguimodifications_getEasedYOffset();
            if (alpha >= 0) return y + (int) ((1 - alpha) * screen.height / 2);
        }
        return y;
    }
    
    public static int applyMouseYAnimation(int y) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AnimationListener) {
            float alpha = ((AnimationListener) screen).slightguimodifications_getEasedMouseY();
            if (alpha >= 0) return y - (int) ((1 - alpha) * screen.height / 2);
        }
        return y;
    }
    
    public static double reverseYAnimation(double y) {return y - applyYAnimation(y) + y;}
    
    public static double applyYAnimation(double y) {
        if (!RenderSystem.isOnRenderThread()) return y;
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AnimationListener) {
            float alpha = ((AnimationListener) screen).slightguimodifications_getEasedYOffset();
            if (alpha >= 0) return y + (int) ((1 - alpha) * screen.height / 2);
        }
        return y;
    }
    
    public static int applyAlphaAnimation(int alpha) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AnimationListener) {
            float animatedAlpha = ((AnimationListener) screen).slightguimodifications_getAlpha();
            if (animatedAlpha >= 0) return (int) (animatedAlpha * alpha);
        }
        return alpha;
    }

    public static BufferedImage applyEffects(BufferedImage image, int cornerRadius, int shadowSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int shadowRgb = new Color(0, 0, 0, 154).getRGB(); // 半透明黑色阴影

        // 创建一个更大的图像以容纳阴影
        BufferedImage outputImage = new BufferedImage(width + shadowSize * 2, height + shadowSize * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = outputImage.createGraphics();

        // 启用抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制阴影
        g2.setColor(new Color(0, 0, 0, 160)); // 半透明黑色
        g2.fill(new RoundRectangle2D.Float(shadowSize, shadowSize, width, height, cornerRadius, cornerRadius));

        // 绘制圆角矩形图像
        g2.setComposite(AlphaComposite.SrcOver); // 确保图像在阴影上方
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));

        // 绘制实际图像
        g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return outputImage;
    }

    public static BufferedImage nativeImageToBufferedImage(NativeImage nativeImage) {
        BufferedImage bufferedImage = new BufferedImage(nativeImage.getWidth(), nativeImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < nativeImage.getHeight(); y++) {
            for (int x = 0; x < nativeImage.getWidth(); x++) {
                int argb = nativeImage.getPixelRGBA(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                bufferedImage.setRGB(x, y, rgba);
            }
        }

        return bufferedImage;
    }

    public static NativeImage bufferedImageToNativeImage(BufferedImage bufferedImage) {
        NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), true);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int rgba = bufferedImage.getRGB(x, y);
                nativeImage.setPixelRGBA(x, y, rgba);
            }
        }
        return nativeImage;
    }

    public static NativeImage applyRoundedCorners(NativeImage image, int cornerRadius, int borderWidth) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage newImage = new NativeImage(width, height, true);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isInCorner(x, y, width, height, cornerRadius) && !isNearEdge(x, y, width, height, borderWidth, cornerRadius)) {
                    newImage.setPixelRGBA(x, y, 0x00000000);  // Make corners transparent
                } else if (isNearEdge(x, y, width, height, borderWidth, cornerRadius)) {
                    newImage.setPixelRGBA(x, y, 0xFFFFFFFF);  // Draw white border
                } else {
                    int originalPixel = image.getPixelRGBA(x, y);
                    newImage.setPixelRGBA(x, y, originalPixel);  // Copy original pixel
                }
            }
        }

        return newImage;
    }

    private static boolean isInCorner(int x, int y, int width, int height, int radius) {
        int centerX = width - radius;
        int centerY = height - radius;
        return (x < radius && y < radius && Math.sqrt(Math.pow(radius - x, 2) + Math.pow(radius - y, 2)) > radius) ||
                (x >= centerX && y < radius && Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(radius - y, 2)) > radius) ||
                (x < radius && y >= centerY && Math.sqrt(Math.pow(radius - x, 2) + Math.pow(y - centerY, 2)) > radius) ||
                (x >= centerX && y >= centerY && Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) > radius);
    }

    private static boolean isNearEdge(int x, int y, int width, int height, int borderWidth, int cornerRadius) {
        // Check if the point is in the border width range, allowing the border to extend into the corners
        return (x < borderWidth || x >= width - borderWidth) || (y < borderWidth || y >= height - borderWidth);
    }


    public static void startPrettyScreenshot(NativeImage cloneImage) {
        if (prettyScreenshotTexture != null) {
            lastPrettyScreenshotTexture = prettyScreenshotTexture;
            lastPrettyScreenshotTextureId = prettyScreenshotTextureId;
        }
        prettyScreenshotTexture = null;
        prettyScreenshotTextureId = null;
        prettyScreenshotTime = -1;
        if (cloneImage != null) {
            if (SlightGuiModifications.getGuiConfig().enhancedScreenshots.withcorner){
                /*
                BufferedImage bufferedImage = nativeImageToBufferedImage(cloneImage);
                BufferedImage roundedImage = applyEffects(bufferedImage, roundedcornerRadius, roundedcornerBorderWidth);
                NativeImage finalImage = bufferedImageToNativeImage(roundedImage);
                prettyScreenshotTexture = new DynamicTexture(finalImage);
                */
                NativeImage roundedImage = applyRoundedCorners(cloneImage, roundedcornerRadius, roundedcornerBorderWidth);
                prettyScreenshotTexture = new DynamicTexture(roundedImage);
            }
            else {
                prettyScreenshotTexture = new DynamicTexture(cloneImage);
            }
            prettyScreenshotTextureId = Minecraft.getInstance().getTextureManager().register("slight-gui-modifications-pretty-screenshots", prettyScreenshotTexture);
        }
    }
    
    @Override
    public void onInitializeClient() {
        getGuiConfig();
        AutoConfig.getGuiRegistry(SlightGuiModificationsConfig.class).registerAnnotationProvider(
                (i13n, field, config, defaults, guiProvider) -> Collections.singletonList(
                        ConfigEntryBuilder.create().startIntSlider(Component.translatable(i13n), (int) (Math.max(1, getUnsafely(field, config, 0.0)) * 100), 100,
                                        (Minecraft.getInstance().getWindow().calculateScale(0, false) + 4) * 100)
                                .setDefaultValue(0)
                                .setTextGetter(integer -> {
                                    if (integer <= 100)
                                        return Component.translatable(i13n + ".text.disabled");
                                    return Component.translatable(i13n + ".text", integer / 100.0);
                                })
                                .setSaveConsumer(integer -> setUnsafely(field, config, integer / 100.0))
                                .build()
                ),
                SlightGuiModificationsConfig.Gui.ScaleSlider.class
        );
        ClientScreenInputEvent.MOUSE_CLICKED_PRE.register((client, screen, mouseX, mouseY, mouseButton) -> {
            if (((MenuWidgetListener) screen).getMenu() != null) {
                if (!((MenuWidgetListener) screen).getMenu().mouseClicked(mouseX, mouseY, mouseButton)) {
                    ((MenuWidgetListener) screen).removeMenu();
                }
                return EventResult.interruptTrue();
            }
            if (SlightGuiModifications.getGuiConfig().rightClickActions && mouseButton == 1) {
                // Pause Menu
                if (screen instanceof PauseScreen || screen instanceof TitleScreen) {
                    Optional<Renderable> optionsButton = ScreenHooks.getRenderables(screen).stream()
                            .filter(button -> button instanceof AbstractWidget widget && widget.getMessage().getString().equals(I18n.get("menu.options"))).findFirst();
                    if (optionsButton.isPresent() && ((AbstractWidget) optionsButton.get()).isMouseOver(mouseX, mouseY)) {
                        ((MenuWidgetListener) screen).applyMenu(new MenuWidget(new Point(mouseX + 2, mouseY + 2),
                                ImmutableList.of(
                                        new TextMenuEntry(I18n.get("options.video").replace("...", ""), () -> {
                                            ((MenuWidgetListener) screen).removeMenu();
                                            client.setScreen(new VideoSettingsScreen(screen, client.options));
                                        }),
                                        new TextMenuEntry(I18n.get("options.controls").replace("...", ""), () -> {
                                            ((MenuWidgetListener) screen).removeMenu();
                                            client.setScreen(new ControlsScreen(screen, client.options));
                                        }),
                                        new TextMenuEntry(I18n.get("options.sounds").replace("...", ""), () -> {
                                            ((MenuWidgetListener) screen).removeMenu();
                                            client.setScreen(new SoundOptionsScreen(screen, client.options));
                                        })
                                )
                        ));
                    }
                }
            }
            return EventResult.pass();
        });
//        ClientTooltipEvent.RENDER_MODIFY_COLOR.register((poseStack, x, y, colorContext) -> {
//            SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
//            SlightGuiModificationsConfig.Gui.TooltipModifications modifications = config.tooltipModifications;
//            if (modifications.enabled) {
//                colorContext.setBackgroundColor(modifications.backgroundColor);
//                colorContext.setOutlineGradientTopColor(modifications.outlineGradientTopColor);
//                colorContext.setOutlineGradientBottomColor(modifications.outlineGradientBottomColor);
//            }
//        });
        reloadCtsAsync();
        /*RRPCallback.AFTER_VANILLA.register(packs -> {
            RuntimeResourcePack pack = RuntimeResourcePack.create("slightguimodifications:cts_textures");
            Path buttons = FabricLoader.getInstance().getConfigDir().resolve("slightguimodifications/buttons.png");
            if (Files.exists(buttons)) {
                try {
                    pack.addAsset(new ResourceLocation("minecraft:textures/gui/widgets.png"), Files.readAllBytes(buttons));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Path textField = FabricLoader.getInstance().getConfigDir().resolve("slightguimodifications/text_field.png");
            if (Files.exists(textField)) {
                try {
                    pack.addAsset(new ResourceLocation("minecraft:textures/gui/text_field.png"), Files.readAllBytes(textField));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Path slider = FabricLoader.getInstance().getConfigDir().resolve("slightguimodifications/slider.png");
            if (Files.exists(slider)) {
                try {
                    pack.addAsset(new ResourceLocation("slightguimodifications:textures/gui/slider.png"), Files.readAllBytes(slider));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Path sliderHovered = FabricLoader.getInstance().getConfigDir().resolve("slightguimodifications/slider_hovered.png");
            if (Files.exists(sliderHovered)) {
                try {
                    pack.addAsset(new ResourceLocation("slightguimodifications:textures/gui/slider_hovered.png"), Files.readAllBytes(sliderHovered));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            packs.add(pack);
        });*/
    }
    
    public static final LazyResettable<Cts> CTS = new LazyResettable<>(Cts::new);
    
    public static void reloadCtsAsync() {
        CtsRegistry.loadScriptsAsync();
    }
    
    public static void reloadCts() {
        CtsRegistry.loadScripts();
    }
    
    public static void openModMenu() {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            try {
                Class.forName("me.shedaniel.slightguimodifications.gui.cts.ModMenuCompat").getDeclaredMethod("openModMenu").invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static String getModMenuText() {
        try {
            return (String) Class.forName("me.shedaniel.slightguimodifications.gui.cts.ModMenuCompat").getDeclaredMethod("getModMenuText").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    
    public static double bezierEase(double value, double[] points) {
        return bezierEase(value, points[0], points[1], points[2], points[3]);
    }
    
    public static float bezierEase(float value, double[] points) {
        return (float) bezierEase(value, points[0], points[1], points[2], points[3]);
    }
    
    private static double bezierEase(double value, double point1, double point2, double point3, double point4) {
        return point1 * Math.pow(1 - value, 3) + 3 * point2 * Math.pow(1 - value, 2) * value + 3 * point2 * (1 - value) * Math.pow(value, 2) + point4 * Math.pow(value, 3);
    }
    
    private static boolean configInitialized = false;
    
    public static SlightGuiModificationsConfig.Gui getGuiConfig() {
        if (!configInitialized) {
            AutoConfig.register(SlightGuiModificationsConfig.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
            configInitialized = true;
        }
        
        return AutoConfig.getConfigHolder(SlightGuiModificationsConfig.class).getConfig().gui;
    }
    
    public static Cts getCtsConfig() {
        return CTS.get();
    }
    
    public static void resetCts() {
        CTS.reset();
    }
    
    public static float getSpeed() {
        return getGuiConfig().openingAnimation.fluidAnimationDuration;
    }
    
    @SuppressWarnings("deprecation")
    public static Screen getConfigScreen(Screen parent) {
        ConfigScreenProvider<SlightGuiModificationsConfig> supplier = (ConfigScreenProvider<SlightGuiModificationsConfig>) AutoConfig.getConfigScreen(SlightGuiModificationsConfig.class, parent);
        supplier.setBuildFunction(builder -> {
            Runnable runnable = builder.getSavingRunnable();
            builder.setSavingRunnable(() -> {
                runnable.run();
                Minecraft.getInstance().resizeDisplay();
            });
            builder.setAfterInitConsumer(screen -> {
                ScreenHooks.addRenderableWidget(screen, new Button(screen.width - 104, 4, 100, 20, Component.translatable("text.slightguimodifications.reloadCts"), button -> {
                    SlightGuiModifications.resetCts();
                    SlightGuiModifications.reloadCts();
                }, Supplier::get) {});
            });
            return builder.build();
        });
        return supplier.get();
    }
}
