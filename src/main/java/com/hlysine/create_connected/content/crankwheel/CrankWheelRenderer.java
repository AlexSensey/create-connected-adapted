package com.hlysine.create_connected.content.crankwheel;

import com.hlysine.create_connected.CreateConnected;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CreateVisualizationManager;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Quaternionf;

import java.util.List;

@EventBusSubscriber(modid = CreateConnected.MODID, value = Dist.CLIENT)
public class CrankWheelRenderer extends SafeBlockEntityRenderer<CrankWheelBlockEntity> {
    private static final StandaloneModelKey<BlockStateModelPart> SMALL_BASE = key("crank_wheel/block");
    private static final StandaloneModelKey<BlockStateModelPart> SMALL_HANDLE = key("crank_wheel/handle");
    private static final StandaloneModelKey<BlockStateModelPart> LARGE_BASE = key("large_crank_wheel/block");
    private static final StandaloneModelKey<BlockStateModelPart> LARGE_HANDLE = key("large_crank_wheel/handle");

    public CrankWheelRenderer(BlockEntityRendererProvider.Context context) {}

    private static StandaloneModelKey<BlockStateModelPart> key(String path) {
        return new StandaloneModelKey<>(() -> CreateConnected.MODID + ":" + path);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterStandalone event) {
        register(event, SMALL_BASE, "crank_wheel/block");
        register(event, SMALL_HANDLE, "crank_wheel/handle");
        register(event, LARGE_BASE, "large_crank_wheel/block");
        register(event, LARGE_HANDLE, "large_crank_wheel/handle");
    }

    private static void register(ModelEvent.RegisterStandalone event,
                                 StandaloneModelKey<BlockStateModelPart> key, String path) {
        event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(CreateConnected.asResource("block/" + path)));
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new CrankRenderState();
    }

    @Override
    public void extractRenderState(CrankWheelBlockEntity be, BlockEntityRenderState state, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (!(state instanceof CrankRenderState crank))
            return;
        crank.visible = !isInvalid(be) && !be.isRemoved()
                && !CreateVisualizationManager.supportsVisualization(be.getLevel());
        if (!crank.visible)
            return;
        var blockState = be.getBlockState();
        crank.large = blockState.getBlock() instanceof CrankWheelBlock block && block.largeCog;
        crank.facing = blockState.getValue(CrankWheelBlock.FACING);
        crank.baseAngle = KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), crank.facing.getAxis(), partialTicks);
        crank.handleAngle = (float) Math.toRadians(be.getIndependentAngle(partialTicks));
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof CrankRenderState crank) || !crank.visible)
            return;
        var manager = Minecraft.getInstance().getModelManager();
        // Resolve after resource reloads instead of retaining stale baked parts.
        submitPart(manager.getStandaloneModel(crank.large ? LARGE_BASE : SMALL_BASE), crank.baseAngle,
                crank.facing, state.lightCoords, ms, collector);
        submitPart(manager.getStandaloneModel(crank.large ? LARGE_HANDLE : SMALL_HANDLE), crank.handleAngle,
                crank.facing, state.lightCoords, ms, collector);
    }

    private static void submitPart(BlockStateModelPart part, float angle, Direction facing, int light,
                                   PoseStack ms, SubmitNodeCollector collector) {
        if (part == null)
            return;
        ms.pushPose();
        ms.translate(.5, .5, .5);
        var axis = Direction.get(Direction.AxisDirection.POSITIVE, facing.getAxis());
        ms.mulPose(new Quaternionf().rotationAxis(angle, axis.getStepX(), axis.getStepY(), axis.getStepZ()));
        ms.mulPose(new Quaternionf().rotateTo(0, 0, -1, facing.getStepX(), facing.getStepY(), facing.getStepZ()));
        ms.translate(-.5, -.5, -.5);
        collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(part),
                BlockModelRenderState.EMPTY_TINTS, light, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @Override
    protected void renderSafe(CrankWheelBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Rendering uses extracted state and SubmitNodeCollector in 26.2.
    }

    private static class CrankRenderState extends BlockEntityRenderState {
        boolean visible;
        boolean large;
        Direction facing = Direction.UP;
        float baseAngle;
        float handleAngle;
    }
}
