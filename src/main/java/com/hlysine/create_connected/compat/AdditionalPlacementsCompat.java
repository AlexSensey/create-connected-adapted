package com.hlysine.create_connected.compat;

import com.hlysine.create_connected.content.copycat.ICopycatWithWrappedBlock;
import com.hlysine.create_connected.content.copycat.IWrappedBlock;
import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

public class AdditionalPlacementsCompat {
    public static void register() {
        if (!Mods.ADDITIONAL_PLACEMENTS.isLoaded()) return;
        try {
            Class<?> initializer = Class.forName("com.firemerald.additionalplacements.generation.RegistrationInitializer");
            Class<?> blacklister = Class.forName("com.firemerald.additionalplacements.generation.IBlockBlacklister");
            Class<?> registration = Class.forName("com.firemerald.additionalplacements.generation.Registration");
            registration.getMethod("addRegistration", initializer).invoke(null, createInitializer(initializer, blacklister));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Additional Placements registration failed", cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Additional Placements registration API is incompatible with Minecraft 26.2", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object createInitializer(Class<?> initializer, Class<?> blacklister) throws ReflectiveOperationException {
        Method blacklist = blacklister.getMethod("blacklist", Block.class, Identifier.class);
        if (blacklist.getReturnType() != boolean.class)
            throw new NoSuchMethodException("Expected boolean blacklist(Block, Identifier)");
        Method addGlobal = initializer.getMethod("addGlobalBlacklisters", Consumer.class);
        Object filter = Proxy.newProxyInstance(blacklister.getClassLoader(), new Class<?>[] { blacklister },
                (proxy, method, args) -> method.equals(blacklist) ? isBlacklisted(args[0]) : defaultCall(proxy, method, args));
        return Proxy.newProxyInstance(initializer.getClassLoader(), new Class<?>[] { initializer }, (proxy, method, args) -> {
            if (method.equals(addGlobal)) {
                ((Consumer<Object>) args[0]).accept(filter);
                return null;
            }
            return defaultCall(proxy, method, args);
        });
    }

    private static boolean isBlacklisted(Object block) {
        return block instanceof CopycatBlock || block instanceof ICopycatWithWrappedBlock || block instanceof IWrappedBlock;
    }

    private static Object defaultCall(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, args);
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "Create Connected Additional Placements adapter";
                default -> throw new UnsupportedOperationException(method.toString());
            };
        }
        throw new UnsupportedOperationException("Unexpected Additional Placements API method: " + method);
    }
}
