package live.kadalyn.herdiercraft.interact;

import live.kadalyn.herdiercraft.behavior.BuildOnFarmlandBehavior;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.entity.livingblock.interact.OnInteract;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BuildAction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class BuildItemInteraction implements OnInteract {
    @Override
    public @NonNull InteractionResult apply(Player player, @NonNull InteractionHand interactionHand, @NonNull Vec3 vec3, @NonNull LivingBlock livingBlock) {
        Level level = player.level();
        if (BuildOnFarmlandBehavior.isUnbuildableSeed(livingBlock)) {
            return InteractionResult.FAIL;
        }
        return BuildAction.build(livingBlock, level);
    }
}
