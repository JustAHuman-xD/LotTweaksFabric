package com.github.lotqwerty.lottweaks.client;

import com.mojang.logging.LogUtils;
import org.lwjgl.glfw.GLFW;

import com.github.lotqwerty.lottweaks.client.keys.RotateKey;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class LotTweaks implements ClientModInitializer {
	public static final RotateKey ROTATE_KEY = new RotateKey(GLFW.GLFW_KEY_R, "LotTweaks");
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "lottweaks";

	@Override
	public void onInitializeClient() {
		RotationHelper.loadAllFromFile();
		RotationHelper.loadAllItemGroupFromStrArray();
		ClientCommandRegistrationCallback.EVENT.register(new LotTweaksCommand());
	}

	public static void showErrorLogToChat() {
		Minecraft mc = Minecraft.getInstance();
		for (String line : RotationHelper.LOG_GROUP_CONFIG) {
			mc.getChatListener().handleSystemMessage(Component.literal(String.format("LotTweaks: %s%s", ChatFormatting.RED, line)), false);
		}
	}
}
