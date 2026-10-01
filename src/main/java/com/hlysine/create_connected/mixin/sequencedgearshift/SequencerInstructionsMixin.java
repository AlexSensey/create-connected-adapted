package com.hlysine.create_connected.mixin.sequencedgearshift;

import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = SequencerInstructions.class, remap = false)
@Unique
public class SequencerInstructionsMixin {
    /**
     * Internal field that holds all enum values
     */
    @Shadow
    @Final
    @Mutable
    private static SequencerInstructions[] $VALUES;

    @Unique
    private static SequencerInstructions TURN_AWAIT;
    @Unique
    private static SequencerInstructions TURN_TIME;
    @Unique
    private static SequencerInstructions LOOP;

    // Extend values immediately after the base constants, before later enum consumers.
    @Inject(method = "<clinit>", at = @At(value = "FIELD",
            target = "Lcom/simibubi/create/content/kinetics/transmission/sequencer/SequencerInstructions;$VALUES:[Lcom/simibubi/create/content/kinetics/transmission/sequencer/SequencerInstructions;",
            opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER))
    private static void create_connected$appendInstructions(CallbackInfo ci) {
        TURN_AWAIT = create_connected$addMember("TURN_AWAIT", "", "sequencer_instruction", false, true, -1, -1, -1);
        TURN_TIME = create_connected$addMember("TURN_TIME", "duration", "sequencer_instruction", true, true, 600, 20, 10);
        LOOP = create_connected$addMember("LOOP", "", "sequencer_end", false, false, -1, -1, -1);
    }

    /**
     * Constructor
     */
    @Invoker("<init>")
    public static SequencerInstructions create_connected$invokeInit(String internalName, int internalId, String parameterName, String background) {
        throw new AssertionError();
    }

    /**
     * Constructor
     */
    @Invoker("<init>")
    public static SequencerInstructions create_connected$invokeInit(String internalName, int internalId, String parameterName, String background, boolean hasValueParameter,
                                                                    boolean hasSpeedParameter, int maxValue, int shiftStep, int defaultValue) {
        throw new AssertionError();
    }

    @Unique
    private static SequencerInstructions create_connected$addMember(String internalName, String parameterName, String background) {
        assert $VALUES != null;
        ArrayList<SequencerInstructions> instructions = new ArrayList<>(Arrays.asList($VALUES));
        SequencerInstructions instruction = create_connected$invokeInit(internalName, instructions.get(instructions.size() - 1).ordinal() + 1, parameterName, background);
        instructions.add(instruction);
        $VALUES = instructions.toArray(new SequencerInstructions[0]);
        return instruction;
    }

    @Unique
    private static SequencerInstructions create_connected$addMember(String internalName, String parameterName, String background, boolean hasValueParameter,
                                                                    boolean hasSpeedParameter, int maxValue, int shiftStep, int defaultValue) {
        ArrayList<SequencerInstructions> instructions = new ArrayList<>(Arrays.asList($VALUES));
        SequencerInstructions instruction = create_connected$invokeInit(internalName, instructions.get(instructions.size() - 1).ordinal() + 1, parameterName, background, hasValueParameter, hasSpeedParameter, maxValue, shiftStep, defaultValue);
        instructions.add(instruction);
        $VALUES = instructions.toArray(new SequencerInstructions[0]);
        return instruction;
    }

    @Inject(method = "needsPropagation()Z", at = @At("HEAD"), cancellable = true)
    private void needsPropagation(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == TURN_AWAIT) {
            cir.setReturnValue(true);
        } else if ((Object) this == TURN_TIME) {
            cir.setReturnValue(true);
        } else if ((Object) this == LOOP) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "formatValue(I)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void formatValue(int value, CallbackInfoReturnable<String> cir) {
        if ((Object) this == TURN_TIME) {
            if (value >= 20) {
                cir.setReturnValue((value / 20) + "s");
                return;
            }
            cir.setReturnValue(value + "t");
        }
    }
}
