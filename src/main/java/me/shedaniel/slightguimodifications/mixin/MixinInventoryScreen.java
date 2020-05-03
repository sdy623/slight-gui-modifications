package me.shedaniel.slightguimodifications.mixin;

import me.shedaniel.slightguimodifications.SlightGuiModifications;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InventoryScreen.class)
public class MixinInventoryScreen {
    @ModifyVariable(method = "drawEntity", ordinal = 1, at = @At("HEAD"))
    private static int drawEntityChangeY(int y) {
        return SlightGuiModifications.applyYAnimation(y);
    }
}
