package zedzee.github.io.chips;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chips implements ModInitializer {
    public static final String MOD_ID = "chips";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void onInitialize() {
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
