package com.hlysine.create_connected.content.contraption.noteblock;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.contraption.AutoPlayMovementBehaviour;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.createmod.catnip.api.level.wrapper.WrappedLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.NoteBlock.INSTRUMENT;

public class NoteBlockMovementBehaviour extends AutoPlayMovementBehaviour {

    @Override
    protected void update(MovementContext context, BlockState state, ContraptionWorld contraptionWorld, BlockPos contraptionPos, Level realWorld, BlockPos realPos, boolean wasActive, boolean isActive) {
        if (!isActive || context.disabled)
            return;
        playNote(context.contraption, state, contraptionPos, realWorld, context.contraption.entity);
    }

    public static void playNote(Contraption contraption, BlockState state, BlockPos localPos,
                                Level realWorld, @Nullable Entity source) {
        if (!(realWorld instanceof ServerLevel server) || contraption == null
                || contraption.entity == null || contraption.entity.isRemoved()
                || contraption.entity.level() != realWorld || !state.is(Blocks.NOTE_BLOCK))
            return;
        var above = contraption.getBlocks().get(localPos.above());
        if (!state.getValue(INSTRUMENT).worksAboveNoteBlock()
                && above != null && !above.state().isAir())
            return;
        BlockPos realPos = BlockPos.containing(contraption.entity.toGlobalVector(Vec3.atCenterOf(localPos), 1));
        Level playbackWorld = new WrappedLevel(realWorld) {
            @Override
            public BlockState getBlockState(BlockPos pos) {
                if (pos.equals(realPos))
                    return state;
                if (pos.equals(realPos.above()))
                    return above == null ? Blocks.AIR.defaultBlockState() : above.state();
                return super.getBlockState(pos);
            }

            @Override
            public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
                if (!pos.equals(realPos.above()) || above == null || above.nbt() == null
                        || !(above.state().getBlock() instanceof AbstractSkullBlock))
                    return null;
                var skull = new SkullBlockEntity(pos, above.state());
                try (var problems = new ProblemReporter.ScopedCollector(CreateConnected.LOGGER)) {
                    skull.loadWithComponents(TagValueInput.create(problems, server.registryAccess(), above.nbt()));
                }
                return skull;
            }

            @Override
            public void playSeededSound(@Nullable Entity player, double x, double y, double z,
                                        Holder<SoundEvent> sound, SoundSource category,
                                        float volume, float pitch, long seed) {
                server.playSeededSound(player, x, y, z, sound, category, volume, pitch, seed);
            }

            @Override
            public void addParticle(ParticleOptions particle, double x, double y, double z,
                                     double dx, double dy, double dz) {
                // Count zero sends the native note color in the velocity fields unchanged.
                server.sendParticles(particle, x, y, z, 0, dx, dy, dz, 1);
            }
        };
        // Keep the installed NoteBlockEvent.Play, pitch and custom-sound logic.
        state.triggerEvent(playbackWorld, realPos, 0, 0);
        server.gameEvent(source, GameEvent.NOTE_BLOCK_PLAY, realPos);
    }
}
