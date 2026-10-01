package com.hlysine.create_connected.content.kineticbattery;


import com.hlysine.create_connected.registries.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Deprecated(forRemoval = true, since = "1.3.0")
public class ChargedKineticBatteryItem extends BlockItem implements com.simibubi.create.foundation.item.CustomBlockEntityTagItem {

    public ChargedKineticBatteryItem(Properties builder) {
        super(CCBlocks.KINETIC_BATTERY.get(), builder.overrideDescription("item.create_connected.charged_kinetic_battery"));
    }



    @Override
    public void registerBlocks(@NotNull Map<Block, Item> map, @NotNull Item self) {
    }

    @Override
    public boolean updateCustomBlockEntityTag(@NotNull BlockPos pos, @NotNull Level world, Player player, @NotNull ItemStack stack, @NotNull BlockState state) {
        boolean ret = BlockItem.updateCustomBlockEntityTag(world, player, pos, stack);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof KineticBatteryBlockEntity batteryBE))
            return ret;
        batteryBE.setBatteryLevel(KineticBatteryBlockEntity.getMaxBatteryLevel());
        return true;
    }

}

