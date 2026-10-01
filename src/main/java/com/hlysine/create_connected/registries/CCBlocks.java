package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.datagen.CCSimpleModelGen;
import com.hlysine.create_connected.datagen.CCLinkedModelGen;
import com.hlysine.create_connected.compat.DyeDepotCompat;
import com.hlysine.create_connected.compat.Mods;
import com.hlysine.create_connected.compat.SimulatedCompat;
import com.hlysine.create_connected.config.CStress;
import com.hlysine.create_connected.config.FeatureCategory;
import com.hlysine.create_connected.config.FeatureToggle;
import com.hlysine.create_connected.content.WrenchableBlock;
import com.hlysine.create_connected.content.brake.BrakeBlock;
import com.hlysine.create_connected.content.brasschute.BrassChuteBlock;
import com.hlysine.create_connected.content.brassgearbox.BrassGearboxBlock;
import com.hlysine.create_connected.content.centrifugalclutch.CentrifugalClutchBlock;
import com.hlysine.create_connected.content.chaincogwheel.ChainCogwheelBlock;
import com.hlysine.create_connected.content.copycat.beam.CopycatBeamBlock;
import com.hlysine.create_connected.content.copycat.block.CopycatBlockBlock;
import com.hlysine.create_connected.content.copycat.board.CopycatBoardBlock;
import com.hlysine.create_connected.content.copycat.fence.CopycatFenceBlock;
import com.hlysine.create_connected.content.copycat.fence.WrappedFenceBlock;
import com.hlysine.create_connected.content.copycat.fencegate.CopycatFenceGateBlock;
import com.hlysine.create_connected.content.copycat.fencegate.WrappedFenceGateBlock;
import com.hlysine.create_connected.content.copycat.slab.CopycatSlabBlock;
import com.hlysine.create_connected.content.copycat.stairs.CopycatStairsBlock;
import com.hlysine.create_connected.content.copycat.stairs.WrappedStairsBlock;
import com.hlysine.create_connected.content.copycat.verticalstep.CopycatVerticalStepBlock;
import com.hlysine.create_connected.content.copycat.wall.CopycatWallBlock;
import com.hlysine.create_connected.content.copycat.wall.WrappedWallBlock;
import com.hlysine.create_connected.content.crankwheel.CrankWheelBlock;
import com.hlysine.create_connected.content.crankwheel.CrankWheelItem;
import com.hlysine.create_connected.content.crossconnector.CrossConnectorBlock;
import com.hlysine.create_connected.content.crossconnector.EncasedCrossConnectorBlock;
import com.hlysine.create_connected.content.dashboard.DashboardBlock;
import com.hlysine.create_connected.content.fancatalyst.FanCatalystRotatingHeadBlock;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselBlock;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselGenerator;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselItem;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselCTBehaviour;
import com.hlysine.create_connected.content.freewheelclutch.FreewheelClutchBlock;
import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlock;
import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortGenerator;
import com.hlysine.create_connected.content.inventorybridge.InventoryBridgeBlock;
import com.hlysine.create_connected.content.invertedclutch.InvertedClutchBlock;
import com.hlysine.create_connected.content.invertedgearshift.InvertedGearshiftBlock;
import com.hlysine.create_connected.content.itemsilo.ItemSiloBlock;
import com.hlysine.create_connected.content.itemsilo.ItemSiloCTBehaviour;
import com.hlysine.create_connected.content.itemsilo.ItemSiloItem;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlock;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlockItem;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryGenerator;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeBlock;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeBlockItem;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeDestinationBlock;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedAnalogLeverBlock;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedButtonBlock;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedLeverBlock;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedTransmitterItem;
import com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlock;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxBlock;
import com.hlysine.create_connected.content.sequencedpulsegenerator.SequencedPulseGeneratorBlock;
import com.hlysine.create_connected.content.shearpin.ShearPinBlock;
import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxBlock;
import com.hlysine.create_connected.datagen.CCBlockStateGen;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.fluids.tank.FluidTankMovementBehavior;
import com.simibubi.create.content.logistics.chute.ChuteItem;
import com.simibubi.create.foundation.block.render.ReducedDestroyEffects;
import com.simibubi.create.foundation.data.*;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.display.DisplayTarget.displayTarget;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType.mountedFluidStorage;
import static com.simibubi.create.foundation.data.AssetLookup.partialBaseModel;
import static com.simibubi.create.foundation.data.BlockStateGen.axisBlock;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

