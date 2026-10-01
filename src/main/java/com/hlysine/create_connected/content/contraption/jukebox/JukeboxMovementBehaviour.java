package com.hlysine.create_connected.content.contraption.jukebox;

import com.hlysine.create_connected.content.contraption.AutoPlayMovementBehaviour;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class JukeboxMovementBehaviour extends AutoPlayMovementBehaviour {

    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide() || context.contraption == null
                || context.contraption.entity == null || context.contraption.entity.isRemoved())
            return;
        super.tick(context);
        if (context.disabled)
            return;
        MovingInteractionBehaviour interactor = context.contraption.getInteractors().get(context.localPos);
        if (interactor instanceof JukeboxInteractionBehaviour jukeboxInteraction)
            jukeboxInteraction.tickPlaying(context.contraption, context.localPos);
    }

    private void stopPlaying(MovementContext context) {
        if (context.world.isClientSide() || context.contraption == null) return;
        MovingInteractionBehaviour interactor = context.contraption.getInteractors().get(context.localPos);
        if (!(interactor instanceof JukeboxInteractionBehaviour jukeboxInteraction)) return;
        var info = context.contraption.getBlocks().get(context.localPos);
        if (info == null) return;
        BlockState currentState = info.state();
        jukeboxInteraction.withTempBlockEntity(
                context.contraption,
                context.localPos,
                currentState,
                be -> be.getSongPlayer().stop(be.getLevel(), currentState),
                true
        );
    }

    @Override
    public void onDisabledByControls(MovementContext context) {
        super.onDisabledByControls(context);
        stopPlaying(context);
    }

    @Override
    public void stopMoving(MovementContext context) {
        stopPlaying(context);
    }

    @Override
    protected void update(MovementContext context, BlockState state, ContraptionWorld contraptionWorld, BlockPos contraptionPos, Level realWorld, BlockPos realPos, boolean wasActive, boolean isActive) {
        if (context.world.isClientSide()) return;
        if (context.disabled) return;
        MovingInteractionBehaviour interactor = context.contraption.getInteractors().get(context.localPos);
        if (!(interactor instanceof JukeboxInteractionBehaviour jukeboxInteraction)) return;
        jukeboxInteraction.withTempBlockEntity(context.contraption, context.localPos, state, be -> {
            if (!isActive) {
                if (!be.getSongPlayer().isPlaying())
                    JukeboxSong.fromStack(be.getTheItem())
                            .ifPresent(song -> be.getSongPlayer().play(be.getLevel(), song));
            } else {
                be.getSongPlayer().stop(be.getLevel(), state);
            }
        }, true);
    }
}
