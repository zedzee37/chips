package zedzee.github.io.chips.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import zedzee.github.io.chips.block.ChipsBlockHelpers;

public class ChiselItem extends Item {
    private static final int MAX_USE_TIME = 200;
    private final int useTime;

    public ChiselItem(Settings settings, int useTime) {
        super(settings);
        this.useTime = useTime;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity != null && this.getHitResult(playerEntity).getType() == HitResult.Type.BLOCK) {
            playerEntity.setCurrentHand(context.getHand());
        }

        return ActionResult.CONSUME;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return useTime;
    }

    private HitResult getHitResult(PlayerEntity user) {
        return ProjectileUtil.getCollision(user, EntityPredicates.CAN_HIT, user.getBlockInteractionRange());
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks <= 0 || !(user instanceof PlayerEntity player)) {
            user.stopUsingItem();
            return;
        }

        if (!(getHitResult(player) instanceof BlockHitResult blockHitResult)) {
            user.stopUsingItem();
            return;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!canChisel(state, state.getHardness(world, pos))) {
            user.stopUsingItem();
            return;
        }

        int corner = -1;

        // adjust the pos to local coords
        Vec3d adjustedPos = blockHitResult.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
        if (state.contains(ChipsBlockHelpers.CHIPS)) {
            corner = ChipsBlockHelpers.getClosestSlice(state, adjustedPos);
        } else if (state.getOutlineShape(world, blockHitResult.getBlockPos()) == VoxelShapes.fullCube()) {
            corner = ChipsBlockHelpers.getClosestSlice(state, adjustedPos);
        } else {
            user.stopUsingItem();
        }

        // do particles here

        if (remainingUseTicks != 1 || corner == -1) {
            return;
        }

        corner = 1 << corner;

        int chipsValue = state.get(ChipsBlockHelpers.CHIPS);
        int afterChisel = chipsValue & ~(corner);

        if (afterChisel == 0) {
            world.breakBlock(pos, false);
        } else {
            world.setBlockState(pos, state.with(ChipsBlockHelpers.CHIPS, afterChisel));
        }

        stack.damage(1, player);
        user.stopUsingItem();
    }

    private static boolean canChisel(BlockState state, float hardness) {
        return state.contains(ChipsBlockHelpers.CHIPS) && hardness != -1;
    }
}
