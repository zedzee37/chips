package zedzee.github.io.chips.client.model;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.ColorHelper;

public record ChipsSprite(Sprite sprite, int tint) {
    public ChipsSprite(Sprite sprite) {
        this(sprite, ColorHelper.getArgb(255, 255, 255, 255));
    }
}
