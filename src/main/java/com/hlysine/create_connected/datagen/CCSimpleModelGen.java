package com.hlysine.create_connected.datagen;

import com.google.gson.JsonObject;
import com.hlysine.create_connected.CreateConnected;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.Direction;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;

/** Native JSON output for simple and directional block models. */
public class CCSimpleModelGen implements DataProvider {
    private record Definition(Identifier model, JsonObject generatedModel, boolean directItem, JsonObject blockstate) {}
    private static final Map<Block, Definition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<Identifier, JsonObject> EXTRA_MODELS = new LinkedHashMap<>();
    private static final Map<Block, JsonObject> ITEM_DEFINITIONS = new LinkedHashMap<>();
    private static final Map<Identifier, JsonObject> STANDALONE_ITEMS = new LinkedHashMap<>();
    private final PackOutput output;

    public CCSimpleModelGen(PackOutput output) { this.output = output; }

    public static void register(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        register(block, Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "/block"));
    }

    public static void register(Block block, Identifier model) {
        DEFINITIONS.put(block, new Definition(model, null, false, null));
    }

    public static void registerModel(Identifier id, JsonObject model) {
        EXTRA_MODELS.put(id, model);
    }

    public static void registerItemDefinition(Block block, JsonObject definition) {
        ITEM_DEFINITIONS.put(block, definition);
    }

    public static void registerStandaloneItem(Item item, Identifier parent) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        Identifier model = Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath());
        registerModel(model, parentModel(parent));
        STANDALONE_ITEMS.put(id, registeredItemDefinition(id, model, parent));
    }

    public static void registerGeneratedItem(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        Identifier model = Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath());
        registerModel(model, generatedItemModel(model));
        STANDALONE_ITEMS.put(id, itemDefinition(model, false));
    }

    public static JsonObject generatedItemModel(Identifier texture) {
        JsonObject json = parentModel(Identifier.withDefaultNamespace("item/generated"));
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", texture.toString());
        json.add("textures", textures);
        return json;
    }

    public static void registerBlockItem(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        registerStandaloneItem(item, Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "/item"));
    }

    public static JsonObject registeredItemDefinition(Identifier id, Identifier model, Identifier parent) {
        boolean waterTint = id.equals(CreateConnected.asResource("fan_splashing_catalyst"));
        return itemDefinition(waterTint ? parent : model, waterTint);
    }

    public static JsonObject parentModel(Identifier parent) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent.toString());
        return json;
    }

    public static void registerBlockstate(Block block, JsonObject blockstate) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        DEFINITIONS.put(block, new Definition(Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()),
                null, false, blockstate));
    }

    public static void registerDirectional(Block block, Function<BlockState, Identifier> models) {
        JsonObject variants = new JsonObject();
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            String key = state.getProperties().stream().sorted(Comparator.comparing(Property::getName))
                    .map(property -> property.getName() + "=" + propertyValue(property, state))
                    .collect(Collectors.joining(","));
            variants.add(key, directionalVariant(models.apply(state), state.getValue(BlockStateProperties.FACING)));
        }
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        DEFINITIONS.put(block, new Definition(models.apply(block.defaultBlockState()), null, false, json));
    }

    private static <T extends Comparable<T>> String propertyValue(Property<T> property, BlockState state) {
        return property.getName(state.getValue(property));
    }

    public static JsonObject directionalVariant(Identifier model, Direction facing) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", model.toString());
        int x = facing == Direction.DOWN ? 180 : facing == Direction.UP ? 0 : 90;
        int y = facing.getAxis().isVertical() ? 0 : ((int) facing.toYRot() + 180) % 360;
        if (x != 0) variant.addProperty("x", x);
        if (y != 0) variant.addProperty("y", y);
        return variant;
    }

    public static void registerBarrier(Block block) {
        generated(block, Identifier.withDefaultNamespace("block/barrier"), null);
    }

    public static void registerContent(Block block, Identifier texture) {
        generated(block, CreateConnected.asResource("block/fan_catalyst/with_content"), texture);
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        registerModel(Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()),
                parentModel(Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath())));
    }

    private static void generated(Block block, Identifier parent, Identifier texture) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent.toString());
        if (texture != null) {
            JsonObject textures = new JsonObject();
            textures.addProperty("content", texture.toString());
            json.add("textures", textures);
        }
        DEFINITIONS.put(block, new Definition(Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()), json, true, null));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var blockstates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        var models = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        var items = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        var writes = new ArrayList<CompletableFuture<?>>();
        DEFINITIONS.forEach((block, definition) -> {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            writes.add(DataProvider.saveStable(cache, definition.blockstate == null ? blockstate(definition.model) : definition.blockstate, blockstates.json(id)));
            if (definition.generatedModel != null)
                writes.add(DataProvider.saveStable(cache, definition.generatedModel, models.json(definition.model)));
            if (block.asItem() != Items.AIR && !STANDALONE_ITEMS.containsKey(id)) {
                boolean waterTint = id.equals(CreateConnected.asResource("fan_splashing_catalyst"));
                Identifier itemModel = waterTint ? CreateConnected.asResource("block/fan_splashing_catalyst/item")
                        : definition.directItem ? definition.model
                        : Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath());
                JsonObject item = ITEM_DEFINITIONS.get(block);
                writes.add(DataProvider.saveStable(cache, item == null ? itemDefinition(itemModel, waterTint) : item, items.json(id)));
            }
        });
        EXTRA_MODELS.forEach((id, model) -> writes.add(DataProvider.saveStable(cache, model, models.json(id))));
        STANDALONE_ITEMS.forEach((id, item) -> writes.add(DataProvider.saveStable(cache, item, items.json(id))));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    public static JsonObject blockstate(Identifier model) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", model.toString());
        JsonObject variants = new JsonObject();
        variants.add("", variant);
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }

    public static JsonObject itemDefinition(Identifier model, boolean waterTint) {
        JsonObject inner = new JsonObject();
        inner.addProperty("type", "minecraft:model");
        inner.addProperty("model", model.toString());
        if (waterTint) {
            JsonObject tint = new JsonObject();
            tint.addProperty("type", "create_connected:water");
            var tints = new com.google.gson.JsonArray();
            tints.add(tint);
            inner.add("tints", tints);
        }
        JsonObject json = new JsonObject();
        json.add("model", inner);
        return json;
    }

    @Override public String getName() { return "Create Connected simple and directional block and item models"; }
}
