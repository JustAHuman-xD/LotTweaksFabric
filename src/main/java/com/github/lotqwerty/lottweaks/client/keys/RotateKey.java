package com.github.lotqwerty.lottweaks.client.keys;

import java.util.Collections;
import java.util.List;

import com.github.lotqwerty.lottweaks.client.RotationHelper;
import com.github.lotqwerty.lottweaks.client.renderer.LTRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class RotateKey extends ItemSelectKeyBase {
	private int group = 0;

	public RotateKey(int keyCode, String category) {
		super("lottweaks-rotate", keyCode, category);
	}

	private void updatePhase() {
		if (this.doubleTapTick == 0) {
			group = 0;
		} else {
			group += 1;
		}
	}

	@Override
	protected void onKeyPressStart() {
		super.onKeyPressStart();
		this.updatePhase();
		candidates.clear();

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (!player.isCreative()) {
			return;
		}

		ItemStack itemStack = player.getMainHandStack();
		List<ItemStack> results = RotationHelper.getAllRotateResult(itemStack, this.group);
		if (results != null) {
			int hashCode = ItemStack.hashCode(itemStack);
			for (int i = 0; i < results.size(); i++) {
				if (ItemStack.hashCode(results.get(i)) == hashCode) {
					Collections.rotate(results, -i);
					break;
				}
			}
			candidates.addAll(results);
		}
	}

	protected void onKeyReleased() {
		super.onKeyReleased();
	}

	public boolean onScroll(double scrollAmount) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (this.pressTime == 0 || scrollAmount == 0 || !mc.player.isCreative() || candidates.isEmpty()) {
			return false;
		}

		this.rotateCandidates(scrollAmount > 0);
		this.updateCurrentItemStack(candidates.getFirst());
		return true;
	}

	public void render(DrawContext context, float tickDelta) {
		if (this.pressTime == 0) {
			candidates.clear();
			return;
		}

		MinecraftClient minecraft = MinecraftClient.getInstance();
		PlayerEntity player = minecraft.player;
		if (!player.isCreative() || candidates.isEmpty()) {
			return;
		}

		Window window = minecraft.getWindow();
		int x = window.getScaledWidth() / 2;
		int y = window.getScaledHeight() / 2;
		LTRenderer.renderItemStacks(context, candidates, x, y, pressTime, tickDelta, lastRotateTime, rotateDirection);
	}
}