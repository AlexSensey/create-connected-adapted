package com.hlysine.create_connected.content.contraption.jukebox;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.api.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.JukeboxSong;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ContraptionMusicManager {
    private static final Map<Pair<Integer, BlockPos>, SoundInstance> playingContraptionRecords = new HashMap<>();

    public static void playContraptionMusic(@Nullable JukeboxSong song,
                                            AbstractContraptionEntity entity,
                                            BlockPos localPos,
                                            BlockPos worldPos,
                                            boolean silent) {
        Pair<Integer, BlockPos> contraption = Pair.of(entity.getId(), localPos);
        SoundInstance soundInstance = playingContraptionRecords.get(contraption);
        if (soundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(soundInstance);
            playingContraptionRecords.remove(contraption);
        }

        if (song != null) {
            if (!silent) {
                Minecraft.getInstance().gui.hud.setNowPlaying(song.description());
            }

            SoundInstance newInstance = new ContraptionRecordSoundInstance(
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
        // Match 26.2 LevelEventHandler without requiring access to its private helper.
        var level = Minecraft.getInstance().level;
        if (level != null) {
            for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPos).inflate(3)))
                nearby.setRecordPlayingNearby(worldPos, song != null);
        }
    }
}
