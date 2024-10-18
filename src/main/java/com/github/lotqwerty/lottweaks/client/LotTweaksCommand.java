package com.github.lotqwerty.lottweaks.client;

import java.util.StringJoiner;

import com.github.lotqwerty.lottweaks.client.RotationHelper.Group;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class LotTweaksCommand implements ClientCommandRegistrationCallback {

	private static void displayMessage(Text textComponent) {
		MinecraftClient.getInstance().getMessageHandler().onGameMessage(textComponent, false);
	}

	@Override
	public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(LotTweaks.MODID)
			.then(literal("add")
				.then(literal("1")
					.executes(context -> {executeAdd(Group.PRIMARY); return Command.SINGLE_SUCCESS;}))
				.then(literal("2")
					.executes(context -> {executeAdd(Group.SECONDARY); return Command.SINGLE_SUCCESS;}))
				)
			.then(literal("reload")
				.executes(context -> {executeReload(); return Command.SINGLE_SUCCESS;})
			)
		;
		dispatcher.register(builder);
	}

	private void executeAdd(Group group) throws LotTweaksCommandRuntimeException {
		MinecraftClient mc = MinecraftClient.getInstance();
		StringJoiner stringJoiner = new StringJoiner(",");
		int count = 0;
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			ItemStack itemStack = mc.player.getInventory().getStack(i);
			if (itemStack.isEmpty()) {
				break;
			}
			Item item = itemStack.getItem();
			if (item == Items.AIR) {
				throw new LotTweaksCommandRuntimeException(String.format("Failed to get item instance. (%d)", i + 1));
			}
			String name = Registries.ITEM.getId(item).toString();
			if (RotationHelper.canRotate(itemStack, group)) {
				throw new LotTweaksCommandRuntimeException(String.format("'%s' already exists (slot %d)", name, i + 1));
			}
			stringJoiner.add(name);
			count++;
		}
		String line = stringJoiner.toString();
		if (line.isEmpty()) {
			throw new LotTweaksCommandRuntimeException(String.format("Hotbar is empty."));
		}
		LotTweaks.LOGGER.debug("adding a new block/item-group from /lottweaks command");
		LotTweaks.LOGGER.debug(line);
		boolean succeeded = RotationHelper.tryToAddItemGroupFromCommand(line, group);
		if (succeeded) {
			displayMessage(Text.literal(String.format("LotTweaks: added %d blocks/items", count)));
		} else {
			displayMessage(Text.literal(Formatting.RED + "LotTweaks: failed to add blocks/items"));
		}
	}

	private void executeReload() throws LotTweaksCommandRuntimeException {
		try {
			boolean f;
			f = RotationHelper.loadAllFromFile();
			if (!f) throw new LotTweaksCommandRuntimeException("LotTweaks: failed to reload config file");
			f = RotationHelper.loadAllItemGroupFromStrArray();
			if (!f) throw new LotTweaksCommandRuntimeException("LotTweaks: failed to reload blocks");
			displayMessage(Text.literal("LotTweaks: reload succeeded!"));
		} catch (LotTweaksCommandRuntimeException e) {
			displayMessage(Text.literal(Formatting.RED + e.getMessage()));
		}
		com.github.lotqwerty.lottweaks.client.LotTweaks.showErrorLogToChat();
	}

	private static final class LotTweaksCommandRuntimeException extends RuntimeException {
		public LotTweaksCommandRuntimeException(String message) {
	        super(message);
	    }
	}
}
