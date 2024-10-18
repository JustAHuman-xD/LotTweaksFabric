package com.github.lotqwerty.lottweaks.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DynamicOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class RotationHelper {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final Map<Integer, List<ItemRotation>> ROTATE_RESULT = new HashMap<>();
	public static final List<ItemRotation> ROTATE_RESULT_LIST = new ArrayList<>();
	public static final List<String> LOG_GROUP_CONFIG = new ArrayList<>();

	public static ItemRotation getRotation(String id) {
		return ROTATE_RESULT_LIST.stream().filter(itemRotation -> itemRotation.id().equals(id)).findFirst().orElse(null);
	}

	public static List<String> getRotationIds() {
		return ROTATE_RESULT_LIST.stream().map(ItemRotation::id).toList();
	}

	public static void sortRotation() {
		ROTATE_RESULT.values().forEach(list -> list.sort(Comparator.comparingInt(ItemRotation::priority)));
		ROTATE_RESULT_LIST.sort(Comparator.comparingInt(ItemRotation::priority));
	}

	public static List<ItemStack> getAllRotateResult(ItemStack itemStack, int group) {
		if (itemStack == null || itemStack.isEmpty()) {
			return null;
		}

		List<ItemRotation> result = ROTATE_RESULT.get(ItemStack.hashCode(itemStack));
		if (result != null) {
			while (group >= result.size()) {
				group -= result.size();
			}
			return result.get(group).results();
		}
		return null;
	}

	public static void load() {
		ROTATE_RESULT.clear();
		ROTATE_RESULT_LIST.clear();

		File configFolder = getConfigFolder();
		File[] configs = configFolder.listFiles();
		if (configs == null) {
			return;
		}

		for (File config : configs) {
			try(FileReader reader = new FileReader(config)) {
				JsonObject rotation = GSON.fromJson(reader, JsonObject.class);
				int priority = JsonHelper.getInt(rotation, "priority", 0);
				List<ItemStack> rotateResult = new ArrayList<>();
				for (JsonElement element : rotation.getAsJsonArray("items")) {
					if (element instanceof JsonPrimitive primitive && primitive.isString()) {
						try {
							rotateResult.add(Registries.ITEM.get(new Identifier(primitive.getAsString())).getDefaultStack());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (element instanceof JsonObject object) {
						try {
							ItemStack itemStack = Registries.ITEM.get(new Identifier(object.get("item").getAsString())).getDefaultStack();
							try {
								if (itemStack.getComponents() instanceof ComponentMapImpl components && JsonHelper.hasString(object, "components")) {
									String componentsString = JsonHelper.getString(object, "components");
									components.setChanges(ComponentChanges.CODEC.decode(withRegistryAccess(NbtOps.INSTANCE),
											StringNbtReader.parse(componentsString)).getOrThrow().getFirst());
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							rotateResult.add(itemStack);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				ItemRotation itemRotation = new ItemRotation(config.getName(), rotateResult, priority);
				for (ItemStack itemStack : rotateResult) {
					ROTATE_RESULT.compute(ItemStack.hashCode(itemStack), (key, value) -> {
						if (value == null) {
							value = new ArrayList<>();
						}
						value.add(itemRotation);
						return value;
					});
				}
				ROTATE_RESULT_LIST.add(itemRotation);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void save() {
		File configFolder = getConfigFolder();
		File[] configs = configFolder.listFiles();
		if (configs != null) {
			for (File config : configs) {
				config.delete();
			}
		}

		for (ItemRotation itemRotation : ROTATE_RESULT_LIST) {
			File file = new File(configFolder, itemRotation.id());
			try(FileWriter writer = new FileWriter(file)) {
				JsonObject rotation = new JsonObject();
				JsonArray items = new JsonArray();
				rotation.addProperty("priority", itemRotation.priority());
				rotation.add("items", items);
				for (ItemStack itemStack : itemRotation.results()) {
					String id = Registries.ITEM.getId(itemStack.getItem()).toString();
					ComponentChanges changes = itemStack.getComponentChanges();
					if (changes == ComponentChanges.EMPTY) {
						items.add(new JsonPrimitive(id));
					} else {
						JsonObject object = new JsonObject();
						object.addProperty("item", id);
						object.addProperty("components", ComponentChanges.CODEC.encodeStart(withRegistryAccess(NbtOps.INSTANCE), changes).getOrThrow().asString());
						items.add(object);
					}
				}
				GSON.toJson(rotation, writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static File getConfigFolder() {
		File file = FabricLoader.getInstance().getConfigDir().resolve("lottweaks").toFile();
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	private static <T> DynamicOps<T> withRegistryAccess(DynamicOps<T> ops) {
		MinecraftClient instance = MinecraftClient.getInstance();
		if (instance == null || instance.world == null) {
			return ops;
		}
		return instance.world.getRegistryManager().getOps(ops);
	}
}
