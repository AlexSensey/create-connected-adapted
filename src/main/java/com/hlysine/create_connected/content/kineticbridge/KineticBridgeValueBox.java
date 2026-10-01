package com.hlysine.create_connected.content.kineticbridge;

import com.hlysine.create_connected.content.kineticbattery.KineticBatteryValueBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Keep the label centered, but allow holding use anywhere on a lateral face. */
public class KineticBridgeValueBox extends KineticBatteryValueBox {
    public KineticBridgeValueBox() {
        super(8);
    }

    @Override
    public boolean testHit(LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
        return isSideActive(state, getSide())
                && localHit.x >= -1e-6 && localHit.x <= 1 + 1e-6
                && localHit.y >= -1e-6 && localHit.y <= 1 + 1e-6
                && localHit.z >= -1e-6 && localHit.z <= 1 + 1e-6;
    }
}
