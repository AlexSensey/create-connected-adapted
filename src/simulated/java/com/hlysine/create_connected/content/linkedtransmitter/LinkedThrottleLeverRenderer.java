package com.hlysine.create_connected.content.linkedtransmitter;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.compat.Mods;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.api.theme.Color;
import net.createmod.catnip.api.math.AngleHelper;
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
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

public class LinkedThrottleLeverRenderer extends SafeBlockEntityRenderer<LinkedThrottleLeverBlockEntity> {
    private static final StandaloneModelKey<BlockStateModelPart> HANDLE = key("handle");
    private static final StandaloneModelKey<BlockStateModelPart> BUTTON = key("button");
    private static final StandaloneModelKey<BlockStateModelPart> DIODE = key("diode");

    public LinkedThrottleLeverRenderer(BlockEntityRendererProvider.Context context) {}

    private static StandaloneModelKey<BlockStateModelPart> key(String part) {
        return new StandaloneModelKey<>(() -> CreateConnected.MODID + ":linked_throttle_lever/" + part);
    }

    public static void registerModels(ModelEvent.RegisterStandalone event) {
        if (!Mods.SIMULATED.isLoaded())
            return;
        event.register(HANDLE, SimpleUnbakedStandaloneModel.simpleModelWrapper(Mods.SIMULATED.rl("block/throttle_lever/handle")));
        event.register(BUTTON, SimpleUnbakedStandaloneModel.simpleModelWrapper(Mods.SIMULATED.rl("block/throttle_lever/button")));
        event.register(DIODE, SimpleUnbakedStandaloneModel.simpleModelWrapper(Mods.SIMULATED.rl("block/throttle_lever/diode")));
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new ThrottleRenderState();
    }

    @Override
    public void extractRenderState(LinkedThrottleLeverBlockEntity be, BlockEntityRenderState state, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (!(state instanceof ThrottleRenderState throttle))
            return;
        throttle.visible = false;
        throttle.first = ItemStack.EMPTY;
        throttle.second = ItemStack.EMPTY;
        throttle.outline = null;
        if (isInvalid(be) || be.isRemoved())
            return;
        throttle.visible = true;
        var blockState = be.getBlockState();
        throttle.face = blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
        throttle.facing = blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
        throttle.handleAngle = handleAngle(be.getHandleRenderValue(partialTicks), throttle.face);
        throttle.buttonAngle = (float) Math.toRadians(be.getButtonRenderValue(partialTicks) * -7);
        throttle.color = Color.mixColors(0xff560101, 0xffcd0000, Math.max(0, be.getState() / 15f));
        if (!be.isVirtual() && Minecraft.getInstance().hitResult instanceof BlockHitResult hit
                && hit.getBlockPos().equals(be.getBlockPos()))
            throttle.outline = be.getHandleOutline();
        float distance = AllConfigs.client().filterItemRenderDistance.getF();
        if (!be.isVirtual() && cameraPos.distanceToSqr(Vec3.atCenterOf(be.getBlockPos())) > distance * distance)
            return;
        LinkBehaviour link = be.getBehaviour(LinkBehaviour.TYPE);
        if (link == null)
            return;
        var frequencies = link.getNetworkKey();
        for (boolean first : new boolean[] { true, false }) {
            var slot = new LinkedTransmitterFrequencySlot(first);
            if (!slot.shouldRender(be.getLevel(), be.getBlockPos(), blockState))
                continue;
            PoseStack transform = new PoseStack();
            slot.transform(be.getLevel(), be.getBlockPos(), blockState, transform);
            if (first) {
                throttle.first = frequencies.get(true).getStack().copy();
                throttle.firstTransform.set(transform.last().pose());
            } else {
                throttle.second = frequencies.get(false).getStack().copy();
                throttle.secondTransform.set(transform.last().pose());
            }
        }
    }

