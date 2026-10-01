package com.hlysine.create_connected.content.kineticbridge;

import com.hlysine.create_connected.ConnectedLang;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/** The output half edits the input half's setting; it owns no separate saved value. */
public class KineticBridgeDestinationSettings extends StressImpactScrollValueBehaviour {
    private final KineticBridgeDestinationBlockEntity destination;

    public KineticBridgeDestinationSettings(KineticBridgeDestinationBlockEntity destination) {
        super(ConnectedLang.translateDirect("kinetic_bridge.stress_impact"), destination,
                new KineticBridgeValueBox());
        this.destination = destination;
    }

    private ScrollValueBehaviour target() {
        KineticBridgeBlockEntity source = destination.getSource();
        return source == null ? null : source.stressMultiplier;
    }

    @Override
    public boolean isActive() {
        return target() != null;
    }

    @Override
    public int getValue() {
        ScrollValueBehaviour target = target();
        return target == null ? 40 : target.getValue();
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, getValue());
    }

    @Override
    public String formatValue() {
        ScrollValueBehaviour target = target();
        return target == null ? "" : target.formatValue();
    }

    @Override
    public void setValue(int value) {
        ScrollValueBehaviour target = target();
        if (target != null)
            target.setValue(value);
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        // Only the source stores and synchronizes ScrollValue.
    }

    @Override
    public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        // Never create an independent ScrollValue on the destination.
    }
}
