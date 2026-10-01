package com.hlysine.create_connected.compat;

import net.neoforged.fml.ModList;

import java.lang.reflect.InvocationTargetException;

/** Loads the separate bridge only when both its mod and Simulated are present. */
public final class SimulatedCompat {
    public static final String MOD_ID = "create_connected_simulated";

    private SimulatedCompat() {}

    public static boolean available() {
        return Mods.SIMULATED.isLoaded() && ModList.get().isLoaded(MOD_ID);
    }

    public static void register() {
        invoke("com.hlysine.create_connected.compat.SimCompatRegistry", "register", null, null);
    }

    public static void invoke(String owner, String method, Class<?> parameterType, Object argument) {
        if (!available())
            return;
        try {
            Class<?> bridge = Class.forName(owner, true, SimulatedCompat.class.getClassLoader());
            if (parameterType == null)
                bridge.getMethod(method).invoke(null);
            else
                bridge.getMethod(method, parameterType).invoke(null, argument);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Connected Simulated bridge failed: " + method, exception.getCause());
        } catch (ReflectiveOperationException | LinkageError exception) {
            throw new IllegalStateException("Cannot load the Connected Simulated bridge: " + method, exception);
        }
    }
}
