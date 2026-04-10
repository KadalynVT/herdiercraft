package live.kadalyn.herdiercraft.behavior;

import live.kadalyn.herdiercraft.mixin.FlowerPotBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.entity.livingblock.LivingBlockGroup;
import net.minecraft.world.entity.livingblock.behavior.LivingBlockBehavior;
import net.minecraft.world.entity.livingblock.behavior.LivingBlockBehaviorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FlowersIntoPotsBehavior implements LivingBlockBehavior {
    private static final int REEVALUATION_TICKS = 10;
    public static final double CHECK_RADIUS = 3.0;
    private int lastTriggeredTick;

    @Override
    public boolean canStartUsing(LivingBlock entity) {
        return this.lastTriggeredTick + REEVALUATION_TICKS < entity.tickCount;
    }

    @Override
    public boolean tick(LivingBlock entity, ServerLevel level, int tickCount) {
        this.lastTriggeredTick = tickCount;
        BlockPos entityPos = entity.blockPosition();
        BlockState blockState = level.getBlockState(entityPos);
        if (blockState.is(Blocks.FLOWER_POT)) {
            BlockState newContents = FlowerPotBlockAccessor.getPottedByContent()
                .getOrDefault(entity.getBlockState().getBlock(), Blocks.AIR).defaultBlockState();
            if (!newContents.isAir()) {
                level.setBlock(entityPos, newContents, 3);
                Player owner = entity.getOwner();
                if (owner != null) owner.awardStat(Stats.POT_FLOWER);
                entity.discard();
                return true;
            }
        }
        return false;
    }

    public static LivingBlockBehaviorType placeInPot() {
        return LivingBlockBehaviorType.behaviorType(FlowersIntoPotsBehavior::new);
    }
}
