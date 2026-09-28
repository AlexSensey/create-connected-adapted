package com.hlysine.create_connected.content.fluidvessel;

import java.util.Objects;
import java.util.function.Supplier;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Transactional access to one tank, including its owner's non-fluid side effects. */
public final class SingleTankTransfer implements ResourceHandler<FluidResource> {
    private final Supplier<? extends IFluidHandler> handler;
    private final SnapshotJournal<Runnable> journal;

    public SingleTankTransfer(Supplier<? extends IFluidHandler> handler, Supplier<Runnable> snapshot) {
        this.handler = Objects.requireNonNull(handler);
        Objects.requireNonNull(snapshot);
        journal = new SnapshotJournal<>() {
            @Override
            protected Runnable createSnapshot() {
                return snapshot.get();
            }

            @Override
            protected void revertToSnapshot(Runnable restore) {
                restore.run();
            }
        };
    }

    private IFluidHandler tank(int index) {
        Objects.checkIndex(index, 1);
        IFluidHandler result = Objects.requireNonNull(handler.get());
        if (result.getTanks() != 1)
            throw new IllegalStateException("A vessel capability must expose exactly one tank");
        return result;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(tank(index).getFluidInTank(0));
    }

    @Override
    public long getAmountAsLong(int index) {
        return tank(index).getFluidInTank(0).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        IFluidHandler current = tank(index);
        return resource.isEmpty() || current.isFluidValid(0, resource.toStack(1))
                ? current.getTankCapacity(0) : 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        IFluidHandler current = tank(index);
        return resource.isEmpty() || current.isFluidValid(0, resource.toStack(1));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        IFluidHandler current = tank(index);
        if (amount < 0) throw new IllegalArgumentException("Negative fluid amount");
        if (amount == 0 || resource.isEmpty()) return 0;
        int accepted = current.fill(resource.toStack(amount), FluidAction.SIMULATE);
        if (accepted <= 0) return 0;
        journal.updateSnapshots(transaction);
        return current.fill(resource.toStack(accepted), FluidAction.EXECUTE);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        IFluidHandler current = tank(index);
        if (amount < 0) throw new IllegalArgumentException("Negative fluid amount");
        if (amount == 0 || resource.isEmpty()) return 0;
        var extracted = current.drain(resource.toStack(amount), FluidAction.SIMULATE);
        if (extracted.isEmpty()) return 0;
        journal.updateSnapshots(transaction);
        return current.drain(extracted, FluidAction.EXECUTE).getAmount();
    }
}
