package com.hlysine.create_connected.content.kineticbridge;

import com.hlysine.create_connected.CreateConnected;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.model.CreateStandaloneModels;
import com.simibubi.create.foundation.render.CreateVisualizationManager;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
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
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Quaternionf;

import java.util.List;

@EventBusSubscriber(modid = CreateConnected.MODID, value = Dist.CLIENT)
public class KineticBridgeRenderer extends SafeBlockEntityRenderer<KineticBlockEntity> {
    private static final StandaloneModelKey<BlockStateModelPart> SOURCE =
            new StandaloneModelKey<>(() -> "create_connected:kinetic_bridge/source_coupler");
    private static final StandaloneModelKey<BlockStateModelPart> DESTINATION =
            new StandaloneModelKey<>(() -> "create_connected:kinetic_bridge/destination_coupler");
    private final boolean isDestination;

    private KineticBridgeRenderer(BlockEntityRendererProvider.Context context, boolean isDestination) {
        this.isDestination = isDestination;
    }

    public static KineticBridgeRenderer source(BlockEntityRendererProvider.Context ctx) {
        return new KineticBridgeRenderer(ctx, false);
    }

    public static KineticBridgeRenderer destination(BlockEntityRendererProvider.Context ctx) {
        return new KineticBridgeRenderer(ctx, true);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterStandalone event) {
        event.register(SOURCE, SimpleUnbakedStandaloneModel.simpleModelWrapper(
                CreateConnected.asResource("block/kinetic_bridge/source_coupler")));
        event.register(DESTINATION, SimpleUnbakedStandaloneModel.simpleModelWrapper(
                CreateConnected.asResource("block/kinetic_bridge/destination_coupler")));
    }

    @Override
    public BlockEntityRenderState createRenderState() { return new BridgeRenderState(); }