    static float handleAngle(float value, AttachFace face) {
        float angle = (float) Math.toRadians(value / 15 * 80 - 40);
        return face == AttachFace.WALL ? -angle : angle;
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof ThrottleRenderState throttle) || !throttle.visible)
            return;
        var models = Minecraft.getInstance().getModelManager();
        ms.pushPose();
        orient(ms, throttle.face, throttle.facing);
        submitPart(models.getStandaloneModel(DIODE), throttle.color, state.lightCoords, ms, collector);
        ms.pushPose();
        tiltHandle(ms, throttle.handleAngle);
        if (throttle.face == AttachFace.WALL)
            rotateCentered(ms, new Quaternionf().rotationY((float) Math.PI));
        submitPart(models.getStandaloneModel(HANDLE), null, state.lightCoords, ms, collector);
        ms.translate(0, 14 / 16f, 8 / 16f);
        ms.mulPose(new Quaternionf().rotationX(throttle.buttonAngle));
        ms.translate(0, -14 / 16f, -8 / 16f);
        submitPart(models.getStandaloneModel(BUTTON), null, state.lightCoords, ms, collector);
        ms.popPose();
        if (throttle.outline != null) {
            ms.pushPose();
            tiltHandle(ms, throttle.handleAngle);
            collector.submitShapeOutline(ms, throttle.outline, RenderTypes.lines(), 0x66000000, 1, false);
            ms.popPose();
        }
        ms.popPose();
        submitFrequency(throttle.first, throttle.firstTransform, state.lightCoords, ms, collector);
        submitFrequency(throttle.second, throttle.secondTransform, state.lightCoords, ms, collector);
    }

    private static void orient(PoseStack ms, AttachFace face, Direction facing) {
        rotateCentered(ms, new Quaternionf().rotationY((float) Math.toRadians(AngleHelper.horizontalAngle(facing))));
        float x = switch (face) { case FLOOR -> 0; case WALL -> 90; case CEILING -> 180; };
        rotateCentered(ms, new Quaternionf().rotationX((float) Math.toRadians(x)));
        if (face == AttachFace.CEILING)
            rotateCentered(ms, new Quaternionf().rotationY((float) Math.PI));
    }

    private static void rotateCentered(PoseStack ms, Quaternionf rotation) {
        ms.translate(.5, .5, .5);
        ms.mulPose(rotation);
        ms.translate(-.5, -.5, -.5);
    }

    private static void tiltHandle(PoseStack ms, float angle) {
        ms.translate(.5, 3 / 16f, .5);
        ms.mulPose(new Quaternionf().rotationX(angle));
        ms.translate(-.5, -3 / 16f, -.5);
    }

    private static void submitPart(BlockStateModelPart part, Integer color, int light,
                                   PoseStack ms, SubmitNodeCollector collector) {
        if (part == null)
            return;
        collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(),
                List.of(color == null ? part : new TintedPart(part)),
                color == null ? BlockModelRenderState.EMPTY_TINTS : new int[] { color },
                light, OverlayTexture.NO_OVERLAY, 0);
    }

    private static void submitFrequency(ItemStack stack, Matrix4f transform, int light,
                                        PoseStack ms, SubmitNodeCollector collector) {
        if (stack.isEmpty())
            return;
        ms.pushPose();
        ms.mulPose(transform);
        ValueBoxRenderer.submitItemIntoValueBox(stack, ms, collector, light);
        ms.popPose();
    }

    // Color every diode face, as the author's old buffer did for untinted faces too.
    private record TintedPart(BlockStateModelPart delegate) implements BlockStateModelPart {
        @Override public List<BakedQuad> getQuads(Direction side) {
            return delegate.getQuads(side).stream().map(quad -> {
                var m = quad.materialInfo();
                var tinted = new BakedQuad.MaterialInfo(m.sprite(), m.layer(), m.itemRenderType(), 0,
                        m.shade(), m.lightEmission(), m.ambientOcclusion());
                return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                        quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.direction(),
                        tinted, quad.bakedNormals(), quad.bakedColors());
            }).toList();
        }
        @Override public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
        @Override public Material.Baked particleMaterial() { return delegate.particleMaterial(); }
        @Override public int materialFlags() { return delegate.materialFlags(); }
        @Override public net.minecraft.util.TriState ambientOcclusion() { return delegate.ambientOcclusion(); }
    }

    @Override
    protected void renderSafe(LinkedThrottleLeverBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Rendering uses extracted state and SubmitNodeCollector in 26.2.
    }

    private static class ThrottleRenderState extends BlockEntityRenderState {
        boolean visible;
        AttachFace face = AttachFace.FLOOR;
        Direction facing = Direction.NORTH;
        float handleAngle;
        float buttonAngle;
        int color;
        VoxelShape outline;
        ItemStack first = ItemStack.EMPTY;
        ItemStack second = ItemStack.EMPTY;
        final Matrix4f firstTransform = new Matrix4f();
        final Matrix4f secondTransform = new Matrix4f();
    }
}
