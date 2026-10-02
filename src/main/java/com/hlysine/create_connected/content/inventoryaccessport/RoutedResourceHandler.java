package com.hlysine.create_connected.content.inventoryaccessport;

import java.util.List;
import java.util.function.Supplier;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** A live, filtered view of neighboring handlers. Storage owns transaction snapshots. */
public final class RoutedResourceHandler<T extends Resource> implements ResourceHandler<T> {
    @FunctionalInterface
    public interface Filter<T> {
        boolean accepts(int route, T resource, int amount);
    }

    private final T empty;
    private final List<Supplier<ResourceHandler<T>>> routes;
    private final Filter<T> filter;
    private boolean accessing;

    public RoutedResourceHandler(T empty, List<Supplier<ResourceHandler<T>>> routes, Filter<T> filter) {
        this.empty = empty;
        this.routes = List.copyOf(routes);
        this.filter = filter;
    }

    private <R> R guarded(Supplier<R> action, R fallback) {
        if (accessing) return fallback;
        accessing = true;
        try {
            return action.get();
        } finally {
            accessing = false;
        }
    }

    private record Slot<T extends Resource>(ResourceHandler<T> handler, int index, int route) {}

    private Slot<T> resolve(int index) {
        if (index < 0) return null;
        for (int route = 0; route < routes.size(); route++) {
            ResourceHandler<T> handler = routes.get(route).get();
            if (handler == null) continue;
            int size = handler.size();
            if (index < size) return new Slot<>(handler, index, route);
            index -= size;
        }
        return null;
    }

    @Override
    public int size() {
        return guarded(() -> {
            int size = 0;
            for (Supplier<ResourceHandler<T>> route : routes) {
                ResourceHandler<T> handler = route.get();
                if (handler != null) size += handler.size();
            }
            return size;
        }, 0);
    }

    @Override
    public T getResource(int index) {
        return guarded(() -> {
            Slot<T> slot = resolve(index);
            if (slot == null) return empty;
            T resource = slot.handler.getResource(slot.index);
            return resource.isEmpty() || !filter.accepts(slot.route, resource, slot.handler.getAmountAsInt(slot.index))
                ? empty : resource;
        }, empty);
    }

    @Override
    public long getAmountAsLong(int index) {
        return guarded(() -> {
            Slot<T> slot = resolve(index);
            if (slot == null) return 0L;
            T resource = slot.handler.getResource(slot.index);
            return resource.isEmpty() || !filter.accepts(slot.route, resource, slot.handler.getAmountAsInt(slot.index))
                ? 0L : slot.handler.getAmountAsLong(slot.index);
        }, 0L);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        return guarded(() -> {
            Slot<T> slot = resolve(index);
            return slot == null ? 0L : slot.handler.getCapacityAsLong(slot.index, resource);
        }, 0L);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return guarded(() -> {
            Slot<T> slot = resolve(index);
            return slot != null && !resource.isEmpty() && filter.accepts(slot.route, resource, 1)
                && slot.handler.isValid(slot.index, resource);
        }, false);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        if (amount < 0) throw new IllegalArgumentException("Negative insertion amount");
        if (amount == 0 || resource.isEmpty()) return 0;
        return guarded(() -> {
            Slot<T> slot = resolve(index);
            if (slot == null || !filter.accepts(slot.route, resource, amount)) return 0;
            return slot.handler.insert(slot.index, resource, amount, transaction);
        }, 0);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        if (amount < 0) throw new IllegalArgumentException("Negative extraction amount");
        if (amount == 0 || resource.isEmpty()) return 0;
        return guarded(() -> {
            Slot<T> slot = resolve(index);
            if (slot == null) return 0;
            // Filter the actual extracted quantity, just as the old simulated extraction did.
            try (Transaction nested = Transaction.open(transaction)) {
                int extracted = slot.handler.extract(slot.index, resource, amount, nested);
                if (extracted == 0 || !filter.accepts(slot.route, resource, extracted)) return 0;
                nested.commit();
                return extracted;
            }
        }, 0);
    }
}
