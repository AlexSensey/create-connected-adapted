package com.hlysine.create_connected.datagen.advancements;

import com.hlysine.create_connected.CreateConnected;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Supplier;

public abstract class CriterionTriggerBase<T extends CriterionTriggerBase.Instance> extends SimpleCriterionTrigger<T> {

    public CriterionTriggerBase(String id) {
        this.id = CreateConnected.asResource(id);
    }

    private final Identifier id;
    public Identifier getId() {
        return id;
    }

    protected void trigger(ServerPlayer player, @Nullable List<Supplier<Object>> suppliers) {
        super.trigger(player, instance -> instance.test(suppliers));
    }

    public abstract static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        protected abstract boolean test(@Nullable List<Supplier<Object>> suppliers);
    }

}

