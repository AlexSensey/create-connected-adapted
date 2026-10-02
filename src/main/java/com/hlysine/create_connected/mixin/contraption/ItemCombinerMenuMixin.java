package com.hlysine.create_connected.mixin.contraption;

import com.hlysine.create_connected.compat.ModMixin;
import com.hlysine.create_connected.content.contraption.menu.TrackingContainerLevelAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ModMixin(mods = {"railways"}, applyIfPresent = false)
@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin {
    @Shadow
    @Final
    protected ContainerLevelAccess access;

    @Shadow
    protected abstract boolean isValidBlock(BlockState state);

    @Inject(
            at = @At("HEAD"),
            method = "stillValid(Lnet/minecraft/world/entity/player/Player;)Z",
            cancellable = true
    )
    private void stillValid(Player pPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (access instanceof TrackingContainerLevelAccess tracking) {
            cir.setReturnValue(tracking.stillValid(pPlayer, this::isValidBlock));
        }
    }
}
