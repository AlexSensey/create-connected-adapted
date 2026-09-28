package com.hlysine.create_connected.content.dashboard;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DashboardRenderer extends SafeBlockEntityRenderer<DashboardBlockEntity> {

    //taken from sign renderer
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);


    public DashboardRenderer(final BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void renderSafe(DashboardBlockEntity be, float partialTick, PoseStack ps, MultiBufferSource buffer,
                           int packedLight, int packedOverlay) {
        // 26.2 renders through submit below.
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new DashboardRenderState();
    }

    @Override
    public void extractRenderState(DashboardBlockEntity be, BlockEntityRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(be, state, overlay);
        DashboardRenderState dashboard = (DashboardRenderState) state;
        Font font = Minecraft.getInstance().font;
        dashboard.facing = be.getBlockState().getValue(DashboardBlock.FACING);
        dashboard.lineHeight = be.getTextLineHeight();
        dashboard.lines = be.text.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), line -> {
            List<FormattedCharSequence> lines = font.split(line, be.getMaxTextLineWidth());
            return lines.isEmpty() ? FormattedCharSequence.EMPTY : lines.get(0);
        }).clone();
        dashboard.darkColor = AbstractSignRenderer.getDarkColor(be.text);
        dashboard.color = be.text.hasGlowingText() ? be.text.getColor().getTextColor() : dashboard.darkColor;
        dashboard.outline = be.text.hasGlowingText() && isOutlineVisible(be.getBlockPos(), dashboard.color);
        dashboard.textLight = be.text.hasGlowingText() ? 15728880 : state.lightCoords;
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ps, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        DashboardRenderState dashboard = (DashboardRenderState) state;
        Font font = Minecraft.getInstance().font;
        Direction facing = dashboard.facing;
        int lineHeight = dashboard.lineHeight;
        int midpoint = SignText.LINES * lineHeight / 2;
        ps.pushPose();

        ps.translate(0.5, 0.5, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        ps.translate(-0.5, -0.5, -0.5);

        ps.translate(0.5, 12/16f, 9/16f);
        ps.mulPose(Axis.XP.rotationDegrees(-66.80141f));
        ps.translate(0, 3.5/16f, 0.15/16f);

        float scale = 0.015625f * 0.52f;
        ps.scale(scale, -scale, scale);

        for (int i = 0; i < SignText.LINES; ++i) {
            FormattedCharSequence line = dashboard.lines[i];
            float x = -font.width(line) / 2;
            collector.submitText(ps, x, i * lineHeight - midpoint, line, false, Font.DisplayMode.POLYGON_OFFSET,
                    dashboard.textLight, dashboard.color, 0, dashboard.outline ? dashboard.darkColor : 0);
        }

        ps.popPose();
    }

    //taken from sign renderer
    private static boolean isOutlineVisible(final BlockPos blockPos, final int i) {
        if (i == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            final Minecraft minecraft = Minecraft.getInstance();
            final LocalPlayer localPlayer = minecraft.player;
            if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
                return true;
            } else {
                final Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(blockPos)) < (double) OUTLINE_RENDER_DISTANCE;
            }
        }
    }
    private static class DashboardRenderState extends BlockEntityRenderState {
        Direction facing;
        FormattedCharSequence[] lines;
        int lineHeight;
        int color;
        int darkColor;
        int textLight;
        boolean outline;
    }

}

