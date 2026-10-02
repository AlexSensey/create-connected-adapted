package com.hlysine.create_connected.mixin.itemsilo;

import com.hlysine.create_connected.content.itemsilo.ItemSiloMountedStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import net.createmod.catnip.api.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MountedStorageManager.class, remap = false)
public abstract class MountedStorageManagerMixin {
    @Shadow
    private void addStorage(MountedItemStorage storage, BlockPos pos) {
        throw new AssertionError("Mixin shadow");
    }

    @Inject(
            method = "readLegacy(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/api/nbt/NBTHelper;iterateCompoundList(Lnet/minecraft/nbt/ListTag;Ljava/util/function/Consumer;)V",
                    ordinal = 0
            )
    )
    private void readLegacy(HolderLookup.Provider registries, CompoundTag nbt, CallbackInfo ci) {
        NBTHelper.iterateCompoundList(nbt.getListOrEmpty("Storage"), tag -> {
            BlockPos pos = NBTHelper.readBlockPos(tag, "Pos");
            CompoundTag data = tag.getCompoundOrEmpty("Data");

            if (data.contains("NoFuel")) {
                addStorage(ItemSiloMountedStorage.fromLegacy(registries, data), pos);
            }
        });
    }
}