@SuppressWarnings("removal")
// Minecraft 26.2 bakes chunk layers from each sprite's transparency (MaterialInfo.of).
// Do not call legacy Registrate.addLayer: it still links the removed RenderType API.
public class CCBlocks {
    private static final CreateRegistrate REGISTRATE = CreateConnected.getRegistrate();

    public static final BlockEntry<ChainCogwheelBlock> ENCASED_CHAIN_COGWHEEL =
            REGISTRATE.block("encased_chain_cogwheel", ChainCogwheelBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

                    .transform(CStress.setNoImpact())
                    .transform(FeatureToggle.register(FeatureCategory.KINETIC))
                    .transform(axeOrPickaxe())
                    .onRegister(CCBlockStateGen::chainCogwheel)
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<CrankWheelBlock.Small> CRANK_WHEEL = REGISTRATE.block("crank_wheel", CrankWheelBlock.Small::new)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(axeOrPickaxe())
            .onRegister(CCBlockStateGen::crankWheel)
            .transform(CStress.setCapacity(8.0))
            .onRegister(BlockStressValues.setGeneratorSpeed(32))
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .tag(AllTags.AllBlockTags.BRITTLE.tag)
            .item(CrankWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<CrankWheelBlock.Large> LARGE_CRANK_WHEEL = REGISTRATE.block("large_crank_wheel", CrankWheelBlock.Large::new)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(axeOrPickaxe())
            .onRegister(CCBlockStateGen::crankWheel)
            .transform(CStress.setCapacity(8.0))
            .onRegister(BlockStressValues.setGeneratorSpeed(32))
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .tag(AllTags.AllBlockTags.BRITTLE.tag)
            .item(CrankWheelItem::new)
            .build()
            .register();

    public static final BlockEntry<ParallelGearboxBlock> PARALLEL_GEARBOX = REGISTRATE.block("parallel_gearbox", ParallelGearboxBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING)))
            .onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.ANDESITE_CASING,
                    (s, f) -> f.getAxis() == s.getValue(ParallelGearboxBlock.AXIS))))
            .onRegister(CCBlockStateGen::axisBlock)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<SixWayGearboxBlock> SIX_WAY_GEARBOX = REGISTRATE.block("six_way_gearbox", SixWayGearboxBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .lang("6-way Gearbox")
            .onRegister(CCBlockStateGen::rotatedAxis)
            .item()
            .build()
            .register();

    public static final BlockEntry<CrossConnectorBlock> CROSS_CONNECTOR = REGISTRATE.block("cross_connector", CrossConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(CCBlockStateGen::rotatedAxis)
            .item()
            .build()
            .register();


    public static final BlockEntry<EncasedCrossConnectorBlock> ANDESITE_ENCASED_CROSS_CONNECTOR =
            REGISTRATE.block("andesite_encased_cross_connector", p -> new EncasedCrossConnectorBlock(p, AllBlocks.ANDESITE_CASING::get))
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(CCBuilderTransformers.encasedCrossConnector("andesite", () -> AllSpriteShifts.ANDESITE_CASING))
                    .transform(EncasingRegistry.addVariantTo(CCBlocks.CROSS_CONNECTOR))
                    .transform(FeatureToggle.registerDependent(CCBlocks.CROSS_CONNECTOR, FeatureCategory.KINETIC))
                    .transform(axeOrPickaxe())
                    .register();

    public static final BlockEntry<EncasedCrossConnectorBlock> BRASS_ENCASED_CROSS_CONNECTOR =
            REGISTRATE.block("brass_encased_cross_connector", p -> new EncasedCrossConnectorBlock(p, AllBlocks.BRASS_CASING::get))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .transform(CCBuilderTransformers.encasedCrossConnector("brass", () -> AllSpriteShifts.BRASS_CASING))
                    .transform(EncasingRegistry.addVariantTo(CCBlocks.CROSS_CONNECTOR))
                    .transform(FeatureToggle.registerDependent(CCBlocks.CROSS_CONNECTOR, FeatureCategory.KINETIC))
                    .transform(axeOrPickaxe())
                    .register();


    public static final BlockEntry<OverstressClutchBlock> OVERSTRESS_CLUTCH = REGISTRATE.block("overstress_clutch", OverstressClutchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(CCBlockStateGen::overstressClutch)
            .item()
            .build()
            .register();


    public static final BlockEntry<ShearPinBlock> SHEAR_PIN = REGISTRATE.block("shear_pin", ShearPinBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL).forceSolidOn())
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(pickaxeOnly())
            .onRegister(CCBlockStateGen::shearPin)
            .simpleItem()
            .register();

    public static final BlockEntry<InvertedClutchBlock> INVERTED_CLUTCH = REGISTRATE.block("inverted_clutch", InvertedClutchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(CCBlockStateGen::poweredAxis)
            .item()
            .build()
            .register();

    public static final BlockEntry<InvertedGearshiftBlock> INVERTED_GEARSHIFT = REGISTRATE.block("inverted_gearshift", InvertedGearshiftBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(CCBlockStateGen::poweredAxis)
            .item()
            .build()
            .register();


    public static final BlockEntry<CentrifugalClutchBlock> CENTRIFUGAL_CLUTCH = REGISTRATE.block("centrifugal_clutch", CentrifugalClutchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(block -> CCSimpleModelGen.registerDirectional(block, state -> CreateConnected.asResource(
                    "block/centrifugal_clutch/block" + (state.getValue(CentrifugalClutchBlock.UNCOUPLED) ? "_uncoupled" : ""))))
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();


    public static final BlockEntry<FreewheelClutchBlock> FREEWHEEL_CLUTCH = REGISTRATE.block("freewheel_clutch", FreewheelClutchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(block -> CCSimpleModelGen.registerDirectional(block, state -> CreateConnected.asResource(
                    "block/freewheel_clutch/block" + (state.getValue(FreewheelClutchBlock.UNCOUPLED) ? "_uncoupled" : ""))))
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();


    public static final BlockEntry<KineticBridgeBlock> KINETIC_BRIDGE = REGISTRATE.block("kinetic_bridge", KineticBridgeBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN))

            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(b -> BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
                if (!(state.getBlock() instanceof KineticBridgeBlock))
                    return BlockMovementChecks.CheckResult.PASS;
                if (state.getValue(KineticBridgeBlock.FACING) != direction)
                    return BlockMovementChecks.CheckResult.PASS;
                return BlockMovementChecks.CheckResult.SUCCESS;
            }))
            .onRegister(b -> BlockMovementChecks.registerBrittleCheck(state -> {
                if (!(state.getBlock() instanceof KineticBridgeBlock))
                    return BlockMovementChecks.CheckResult.PASS;
                return BlockMovementChecks.CheckResult.SUCCESS;
            }))
            .onRegister(block -> CCSimpleModelGen.registerDirectional(block, state -> CreateConnected.asResource("block/kinetic_bridge/block")))
            .item(KineticBridgeBlockItem::new)
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();


    public static final BlockEntry<KineticBridgeDestinationBlock> KINETIC_BRIDGE_DESTINATION = REGISTRATE.block("kinetic_bridge_destination", KineticBridgeDestinationBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN))

            .transform(FeatureToggle.registerDependent(CCBlocks.KINETIC_BRIDGE, FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(b -> BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
                if (!(state.getBlock() instanceof KineticBridgeDestinationBlock))
                    return BlockMovementChecks.CheckResult.PASS;
                if (state.getValue(KineticBridgeDestinationBlock.FACING).getOpposite() != direction)
                    return BlockMovementChecks.CheckResult.PASS;
                return BlockMovementChecks.CheckResult.SUCCESS;
            }))
            .onRegister(block -> CCSimpleModelGen.registerDirectional(block, state -> CreateConnected.asResource("block/kinetic_bridge/block_destination")))
            .lang("Kinetic Bridge")
            .register();

    public static final BlockEntry<BrassGearboxBlock> BRASS_GEARBOX = REGISTRATE.block("brass_gearbox", BrassGearboxBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.BRASS_CASING)))
            .onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.BRASS_CASING,
                    (s, f) -> f.getAxis() == s.getValue(BrassGearboxBlock.AXIS))))
            .onRegister(CCBlockStateGen::brassGearbox)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<BrakeBlock> BRAKE = REGISTRATE.block("brake", BrakeBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))

            .transform(CStress.setNoImpact()) // active stress is a separate config
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(CCBlockStateGen::poweredAxis)
            .item()
            .build()
            .register();

    public static final BlockEntry<KineticBatteryBlock> KINETIC_BATTERY = REGISTRATE.block("kinetic_battery", KineticBatteryBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN))

            .transform(CStress.setCapacity(32.0))
            .transform(CStress.setImpact(64.0))
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(DisplaySource.displaySource(CCDisplaySources.KINETIC_BATTERY))
            .transform(axeOrPickaxe())
            .onRegister(new KineticBatteryGenerator()::register)
            .loot((lt, block) -> {
                LootTable.Builder builder = LootTable.lootTable();
                LootItemCondition.Builder survivesExplosion = ExplosionCondition.survivesExplosion();
                lt.add(block, builder.withPool(LootPool.lootPool()
                        .when(survivesExplosion)
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(CCBlocks.KINETIC_BATTERY.asItem())
                                .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY)
                                        .include(CCDataComponents.KINETIC_BATTERY_CHARGE)))));
            })
            .item(KineticBatteryBlockItem::new)
            .properties(p -> p.component(CCDataComponents.KINETIC_BATTERY_CHARGE, 0.0))
            .build()
            .register();

    public static final BlockEntry<SequencedPulseGeneratorBlock> SEQUENCED_PULSE_GENERATOR =
            REGISTRATE.block("sequenced_pulse_generator", SequencedPulseGeneratorBlock::new)
                    .initialProperties(() -> Blocks.REPEATER)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .onRegister(CCBlockStateGen::sequencedPulseGenerator)
                    .transform(FeatureToggle.register(FeatureCategory.REDSTONE))

                    .item()
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/sequenced_pulse_generator")))
                    .build()
                    .register();

    public static final Map<BlockSetType, BlockEntry<LinkedButtonBlock>> LINKED_BUTTONS = new HashMap<>();

    static {
        BlockSetType.values().forEach(type -> {
            Block button = RegisteredObjectsHelper.getBlock(Identifier.parse(type.name() + "_button")).orElse(null);
            if (button == null) return;
            if (!(button instanceof ButtonBlock buttonBlock))
                return;
            String namePath = type.name().contains(":") ? type.name().replace(':', '_') : type.name();
            LINKED_BUTTONS.put(type, REGISTRATE
                    .block("linked_" + namePath + "_button", properties -> new LinkedButtonBlock(properties, buttonBlock))
                    .initialProperties(() -> buttonBlock)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

                    .transform(LinkedTransmitterItem.register())
                    .onRegister(PreciseItemUseOverrides::addBlock)
                    .onRegister(block -> CCLinkedModelGen.register(block,
                            Identifier.withDefaultNamespace("block/" + namePath + "_button"),
                            Identifier.withDefaultNamespace("block/" + namePath + "_button_pressed"), CCLinkedModelGen.Mode.BUTTON))
                    .register());
        });
    }

    public static final BlockEntry<LinkedLeverBlock> LINKED_LEVER = REGISTRATE
            .block("linked_lever", properties -> new LinkedLeverBlock(properties, (LeverBlock) Blocks.LEVER))
            .initialProperties(() -> Blocks.LEVER)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

            .transform(LinkedTransmitterItem.register())
            .onRegister(PreciseItemUseOverrides::addBlock)
            .onRegister(block -> CCLinkedModelGen.register(block,
                    Identifier.withDefaultNamespace("block/lever"),
                    Identifier.withDefaultNamespace("block/lever_on"), CCLinkedModelGen.Mode.LEVER))
            .register();

    public static final BlockEntry<LinkedAnalogLeverBlock> LINKED_ANALOG_LEVER = REGISTRATE
            .block("linked_analog_lever", properties -> new LinkedAnalogLeverBlock(properties, AllBlocks.ANALOG_LEVER))
            .initialProperties(() -> Blocks.LEVER)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)

            .transform(LinkedTransmitterItem.register())
            .onRegister(PreciseItemUseOverrides::addBlock)
            .onRegister(block -> CCLinkedModelGen.register(block,
                    Create.asResource("block/analog_lever/block"), null, CCLinkedModelGen.Mode.ANALOG))
            .register();

    public static final BlockEntry<WrenchableBlock> EMPTY_FAN_CATALYST = REGISTRATE.block("empty_fan_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_BLASTING_CATALYST = REGISTRATE.block("fan_blasting_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .tag(AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SMOKING_CATALYST = REGISTRATE.block("fan_smoking_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .tag(AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SPLASHING_CATALYST = REGISTRATE.block("fan_splashing_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .onRegister(CCSimpleModelGen::register)
            .lang("Fan Washing Catalyst")
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .tag(AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_SPLASHING.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_HAUNTING_CATALYST = REGISTRATE.block("fan_haunting_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 5)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .tag(AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_HAUNTING.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_FREEZING_CATALYST = REGISTRATE.block("fan_freezing_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(() -> Mods.GARNISHED.isLoaded() || Mods.DREAMS_DESIRES.isLoaded() || Mods.DRAGONS_PLUS.isLoaded()))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SEETHING_CATALYST = REGISTRATE.block("fan_seething_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 12)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.DREAMS_DESIRES::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SANDING_CATALYST = REGISTRATE.block("fan_sanding_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(() -> Mods.DREAMS_DESIRES.isLoaded() || Mods.DRAGONS_PLUS.isLoaded()))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_ENRICHED_CATALYST = REGISTRATE.block("fan_enriched_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 13)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.NUCLEAR::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_ENDING_CATALYST_DRAGONS_BREATH = REGISTRATE.block("fan_ending_catalyst_dragons_breath", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 15)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.DRAGONS_PLUS::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .lang("Fan Ending Catalyst with Dragon's Breath")
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .lang("Fan Ending Catalyst with Dragon's Breath")
            .register();

    public static final BlockEntry<FanCatalystRotatingHeadBlock> FAN_ENDING_CATALYST_DRAGON_HEAD = REGISTRATE
            .block("fan_ending_catalyst_dragon_head", properties -> new FanCatalystRotatingHeadBlock(properties, CCBlockEntityTypes.FAN_ENDING_CATALYST_DRAGON_HEAD))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 0)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.DRAGONS_PLUS::isLoaded))
            .onRegister(block -> CCSimpleModelGen.register(block, CreateConnected.asResource("block/empty_fan_catalyst/block")))
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .lang("Fan Ending Catalyst with Dragon Head")
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .lang("Fan Ending Catalyst with Dragon Head")
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_WITHERING_CATALYST = REGISTRATE.block("fan_withering_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 0)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(() -> false)) // No mods support bulk withering in 1.21.1
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_CHOCOLATE_COATING_CATALYST = REGISTRATE.block("fan_chocolate_coating_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_HONEY_COATING_CATALYST = REGISTRATE.block("fan_honey_coating_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<FanCatalystRotatingHeadBlock> FAN_EXPLODING_CATALYST = REGISTRATE
            .block("fan_exploding_catalyst", properties -> new FanCatalystRotatingHeadBlock(properties, CCBlockEntityTypes.FAN_EXPLODING_CATALYST))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))
            .onRegister(block -> CCSimpleModelGen.register(block, CreateConnected.asResource("block/empty_fan_catalyst/block")))
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_RESONANCE_CATALYST = REGISTRATE.block("fan_resonance_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 3)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SCULKING_CATALYST = REGISTRATE.block("fan_sculking_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 4)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_PURIFYING_CATALYST = REGISTRATE.block("fan_purifying_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 14)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_TRANSMUTATION_CATALYST = REGISTRATE.block("fan_transmutation_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.SHIMMER::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_GLOOMING_CATALYST = REGISTRATE.block("fan_glooming_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.SHIMMER::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SOUL_STRIPPING_CATALYST = REGISTRATE.block("fan_soul_stripping_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )

            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.NETHER_INDUSTRY::isLoaded))
            .onRegister(CCSimpleModelGen::register)
            .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
            .item()
            .onRegister(CCSimpleModelGen::registerBlockItem)
            .build()
            .register();

    public static final Map<DyeColor, BlockEntry<WrenchableBlock>> FAN_DYEING_CATALYSTS = new TreeMap<>();

    static {
        for (DyeColor color : DyeColor.values()) {
            String namespace = DyeDepotCompat.getColorNamespace(color);
            boolean isVanilla = namespace.equals(Identifier.DEFAULT_NAMESPACE);
            FAN_DYEING_CATALYSTS.put(color, REGISTRATE.block((isVanilla ? "" : (namespace + "_")) + color.getName() + "_fan_dyeing_catalyst", WrenchableBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p
                            .mapColor(MapColor.TERRACOTTA_YELLOW)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) -> false)
                    )

                    .transform(pickaxeOnly())
                    .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
                    .transform(FeatureToggle.addCondition(() -> (Mods.DRAGONS_PLUS.isLoaded() || Mods.GARNISHED.isLoaded()) && (isVanilla || Mods.DYE_DEPOT.isLoaded())))
                    .onRegister(block -> CCSimpleModelGen.registerContent(block,
                            Identifier.fromNamespaceAndPath(DyeDepotCompat.getColorNamespace(color), "block/" + color.getName() + "_concrete_powder")))
                    .lang(RegistrateLangProvider.toEnglishName(color.getName() + "_fan_dyeing_catalyst"))
                    .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
                    .asOptional()
                    .simpleItem()
                    .register());
        }
    }

    public static final BlockEntry<ItemSiloBlock> ITEM_SILO = REGISTRATE.block("item_silo", ItemSiloBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
                    .explosionResistance(1200))
            .transform(pickaxeOnly())
            .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))
            .onRegister(block -> CCSimpleModelGen.register(block, CreateConnected.asResource("block/item_silo")))
            .onRegister(connectedTextures(ItemSiloCTBehaviour::new))
            .transform(MountedItemStorageType.mountedItemStorage(CCMountedStorageTypes.SILO))
            .onRegister(b -> BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
                if (state.getBlock() instanceof ItemSiloBlock)
                    return BlockMovementChecks.CheckResult.of(ConnectivityHandler.isConnected(world, pos, pos.relative(direction)));
                return BlockMovementChecks.CheckResult.PASS;
            }))
            .item(ItemSiloItem::new)
            .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                    CreateConnected.asResource("block/item_silo")))
            .build()
            .register();

    public static final BlockEntry<FluidVesselBlock> FLUID_VESSEL = REGISTRATE.block("fluid_vessel", FluidVesselBlock::regular)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.noOcclusion().isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))
            .onRegister(new FluidVesselGenerator()::register)
            .onRegister(connectedTextures(() -> new FluidVesselCTBehaviour(AllSpriteShifts.FLUID_TANK,
                    AllSpriteShifts.FLUID_TANK_TOP, AllSpriteShifts.FLUID_TANK_INNER)))
            .onRegister(b -> BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
                if (state.getBlock() instanceof FluidVesselBlock)
                    return BlockMovementChecks.CheckResult.of(ConnectivityHandler.isConnected(world, pos, pos.relative(direction)));
                return BlockMovementChecks.CheckResult.PASS;
            }))
            .transform(displaySource(CCDisplaySources.BOILER_STATUS))
            .transform(mountedFluidStorage(CCMountedStorageTypes.FLUID_VESSEL))
            .onRegister(movementBehaviour(new FluidTankMovementBehavior()))

            .item(FluidVesselItem::new)
            .build()
            .register();

    public static final BlockEntry<FluidVesselBlock> CREATIVE_FLUID_VESSEL =
            REGISTRATE.block("creative_fluid_vessel", FluidVesselBlock::creative)
                    .initialProperties(SharedProperties::copperMetal)
                    .properties(p -> p.noOcclusion().mapColor(MapColor.COLOR_PURPLE))
                    .transform(pickaxeOnly())
                    .transform(FeatureToggle.registerDependent(FLUID_VESSEL))
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .onRegister(new FluidVesselGenerator("creative_")::register)
                    .onRegister(connectedTextures(() -> new FluidVesselCTBehaviour(AllSpriteShifts.CREATIVE_FLUID_TANK,
                            AllSpriteShifts.CREATIVE_CASING, AllSpriteShifts.CREATIVE_CASING)))

                    .item(FluidVesselItem::new)
                    .properties(p -> p.rarity(Rarity.EPIC))
                    .build()
                    .register();

    public static final BlockEntry<InventoryAccessPortBlock> INVENTORY_ACCESS_PORT =
            REGISTRATE.block("inventory_access_port", InventoryAccessPortBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion())
                    .transform(axeOrPickaxe())
                    .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))
                    .onRegister(new InventoryAccessPortGenerator()::register)
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<InventoryBridgeBlock> INVENTORY_BRIDGE =
            REGISTRATE.block("inventory_bridge", InventoryBridgeBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion())
                    .transform(axeOrPickaxe())
                    .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))
                    .onRegister(InventoryAccessPortGenerator::registerBridge)
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<BrassChuteBlock> BRASS_CHUTE = REGISTRATE.block("brass_chute", BrassChuteBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false))
            .transform(pickaxeOnly())
            .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))

            .clientExtension(() -> () -> new ReducedDestroyEffects())
            .onRegister(CCBlockStateGen::brassChute)
            .item(ChuteItem::new)
            .build()
            .register();

    public static final BlockEntry<DashboardBlock> DASHBOARD =
            REGISTRATE.block("dashboard", DashboardBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.PODZOL))

                    .transform(axeOrPickaxe())
                    .transform(FeatureToggle.register(FeatureCategory.KINETIC))
                    .transform(displayTarget(CCDisplayTargets.DASHBOARD))
                    .onRegister(CCBlockStateGen::dashboard)
                    .item()
                    .build()
                    .register();

    public static final BlockEntry<CopycatSlabBlock> COPYCAT_SLAB =
            REGISTRATE.block("copycat_slab", CopycatSlabBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .tag(BlockTags.SLABS)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .loot((lt, block) -> lt.add(block, lt.createSlabItemTable(block)))
                    .item()
                    .tag(CCTags.Items.COPYCAT_SLAB.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/slab")))
                    .build()
                    .register();

    public static final BlockEntry<CopycatBlockBlock> COPYCAT_BLOCK =
            REGISTRATE.block("copycat_block", CopycatBlockBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_BLOCK.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/block")))
                    .build()
                    .register();

    public static final BlockEntry<CopycatBeamBlock> COPYCAT_BEAM =
            REGISTRATE.block("copycat_beam", CopycatBeamBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_BEAM.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/beam")))
                    .build()
                    .register();

    public static final BlockEntry<CopycatVerticalStepBlock> COPYCAT_VERTICAL_STEP =
            REGISTRATE.block("copycat_vertical_step", CopycatVerticalStepBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_VERTICAL_STEP.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/vertical_step")))
                    .build()
                    .register();

    public static final BlockEntry<CopycatStairsBlock> COPYCAT_STAIRS =
            REGISTRATE.block("copycat_stairs", CopycatStairsBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .tag(BlockTags.STAIRS)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_STAIRS.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/stairs")))
                    .build()
                    .register();

    public static final BlockEntry<WrappedStairsBlock> WRAPPED_COPYCAT_STAIRS =
            REGISTRATE.block("wrapped_copycat_stairs", p -> new WrappedStairsBlock(Blocks.STONE.defaultBlockState(), p))
                    .initialProperties(() -> Blocks.STONE_STAIRS)
                    .onRegister(b -> CopycatStairsBlock.stairs = b)
                    .tag(BlockTags.STAIRS)
                    .onRegister(CCSimpleModelGen::registerBarrier)
                    .register();

    public static final BlockEntry<CopycatFenceBlock> COPYCAT_FENCE =
            REGISTRATE.block("copycat_fence", CopycatFenceBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .tag(BlockTags.FENCES, Tags.Blocks.FENCES)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_FENCE.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/fence")))
                    .build()
                    .register();

    public static final BlockEntry<WrappedFenceBlock> WRAPPED_COPYCAT_FENCE =
            REGISTRATE.block("wrapped_copycat_fence", WrappedFenceBlock::new)
                    .initialProperties(() -> Blocks.OAK_FENCE)
                    .onRegister(b -> CopycatFenceBlock.fence = b)
                    .tag(BlockTags.FENCES, Tags.Blocks.FENCES)
                    .onRegister(CCSimpleModelGen::registerBarrier)
                    .register();

    public static final BlockEntry<CopycatWallBlock> COPYCAT_WALL =
            REGISTRATE.block("copycat_wall", CopycatWallBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .properties(p -> p.forceSolidOn())
                    .tag(BlockTags.WALLS)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_WALL.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/wall")))
                    .build()
                    .register();

    public static final BlockEntry<WrappedWallBlock> WRAPPED_COPYCAT_WALL =
            REGISTRATE.block("wrapped_copycat_wall", WrappedWallBlock::new)
                    .initialProperties(() -> Blocks.COBBLESTONE_WALL)
                    .onRegister(b -> CopycatWallBlock.wall = b)
                    .tag(BlockTags.WALLS)
                    .onRegister(CCSimpleModelGen::registerBarrier)
                    .register();

    public static final BlockEntry<CopycatFenceGateBlock> COPYCAT_FENCE_GATE =
            REGISTRATE.block("copycat_fence_gate", CopycatFenceGateBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .properties(p -> p.forceSolidOn())
                    .tag(BlockTags.FENCE_GATES, Tags.Blocks.FENCE_GATES, BlockTags.UNSTABLE_BOTTOM_CENTER, AllTags.AllBlockTags.MOVABLE_EMPTY_COLLIDER.tag)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_FENCE_GATE.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/fence_gate")))
                    .build()
                    .register();

    public static final BlockEntry<WrappedFenceGateBlock> WRAPPED_COPYCAT_FENCE_GATE =
            REGISTRATE.block("wrapped_copycat_fence_gate", p -> new WrappedFenceGateBlock(WoodType.OAK, p))
                    .initialProperties(() -> Blocks.OAK_FENCE_GATE)
                    .onRegister(b -> CopycatFenceGateBlock.fenceGate = b)
                    .tag(BlockTags.FENCE_GATES, Tags.Blocks.FENCE_GATES, BlockTags.UNSTABLE_BOTTOM_CENTER, AllTags.AllBlockTags.MOVABLE_EMPTY_COLLIDER.tag)
                    .onRegister(CCSimpleModelGen::registerBarrier)
                    .register();

    public static final BlockEntry<CopycatBoardBlock> COPYCAT_BOARD =
            REGISTRATE.block("copycat_board", CopycatBoardBlock::new)
                    .transform(BuilderTransformers.copycat())
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .loot((lt, block) -> {
                        LootTable.Builder builder = LootTable.lootTable();
                        for (Direction direction : Iterate.directions) {
                            builder.withPool(
                                    LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1.0F))
                                            .when(ExplosionCondition.survivesExplosion())
                                            .when(LootItemBlockStatePropertyCondition
                                                    .hasBlockStateProperties(block)
                                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CopycatBoardBlock.byDirection(direction), true)))
                                            .add(LootItem.lootTableItem(block))
                            );
                        }
                        lt.add(block, builder);
                    })
                    .item()
                    .tag(CCTags.Items.COPYCAT_BOARD.tag)
                    .onRegister(item -> CCSimpleModelGen.registerStandaloneItem(item,
                            CreateConnected.asResource("block/copycat_base/board")))
                    .build()
                    .register();

    public static void register() {
        SimulatedCompat.register();
    }

    private static Function<BlockState, ModelFile> forBoolean(DataGenContext<?, ?> ctx,
                                                              Function<BlockState, Boolean> condition,
                                                              String key,
                                                              RegistrateBlockstateProvider prov) {
        return state -> condition.apply(state) ? partialBaseModel(ctx, prov, key)
                : partialBaseModel(ctx, prov);
    }
}
