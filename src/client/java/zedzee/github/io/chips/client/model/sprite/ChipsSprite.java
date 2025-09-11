package zedzee.github.io.chips.client.model.sprite;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.ColorHelper;

import java.util.Arrays;

public class ChipsSprite {
    private final Sprite sprite;
    private final int tintIndex;
    private final int[] colors;
    private boolean transparent = false;

    private ChipsSprite(Sprite sprite, int tintIndex, int[] colors) {
        this.sprite = sprite;
        this.tintIndex = tintIndex;
        this.colors = colors;
    }

    public ChipsSprite(Sprite sprite) {
        this(sprite, -1, getDefaultColors());
    }

    private static int[] getDefaultColors() {
        int[] colArray = new int[4];
        Arrays.fill(colArray, ColorHelper.getArgb(255, 255, 255, 255));
        return colArray;
    }

    public void mixColor(int color) {
        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorHelper.mix(colors[i], color);
        }
    }

    public void setTransparent() {
        this.transparent = true;
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public int getTintIndex() {
        return tintIndex;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public int[] getColors() {
        return this.colors;
    }

    public static class Builder {
        private Sprite sprite;
        private int tintIndex;
        private int[] colors;

        public Builder() {
            this.sprite = null;
            this.tintIndex = -1;
            this.colors = getDefaultColors();
        }

        public Builder sprite(Sprite sprite) {
            this.sprite = sprite;
            return this;
        }

        public Builder tintIndex(int tintIndex) {
            this.tintIndex = tintIndex;
            return this;
        }

        public Builder color(int idx, int color) {
            this.colors[idx] = color;
            return this;
        }

        public ChipsSprite build() {
            return new ChipsSprite(this.sprite, this.tintIndex, this.colors);
        }

        public boolean canBuild() {
            return sprite != null;
        }
    }
}
