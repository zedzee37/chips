package zedzee.github.io.chips.client.model;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import zedzee.github.io.chips.client.model.sprite.CapturingQuadEmitter;
import zedzee.github.io.chips.client.model.sprite.ChipsSprite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class BlockStateModelHelper {
    private static final Predicate<@Nullable Direction> badCullTest = direction -> false;
    private final BlockState state;
    private final CapturingQuadEmitter capturingEmitter;
    private final BlockStateModel blockStateModel;
    private final BlockColors blockColors;

    public BlockStateModelHelper(Block block) {
        this.state = block.getDefaultState();
        this.capturingEmitter = new CapturingQuadEmitter();

        MinecraftClient client = MinecraftClient.getInstance();
        this.blockStateModel = client.getBlockRenderManager().getModel(this.state);
        this.blockColors = client.getBlockColors();
    }

    public Map<Direction, List<ChipsSprite>> getSprites(
            BlockRenderView blockView,
            BlockPos pos,
            Random random) {
        Map<Direction, List<ChipsSprite>> sprites = new HashMap<>();

        this.capturingEmitter.captureTo(sprites);
        this.blockStateModel.emitQuads(this.capturingEmitter, blockView, pos, this.state, random, badCullTest);

        sprites.values().forEach(spriteList -> {
            spriteList.forEach(chipsSprite -> {
                if (chipsSprite.tintIndex() == -1) {
                    return;
                }


                int colorToMix = this.blockColors.getColor(this.state, blockView, pos, chipsSprite.tintIndex());
                colorToMix = ColorHelper.withAlpha(255, colorToMix);

                chipsSprite.mixColor(colorToMix);
            });
        });

        return sprites;
    }

    public Sprite getParticleSprite() {
        return this.blockStateModel.particleSprite();
    }
}
