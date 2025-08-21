package zedzee.github.io.chips.client.model;

import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Map;

public class ChipsSpriteInfo {
    private ChipsSprite particleSprite;
    private Map<Direction, List<ChipsSprite>> spriteMap;

    public ChipsSpriteInfo(ChipsSprite particleSprite, Map<Direction, List<ChipsSprite>> spriteMap) {
        this.particleSprite = particleSprite;
        this.spriteMap = spriteMap;
    }

    public ChipsSprite getParticleSprite() {
        return particleSprite;
    }

    public List<ChipsSprite> getSprites(Direction direction) {
        return spriteMap.containsKey(direction) ? spriteMap.get(direction) : List.of();
    }
}
