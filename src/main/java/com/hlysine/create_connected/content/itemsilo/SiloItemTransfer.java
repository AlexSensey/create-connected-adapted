package com.hlysine.create_connected.content.itemsilo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.item.ItemStack;
import com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Transactional access to a fixed silo assembly's modifiable inventory. */
final class SiloItemTransfer implements ResourceHandler<ItemResource> {
    private final IItemHandlerModifiable handler;
    private final List<SnapshotJournal<ItemStack>> journals = new ArrayList<>();

    SiloItemTransfer(IItemHandlerModifiable handler) {
        this.handler = handler;
        for (int i = 0; i < handler.getSlots(); i++) {
            final int slot = i;
            journals.add(new SnapshotJournal<>() {
                @Override
                protected ItemStack createSnapshot() {
                    return handler.getStackInSlot(slot).copy();
                }

                @Override
                protected void revertToSnapshot(ItemStack snapshot) {
                    handler.setStackInSlot(slot, snapshot.copy());
                }
            });
        }
    }

    @Override
    public int size() { return handler.getSlots(); }

    @Override
    public ItemResource getResource(int index) { return ItemResource.of(handler.getStackInSlot(index)); }

    @Override
    public long getAmountAsLong(int index) { return handler.getStackInSlot(index).getCount(); }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        if (!isValid(index, resource)) return 0;
        int limit = handler.getSlotLimit(index);
        return resource.isEmpty() ? limit : Math.min(limit, resource.toStack(1).getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || handler.isItemValid(index, resource.toStack(1));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        check(index, amount);
        if (resource.isEmpty() || amount == 0) return 0;
        int accepted = amount - handler.insertItem(index, resource.toStack(amount), true).getCount();
        if (accepted == 0) return 0;
        journals.get(index).updateSnapshots(transaction);
        return accepted - handler.insertItem(index, resource.toStack(accepted), false).getCount();
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        check(index, amount);
        if (resource.isEmpty() || amount == 0 || !resource.matches(handler.getStackInSlot(index))) return 0;
        int available = handler.extractItem(index, amount, true).getCount();
        if (available == 0) return 0;
        journals.get(index).updateSnapshots(transaction);
        return handler.extractItem(index, available, false).getCount();
    }

    private void check(int index, int amount) {
        Objects.checkIndex(index, size());
        if (amount < 0) throw new IllegalArgumentException("Negative transfer amount: " + amount);
    }
}
