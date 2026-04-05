package live.kadalyn.herdiercraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.item.BuildAction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BuildAction.class)
public class BuildActionMixin {
    @Unique
    private static void modifyRedstone(LivingBlock entity) {
        if (entity.isItem(Items.REDSTONE)) {
            entity.setBlockState(Blocks.REDSTONE_WIRE.defaultBlockState());
        }
    }

    @WrapMethod(method = "build")
    private static InteractionResult onBuild(LivingBlock target, Level level, Operation<InteractionResult> original) {
        modifyRedstone(target);
        return original.call(target, level);
    }

    @WrapMethod(method = "command")
    private void onCommand(LivingBlock block, ServerPlayer player, Vec3 pos, BlockPos blockPos, Direction direction, Operation<Void> original) {
        modifyRedstone(block);
        original.call(block, player, pos, blockPos, direction);
    }
}
