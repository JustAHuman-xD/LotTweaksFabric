package com.github.lotqwerty.lottweaks.fabric.mixin;

import com.github.lotqwerty.lottweaks.client.LotTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

@Mixin(Gui.class)
public abstract class HotbarRendererHook {

	@Inject(at = @At("TAIL"), method = "renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V")
	private void renderHotbar(float tickDelta, GuiGraphics guiGraphics, CallbackInfo info) {
		LotTweaks.ROTATE_KEY.onRenderHotbar(tickDelta, guiGraphics);
	}

}
