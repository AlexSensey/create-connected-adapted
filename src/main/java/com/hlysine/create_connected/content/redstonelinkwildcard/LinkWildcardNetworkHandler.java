package com.hlysine.create_connected.content.redstonelinkwildcard;

import com.hlysine.create_connected.registries.CCItems;
import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.config.CServer;
import com.hlysine.create_connected.compat.Mods;
import com.hlysine.create_connected.config.FeatureToggle;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.level.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.*;

@EventBusSubscriber(modid = CreateConnected.MODID)
public class LinkWildcardNetworkHandler {
    static final Map<LevelAccessor, Map<Couple<Frequency>, Set<Couple<Frequency>>>> transmitter_connections =
            new IdentityHashMap<>();
    static final Map<LevelAccessor, Map<Couple<Frequency>, Set<Couple<Frequency>>>> receiver_connections =
            new IdentityHashMap<>();

    @SubscribeEvent
    public static void onLoadWorld(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide())
            return;
        transmittersIn(level);
        receiversIn(level);
        CreateConnected.LOGGER.debug("Link-Wildcard: Prepared Redstone Network Wildcards for {}", WorldHelper.getDimensionID(event.getLevel()));
    }

    @SubscribeEvent
    public static void onUnloadWorld(LevelEvent.Unload event) {
        transmitter_connections.remove(event.getLevel());
        receiver_connections.remove(event.getLevel());
        CreateConnected.LOGGER.debug("Link-Wildcard: Removed Redstone Network Wildcards for {}", WorldHelper.getDimensionID(event.getLevel()));
    }

    public static Map<Couple<Frequency>, Set<Couple<Frequency>>> transmittersIn(LevelAccessor world) {
        return transmitter_connections.computeIfAbsent(world, ignored -> new HashMap<>());
    }

    public static Map<Couple<Frequency>, Set<Couple<Frequency>>> receiversIn(LevelAccessor world) {
        return receiver_connections.computeIfAbsent(world, ignored -> new HashMap<>());
    }

    private static boolean isServerWorld(LevelAccessor world) {
        return world instanceof Level level && !level.isClientSide();
    }

    public static boolean updateNetworkOf(RedstoneLinkNetworkHandler handler, LevelAccessor world, IRedstoneLinkable actor) {
        if (!isServerWorld(world) || !FeatureToggle.isEnabled(CCItems.REDSTONE_LINK_WILDCARD.getId()))
            return false;

        Couple<Frequency> key = actor.getNetworkKey();
        updateNetworkForReceiver(handler, world, actor, key);
        if (actor.isListening())
            return true;
        Map<Couple<Frequency>, Set<Couple<Frequency>>> transmitters = transmittersIn(world);
        if (transmitters.containsKey(key)) {
            Set<Couple<Frequency>> connections = transmitters.get(key);
            for (Couple<Frequency> connection : connections) {
                updateNetworkForReceiver(handler, world, actor, connection);
            }
        }
        return true;
    }

    private static void updateNetworkForReceiver(RedstoneLinkNetworkHandler handler, LevelAccessor world, IRedstoneLinkable actor, Couple<Frequency> key) {
        var networks = handler.networksIn(world);
        var network = networks.get(key);
        handler.globalPowerVersion.incrementAndGet();
        if (network == null)
            return;

        Set<IRedstoneLinkable> transmitters = Collections.newSetFromMap(new IdentityHashMap<>());
        if (hasAllowedWildcards(key)) {
            collectTransmitters(network, transmitters);
            var wildcardKeys = receiversIn(world).get(key);
            if (wildcardKeys != null) {
                for (var wildcardKey : wildcardKeys) {
                    // Recheck the rule before using a retained route.
                    if (test(wildcardKey, key))
                        collectTransmitters(networks.get(wildcardKey), transmitters);
                }
            }
        } else {
            network.removeIf(node -> !node.isAlive());
        }

        // A frequency may contain receivers at different positions. Each needs
        // the strongest transmitter within its own range, including zero after a move.
        for (IRedstoneLinkable receiver : List.copyOf(network)) {
            if (!receiver.isAlive() || !receiver.isListening())
                continue;
            int power = 0;
            for (IRedstoneLinkable transmitter : transmitters) {
                if (!transmitter.isAlive() || !withinRange(receiver, transmitter, world))
                    continue;
                power = Math.max(power, Math.clamp(transmitter.getTransmittedStrength(), 0, 15));
                if (power == 15)
                    break;
            }
            if (receiver == actor && receiver instanceof LinkBehaviour link)
                link.newPosition = true;
            receiver.setReceivedStrength(power);
        }
    }

    private static void collectTransmitters(Set<IRedstoneLinkable> network, Set<IRedstoneLinkable> transmitters) {
        if (network == null)
            return;
        for (var iterator = network.iterator(); iterator.hasNext();) {
            var node = iterator.next();
            if (!node.isAlive()) {
                iterator.remove();
            } else if (!node.isListening()) {
                transmitters.add(node);
            }
        }
    }

    public static void addToNetwork(RedstoneLinkNetworkHandler handler, LevelAccessor world, IRedstoneLinkable actor) {
        if (!isServerWorld(world))
            return;
        Couple<Frequency> key = actor.getNetworkKey();
        Map<Couple<Frequency>, Set<Couple<Frequency>>> wildcards = actor.isListening() ? receiversIn(world) : transmittersIn(world);
//        CreateConnected.LOGGER.debug("Link-Wildcard: New {}: {}", actor.isListening() ? "receiver" : "transmitter", keyToString(key));
        if (!wildcards.containsKey(key)) {
            HashSet<Couple<Frequency>> connections = new LinkedHashSet<>();
            Map<Couple<Frequency>, Set<IRedstoneLinkable>> networks = handler.networksIn(world);
            for (Couple<Frequency> otherKey : networks.keySet()) {
                if (!otherKey.equals(key) && test(key, otherKey)) {
                    if (connections.add(otherKey)) {
//                        CreateConnected.LOGGER.debug("Link-Wildcard: - {} {}", actor.isListening() ? "Receiving from" : "Transmitting to", keyToString(otherKey));
                    }
                }
            }
            wildcards.put(key, connections);
        }
        Map<Couple<Frequency>, Set<Couple<Frequency>>> oppositeSet = actor.isListening() ? transmittersIn(world) : receiversIn(world);
        for (Map.Entry<Couple<Frequency>, Set<Couple<Frequency>>> entry : oppositeSet.entrySet()) {
            if (!entry.getKey().equals(key) && test(entry.getKey(), key)) {
                if (entry.getValue().add(key)) {
//                    CreateConnected.LOGGER.debug("Link-Wildcard: - Reverse: {} {}", actor.isListening() ? "Receiving from" : "Transmitting to", keyToString(entry.getKey()));
                }
            }
        }
    }

    public static void removeFromNetwork(RedstoneLinkNetworkHandler handler, LevelAccessor world, IRedstoneLinkable actor) {
        if (!isServerWorld(world))
            return;
        Couple<Frequency> key = actor.getNetworkKey();
        Map<Couple<Frequency>, Set<IRedstoneLinkable>> networks = handler.networksIn(world);
        var remaining = networks.get(key);
        if (remaining != null && remaining.stream().anyMatch(other ->
                other.isAlive() && other.isListening() == actor.isListening()))
            return;
//        CreateConnected.LOGGER.debug("Link-Wildcard: Removing {} {}", actor.isListening() ? "receiver" : "transmitter", keyToString(key));
        Map<Couple<Frequency>, Set<Couple<Frequency>>> wildcards = actor.isListening() ? receiversIn(world) : transmittersIn(world);
        wildcards.remove(key);
        Map<Couple<Frequency>, Set<Couple<Frequency>>> oppositeSet = actor.isListening() ? transmittersIn(world) : receiversIn(world);
        for (Map.Entry<Couple<Frequency>, Set<Couple<Frequency>>> entry : oppositeSet.entrySet()) {
            if (entry.getValue().remove(key)) {
//                CreateConnected.LOGGER.debug("Link-Wildcard: - No longer {} {}", actor.isListening() ? "receiving from" : "transmitting to", keyToString(entry.getKey()));
                handler.updateNetworkOf(world, new IRedstoneLinkable() {
                    @Override
                    public int getTransmittedStrength() {
                        return 0;
                    }

                    @Override
                    public void setReceivedStrength(int power) {

                    }

                    @Override
                    public boolean isListening() {
                        return false;
                    }

                    @Override
                    public boolean isAlive() {
                        return true;
                    }

                    @Override
                    public Couple<Frequency> getNetworkKey() {
                        return entry.getKey();
                    }

                    @Override
                    public BlockPos getLocation() {
                        return actor.getLocation();
                    }
                });
            }
        }
        if (actor.isListening())
            actor.setReceivedStrength(0);
    }

    private static String keyToString(Couple<Frequency> key) {
        return String.format("%s + %s",
                BuiltInRegistries.ITEM.getKey(key.getFirst().getStack().getItem()),
                BuiltInRegistries.ITEM.getKey(key.getSecond().getStack().getItem())
        );
    }

    private static boolean hasAllowedWildcards(Couple<Frequency> key) {
        return CServer.AllowDualWildcardLink.get()
                || !(key.getFirst().getStack().getItem() instanceof ILinkWildcard
                && key.getSecond().getStack().getItem() instanceof ILinkWildcard);
    }

    private static boolean test(Couple<Frequency> transmitter, Couple<Frequency> receiver) {
        if (!hasAllowedWildcards(transmitter) || !hasAllowedWildcards(receiver))
            return false;
        return (wildcardTransmit(transmitter.getFirst(), receiver.getFirst())
                || wildcardReceive(transmitter.getFirst(), receiver.getFirst()))
                && (wildcardTransmit(transmitter.getSecond(), receiver.getSecond())
                || wildcardReceive(transmitter.getSecond(), receiver.getSecond()));
    }

    private static boolean wildcardTransmit(Frequency transmitter, Frequency receiver) {
        if (transmitter.getStack().getItem() instanceof ILinkWildcard wildcard) {
            return wildcard.test(receiver);
        } else {
            return transmitter.equals(receiver);
        }
    }

    private static boolean wildcardReceive(Frequency transmitter, Frequency receiver) {
        if (receiver.getStack().getItem() instanceof ILinkWildcard wildcard) {
            return wildcard.test(transmitter);
        } else {
            return transmitter.equals(receiver);
        }
    }

    // Implement a custom range check for compatibility with sable. Modified version of dev.ryanhcode.sable.neoforge.mixin.compatibility.create.redstone_links.RedstoneLinkNetworkHandlerMixin.sable$projectComparisons
    private static boolean withinRange(IRedstoneLinkable from, IRedstoneLinkable to, LevelAccessor levelAccessor) {
        final Level level = (Level) levelAccessor;

        if (from == to) return true;

        final Vector3d fromPos = linkPosition(from.getLocation());
        final Vector3d toPos = linkPosition(to.getLocation());

        final double distance = Mods.SABLE.isLoaded()
                ? SableRange.API.distanceSquared(level, fromPos, toPos)
                : fromPos.distanceSquared(toPos);
        final int linkRange = AllConfigs.server().logistics.linkRange.get();
        return distance < (double) linkRange * linkRange;
    }

    /** No Companion classes are loaded when Sable is absent. */
    private static class SableRange {
        private static final RangeApi API = RangeApi.load();
    }

    private record RangeApi(Object companion, Method distance) {
        private static RangeApi load() {
            try {
                Class<?> api = Class.forName("dev.ryanhcode.sable.companion.SableCompanion");
                Method method = api.getMethod("distanceSquaredWithSubLevels", Level.class, Vector3dc.class, Vector3dc.class);
                if (method.getReturnType() != double.class)
                    throw new NoSuchMethodException("Expected double distanceSquaredWithSubLevels(Level, Vector3dc, Vector3dc)");
                return new RangeApi(api.getField("INSTANCE").get(null), method);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Sable Companion range API is incompatible with Minecraft 26.2", e);
            }
        }

        private double distanceSquared(Level level, Vector3dc from, Vector3dc to) {
            try {
                return (double) distance.invoke(companion, level, from, to);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtime) throw runtime;
                if (cause instanceof Error error) throw error;
                throw new IllegalStateException("Sable Companion range check failed", cause);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Sable Companion range API is inaccessible", e);
            }
        }
    }

    private static Vector3d linkPosition(BlockPos pos) {
        // Transform block centers, matching Sable's Create link comparison.
        return new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
}
