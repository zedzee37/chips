package zedzee.github.io.chips.client.model.sprite;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.ColorHelper;

public record ChipsSprite(Sprite sprite, int tint) {
    public ChipsSprite(Sprite sprite) {
        this(sprite, ColorHelper.getArgb(255, 255, 255, 255));
    }

    public static class Builder {
        private Sprite sprite;
        private int tintIndex;

        public Builder sprite(Sprite sprite) {
            this.sprite = sprite;
            return this;
        }

        public Builder tintIndex(int tintIndex) {
            this.tintIndex = tintIndex;
            return this;
        }

        public ChipsSprite build() {
            return new ChipsSprite(this.sprite, this.tintIndex);
        }
    }
}
