package com.hlysine.create_connected.content.contraption.noteblock;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.SimpleBlockMovingInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.CommonHooks;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.NOTE;

public class NoteBlockInteractionBehaviour extends SimpleBlockMovingInteraction {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos,
                                           AbstractContraptionEntity entity) {
        if (player.level().isClientSide())
            return true;
        if (entity.isRemoved() || entity.level() != player.level())
            return false;
        var contraption = entity.getContraption();
        if (contraption == null)
            return false;
        var info = contraption.getBlocks().get(localPos);
        if (info == null || !info.state().is(Blocks.NOTE_BLOCK))
            return false;
        return super.handlePlayerInteraction(player, hand, localPos, entity);
    }

    @Override
    protected BlockState handle(Player player, Contraption contraption, BlockPos contraptionPos, BlockState currentState) {
        Level contraptionWorld = contraption.getContraptionWorld();
        Level realWorld = player.level();
        int _new = CommonHooks.onNoteChange(contraptionWorld,
                contraptionPos,
                currentState,
                currentState.getValue(NOTE),
                currentState.cycle(NOTE).getValue(NOTE)
        );
        if (_new == -1) return currentState;
        currentState = currentState.setValue(NOTE, _new);

        NoteBlockMovementBehaviour.playNote(contraption, currentState, contraptionPos, realWorld, player);

        player.awardStat(Stats.TUNE_NOTEBLOCK);
        return currentState;
    }
}
