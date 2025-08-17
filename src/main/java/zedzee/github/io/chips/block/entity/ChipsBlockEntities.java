package zedzee.github.io.chips.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import zedzee.github.io.chips.Chips;
import zedzee.github.io.chips.block.ChipsBlocks;

public class ChipsBlockEntities {
    public static final BlockEntityType<ChipsBlockEntity> CHIPS_BLOCK_ENTITY =
            register("chips", ChipsBlockEntity::new, ChipsBlocks.CHIPS_BLOCK);

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = Chips.identifier(name);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE,
                id,
                FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build()
        );
    }

    public static void init() {}
}
