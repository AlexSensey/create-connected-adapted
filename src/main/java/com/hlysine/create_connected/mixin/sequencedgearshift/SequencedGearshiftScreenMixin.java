package com.hlysine.create_connected.mixin.sequencedgearshift;

import com.hlysine.create_connected.registries.CCSequencerInstructions;
import com.simibubi.create.content.kinetics.transmission.sequencer.Instruction;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencedGearshiftScreen;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Vector;

@Mixin(value = SequencedGearshiftScreen.class, remap = false)
public class SequencedGearshiftScreenMixin {
    @Shadow
    private Vector<Instruction> instructions;
    @Shadow
    private Vector<Vector<ScrollInput>> inputs;

    @Inject(
            method = "backgroundFor(Lcom/simibubi/create/content/kinetics/transmission/sequencer/SequencerInstructions;)Lcom/simibubi/create/foundation/gui/AllGuiTextures;",
            at = @At("HEAD"), cancellable = true
    )
    private static void create_connected$backgroundFor(SequencerInstructions instruction,
                                                       CallbackInfoReturnable<AllGuiTextures> cir) {
        if (instruction == CCSequencerInstructions.TURN_AWAIT || instruction == CCSequencerInstructions.TURN_TIME)
            cir.setReturnValue(AllGuiTextures.SEQUENCER_INSTRUCTION);
        else if (instruction == CCSequencerInstructions.LOOP)
            cir.setReturnValue(AllGuiTextures.SEQUENCER_END);
    }

    @Inject(
            method = "updateParamsOfRow(I)V",
            at = @At("RETURN")
    )
    public void updateParamsOfRow(int row, CallbackInfo ci) {
        if (((InstructionAccessor) instructions.get(row)).getInstruction() == CCSequencerInstructions.TURN_TIME) {
            Vector<ScrollInput> rowInputs = inputs.get(row);
            ScrollInput value = rowInputs.get(1);
            value.withStepFunction(context -> {
                int v = context.currentValue;
                if (!context.forward)
                    v--;
                if (v < 20)
                    return context.shift ? 20 : 1;
                return context.shift ? 100 : 20;
            });
        }
    }

    @Inject(
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/transmission/sequencer/SequencedGearshiftScreen;updateParamsOfRow(I)V", shift = At.Shift.AFTER),
            method = "instructionUpdated(II)V",
            cancellable = true
    )
    private void handleLoop(int index, int state, CallbackInfo ci) {
        SequencerInstructions newValue = SequencerInstructions.values()[state];
        if (newValue == CCSequencerInstructions.LOOP) {
            for (int i = instructions.size() - 1; i > index; i--) {
                instructions.remove(i);
                Vector<ScrollInput> rowInputs = inputs.get(i);
                for (ScrollInput widget : rowInputs)
                    ((AbstractSimiScreenAccessor) this).callRemoveWidget(widget);
                rowInputs.clear();
            }
            ci.cancel();
        }
    }
}
