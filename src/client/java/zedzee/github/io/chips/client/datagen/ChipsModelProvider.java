package zedzee.github.io.chips.client.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.data.*;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import zedzee.github.io.chips.util.ShapeHelpers;

public class ChipsModelProvider extends FabricModelProvider {
    public ChipsModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator modelGenerator) {
        registerChipModel(modelGenerator, Blocks.DIAMOND_BLOCK);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

    }

    private void registerChipModel(BlockStateModelGenerator modelGenerator, Block block) {
        Identifier identifier = TextureMap.getId(block);
        BlockStateVariantMap.SingleProperty<WeightedVariant, Integer> singleProperty = BlockStateVariantMap.models(ShapeHelpers.CHIPS_PROPERTY);

        for (int i : ShapeHelpers.CHIPS_PROPERTY.getValues()) {
            Identifier identifier2 = ModelIds.getBlockSubModelId(block, "_" + Integer.toBinaryString(i));
            singleProperty.register(i, BlockStateModelGenerator.createWeightedVariant(identifier2));
            VoxelShape voxelShape = ShapeHelpers.SHAPES[i];
            modelGenerator.modelCollector.accept(identifier2, () -> createModelFromShape(identifier, voxelShape));
        }

        modelGenerator.blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block).with(singleProperty));
        //this.registerParentedItemModel(Blocks.CHEESE, ModelIds.getBlockSubModelId(Blocks.CHEESE, "_" + Integer.toBinaryString(255)));
    }

    public static JsonObject createModelFromShape(Identifier id, VoxelShape shape) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("parent", "block/block");
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("all", id.toString());
        jsonObject2.addProperty("particle", id.toString());
        jsonObject.add("textures", jsonObject2);
        JsonArray jsonArray = new JsonArray();
        shape.forEachBox((fromX, fromY, fromZ, toX, toY, toZ) -> {
            JsonObject jsonObjectx = new JsonObject();
            jsonObjectx.add("from", Util.make(new JsonArray(), from -> {
                from.add(fromX * 16.0);
                from.add(fromY * 16.0);
                from.add(fromZ * 16.0);
            }));
            jsonObjectx.add("to", Util.make(new JsonArray(), to -> {
                to.add(toX * 16.0);
                to.add(toY * 16.0);
                to.add(toZ * 16.0);
            }));
            JsonObject jsonObject2x = new JsonObject();

            for (Direction direction : Direction.values()) {
                JsonObject jsonObject3 = new JsonObject();
                jsonObject3.addProperty("texture", "#all");
                if (shouldCullFace(fromX, fromY, fromZ, toX, toY, toZ, direction)) {
                    jsonObject3.addProperty("cullface", direction.asString());
                }

                jsonObject2x.add(direction.asString(), jsonObject3);
            }

            jsonObjectx.add("faces", jsonObject2x);
            jsonArray.add(jsonObjectx);
        });
        jsonObject.add("elements", jsonArray);
        return jsonObject;
    }

    public static boolean shouldCullFace(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, Direction face) {
        if (face.getDirection() == Direction.AxisDirection.POSITIVE) {
            double d = face.getAxis().choose(toX, toY, toZ);
            return d >= 0.99999F;
        } else {
            double d = face.getAxis().choose(fromX, fromY, fromZ);
            return d <= 1.0E-5F;
        }
    }
}
