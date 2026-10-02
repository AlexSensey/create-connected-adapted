package com.hlysine.create_connected.mixin.linkedtransmitter;

import com.hlysine.create_connected.compat.ModMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@ModMixin(mods = {"simulated"})
@Mixin(targets = "dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverBlock")
public abstract class ThrottleLeverBlockMixin {
    @Inject(
            cancellable = true,
            at = @At("HEAD"),
            method = "onRemove"
    )
    private void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (createConnected$isThrottleLever(state.getBlock().getClass())
                && createConnected$isThrottleLever(newState.getBlock().getClass()))
            ci.cancel();
    }

    /** Matches the optional base class and its subclasses without loading it. */
    @Unique
    private static boolean createConnected$isThrottleLever(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (current.getName().equals("dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverBlock"))
                return true;
        }
        return false;
    }
}
