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

	protected void rotateCandidates(boolean forward) {
        if (forward) {
            candidates.addFirst(candidates.pollLast());
        } else {
            candidates.addLast(candidates.pollFirst());
        }
        this.updateLastRotateTime();
		this.rotateDirection = (byte) (forward ? 1 : -1);
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
