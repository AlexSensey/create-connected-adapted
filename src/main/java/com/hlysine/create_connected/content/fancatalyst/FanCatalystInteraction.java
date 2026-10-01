package com.hlysine.create_connected.content.fancatalyst;

import com.hlysine.create_connected.registries.CCBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class FanCatalystInteraction {
    private FanCatalystInteraction() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var player = event.getEntity();
        var pos = event.getPos();
        if (event.isCanceled() || player.isSpectator() || !player.mayBuild()
                || !level.mayInteract(player, pos))
            return;

        var state = level.getBlockState(pos);
        var held = event.getItemStack();
        if (level.isClientSide() && state.is(CCBlocks.EMPTY_FAN_CATALYST.get())
                && (held.is(Items.NETHERRACK) || held.is(Items.SOUL_SAND))) {
            // These block items are recipe inputs, not adjacent block placements.
            // Leave the authoritative transform and consumption to Create.
            consume(event);
            return;
        }
        boolean water = state.is(CCBlocks.FAN_SPLASHING_CATALYST.get());
        boolean lava = state.is(CCBlocks.FAN_BLASTING_CATALYST.get());
        if ((state.is(CCBlocks.EMPTY_FAN_CATALYST.get()) || water || lava)
                && held.getItem() instanceof BucketItem bucket
                && bucket.content != Fluids.EMPTY) {
            // Create applies the recipe on the server. Consume the client block
            // click too, so it cannot send a second, ordinary bucket-use packet.
            // Also consume repeated clicks on a full water/lava catalyst.
            if (level.isClientSide() || water || lava)
                consume(event);
            return;
        }

        if (!held.is(Items.BUCKET))
            return;
        if (!water && !lava)
            return;

        consume(event);
        if (level.isClientSide())
            return;
        if (!level.setBlock(pos, CCBlocks.EMPTY_FAN_CATALYST.getDefaultState(), Block.UPDATE_ALL))
            return;
        player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(held, player,
                new ItemStack(water ? Items.WATER_BUCKET : Items.LAVA_BUCKET)));
        level.playSound(null, pos, water ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_FILL_LAVA,
                SoundSource.BLOCKS, 1, 1);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
    }

    private static void consume(PlayerInteractEvent.RightClickBlock event) {
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
