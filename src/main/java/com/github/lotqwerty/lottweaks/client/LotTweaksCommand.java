package com.github.lotqwerty.lottweaks.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class LotTweaksCommand implements ClientCommandRegistrationCallback {
	private static final String[] ACTIONS = { "include", "includeHotbar", "includeAll", "exclude", "excludeHotbar", "excludeAll", "retain", "retainHotbar", "retainAll" };

	@Override
	public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(LotTweaks.MODID)
				.then(literal("new")
						.then(argument("id", StringArgumentType.string())
								.then(argument("priority", IntegerArgumentType.integer())
										.then(argument("action", StringArgumentType.string())
												.suggests((context, suggestions) -> suggest(List.of(ACTIONS), suggestions))
												.executes(context -> newRotation(
														StringArgumentType.getString(context, "id"),
														IntegerArgumentType.getInteger(context, "priority"),
														StringArgumentType.getString(context, "action")
												))
										)
								)
						)
				)
				.then(literal("edit")
						.then(argument("id", StringArgumentType.string())
								.suggests((context, suggestions) -> suggest(RotationHelper.getRotationIds(), suggestions))
								.then(literal("priority")
										.then(argument("priority", IntegerArgumentType.integer())
												.executes(context -> editRotation(
														StringArgumentType.getString(context, "id"),
														"priority",
														IntegerArgumentType.getInteger(context, "priority")
												))
										)
								)
								.then(argument("action", StringArgumentType.string())
										.suggests((context, suggestions) -> suggest(List.of(ACTIONS), suggestions))
										.executes(context -> editRotation(
												StringArgumentType.getString(context, "id"),
												StringArgumentType.getString(context, "action"),
												0
										))
								)
						)
				)
				.then(literal("delete")
						.then(argument("id", StringArgumentType.string())
								.suggests((context, suggestions) -> suggest(RotationHelper.getRotationIds(), suggestions))
								.executes(context -> deleteRotation(StringArgumentType.getString(context, "id")))
						)
				)
				.then(literal("reload")
						.executes(context -> executeReload())
				)
		;
		dispatcher.register(builder);
	}

	private int newRotation(String id, int priority, String action) {
		if (!id.endsWith(".json")) {
			id += ".json";
		}

		if (RotationHelper.getRotation(id) != null) {
			return Command.SINGLE_SUCCESS;
		}

		ItemRotation rotation = new ItemRotation(id, new ArrayList<>(), priority);
		RotationHelper.ROTATE_RESULT_LIST.add(rotation);
		editRotation(id, action, priority);
		return Command.SINGLE_SUCCESS;
	}

	private int editRotation(String id, String action, int priority) {
		ItemRotation rotation = RotationHelper.getRotation(id);
		if (action.equals("priority")) {
			rotation.setPriority(priority);
			RotationHelper.sortRotation();
			RotationHelper.save();
			return Command.SINGLE_SUCCESS;
		}

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		List<ItemStack> results = rotation.results();
		List<ItemStack> itemStacks = switch(action) {
			case "include", "exclude", "retain" -> List.of(player.getMainHandStack());
			case "includeHotbar", "excludeHotbar", "retainHotbar" -> player.getInventory().main.subList(0, 9);
			case "includeAll", "excludeAll", "retainAll" -> player.getInventory().main;
            default -> List.of();
		};

		if (itemStacks.isEmpty()) {
			return Command.SINGLE_SUCCESS;
		}

		itemStacks = itemStacks.stream()
				.filter(itemStack -> !itemStack.isEmpty())
				.map(itemStack -> itemStack.copyWithCount(1))
				.toList();

		if (action.startsWith("include")) {
			results.addAll(itemStacks);
			for (ItemStack itemStack : itemStacks) {
				RotationHelper.ROTATE_RESULT.compute(ItemStack.hashCode(itemStack), (key, value) -> {
					if (value == null) {
						value = new ArrayList<>();
					}

					if (!value.contains(rotation)) {
						value.add(rotation);
					}
					return value;
				});
			}
		} else if (action.startsWith("exclude")) {
			results.removeAll(itemStacks);
			for (ItemStack itemStack : itemStacks) {
				RotationHelper.ROTATE_RESULT.compute(ItemStack.hashCode(itemStack), (key, value) -> {
					if (value == null) {
						return null;
					}

					value.remove(rotation);
					if (value.isEmpty()) {
						return null;
					}
					return value;
				});
			}
		} else {
			results.retainAll(itemStacks);
			for (ItemStack itemStack : itemStacks) {
				RotationHelper.ROTATE_RESULT.compute(ItemStack.hashCode(itemStack), (key, value) -> {
					if (value == null) {
						return null;
					}

					value.remove(rotation);
					if (value.isEmpty()) {
						return null;
					}
					return value;
				});
			}
		}
		RotationHelper.save();
		return Command.SINGLE_SUCCESS;
	}

	private int deleteRotation(String id) {
		ItemRotation rotation = RotationHelper.getRotation(id);
		if (rotation != null) {
			RotationHelper.ROTATE_RESULT_LIST.remove(rotation);
			for (ItemStack itemStack : rotation.results()) {
				RotationHelper.ROTATE_RESULT.compute(ItemStack.hashCode(itemStack), (key, value) -> {
					if (value == null) {
						return null;
					}

					value.remove(rotation);
					if (value.isEmpty()) {
						return null;
					}
					return value;
				});
			}
		}
		RotationHelper.save();
		return Command.SINGLE_SUCCESS;
	}

	private int executeReload() {
		RotationHelper.load();
		return Command.SINGLE_SUCCESS;
	}

	private static CompletableFuture<Suggestions> suggest(Iterable<String> suggestions, SuggestionsBuilder builder) {
		String remaining = builder.getRemaining();
		for (String suggestion : suggestions) {
			if (suggestion.startsWith(remaining)) {
				builder.suggest(suggestion);
			}
		}
		return builder.buildFuture();
	}
}
