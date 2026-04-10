package live.kadalyn.herdiercraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import live.kadalyn.herdiercraft.Herdiercraft;
import live.kadalyn.herdiercraft.behavior.BuildOnFarmlandBehavior;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BuildAction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BuildAction.class)
public class BuildActionMixin {
    @Unique
    private static void modifyBlockState(LivingBlock entity) {
        Item item = entity.getItemStack().getItem();
        if (item instanceof BlockItem blockItem) {
            entity.setBlockState(blockItem.getBlock().defaultBlockState());
        }
    }

    @WrapMethod(method = "build")
    private static InteractionResult onBuild(LivingBlock target, Level level, Operation<InteractionResult> original) {
        modifyBlockState(target);
        Herdiercraft.LOGGER.info(level.getBlockState(target.blockPosition().below()).getBlock().getClass().getName());
        if (BuildOnFarmlandBehavior.isUnbuildableSeed(target)) {
            return InteractionResult.FAIL;
        }
        return original.call(target, level);
    }

    @WrapMethod(method = "command")
    private void onCommand(LivingBlock block, ServerPlayer player, Vec3 pos, BlockPos blockPos, Direction direction, Operation<Void> original) {
        modifyBlockState(block);
        original.call(block, player, pos, blockPos, direction);
    }
}
