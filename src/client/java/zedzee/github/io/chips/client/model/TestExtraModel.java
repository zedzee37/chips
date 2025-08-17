package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;

import java.util.function.BiFunction;

public class TestExtraModel<T> implements UnbakedExtraModel<T> {
    public static Identifier MODEL_ID = Chips.identifier("block/test_model");
    public static final ExtraModelKey<BlockStateModel> MODEL_KEY = ExtraModelKey.create(MODEL_ID::toString);

    private final Identifier model;
    private final BiFunction<BakedSimpleModel, Baker, T> bake;

    public TestExtraModel(Identifier model, BiFunction<BakedSimpleModel, Baker, T> bake) {
        this.model = model;
        this.bake = bake;
    }

    public static void register() {
        ModelLoadingPlugin.register(pluginContext -> pluginContext.addModel(MODEL_KEY, TestExtraModel.blockStateModel(MODEL_ID)));
    }

    public static TestExtraModel<BlockStateModel> blockStateModel(Identifier model) {
        return blockStateModel(model, ModelRotation.X0_Y0);
    }

    public static TestExtraModel<BlockStateModel> blockStateModel(Identifier model, ModelBakeSettings settings) {
        return new TestExtraModel<>(model, (baked, baker) ->
                new SimpleBlockStateModel.Unbaked(
                        new ModelVariant(Identifier.ofVanilla(
                                "block/diamond_block"))).bake(baker
                ));
    }

    @Override
    public T bake(Baker baker) {
        return bake.apply(baker.getModel(model), baker);
    }

    @Override
    public void resolve(Resolver resolver) {
        resolver.markDependency(model);
    }
}
