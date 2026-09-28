package com.hlysine.create_connected.registries;

import com.simibubi.create.foundation.block.ItemUseOverrides;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public class PreciseItemUseOverrides {

    public static final Set<Identifier> OVERRIDES = new HashSet<>();

    public static void addBlock(Block block) {
        OVERRIDES.add(RegisteredObjectsHelper.getKeyOrThrow(block));
        ItemUseOverrides.addBlock(block);
    }
}
