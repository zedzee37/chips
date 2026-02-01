package zedzee.github.io.chips.client.util;

import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.random.Random;

public record ChipsSpriteProvider(Sprite sprite) implements SpriteProvider {
    @Override
    public Sprite getSprite(int age, int maxAge) {
        return sprite();
    }

    @Override
    public Sprite getSprite(Random random) {
        return sprite();
    }
}
