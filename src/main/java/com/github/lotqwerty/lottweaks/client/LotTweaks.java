package com.github.lotqwerty.lottweaks.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

import com.github.lotqwerty.lottweaks.client.keys.RotateKey;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LotTweaks implements ClientModInitializer {
	public static final RotateKey ROTATE_KEY = new RotateKey(GLFW.GLFW_KEY_R, "LotTweaks");
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "lottweaks";

	@Override
	public void onInitializeClient() {
		RotationHelper.load();
		ClientCommandRegistrationCallback.EVENT.register(new LotTweaksCommand());
		ClientTickEvents.END_CLIENT_TICK.register(ROTATE_KEY);
	}

	public static void showErrorLogToChat() {
		MinecraftClient mc = MinecraftClient.getInstance();
		for (String line : RotationHelper.LOG_GROUP_CONFIG) {
			mc.getMessageHandler().onGameMessage(Text.literal(String.format("LotTweaks: %s%s", Formatting.RED, line)), false);
		}
	}
}
