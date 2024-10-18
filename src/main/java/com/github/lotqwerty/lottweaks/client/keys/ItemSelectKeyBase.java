package com.github.lotqwerty.lottweaks.client.keys;

import java.util.Deque;
import java.util.LinkedList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public class ItemSelectKeyBase extends LTKeyBase {

	protected final Deque<ItemStack> candidates = new LinkedList<>();
	protected int lastRotateTime = -1;
	protected byte rotateDirection = 0;

	public ItemSelectKeyBase(String description, int keyCode, String category) {
		super(description, keyCode, category);
	}

	protected void addToCandidatesWithDedup(ItemStack itemStack) {
		for (ItemStack c: candidates) {
			if (ItemStack.areEqual(c, itemStack)) {
				return;
			}
		}
		candidates.add(itemStack);
	}
	
	protected void rotateCandidatesForward() {
		candidates.addFirst(candidates.pollLast());
		this.updateLastRotateTime();
		this.rotateDirection = 1;
	}

	protected void rotateCandidatesBackward() {
		candidates.addLast(candidates.pollFirst());
		this.updateLastRotateTime();
		this.rotateDirection = -1;
	}

	protected void updateCurrentItemStack(ItemStack itemStack) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.player.getInventory().setStack(mc.player.getInventory().selectedSlot, itemStack);
        mc.interactionManager.clickCreativeStack(mc.player.getStackInHand(Hand.MAIN_HAND), 36 + mc.player.getInventory().selectedSlot);
	}

	@Override
	protected void onKeyPressStart() {
		this.resetLastRotateTime();
		this.rotateDirection = 0;
	}

	@Override
	protected void onKeyReleased() {
		candidates.clear();
	}

	private void resetLastRotateTime() {
		this.lastRotateTime = 0;
	}

	private void updateLastRotateTime() {
		this.lastRotateTime = this.pressTime;
	}
	
}