    @Override
    public void extractRenderState(KineticBlockEntity be, BlockEntityRenderState state, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (!(state instanceof BridgeRenderState bridge))
            return;
        bridge.blockEntity = !isInvalid(be) && !be.isRemoved() ? be : null;
        bridge.visible = !isInvalid(be) && !be.isRemoved()
                && !CreateVisualizationManager.supportsVisualization(be.getLevel());
        if (!bridge.visible)
            return;
        Direction facing = be.getBlockState().getValue(KineticBridgeBlock.FACING);
        bridge.facing = isDestination ? facing : facing.getOpposite();
        bridge.angle = KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), facing.getAxis(), partialTicks);
        bridge.shaftLight = net.minecraft.client.renderer.LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().relative(facing.getOpposite()));
        bridge.couplingLight = net.minecraft.client.renderer.LevelRenderer.getLightCoords(be.getLevel(), be.getBlockPos().relative(facing));
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof BridgeRenderState bridge))
            return;
        submitSettings(bridge.blockEntity, ms, collector);
        if (!bridge.visible) return;
        var manager = Minecraft.getInstance().getModelManager();
        // Resolve models each submission so resource reloads replace the baked parts.
        var shaft = manager.getStandaloneModel(CreateStandaloneModels.SHAFT_HALF);
        var coupling = manager.getStandaloneModel(isDestination ? DESTINATION : SOURCE);
        ms.pushPose();
        ms.translate(.5, .5, .5);
        Direction axis = Direction.get(Direction.AxisDirection.POSITIVE, bridge.facing.getAxis());
        ms.mulPose(new Quaternionf().rotationAxis(bridge.angle, axis.getStepX(), axis.getStepY(), axis.getStepZ()));
        // Both assets are authored facing SOUTH, matching the existing Flywheel visual.
        ms.mulPose(new Quaternionf().rotateTo(0, 0, 1, bridge.facing.getStepX(), bridge.facing.getStepY(), bridge.facing.getStepZ()));
        ms.translate(-.5, -.5, -.5);
        submitPart(shaft, bridge.shaftLight, ms, collector);
        submitPart(coupling, bridge.couplingLight, ms, collector);
        ms.popPose();
    }

    private static void submitPart(BlockStateModelPart model, int light, PoseStack ms, SubmitNodeCollector collector) {
        if (model != null)
            collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(model),
                    BlockModelRenderState.EMPTY_TINTS, light, OverlayTexture.NO_OVERLAY, 0);
    }

    private static void submitSettings(KineticBlockEntity be, PoseStack pose, SubmitNodeCollector collector) {
        Minecraft mc = Minecraft.getInstance();
        if (be == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult hit)
                || !hit.getBlockPos().equals(be.getBlockPos())) return;
        for (var behaviour : be.getAllBehaviours()) {
            if (!(behaviour instanceof ScrollValueBehaviour scroll) || !scroll.isActive()) continue;
            var slot = scroll.getSlotPositioning();
            if (slot instanceof ValueBoxTransform.Sided sided) sided.fromSide(hit.getDirection());
            if (!slot.shouldRender(be.getLevel(), be.getBlockPos(), be.getBlockState())) continue;
            Vec3 offset = slot.getLocalOffset(be.getLevel(), be.getBlockPos(), be.getBlockState());
            if (offset == null) continue;
            // Place the label on the solid panel, one voxel farther from the outer casing trim.
            Direction facing = be.getBlockState().getValue(KineticBridgeBlock.FACING);
            double panelShift = be instanceof KineticBridgeDestinationBlockEntity ? 3 / 16d : -3 / 16d;
            offset = offset.add(facing.getStepX() * panelShift, facing.getStepY() * panelShift,
                    facing.getStepZ() * panelShift);
            String value = scroll.formatValue(); // Includes the multiplier suffix and decimal separator.
            if (value.isEmpty()) continue;
            Direction side = hit.getDirection();
            pose.pushPose();
            // The slot lies 1/32 inside the casing; put the text just outside its surface.
            double outward = 1 / 32d + 1 / 512d;
            pose.translate(offset.x + side.getStepX() * outward,
                    offset.y + side.getStepY() * outward, offset.z + side.getStepZ() * outward);
            switch (side) {
                case NORTH -> pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                case EAST -> pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
                case WEST -> pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270));
                case UP -> pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(270));
                case DOWN -> pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                case SOUTH -> {}
            }
            collector.submitCustomGeometry(pose, RenderTypes.debugQuads(),
                    KineticBridgeRenderer::renderSettingCorners);
            float scale = Math.min(1 / 96f, .24f / Math.max(1, mc.font.width(value)));
            pose.scale(scale, -scale, scale);
            mc.font.prepareText(net.minecraft.network.chat.Component.literal(value).getVisualOrderText(),
                    -mc.font.width(value) / 2f, -mc.font.lineHeight / 2f,
                    0xFFFFFFFF, false, false, 0).visit(new Font.GlyphVisitor() {
                public void acceptRenderable(TextRenderable renderable) {
                    collector.submitCustomGeometry(pose, renderable.renderType(Font.DisplayMode.NORMAL, true),
                            (matrix, consumer) -> renderable.render(matrix.pose(), consumer,
                                    LightCoordsUtil.pack(15, 15), false));
                }
            });
            pose.popPose();
        }
    }

    private static void renderSettingCorners(PoseStack.Pose pose, VertexConsumer consumer) {
        // Keep the five-voxel frame size; its anchor supplies the casing-trim clearance.
        float half = 5 / 32f;
        float length = 1 / 16f;
        float thickness = 1.5f / 128f;
        for (int xSign : new int[] {-1, 1}) {
            for (int ySign : new int[] {-1, 1}) {
                float x0 = xSign < 0 ? -half : half - length;
                float y0 = ySign < 0 ? -half : half - thickness;
                settingQuad(pose, consumer, x0, y0, x0 + length, y0 + thickness);
                x0 = xSign < 0 ? -half : half - thickness;
                y0 = ySign < 0 ? -half : half - length;
                settingQuad(pose, consumer, x0, y0, x0 + thickness, y0 + length);
            }
        }
    }

    private static void settingQuad(PoseStack.Pose pose, VertexConsumer consumer,
                                    float x0, float y0, float x1, float y1) {
        consumer.addVertex(pose, x0, y0, 0).setColor(0xFFFFFFFF);
        consumer.addVertex(pose, x1, y0, 0).setColor(0xFFFFFFFF);
        consumer.addVertex(pose, x1, y1, 0).setColor(0xFFFFFFFF);
        consumer.addVertex(pose, x0, y1, 0).setColor(0xFFFFFFFF);
        consumer.addVertex(pose, x0, y1, 0).setColor(0xFFFFFFFF);
        consumer.addVertex(pose, x1, y1, 0).setColor(0xFFFFFFFF);
        consumer.addVertex(pose, x1, y0, 0).setColor(0xFFFFFFFF);
        consumer.addVertex(pose, x0, y0, 0).setColor(0xFFFFFFFF);
    }

    @Override
    protected void renderSafe(KineticBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        // Rendering uses extracted state and SubmitNodeCollector in 26.2.
    }

    private static class BridgeRenderState extends BlockEntityRenderState {
        KineticBlockEntity blockEntity;
        boolean visible;
        Direction facing = Direction.SOUTH;
        float angle;
        int shaftLight;
        int couplingLight;
    }
}
