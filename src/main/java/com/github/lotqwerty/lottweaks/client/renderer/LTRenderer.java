package com.github.lotqwerty.lottweaks.client.renderer;

import java.util.Collection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;

public final class LTRenderer {

	public static void renderItemStacks(DrawContext context, Collection<ItemStack> stacks, int x, int y, int t, float pt, int lt, byte direction) {
		renderItemStacks(context, stacks, x, y, t, pt, lt, direction, RenderMode.CIRCLE);
	}

	public static void renderItemStacks(DrawContext context, Collection<ItemStack> stacks, int x, int y, int t, float pt, int lt, byte direction, RenderMode renderMode) {
		if (stacks.isEmpty()) {
			return;
		}
		glInitialize();
		if (renderMode == RenderMode.CIRCLE) {
			circular(context, stacks, x, y, t, pt, lt, direction);
		} else {
			linear(context, stacks, x, y, t, pt, lt, direction);
		}
		glFinalize();
	}

	public enum RenderMode {
		CIRCLE,
		LINE,
	}

	private static void circular(DrawContext context, Collection<ItemStack> stacks, int x, int y, int t, float pt, int lt, byte direction) {
		double max_r = 20 + stacks.size() * 1.2;
		double r = max_r * Math.tanh((t + pt) / 3);
		double afterimage = 1 - Math.tanh((t + pt - lt)/1.5);
		//
		int i = 0;
		for (ItemStack c: stacks) {
			double theta = -((double)i - afterimage*direction) / stacks.size() * 2 * Math.PI + Math.PI / 2;
			double dx = r * Math.cos(theta);
			double dy = r * Math.sin(theta);
			renderAndDecorateItem(context, c, (int)Math.round(x + dx), (int)Math.round(y + dy));
			i++;
		}
	}

	private static void linear(DrawContext context, Collection<ItemStack> stacks, int x, int y, int t, float pt, int lt, byte direction) {
		int i = 0;
		int R = 16;
		double afterimage = 1 - Math.tanh((t + pt - lt)/1.5);
		for (ItemStack c: stacks) {
			renderAndDecorateItem(context, c, x, (int)Math.round(y - i*R + afterimage*direction*R));
			i++;
		}
	}

	private static void renderAndDecorateItem(DrawContext context, ItemStack itemStack, int x, int y) {
		context.drawItem(itemStack, x, y);
	}

	private static void glInitialize() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
	}
	
	private static void glFinalize() {
        RenderSystem.disableBlend();
	}
	
}
