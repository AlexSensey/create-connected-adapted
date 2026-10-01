package com.hlysine.create_connected.content.contraption.jukebox;

import com.hlysine.create_connected.registries.CCPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;

public class PlayContraptionJukeboxPacket implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayContraptionJukeboxPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, packet -> packet.level,
            ByteBufCodecs.VAR_INT, packet -> packet.contraptionId,
            BlockPos.STREAM_CODEC, packet -> packet.contraptionPos,
            BlockPos.STREAM_CODEC, packet -> packet.worldPos,
            ByteBufCodecs.VAR_INT, packet -> packet.recordId,
            ByteBufCodecs.BOOL, packet -> packet.play,
            ByteBufCodecs.BOOL, packet -> packet.silent,
            PlayContraptionJukeboxPacket::new
    );

    protected Identifier level;
    protected int contraptionId;
    protected BlockPos contraptionPos;
    protected BlockPos worldPos;
    protected int recordId;
    protected boolean play;
    protected boolean silent;

    public PlayContraptionJukeboxPacket(Identifier level, int contraptionId, BlockPos contraptionPos, BlockPos worldPos, int recordId, boolean play, boolean silent) {
        this.level = level;
        this.contraptionId = contraptionId;
        this.contraptionPos = contraptionPos.immutable();
        this.worldPos = worldPos.immutable();
        this.recordId = recordId;
        this.play = play;
        this.silent = silent;
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CCPackets.PLAY_CONTRAPTION_JUKEBOX;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(Player player) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null || !world.dimension().identifier().equals(level))
            return;
        if (play && !world.isLoaded(worldPos))
            return;
        Entity entity = world.getEntity(contraptionId);
        if (!(entity instanceof AbstractContraptionEntity contraptionEntity) || entity.isRemoved())
            return;
        if (play) {
            var contraption = contraptionEntity.getContraption();
            if (contraption == null)
                return;
            var info = contraption.getBlocks().get(contraptionPos);
            if (info == null || !info.state().is(net.minecraft.world.level.block.Blocks.JUKEBOX))
                return;
            Optional<JukeboxSong> song = world.registryAccess()
                    .lookupOrThrow(Registries.JUKEBOX_SONG)
                    .get(recordId)
                    .map(Holder.Reference::value);
            if (song.isEmpty())
                return;
            ContraptionMusicManager.playContraptionMusic(
                    song.get(),
                    contraptionEntity,
                    contraptionPos,
                    worldPos,
                    silent
            );
        } else {
            ContraptionMusicManager.playContraptionMusic(
                    null,
                    contraptionEntity,
                    contraptionPos,
                    worldPos,
                    silent
            );
        }
    }

}
