package live.kadalyn.herdiercraft.interact;

import live.kadalyn.herdiercraft.mixin.SignBlockInvoker;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.livingblock.LivingBlock;
import net.minecraft.world.entity.livingblock.interact.OnInteract;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ApplyToSignInteraction  implements OnInteract {
    @Override
    public @NonNull InteractionResult apply(Player player, @NonNull InteractionHand interactionHand, @NonNull Vec3 vec3, @NonNull LivingBlock livingBlock) {
        ItemStack itemStack = livingBlock.getItemStack();
        BlockPos pos = livingBlock.blockPosition();
        Level level = player.level();
        List<BlockPos> checks = List.of(pos, pos.below(), pos.east(), pos.south(), pos.north(), pos.west(), pos.above());
        for (BlockPos check : checks) {
            BlockState state = level.getBlockState(check);
            Block block = state.getBlock();
            if (block instanceof SignBlock) {
                if (((SignBlockInvoker)block).herdiercraft$useItemOn(
                    itemStack, state, level, check, player, null, null
                ) == InteractionResult.SUCCESS) {
                    livingBlock.discard();
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.FAIL;
    }
}
