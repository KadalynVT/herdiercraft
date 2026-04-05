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
import net.minecraft.world.item.BuildAction;
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

import java.util.ArrayList;
import java.util.List;

public class WaxNearbyBlockInteraction implements OnInteract {
    @Override
    public @NonNull InteractionResult apply(Player player, @NonNull InteractionHand interactionHand, @NonNull Vec3 vec3, @NonNull LivingBlock livingBlock) {
        ItemStack itemStack = livingBlock.getItemStack();
        BlockPos pos = livingBlock.blockPosition();
        Level level = player.level();
        List<BlockPos> checks = List.of(pos, pos.below(), pos.east(), pos.south(), pos.north(), pos.west(), pos.above());
        for (BlockPos check : checks) {
            BlockState state = level.getBlockState(check);
            InteractionResult res = HoneycombItem.getWaxed(state).map(waxedState -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                }
                livingBlock.discard();
                level.setBlock(pos, waxedState, 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, waxedState));
                level.levelEvent(player, 3003, pos, 0);
                if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    BlockPos neighborPos = ChestBlock.getConnectedBlockPos(pos, state);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, neighborPos, GameEvent.Context.of(player, level.getBlockState(neighborPos)));
                    level.levelEvent(player, 3003, neighborPos, 0);
                }

                return (InteractionResult)InteractionResult.SUCCESS;
            }).orElse(InteractionResult.PASS);
            if (res == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }

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
