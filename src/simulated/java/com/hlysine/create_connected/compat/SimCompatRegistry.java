package com.hlysine.create_connected.compat;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.registries.PreciseItemUseOverrides;
import com.hlysine.create_connected.content.linkedtransmitter.*;
import com.hlysine.create_connected.datagen.CCLinkedModelGen;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.world.level.block.Blocks;

public class SimCompatRegistry {
    private static final CreateRegistrate REGISTRATE = CreateConnected.getRegistrate();

    public static final BlockEntry<LinkedThrottleLeverBlock> LINKED_THROTTLE_LEVER = REGISTRATE
            .block("linked_throttle_lever", properties -> new LinkedThrottleLeverBlock(properties, SimBlocks.THROTTLE_LEVER))
            .initialProperties(() -> Blocks.LEVER)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

            .transform(LinkedTransmitterItem.register())
            .onRegister(PreciseItemUseOverrides::addBlock)
            .onRegister(block -> CCLinkedModelGen.register(block,
                    Mods.SIMULATED.rl("block/throttle_lever/block"), null,
                    CCLinkedModelGen.Mode.ANALOG))
            .asOptional()
            .register();

    public static final BlockEntityEntry<LinkedThrottleLeverBlockEntity> LINKED_THROTTLE_LEVER_ENTITY = REGISTRATE
            .blockEntity("linked_throttle_lever", LinkedThrottleLeverBlockEntity::new)
            .validBlocks(SimCompatRegistry.LINKED_THROTTLE_LEVER)
            .register();

    public static void register() {
    }
}
