package com.hlysine.create_connected.content.copycat.slab;

import com.hlysine.create_connected.content.copycat.ConnectedCopycatModel;
import com.hlysine.create_connected.content.copycat.CopycatQuadGeometry;
import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


import java.util.ArrayList;
import java.util.List;

public class CopycatSlabModel extends ConnectedCopycatModel {

    protected static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);

    public CopycatSlabModel(BlockStateModel originalModel) {
        super(originalModel);
    }

    @Override
    protected List<BakedQuad> getCroppedQuads(BlockState state, List<BakedQuad> templateQuads) {
        Direction facing = state.getOptionalValue(CopycatSlabBlock.SLAB_TYPE).isPresent() ? CopycatSlabBlock.getApparentDirection(state) : Direction.UP;


        List<BakedQuad> quads = new ArrayList<>();
        boolean isDouble = state.getOptionalValue(CopycatSlabBlock.SLAB_TYPE).orElse(SlabType.BOTTOM) == SlabType.DOUBLE;

        // 2 pieces
        for (boolean front : Iterate.trueAndFalse) {
            assemblePiece(facing, templateQuads, quads, front, false, isDouble);
        }

        // 2 more pieces for double slabs
        if (isDouble) {
            for (boolean front : Iterate.trueAndFalse) {
                assemblePiece(facing, templateQuads, quads, front, true, isDouble);
            }
        }

        return quads;
    }

    private static void assemblePiece(Direction facing, List<BakedQuad> templateQuads, List<BakedQuad> quads, boolean front, boolean topSlab, boolean isDouble) {
        int size = templateQuads.size();
        Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        Vec3 normalScaled12 = normal.scale(12 / 16f);
        Vec3 normalScaledN8 = topSlab ? normal.scale((front ? 0 : -8) / 16f) : normal.scale((front ? 8 : 0) / 16f);
        float contract = 12;
        AABB bb = CUBE_AABB.contract(normal.x * contract / 16, normal.y * contract / 16, normal.z * contract / 16);
        if (!front)
            bb = bb.move(normalScaled12);

        for (int i = 0; i < size; i++) {
            BakedQuad quad = templateQuads.get(i);
            Direction direction = quad.direction();

            if (front && direction == facing)
                continue;
            if (!front && direction == facing.getOpposite())
                continue;
            if (isDouble && topSlab && direction == facing)
                continue;
            if (isDouble && !topSlab && direction == facing.getOpposite())
                continue;

            quads.add(CopycatQuadGeometry.cropAndMove(quad, bb, normalScaledN8));
        }
    }
}
