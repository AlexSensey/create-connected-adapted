import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.hlysine.create_connected.content.inventoryaccessport.RoutedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.*;

public class ItemRoutingTransactionCheck {
    record TestResource(boolean isEmpty) implements Resource {}
    static final TestResource EMPTY = new TestResource(true), ITEM = new TestResource(false);

    static final class Store extends SnapshotJournal<Integer> implements ResourceHandler<TestResource> {
        int count = 10;
        public int size() { return 1; }
        public TestResource getResource(int i) { return count == 0 ? EMPTY : ITEM; }
        public long getAmountAsLong(int i) { return count; }
        public long getCapacityAsLong(int i, TestResource r) { return 64; }
        public boolean isValid(int i, TestResource r) { return !r.isEmpty(); }
        public int insert(int i, TestResource r, int amount, TransactionContext tx) {
            int accepted = Math.min(amount, 64 - count);
            updateSnapshots(tx);
            count += accepted;
            return accepted;
        }
        public int extract(int i, TestResource r, int amount, TransactionContext tx) {
            int taken = Math.min(amount, count);
            updateSnapshots(tx);
            count -= taken;
            return taken;
        }
        protected Integer createSnapshot() { return count; }
        protected void revertToSnapshot(Integer snapshot) { count = snapshot; }
    }

    static void equal(long expected, long actual) {
        if (expected != actual) throw new AssertionError(expected + " != " + actual);
    }

    public static void main(String[] args) {
        Store store = new Store();
        AtomicReference<ResourceHandler<TestResource>> route = new AtomicReference<>(store);
        var handler = new RoutedResourceHandler<>(EMPTY, List.of(route::get), (side, item, count) -> true);
        try (Transaction tx = Transaction.openRoot()) {
            equal(5, handler.insert(0, ITEM, 5, tx));
            equal(15, store.count);
        }
        equal(10, store.count);
        try (Transaction tx = Transaction.openRoot()) {
            equal(5, handler.insert(0, ITEM, 5, tx));
            tx.commit();
        }
        equal(15, store.count);
        try (Transaction tx = Transaction.openRoot()) {
            equal(4, handler.extract(0, ITEM, 4, tx));
            equal(11, store.count);
        }
        equal(15, store.count); // Committed child extraction still rolls back with its parent.
        try (Transaction tx = Transaction.openRoot()) {
            equal(4, handler.extract(0, ITEM, 4, tx));
            tx.commit();
        }
        equal(11, store.count);
        var rejected = new RoutedResourceHandler<>(EMPTY, List.of(() -> store), (side, item, count) -> false);
        try (Transaction tx = Transaction.openRoot()) {
            equal(0, rejected.extract(0, ITEM, 4, tx));
            equal(11, store.count);
            equal(0, rejected.insert(0, ITEM, 4, tx));
            tx.commit();
        }
        equal(0, rejected.getAmountAsLong(0));
        if (!rejected.getResource(0).isEmpty()) throw new AssertionError("Hidden resource leaked");
        var positiveOnly = new RoutedResourceHandler<>(EMPTY, List.of(() -> null, () -> store),
            (side, item, count) -> side == 1 && count == 11);
        equal(1, positiveOnly.size());
        equal(11, positiveOnly.getAmountAsLong(0));
        try (Transaction tx = Transaction.openRoot()) {
            equal(11, positiveOnly.extract(0, ITEM, 64, tx)); // Filter sees actual quantity.
        }
        equal(11, store.count);
        try (Transaction tx = Transaction.openRoot()) {
            equal(0, handler.insert(3, ITEM, 1, tx));
            equal(0, handler.extract(-1, ITEM, 1, tx));
        }
        route.set(null);
        equal(0, handler.size());
        route.set(handler); // An indirect routing cycle must not overflow the stack.
        equal(0, handler.size());
        route.set(store);
        equal(1, handler.size());
        equal(11, handler.getAmountAsLong(0));
        System.out.println("Item routing: commit, rollback, filtered extraction, route identity and recursion checks passed.");
    }
}
