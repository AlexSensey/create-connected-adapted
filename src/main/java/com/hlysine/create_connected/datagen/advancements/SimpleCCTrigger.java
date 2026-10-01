package com.hlysine.create_connected.datagen.advancements;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;

public class SimpleCCTrigger extends CriterionTriggerBase<SimpleCCTrigger.Instance> {

    public SimpleCCTrigger(String id) {
        super(id);
    }

    public void trigger(ServerPlayer player) {
        super.trigger(player, (List<Supplier<Object>>) null);
    }

    public SimpleCCTrigger.Instance instance() {
        return new SimpleCCTrigger.Instance();
    }

    @Override
    public Codec<SimpleCCTrigger.Instance> codec() {
        return SimpleCCTrigger.Instance.CODEC;
    }

    public static class Instance extends CriterionTriggerBase.Instance {
        private static final Codec<SimpleCCTrigger.Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(SimpleCCTrigger.Instance::player)
        ).apply(instance, SimpleCCTrigger.Instance::new));

        private final Optional<Holder<LootItemCondition>> player;

        public Instance() {
            player = Optional.empty();
        }

        public Instance(Optional<Holder<LootItemCondition>> player) {
            this.player = player;
        }

        @Override
        protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
            return true;
        }

        @Override
        public Optional<Holder<LootItemCondition>> player() {
            return player;
        }
    }
}

