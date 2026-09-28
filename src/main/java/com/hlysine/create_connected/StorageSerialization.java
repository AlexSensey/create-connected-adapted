package com.hlysine.create_connected;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.fluids.FluidStack;

/** Bridges Create's compound-based storage hooks to the 26.2 serialization API. */
public final class StorageSerialization {
    private StorageSerialization() {}

    public static void read(ValueIOSerializable storage, HolderLookup.Provider registries, CompoundTag tag) {
        try (var problems = new ProblemReporter.ScopedCollector(CreateConnected.LOGGER)) {
            storage.deserialize(TagValueInput.create(problems, registries, tag));
        }
    }

    public static CompoundTag write(ValueIOSerializable storage, HolderLookup.Provider registries) {
        try (var problems = new ProblemReporter.ScopedCollector(CreateConnected.LOGGER)) {
            var output = TagValueOutput.createWithContext(problems, registries);
            storage.serialize(output);
            return output.buildResult();
        }
    }

    // TankContent is a direct FluidStack compound in Create, not FluidTank's
    // newer wrapper containing a separate "Fluid" field.
    public static FluidStack readFluid(HolderLookup.Provider registries, CompoundTag tag) {
        return FluidStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(error -> CreateConnected.LOGGER.error("Cannot read stored fluid: {}", error))
                .orElse(FluidStack.EMPTY);
    }

    public static void writeFluid(HolderLookup.Provider registries, CompoundTag parent, String key, FluidStack fluid) {
        FluidStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), fluid)
                .resultOrPartial(error -> CreateConnected.LOGGER.error("Cannot write stored fluid: {}", error))
                .ifPresent(tag -> parent.put(key, tag));
    }
    public static void writeItem(HolderLookup.Provider registries, CompoundTag parent, String key, ItemStack stack) {
        ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .resultOrPartial(error -> CreateConnected.LOGGER.error("Cannot write stored item: {}", error))
                .ifPresent(tag -> parent.put(key, tag));
    }

}
