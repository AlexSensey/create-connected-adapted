import com.hlysine.create_connected.content.fluidvessel.SingleTankTransfer;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class FluidTransactionCheck {
    private static void equal(int expected, int actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + ", got " + actual);
    }

    private static class Tank implements IFluidHandler {
        int amount = 100;
        int supplied;
        boolean boiler;

        Runnable snapshot() {
            int oldAmount = amount, oldSupply = supplied;
            return () -> { amount = oldAmount; supplied = oldSupply; };
        }

        public int getTanks() { return 1; }
        public FluidStack getFluidInTank(int index) { return new FluidStack(Fluids.WATER, amount); }
        public int getTankCapacity(int index) { return 1000; }
        public boolean isFluidValid(int index, FluidStack stack) { return stack.getFluid() == Fluids.WATER; }
        public int fill(FluidStack stack, FluidAction action) {
            if (!isFluidValid(0, stack)) return 0;
            int accepted = boiler ? stack.getAmount() : Math.min(1000 - amount, stack.getAmount());
            if (action.execute()) {
                if (!boiler) amount += accepted;
                supplied += accepted;
            }
            return accepted;
        }
        public FluidStack drain(FluidStack stack, FluidAction action) {
            return isFluidValid(0, stack) ? drain(stack.getAmount(), action) : FluidStack.EMPTY;
        }
        public FluidStack drain(int max, FluidAction action) {
            int extracted = boiler ? 0 : Math.min(max, amount);
            if (action.execute()) amount -= extracted;
            return new FluidStack(Fluids.WATER, extracted);
        }
    }

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Tank tank = new Tank();
        SingleTankTransfer transfer = new SingleTankTransfer(() -> tank, tank::snapshot);
        FluidResource water = FluidResource.of(Fluids.WATER);

        try (var tx = Transaction.openRoot()) {
            equal(200, transfer.insert(0, water, 200, tx));
            equal(300, tank.amount);
        }
        equal(100, tank.amount);
        equal(0, tank.supplied);

        try (var outer = Transaction.openRoot()) {
            transfer.insert(0, water, 200, outer);
            try (var inner = Transaction.open(outer)) {
                transfer.insert(0, water, 300, inner);
            }
            equal(300, tank.amount);
            equal(200, tank.supplied);
            outer.commit();
        }
        equal(300, tank.amount);

        try (var outer = Transaction.openRoot()) {
            transfer.insert(0, water, 100, outer);
            try (var inner = Transaction.open(outer)) {
                transfer.extract(0, water, 250, inner);
                inner.commit();
            }
            equal(150, tank.amount);
        }
        equal(300, tank.amount);
        equal(200, tank.supplied);

        try (var tx = Transaction.openRoot()) {
            equal(300, transfer.extract(0, water, 1000, tx));
            tx.commit();
        }
        equal(0, tank.amount);

        tank.boiler = true;
        try (var tx = Transaction.openRoot()) {
            equal(1500, transfer.insert(0, water, 1500, tx));
            equal(1700, tank.supplied);
            equal(0, tank.amount);
        }
        equal(200, tank.supplied);
        try (var tx = Transaction.openRoot()) {
            equal(0, transfer.insert(0, FluidResource.of(Fluids.LAVA), 50, tx));
            equal(0, transfer.insert(0, FluidResource.EMPTY, 50, tx));
            tx.commit();
        }
        equal(200, tank.supplied);
        System.out.println("Passed fluid commit, rollback, nested transaction, extraction and boiler-side-effect checks.");
    }
}
