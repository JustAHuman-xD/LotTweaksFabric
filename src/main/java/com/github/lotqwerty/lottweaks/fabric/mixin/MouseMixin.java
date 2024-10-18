package com.github.lotqwerty.lottweaks.fabric.mixin;

import com.github.lotqwerty.lottweaks.client.LotTweaks;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MouseMixin {
	@Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
	private void onScroll(PlayerInventory inventory, double scrollAmount) {
		if (!LotTweaks.ROTATE_KEY.onScroll(scrollAmount)) {
			inventory.scrollInHotbar(scrollAmount);
		}
	}
}
