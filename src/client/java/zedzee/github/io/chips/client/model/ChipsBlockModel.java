package zedzee.github.io.chips.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ChipsBlockModel implements UnbakedModel, BakedSimpleModel, FabricBakedModelManager {
    @Override
    public UnbakedModel getModel() {
        return null;
    }

    @Override
    public @Nullable BakedSimpleModel getParent() {
        return null;
    }

    @Override
    public ModelTextures getTextures() {
        return BakedSimpleModel.super.getTextures();
    }

    @Override
    public boolean getAmbientOcclusion() {
        return BakedSimpleModel.super.getAmbientOcclusion();
    }

    @Override
    public GuiLight getGuiLight() {
        return BakedSimpleModel.super.getGuiLight();
    }

    @Override
    public Geometry getGeometry() {
        return BakedSimpleModel.super.getGeometry();
    }

    @Override
    public BakedGeometry bakeGeometry(ModelTextures textures, Baker baker, ModelBakeSettings settings) {
    }

    @Override
    public Sprite getParticleTexture(ModelTextures textures, Baker baker) {
        return BakedSimpleModel.super.getParticleTexture(textures, baker);
    }

    @Override
    public ModelTransformation getTransformations() {
        return BakedSimpleModel.super.getTransformations();
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public <T> @Nullable T getModel(ExtraModelKey<T> key) {
        return FabricBakedModelManager.super.getModel(key);
    }

    @Override
    public @Nullable Boolean ambientOcclusion() {
        return UnbakedModel.super.ambientOcclusion();
    }

    @Override
    public @Nullable GuiLight guiLight() {
        return UnbakedModel.super.guiLight();
    }

    @Override
    public @Nullable ModelTransformation transformations() {
        return UnbakedModel.super.transformations();
    }

    @Override
    public ModelTextures.Textures textures() {
        return UnbakedModel.super.textures();
    }

    @Override
    public @Nullable Geometry geometry() {
        return UnbakedModel.super.geometry();
    }

    @Override
    public @Nullable Identifier parent() {
        return UnbakedModel.super.parent();
    }
}
