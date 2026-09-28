package com.hlysine.create_connected;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public final class ConnectedDirections {
    private ConnectedDirections() {}

    /** Exact unit-vector lookup, preserving the removed Direction.fromDelta contract. */
    @Nullable
    public static Direction fromDelta(int x, int y, int z) {
        for (Direction direction : Direction.values()) {
            if (direction.getStepX() == x && direction.getStepY() == y && direction.getStepZ() == z)
                return direction;
        }
        return null;
    }
}
