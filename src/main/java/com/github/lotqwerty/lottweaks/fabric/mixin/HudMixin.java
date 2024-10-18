package com.github.lotqwerty.lottweaks.fabric.mixin;

import com.github.lotqwerty.lottweaks.client.LotTweaks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudMixin {
	@Inject(at = @At("TAIL"), method = "renderHotbar")
	private void renderHotbar(DrawContext context, float tickDelta, CallbackInfo ci) {
		LotTweaks.ROTATE_KEY.onRenderHotbar(context, tickDelta);
	}
}
