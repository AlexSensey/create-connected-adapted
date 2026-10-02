import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

/** Native holder syntax checks; symbolic registry, no full FML recipe decoding. */
public class CatalystHolderCodecCheck {
    private static void collectInputs(com.google.gson.JsonObject recipe, List<JsonElement> inputs) {
        if (recipe.has("ingredients")) recipe.getAsJsonArray("ingredients").forEach(inputs::add);
        if (recipe.has("ingredient")) inputs.add(recipe.get("ingredient"));
        if (recipe.has("key")) recipe.getAsJsonObject("key").asMap().values().forEach(inputs::add);
        if (recipe.has("sequence"))
            recipe.getAsJsonArray("sequence").forEach(step -> collectInputs(step.getAsJsonObject(), inputs));
    }
    public static void main(String[] args) throws Exception {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, new RegistryOps.RegistryInfoLookup() {
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                HolderOwner<T> owner = new HolderOwner<>() {};
                HolderGetter<T> getter = new HolderGetter<>() {
                    public Optional<Holder.Reference<T>> get(ResourceKey<T> entry) {
                        return Optional.of(Holder.Reference.createStandAlone(owner, entry));
                    }
                    public Optional<HolderSet.Named<T>> get(TagKey<T> tag) {
                        return Optional.of(HolderSet.emptyNamed(owner, tag));
                    }
                };
                return Optional.of(new RegistryOps.RegistryInfo<>(owner, getter, Lifecycle.stable()));
            }
        });
        ResourceKey<Registry<Object>> itemKey = ResourceKey.createRegistryKey(Identifier.parse("minecraft:item"));
        ResourceKey<Registry<Object>> fluidKey = ResourceKey.createRegistryKey(Identifier.parse("minecraft:fluid"));
        var itemCodec = HolderSetCodec.create(itemKey, RegistryFixedCodec.create(itemKey), false);
        var fluidCodec = HolderSetCodec.create(fluidKey, RegistryFixedCodec.create(fluidKey), false);
        int recipes = 0, items = 0, fluids = 0;
        for (String directory : args) {
            try (var paths = Files.walk(Path.of(directory))) {
                for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                    var recipe = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                    List<JsonElement> inputs = new ArrayList<>();
                    collectInputs(recipe, inputs);
                    for (JsonElement input : inputs) {
                        boolean fluid = input.isJsonObject() && input.getAsJsonObject().has("amount");
                        JsonElement holder = fluid ? input.getAsJsonObject().get("ingredient") : input;
                        var result = (fluid ? fluidCodec : itemCodec).parse(ops, holder);
                        if (result.error().isPresent()) throw new AssertionError(path + ": " + result.error().get());
                        if (fluid) {
                            if (input.getAsJsonObject().get("amount").getAsInt() != 1000)
                                throw new AssertionError(path + ": volume");
                            fluids++;
                        } else items++;
                    }
                    recipes++;
                }
            }
        }
        if (itemCodec.parse(ops, JsonParser.parseString("{\"item\":\"minecraft:water_bucket\"}"))
                .error().isEmpty()) throw new AssertionError("Legacy input unexpectedly accepted");
        System.out.println("PASS: native holder syntax for " + recipes + " recipes, " + items
                + " item inputs, " + fluids + " fluid inputs; full FML recipe loading not tested");
    }
}
