package com.hlysine.create_connected.mixin.copycat.fence;

import com.hlysine.create_connected.content.copycat.ICopycatWithWrappedBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ValueBoxRenderer.class, remap = false)
public class ValueBoxRendererMixin {
    @WrapOperation(
            method = "submitItemIntoValueBox(Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IFF)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V")
    )
    private static void offsetWrappedBlock(PoseStack pose, float x, float y, float z, Operation<Void> original,
                                           @Local(argsOnly = true) ItemStack filter,
                                           @Local(argsOnly = true, ordinal = 0) float scale) {
        if (filter.getItem() instanceof BlockItem item && item.getBlock() instanceof ICopycatWithWrappedBlock wrapper) {
            var block = wrapper.getWrappedBlock();
            if (block instanceof AbstractSimpleShaftBlock || block instanceof FenceBlock
                    || block.builtInRegistryHolder().is(BlockTags.BUTTONS) || block == Blocks.END_ROD)
                // Translation now precedes scaling; compensate for the block item's fixed scale.
                z -= .1f * scale * 4;
        }
        original.call(pose, x, y, z);
    }
}
