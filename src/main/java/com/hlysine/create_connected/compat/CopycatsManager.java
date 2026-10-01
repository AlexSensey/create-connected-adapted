package com.hlysine.create_connected.compat;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.config.CCConfigs;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CopycatsManager {
    private static final Map<String, Identifier> BLOCK_MAP = Map.ofEntries(
            Map.entry("copycat_block", Mods.COPYCATS.rl("copycat_block")),
            Map.entry("copycat_slab", Mods.COPYCATS.rl("copycat_slab")),
            Map.entry("copycat_beam", Mods.COPYCATS.rl("copycat_beam")),
            Map.entry("copycat_vertical_step", Mods.COPYCATS.rl("copycat_vertical_step")),
            Map.entry("copycat_stairs", Mods.COPYCATS.rl("copycat_stairs")),
            Map.entry("copycat_fence", Mods.COPYCATS.rl("copycat_fence")),
            Map.entry("copycat_fence_gate", Mods.COPYCATS.rl("copycat_fence_gate")),
            Map.entry("copycat_wall", Mods.COPYCATS.rl("copycat_wall")),
            Map.entry("copycat_board", Mods.COPYCATS.rl("copycat_board")));
    private static final Map<String, Identifier> ITEM_MAP = Map.of(
            "copycat_box", Mods.COPYCATS.rl("copycat_box"),
            "copycat_catwalk", Mods.COPYCATS.rl("copycat_catwalk"));

    public static final Map<Level, Set<BlockPos>> migrationQueue = Collections.synchronizedMap(new WeakHashMap<>());

    public static Block convert(Block self) {
        Identifier key = RegisteredObjectsHelper.getKeyOrThrow(self);
        if (!validateNamespace(key)) return self;
        Identifier result = BLOCK_MAP.get(key.getPath());
        if (result != null && BuiltInRegistries.BLOCK.containsKey(result))
            return BuiltInRegistries.BLOCK.getValue(result);
        return self;
    }

    public static Item convert(Item self) {
        Identifier key = RegisteredObjectsHelper.getKeyOrThrow(self);
        if (!validateNamespace(key)) return self;
        Identifier result = ITEM_MAP.get(key.getPath());
        if (result != null && BuiltInRegistries.ITEM.containsKey(result))
            return BuiltInRegistries.ITEM.getValue(result);
        Identifier blockResult = BLOCK_MAP.get(key.getPath());
        if (blockResult != null && BuiltInRegistries.BLOCK.containsKey(blockResult))
            return BuiltInRegistries.BLOCK.getValue(blockResult).asItem();
        return self;
    }

    public static ItemLike convert(ItemLike self) {
        return convert(self.asItem());
    }

    public static BlockState convert(BlockState state) {
        Block converted = convert(state.getBlock());
        if (state.getBlock() == converted) return state;
        BlockState newState = converted.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            newState = copyProperty(state, newState, property);
        }
        return newState;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return from.getOptionalValue(property).map(value -> to.trySetValue(property, value)).orElse(to);
    }

    public static Block convertIfEnabled(Block block) {
        Identifier key = RegisteredObjectsHelper.getKeyOrThrow(block);
        if (!validateNamespace(key)) return block;
        if (isFeatureEnabled(key))
            return convert(block);
        return block;
    }

    public static BlockState convertIfEnabled(BlockState state) {
        Identifier key = RegisteredObjectsHelper.getKeyOrThrow(state.getBlock());
        if (!validateNamespace(key)) return state;
        if (isFeatureEnabled(key))
            return convert(state);
        return state;
    }

    public static ItemLike convertIfEnabled(ItemLike item) {
        Identifier key = RegisteredObjectsHelper.getKeyOrThrow(item.asItem());
        if (!validateNamespace(key)) return item;
        if (isFeatureEnabled(key))
            return convert(item);
        return item;
    }

    private static boolean validateNamespace(Identifier key) {
        return key.getNamespace().equals(CreateConnected.MODID) || key.getNamespace().equals(Mods.COPYCATS.id());
    }

    public static boolean existsInCopycats(Identifier key) {
        if (!validateNamespace(key)) return false;
        if (BLOCK_MAP.containsKey(key.getPath())) return true;
        if (ITEM_MAP.containsKey(key.getPath())) return true;
        return false;
    }

    public static boolean isFeatureEnabled(Identifier key) {
        if (!existsInCopycats(key) || !Mods.COPYCATS.isLoaded())
            return false;
        return CopycatsFeatureToggle.isEnabled(Mods.COPYCATS.rl(key.getPath()));
    }

    /** Resolve the optional public API only when Copycats+ is actually used. */
    private static class CopycatsFeatureToggle {
        private static final Method IS_ENABLED = resolve();

        private static Method resolve() {
            try {
                Class<?> api = Class.forName("com.copycatsplus.copycats.config.FeatureToggle");
                Method method = api.getMethod("isEnabled", Identifier.class);
                if (method.getReturnType() != boolean.class || !java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                    throw new NoSuchMethodException("Expected static boolean isEnabled(Identifier)");
                return method;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Copycats+ feature toggle API is incompatible with Minecraft 26.2", e);
            }
        }

        private static boolean isEnabled(Identifier key) {
            try {
                // Cache the method, not its result: config changes must remain visible.
                return (boolean) IS_ENABLED.invoke(null, key);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtime) throw runtime;
                if (cause instanceof Error error) throw error;
                throw new IllegalStateException("Copycats+ feature check failed", cause);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Copycats+ feature toggle API is inaccessible", e);
            }
        }
    }

    public static void enqueueMigration(Level level, BlockPos pos) {
        synchronized (migrationQueue) {
            Set<BlockPos> list = migrationQueue.computeIfAbsent(level, $ -> Collections.synchronizedSet(new LinkedHashSet<>()));
            synchronized (list) {
                list.add(pos);
            }
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.hasTime() && !event.getLevel().isClientSide()) {
            if (!CCConfigs.common().migrateCopycatsOnInitialize.get()) {
                migrationQueue.clear();
                return;
            }
            Level level = event.getLevel();
            synchronized (migrationQueue) {
                if (migrationQueue.containsKey(level)) {
                    Set<BlockPos> list = migrationQueue.get(level);
                    synchronized (list) {
                        if (list.size() > 0)
                            CreateConnected.LOGGER.debug("Copycats: Migrated " + list.size() + " copycats in " + level.dimension().identifier());
                        for (Iterator<BlockPos> iterator = list.iterator(); iterator.hasNext(); ) {
                            BlockPos pos = iterator.next();
                            if (!level.isLoaded(pos)) {
                                continue;
                            }
                            BlockState state = level.getBlockState(pos);
                            BlockState converted = CopycatsManager.convert(state);
                            if (!converted.is(state.getBlock())) {
                                level.setBlock(pos, converted, 2 | 16 | 32);
                            }
                            // Re-set block entity to trigger Copycats+ migration
                            BlockEntity be = level.getBlockEntity(pos);
                            if (be != null)
                                level.setBlockEntity(be);
                        }
                    }
                    migrationQueue.remove(level);
                }
            }
        }
    }
}
