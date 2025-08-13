package zedzee.github.io.chips.client.model;

import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import java.util.Map;

public record ChipsSpriteInfo(Sprite particleSprite, Map<Direction, Sprite> spriteMap) {
}
