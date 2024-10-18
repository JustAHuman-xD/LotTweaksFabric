package com.github.lotqwerty.lottweaks.fabric.mixin;

import com.github.lotqwerty.lottweaks.client.LotTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

	@Redirect(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"))
	private void onScroll(Inventory playerInventory, double scrollAmount) {
		if (!LotTweaks.ROTATE_KEY.onScroll(scrollAmount)) {
			playerInventory.swapPaint(scrollAmount);
		}
	}

}
