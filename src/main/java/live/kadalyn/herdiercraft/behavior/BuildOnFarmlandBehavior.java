package live.kadalyn.herdiercraft.behavior;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.entity.livingblock.Target;
import net.minecraft.world.entity.livingblock.behavior.BuildBehavior;
import net.minecraft.world.entity.livingblock.behavior.LivingBlockBehavior;
import net.minecraft.world.entity.livingblock.behavior.LivingBlockBehaviorType;
import net.minecraft.world.entity.livingblock.cognition.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class BuildOnFarmlandBehavior extends BuildBehavior {
    public static LivingBlockBehaviorType BUILD = LivingBlockBehaviorType.behaviorType(
        a -> new BuildOnFarmlandBehavior(a.inPursuitOf(Desires.BUILD), a.withIntentTo(LivingBlock.MOVE_TOWARDS))
    );

    public BuildOnFarmlandBehavior(final Prize<BuildTarget> target, final Intent<Target> move) {
        super(target, move);
    }

    @Override
    public boolean tick(LivingBlock entity, ServerLevel level, int tickCount) {
        if (!isBuildableAtTarget(level)) {
            this.currentTarget = this.findTarget(level);
            if (this.currentTarget == null || !isBuildableAtTarget(level)) {
                this.target.forget();
                return false;
            }

            this.move.update(Target.exactlyAt(Vec3.atBottomCenterOf(this.currentTarget)));
        }
        return super.tick(entity, level, tickCount);
    }

    private boolean isBuildableAtTarget(ServerLevel level) {
        return (
            this.currentTarget != null && isBuildable(level, this.currentTarget)
        );
    }

    public static boolean isUnbuildableSeed(LivingBlock entity) {
        return isSeed(entity) && !isBuildable(entity);
    }

    public static boolean isSeed(LivingBlock entity) {
        // VILLAGER_PLANTABLE_SEEDS doesn't contain melon/pumpkin seeds
        // c:SEEDS doesn't contain carrot/potato
        return entity.isItem(ItemTags.VILLAGER_PLANTABLE_SEEDS) || entity.isItem(ConventionalItemTags.SEEDS);
    }

    public static boolean isBuildable(LivingBlock entity) {
        Level level = entity.level();
        BlockPos blockPos = entity.blockPosition();
        if (
            level.getBlockState(blockPos).canBeReplaced()
            && level.getBlockState(blockPos.below()).is(Blocks.FARMLAND)
        ) {
            return true;
        }
        if (level.getBlockState(blockPos).is(Blocks.FARMLAND)) {
            // Move entity up slightly so it's in a replaceable block...
            Vec3 pos = entity.position();
            entity.setPosRaw(pos.x, pos.y + 0.1, pos.z);
            return level.getBlockState(entity.blockPosition()).canBeReplaced();
        }
        return false;
    }

    public static boolean isBuildable(Level level, BlockPos blockPos) {
        return (
            (
                level.getBlockState(blockPos).canBeReplaced()
                && level.getBlockState(blockPos.below()).is(Blocks.FARMLAND)
            ) || level.getBlockState(blockPos).is(Blocks.FARMLAND)
        );
    }
}
