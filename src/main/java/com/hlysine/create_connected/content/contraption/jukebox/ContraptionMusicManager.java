package com.hlysine.create_connected.content.contraption.jukebox;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.api.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.JukeboxSong;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ContraptionMusicManager {
    private static final Map<Pair<Integer, BlockPos>, ContraptionRecordSoundInstance> playingContraptionRecords = new HashMap<>();

    private static ClientLevel playingLevel;

    public static void clear() {
        var sounds = Minecraft.getInstance().getSoundManager();
        playingContraptionRecords.values().forEach(sounds::stop);
        playingContraptionRecords.clear();
        playingLevel = null;
    }

    public static void tick() {
        var minecraft = Minecraft.getInstance();
        if (playingLevel != minecraft.level) {
            clear();
            playingLevel = minecraft.level;
        }
        playingContraptionRecords.entrySet().removeIf(entry -> {
            var sound = entry.getValue();
            if (!sound.isStopped() && minecraft.getSoundManager().isActive(sound))
                return false;
            minecraft.getSoundManager().stop(sound);
            notifyNearby(BlockPos.containing(sound.getX(), sound.getY(), sound.getZ()), false);
            return true;
        });
    }

    private static void notifyNearby(BlockPos pos, boolean playing) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3)))
                nearby.setRecordPlayingNearby(pos, playing);
        }
    }

    public static void playContraptionMusic(@Nullable JukeboxSong song,
                                            AbstractContraptionEntity entity,
                                            BlockPos localPos,
                                            BlockPos worldPos,
                                            boolean silent) {
        var level = Minecraft.getInstance().level;
        if (level == null || entity.isRemoved() || entity.level() != level)
            return;
        if (playingLevel != level) {
            clear();
            playingLevel = level;
        }
        Pair<Integer, BlockPos> contraption = Pair.of(entity.getId(), localPos.immutable());
        SoundInstance soundInstance = playingContraptionRecords.get(contraption);
        if (soundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(soundInstance);
            playingContraptionRecords.remove(contraption);
        }

        if (song != null) {
            if (!silent) {
                Minecraft.getInstance().gui.setNowPlaying(song.description());
            }

            ContraptionRecordSoundInstance newInstance = new ContraptionRecordSoundInstance(
                    song.soundEvent().value(),
                    SoundSource.RECORDS,
                    4.0F,
                    1.0F,
                    SoundInstance.createUnseededRandom(),
                    false,
                    0,
                    SoundInstance.Attenuation.LINEAR,
                    entity,
                    localPos
            );
            playingContraptionRecords.put(contraption, newInstance);
            Minecraft.getInstance().getSoundManager().play(newInstance);
        }
        notifyNearby(worldPos, song != null);
    }
}
