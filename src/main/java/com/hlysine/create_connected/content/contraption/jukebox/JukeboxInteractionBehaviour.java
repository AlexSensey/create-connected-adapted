package com.hlysine.create_connected.content.contraption.jukebox;

import com.hlysine.create_connected.CreateConnected;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.createmod.catnip.api.level.wrapper.WrappedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HAS_RECORD;

public class JukeboxInteractionBehaviour extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos contraptionPos,
                                           AbstractContraptionEntity contraptionEntity) {
        if (player.level().isClientSide()) {
            return true;
        }
        Contraption contraption = contraptionEntity.getContraption();
        StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(contraptionPos);
        BlockState currentState = info.state();

        if (currentState.getValue(HAS_RECORD)) {
            withTempBlockEntity(contraption, contraptionPos, currentState, JukeboxBlockEntity::popOutTheItem, false);
        } else {
            ItemStack item = player.getItemInHand(activeHand);
            if (JukeboxSong.fromStack(item).isPresent()) {
                withTempBlockEntity(contraption, contraptionPos, currentState, be -> {
                    be.setTheItem(item.copyWithCount(1));
                    be.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, be.getBlockPos(), GameEvent.Context.of(player, currentState));
                    if (!player.isCreative())
                        item.shrink(1);
                    player.awardStat(Stats.PLAY_RECORD);
                }, false);
            }
        }
        return true;
    }

    public void withTempBlockEntity(Contraption contraption, BlockPos contraptionPos, BlockState currentState, Consumer<JukeboxBlockEntity> action, boolean silent) {
        AtomicReference<BlockState> state = new AtomicReference<>(currentState);
        AbstractContraptionEntity contraptionEntity = contraption.entity;
        BlockPos realPos = BlockPos.containing(contraptionEntity.toGlobalVector(Vec3.atCenterOf(contraptionPos), 1));
        JukeboxBlockEntity be = new JukeboxBlockEntity(realPos, currentState);
        var savedData = contraption.getBlocks().get(contraptionPos).nbt();
        if (savedData != null) {
            try (var problems = new ProblemReporter.ScopedCollector(CreateConnected.LOGGER)) {
                be.loadWithComponents(TagValueInput.create(problems, contraptionEntity.level().registryAccess(), savedData));
            }
        }
        be.setLevel(new WrappedLevel(contraptionEntity.level()) {
            @Override
            public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
                if (pos.equals(realPos)) {
                    state.set(newState);
                    return true;
                }
                return false;
            }

            @Override
            public BlockState getBlockState(BlockPos pos) {
                if (pos.equals(realPos))
                    return state.get();
                return super.getBlockState(pos);
            }

            @Override
            public void levelEvent(@Nullable Entity source, int type, BlockPos pos, int data) {
                if (type == 1010 || type == 1011)
                    PacketDistributor.sendToPlayersInDimension(
                            (ServerLevel) contraptionEntity.level(),
                            new PlayContraptionJukeboxPacket(dimension().identifier(),
                                    contraptionEntity.getId(),
                                    contraptionPos,
                                    pos,
                                    data,
                                    type == 1010,
                                    silent
                            )
                    );
            }
        });
        action.accept(be);
        setContraptionBlockData(contraptionEntity, contraptionPos, new StructureTemplate.StructureBlockInfo(contraptionPos, state.get(), be.saveWithoutMetadata(contraptionEntity.level().registryAccess())));
    }
}
