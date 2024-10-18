package com.github.lotqwerty.lottweaks.client.keys;

import java.util.List;

import com.github.lotqwerty.lottweaks.client.RotationHelper;
import com.github.lotqwerty.lottweaks.client.RotationHelper.Group;
import com.github.lotqwerty.lottweaks.client.renderer.LTRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class RotateKey extends ItemSelectKeyBase {

	private int phase = 0;

	public RotateKey(int keyCode, String category) {
		super("lottweaks-rotate", keyCode, category);
	}

	private void updatePhase() {
		if (this.doubleTapTick == 0) {
			phase = 0;
		} else {
			phase ^= 1;
		}
	}

	private Group getGroup() {
		return this.phase==0 ? Group.PRIMARY : Group.SECONDARY;
	}

	@Override
	protected void onKeyPressStart() {
		super.onKeyPressStart();
		this.updatePhase();
		candidates.clear();
		MinecraftClient mc = MinecraftClient.getInstance();
		if (!mc.player.isCreative()) {
			return;
		}
		ItemStack itemStack = mc.player.getInventory().getMainHandStack();
		if (itemStack.isEmpty()) {
			return;
		}
		List<ItemStack> results = RotationHelper.getAllRotateResult(itemStack, getGroup());
		if (results == null || results.size() <= 1) {
			return;
		}
		candidates.addAll(results);
	}

	protected void onKeyReleased() {
		super.onKeyReleased();
	}

	public boolean onScroll(double scrollAmount) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (this.pressTime == 0 || scrollAmount == 0 || !mc.player.isCreative()) {
			return false;
		}

		if (candidates.isEmpty()) {
			return true;
		}
		if (scrollAmount > 0) {
			this.rotateCandidatesForward();
		} else {
			this.rotateCandidatesBackward();
		}
		this.updateCurrentItemStack(candidates.getFirst());
		return true;
	}

	public void onRenderHotbar(DrawContext context, float tickDelta) {
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
		int x = window.getScaledWidth() / 2 - 90 + player.getInventory().selectedSlot * 20 + 2;
		int y = window.getScaledHeight() - 16 - 3 - 50 + (20 + candidates.size());
		LTRenderer.renderItemStacks(context, candidates, x, y, pressTime, tickDelta, lastRotateTime, rotateDirection);
	}
}