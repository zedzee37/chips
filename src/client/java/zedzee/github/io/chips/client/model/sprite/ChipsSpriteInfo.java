package zedzee.github.io.chips.client.model.sprite;

import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Map;

public class ChipsSpriteInfo {
    private final ChipsSprite particleSprite;
    private final Map<Direction, List<ChipsSprite>> spriteMap;
    private final boolean defaultUv;

    public ChipsSpriteInfo(ChipsSprite particleSprite, Map<Direction, List<ChipsSprite>> spriteMap, boolean defaultUv) {
        this.particleSprite = particleSprite;
        this.spriteMap = spriteMap;
        this.defaultUv = defaultUv;
    }

    public ChipsSprite getParticleSprite() {
        return particleSprite;
    }

    public List<ChipsSprite> getSprites(Direction direction) {
        return spriteMap.containsKey(direction) ? spriteMap.get(direction) : List.of();
    }

    public boolean shouldUseDefaultUv() {
        return this.defaultUv;
    }
}
