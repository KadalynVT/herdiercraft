package live.kadalyn.herdiercraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ActionItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class RotateAction extends ActionItem {
    public RotateAction(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(final UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos pos = context.getClickedPos();
            Direction face = context.getClickedFace();
            Vec3 loc = context.getClickLocation();
            BlockState newState = this.handleInteraction(
                serverPlayer,
                level.getBlockState(pos),
                level,
                pos, face, loc,
                true
            );
            if (newState == null) return InteractionResult.FAIL;
            level.setBlock(pos, newState, 18);
        }

        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("unchecked")
    private BlockState handleInteraction(ServerPlayer player, BlockState state, Level level, BlockPos pos, Direction face, Vec3 loc, boolean checkAgain) {
        /* Right-click handles directional type stuff:
           * north, west, east, south[, up/in_wall]
           * facing
           * type (slabs, top/bottom toggle only)
           * half (trapdoors)
           * axis (chains)
           * rotation
        */
        Holder<Block> block = state.typeHolder();
        StateDefinition<Block, BlockState> definition = ((Block)block.value()).getStateDefinition();
        Collection<Property<?>> properties = definition.getProperties();
        if (properties.isEmpty()) {
            return null;
        }

        double x = loc.x - pos.getX();
        double y = loc.y - pos.getY();
        double z = loc.z - pos.getZ();

        Property<?> prop = null;
        if (properties.contains(BlockStateProperties.NORTH)) {
            // Bars, glass panes, fences, tripwire
            // Technically also fire, chorus plants, and mushroom blocks...
            // we don't want those but idrc
            switch (face) {
                case Direction.UP:
                    if (properties.contains(BlockStateProperties.ATTACHED)) {
                        return cycleState(state, BlockStateProperties.ATTACHED);
                    }
                    break;
                case Direction.NORTH:
                    return cycleState(state, BlockStateProperties.NORTH);
                case Direction.SOUTH:
                    return cycleState(state, BlockStateProperties.SOUTH);
                case Direction.WEST:
                    return cycleState(state, BlockStateProperties.WEST);
                case Direction.EAST:
                    return cycleState(state, BlockStateProperties.EAST);
            }
            return null;
        }
        else if (properties.contains(BlockStateProperties.NORTH_WALL)) {
            return switch (face) {
                case Direction.UP -> cycleState(state, BlockStateProperties.UP);
                case Direction.NORTH -> cycleState(state, BlockStateProperties.NORTH_WALL);
                case Direction.SOUTH -> cycleState(state, BlockStateProperties.SOUTH_WALL);
                case Direction.WEST -> cycleState(state, BlockStateProperties.WEST_WALL);
                case Direction.EAST -> cycleState(state, BlockStateProperties.EAST_WALL);
                default -> null;
            };
        }
        else if (properties.contains(BlockStateProperties.NORTH_REDSTONE)) {
            if (x > 0.33 && x < 0.67 && z < 0.5) {
                prop = BlockStateProperties.NORTH_REDSTONE;
            }
            else if (x > 0.33 && x < 0.67 && z >= 0.5) {
                prop = BlockStateProperties.SOUTH_REDSTONE;
            }
            else if (z > 0.33 && z < 0.67 && x < 0.5) {
                prop = BlockStateProperties.WEST_REDSTONE;
            }
            else if (z > 0.33 && z < 0.67 && x >= 0.5) {
                prop = BlockStateProperties.EAST_REDSTONE;
            }
            if (prop != null) {
                return cycleState(state, prop);
            }
        } else if ((prop=this.getProperty(properties, "facing")) != null) {
            if (prop.getValueClass() != Direction.class) return null;
            // TODO: update other door part, if there is one
            switch (face) {
                case Direction.DOWN:
                    if (prop.getPossibleValues().contains(Direction.DOWN)) {
                        // Dispensers, hoppers
                        return state.setValue(BlockStateProperties.FACING, Direction.DOWN);
                    }
                    // fall-through
                case Direction.UP:
                    if (properties.contains(BlockStateProperties.FACING)) {
                        // Dispensers
                        return state.setValue(BlockStateProperties.FACING, Direction.UP);
                    }
                    // Change door hinge
                    if (properties.contains(DoorBlock.HINGE)) {
                        return cycleState(state, DoorBlock.HINGE);
                    }
                    else if (properties.contains(BlockStateProperties.HALF)) {
                        // trapdoors and stairs
                        return cycleState(state, BlockStateProperties.HALF);
                    }
                    else if (properties.contains(BedBlock.PART)) {
                        // bed half
                        return cycleState(state, BedBlock.PART);
                    }
                    break;
                case Direction.NORTH:
                    return state.setValue((Property<Direction>)prop, Direction.SOUTH);
                case Direction.SOUTH:
                    return state.setValue((Property<Direction>)prop, Direction.NORTH);
                case Direction.WEST:
                    return state.setValue((Property<Direction>)prop, Direction.EAST);
                case Direction.EAST:
                    return state.setValue((Property<Direction>)prop, Direction.WEST);
            }
            return null;
        } else if (properties.contains(SlabBlock.TYPE)) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            return switch (type) {
                case TOP -> state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                case BOTTOM -> state.setValue(SlabBlock.TYPE, SlabType.TOP);
                default -> null;
            };
        } else if (properties.contains(BlockStateProperties.AXIS)) {
            // Chains TODO
            return cycleState(state, BlockStateProperties.AXIS);
        } else if (properties.contains(BlockStateProperties.ROTATION_16)) {
            // Heads, signs, hanging signs, banners
            // TODO: Face the player
            return null;
        } else if (checkAgain) {
            // Check the block next to it
            pos = pos.relative(face);
            BlockState newState = level.getBlockState(pos);
            newState = this.handleInteraction(player, newState, level, pos, face, loc, false);
            if (newState != null) {
                // uhhh sry
                level.setBlock(pos, newState, 18);
                return state;
            }
        }
        return null;
    }

    private Property<?> getProperty(Collection<Property<?>> properties, String propertyName) {
        for (Property<?> property : properties) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    private boolean hasProperty(Collection<Property<?>> properties, String propertyName) {
        return this.getProperty(properties, propertyName) != null;
    }

    @Override
    public boolean actionOnBlock(Player player, final @NonNull BlockPos pos, final @NonNull Direction direction) {
        /* Left-click handles other state changes:
           * toggle between ground and wall blocks (torches, heads, signs)
           * toggle between half/full door/bed
           * attached (hanging signs)
           * "punch out" parts of stairs, avoiding full block and slab options
         */
        if (player.isSpectator()) {
            return false;
        }
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        switch (block) {
            case WallTorchBlock _ ->
                level.setBlock(pos, Blocks.TORCH.defaultBlockState(), 18);
            case TorchBlock _ ->
                level.setBlock(pos, Blocks.WALL_TORCH.defaultBlockState(), 18);
            // TODO: copy redstone state
            case RedstoneWallTorchBlock _ ->
                level.setBlock(pos, Blocks.REDSTONE_TORCH.defaultBlockState(), 18);
            case RedstoneTorchBlock _ ->
                level.setBlock(pos, Blocks.REDSTONE_WALL_TORCH.defaultBlockState(), 18);
            case SkullBlock skullBlock -> {
                Direction facing = this.rotation16ToFacing(state.getValue(SkullBlock.ROTATION));
                if (facing == null) return false;
                Block b = switch (skullBlock.getType()) {
                    case SkullBlock.Types.SKELETON -> Blocks.SKELETON_WALL_SKULL;
                    case SkullBlock.Types.ZOMBIE -> Blocks.ZOMBIE_WALL_HEAD;
                    case SkullBlock.Types.CREEPER -> Blocks.CREEPER_WALL_HEAD;
                    case SkullBlock.Types.DRAGON -> Blocks.DRAGON_WALL_HEAD;
                    case SkullBlock.Types.PIGLIN -> Blocks.PIGLIN_WALL_HEAD;
                    case SkullBlock.Types.WITHER_SKELETON -> Blocks.WITHER_SKELETON_WALL_SKULL;
                    default -> Blocks.PLAYER_WALL_HEAD;
                };
                BlockState newState = b.defaultBlockState();
                newState.setValue(WallSkullBlock.FACING, facing);
                level.setBlock(pos, newState, 18);
            }
            case WallSkullBlock skullBlock -> {
                Integer rot = this.facingToRotation16(state.getValue(WallSkullBlock.FACING));
                if (rot == null) return false;
                Block b = switch (skullBlock.getType()) {
                    case SkullBlock.Types.SKELETON -> Blocks.SKELETON_SKULL;
                    case SkullBlock.Types.ZOMBIE -> Blocks.ZOMBIE_HEAD;
                    case SkullBlock.Types.CREEPER -> Blocks.CREEPER_HEAD;
                    case SkullBlock.Types.DRAGON -> Blocks.DRAGON_HEAD;
                    case SkullBlock.Types.PIGLIN -> Blocks.PIGLIN_HEAD;
                    case SkullBlock.Types.WITHER_SKELETON -> Blocks.WITHER_SKELETON_SKULL;
                    default -> Blocks.PLAYER_HEAD;
                };
                BlockState newState = b.defaultBlockState();
                newState.setValue(SkullBlock.ROTATION, rot);
                level.setBlock(pos, newState, 18);
            }
            case StandingSignBlock signBlock -> {
                Integer rot = state.getValue(SkullBlock.ROTATION);
                player.sendOverlayMessage(Component.literal(String.format("sign %d", rot)));
                Direction facing = this.rotation16ToFacing(rot);
                if (facing == null) return false;
                Block b = switch (signBlock.type().name()) {
                    case "acacia" -> Blocks.ACACIA_WALL_SIGN;
                    case "bamboo" -> Blocks.BAMBOO_WALL_SIGN;
                    case "birch" -> Blocks.BIRCH_WALL_SIGN;
                    case "cherry" -> Blocks.CHERRY_WALL_SIGN;
                    case "crimson" -> Blocks.CRIMSON_WALL_SIGN;
                    case "dark_oak" -> Blocks.DARK_OAK_WALL_SIGN;
                    case "jungle" -> Blocks.JUNGLE_WALL_SIGN;
                    case "mangrove" -> Blocks.MANGROVE_WALL_SIGN;
                    case "pale_oak" -> Blocks.PALE_OAK_WALL_SIGN;
                    case "spruce" -> Blocks.SPRUCE_WALL_SIGN;
                    case "warped" -> Blocks.WARPED_WALL_SIGN;
                    default -> Blocks.OAK_WALL_SIGN;
                };
                BlockState newState = b.defaultBlockState();
                newState.setValue(WallSignBlock.FACING, facing);
                newState.setValue(WallSignBlock.WATERLOGGED, state.getValue(StandingSignBlock.WATERLOGGED));
                // TODO: save text/color/wax
                level.setBlock(pos, newState, 18);
            }
            case WallSignBlock signBlock -> {
                Integer rot = this.facingToRotation16(state.getValue(WallSkullBlock.FACING));
                if (rot == null) return false;
                Block b = switch (signBlock.type().name()) {
                    case "acacia" -> Blocks.ACACIA_SIGN;
                    case "bamboo" -> Blocks.BAMBOO_SIGN;
                    case "birch" -> Blocks.BIRCH_SIGN;
                    case "cherry" -> Blocks.CHERRY_SIGN;
                    case "crimson" -> Blocks.CRIMSON_SIGN;
                    case "dark_oak" -> Blocks.DARK_OAK_SIGN;
                    case "jungle" -> Blocks.JUNGLE_SIGN;
                    case "mangrove" -> Blocks.MANGROVE_SIGN;
                    case "pale_oak" -> Blocks.PALE_OAK_SIGN;
                    case "spruce" -> Blocks.SPRUCE_SIGN;
                    case "warped" -> Blocks.WARPED_SIGN;
                    default -> Blocks.OAK_SIGN;
                };
                BlockState newState = b.defaultBlockState();
                newState.setValue(StandingSignBlock.ROTATION, rot);
                newState.setValue(StandingSignBlock.WATERLOGGED, state.getValue(WallSignBlock.WATERLOGGED));
                // TODO: save text/color/wax
                level.setBlock(pos, newState, 18);
            }
            case CeilingHangingSignBlock _ -> {
                // Should this switch to WallHangingSignBlock instead?
                BlockState newState = cycleState(state, CeilingHangingSignBlock.ATTACHED);
                level.setBlock(pos, newState, 18);
            }
            case StairBlock _ -> {
                // TODO
                return false;
            }
            default -> { return false; }
        }
        return true;
    }

    private Direction rotation16ToFacing(Integer rot) {
        return switch (rot) {
            case 15, 0, 1 -> Direction.NORTH;
            case 3, 4, 5 -> Direction.EAST;
            case 7, 8, 9 -> Direction.SOUTH;
            case 11, 12, 13 -> Direction.WEST;
            default -> null;
        };
    }

    private Integer facingToRotation16(Direction facing) {
        return switch (facing) {
            case Direction.NORTH -> 0;
            case Direction.EAST -> 4;
            case Direction.SOUTH -> 8;
            case Direction.WEST -> 12;
            default -> null;
        };
    }

    private static <T extends Comparable<T>> BlockState cycleState(final BlockState state, final Property<T> property) {
        return state.setValue(property, getRelative(property.getPossibleValues(), state.getValue(property)));
    }

    private static <T> T getRelative(final Iterable<T> collection, final @Nullable T current) {
        return (T)Util.findNextInIterable(collection, current);
    }
}
