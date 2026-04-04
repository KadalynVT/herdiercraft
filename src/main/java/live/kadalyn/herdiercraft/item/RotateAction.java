package live.kadalyn.herdiercraft.item;

import live.kadalyn.herdiercraft.util.CardinalDirection;
import live.kadalyn.herdiercraft.util.Quadrant3D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RotateAction extends ActionItem {
    public static final Identifier ROTATE_RANGE_MODIFIER_ID = Identifier.withDefaultNamespace("rotate_range");

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
            BlockState state = level.getBlockState(pos);
            BlockState newState = this.handleInteraction(
                serverPlayer,
                state,
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
           * "punch out" parts of stairs, avoiding full block and slab options
             * this is facing, half, & shape properties
           * north, west, east, south[, up/in_wall]
           * facing
           * type (slabs, top/bottom toggle only)
           * half (trapdoors)
           * axis (chains)
           * rotation
           * shape (for rails only)
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
        if (state.getBlock() instanceof StairBlock) {
            // To differentiate 0.5
            switch (face) {
                case UP -> y += 0.1;
                case DOWN -> y -= 0.1;
                case NORTH -> z += 0.1;
                case EAST -> x -= 0.1;
                case WEST -> x += 0.1;
                case SOUTH -> z -= 0.1;
            }
            Quadrant3D section = Quadrant3D.fromUnitXYZ(x, y, z);
            //player.sendOverlayMessage(Component.literal(String.format("pos: %f, %f, %f; quad: %s, face: %s", x, y, z, section, face)));
            Half half = state.getValue(StairBlock.HALF);
            Direction facing = state.getValue(StairBlock.FACING);
            StairsShape shape = state.getValue(StairBlock.SHAPE);
            if (Boolean.TRUE.equals(section.isFilled(state))) {
                // Do our best to remove the one quadrant
                BlockState newState = state;
                if (section.isTop() && half == Half.TOP) {
                    // Flip it first...
                    newState = newState.setValue(StairBlock.HALF, Half.BOTTOM);
                }
                else if (section.isBottom() && half == Half.BOTTOM) {
                    newState = newState.setValue(StairBlock.HALF, Half.TOP);
                }

                if (Boolean.TRUE.equals(section.isFilled(newState))) {
                    newState = switch (shape) {
                        case INNER_LEFT -> switch (section) {
                            case NW_TOP, NW_BOTTOM -> removeFromInnerLeft(newState, facing, Direction.WEST, Direction.NORTH, Direction.EAST);
                            case NE_TOP, NE_BOTTOM -> removeFromInnerLeft(newState, facing, Direction.NORTH, Direction.EAST, Direction.SOUTH);
                            case SW_TOP, SW_BOTTOM -> removeFromInnerLeft(newState, facing, Direction.SOUTH, Direction.WEST, Direction.NORTH);
                            case SE_TOP, SE_BOTTOM -> removeFromInnerLeft(newState, facing, Direction.EAST, Direction.SOUTH, Direction.WEST);
                        };
                        case INNER_RIGHT -> switch (section) {
                            case NW_TOP, NW_BOTTOM -> removeFromInnerRight(newState, facing, Direction.NORTH, Direction.WEST, Direction.SOUTH);
                            case NE_TOP, NE_BOTTOM -> removeFromInnerRight(newState, facing, Direction.EAST, Direction.NORTH, Direction.WEST);
                            case SW_TOP, SW_BOTTOM -> removeFromInnerRight(newState, facing, Direction.WEST, Direction.SOUTH, Direction.EAST);
                            case SE_TOP, SE_BOTTOM -> removeFromInnerRight(newState, facing, Direction.SOUTH, Direction.EAST, Direction.NORTH);
                        };
                        case STRAIGHT -> switch (section) {
                            case NW_TOP, NW_BOTTOM -> removeFromStraight(newState, facing, Direction.NORTH, Direction.WEST);
                            case NE_TOP, NE_BOTTOM -> removeFromStraight(newState, facing, Direction.EAST, Direction.NORTH);
                            case SW_TOP, SW_BOTTOM -> removeFromStraight(newState, facing, Direction.WEST, Direction.SOUTH);
                            case SE_TOP, SE_BOTTOM -> removeFromStraight(newState, facing, Direction.SOUTH, Direction.EAST);
                        };
                        default -> newState;
                    };
                    if (state != newState && newState != null) {
                        playSound(level, pos, newState.getSoundType().getBreakSound());
                    }
                }
                return newState;
            }
            // Fill an empty quadrant as best we can...
            BlockState newState = switch (shape) {
                case OUTER_LEFT -> switch (section) {
                    case NW_TOP, NW_BOTTOM -> addToOuterLeft(state, facing, Direction.EAST, Direction.SOUTH, Direction.WEST);
                    case NE_TOP, NE_BOTTOM -> addToOuterLeft(state, facing, Direction.SOUTH, Direction.WEST, Direction.NORTH);
                    case SW_TOP, SW_BOTTOM -> addToOuterLeft(state, facing, Direction.NORTH, Direction.EAST, Direction.SOUTH);
                    case SE_TOP, SE_BOTTOM -> addToOuterLeft(state, facing, Direction.WEST, Direction.NORTH, Direction.EAST);
                };
                case OUTER_RIGHT -> switch (section) {
                    case NW_TOP, NW_BOTTOM -> addToOuterRight(state, facing, Direction.SOUTH, Direction.EAST, Direction.NORTH);
                    case NE_TOP, NE_BOTTOM -> addToOuterRight(state, facing, Direction.WEST, Direction.SOUTH, Direction.EAST);
                    case SW_TOP, SW_BOTTOM -> addToOuterRight(state, facing, Direction.EAST, Direction.NORTH, Direction.WEST);
                    case SE_TOP, SE_BOTTOM -> addToOuterRight(state, facing, Direction.NORTH, Direction.WEST, Direction.SOUTH);
                };
                case STRAIGHT -> switch (section) {
                    case NW_TOP, NW_BOTTOM -> addToStraight(state, facing, Direction.EAST, Direction.SOUTH);
                    case NE_TOP, NE_BOTTOM -> addToStraight(state, facing, Direction.SOUTH, Direction.WEST);
                    case SW_TOP, SW_BOTTOM -> addToStraight(state, facing, Direction.NORTH, Direction.EAST);
                    case SE_TOP, SE_BOTTOM -> addToStraight(state, facing, Direction.WEST, Direction.NORTH);
                };
                default -> null;
            };
            if (newState != null) {
                playSound(level, pos, state.getSoundType().getPlaceSound());
            }
            return newState;
        }
        else if (properties.contains(BlockStateProperties.NORTH)) {
            // Bars, glass panes, fences, tripwire
            // Technically also fire, chorus plants, and mushroom blocks...
            // we don't want those but idrc
            prop = switch (face) {
                case Direction.UP -> properties.contains(BlockStateProperties.ATTACHED) ? BlockStateProperties.ATTACHED : null;
                case Direction.NORTH -> BlockStateProperties.NORTH;
                case Direction.SOUTH -> BlockStateProperties.SOUTH;
                case Direction.WEST -> BlockStateProperties.WEST;
                case Direction.EAST -> BlockStateProperties.EAST;
                default -> null;
            };
            if (prop != null) {
                playSound(level, pos, SoundEvents.PINK_PETALS_STEP);
                return cycleState(state, prop);
            }
            return null;
        }
        else if (properties.contains(BlockStateProperties.NORTH_WALL)) {
            prop = switch (face) {
                case Direction.UP -> BlockStateProperties.UP;
                case Direction.NORTH -> BlockStateProperties.NORTH_WALL;
                case Direction.SOUTH -> BlockStateProperties.SOUTH_WALL;
                case Direction.WEST -> BlockStateProperties.WEST_WALL;
                case Direction.EAST -> BlockStateProperties.EAST_WALL;
                default -> null;
            };
            if (prop != null) {
                playSound(level, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT);
                return cycleState(state, prop);
            }
            return null;
        }
        else if (properties.contains(BlockStateProperties.NORTH_REDSTONE)) {
            player.sendOverlayMessage(Component.literal("??"));
            CardinalDirection dir = CardinalDirection.fromUnitXZ(x, z);
            List<Property<RedstoneSide>> props = switch (dir) {
                case NW -> Arrays.asList(BlockStateProperties.NORTH_REDSTONE, BlockStateProperties.WEST_REDSTONE);
                case N -> List.of(BlockStateProperties.NORTH_REDSTONE);
                case NE -> Arrays.asList(BlockStateProperties.NORTH_REDSTONE, BlockStateProperties.EAST_REDSTONE);
                case W -> List.of(BlockStateProperties.WEST_REDSTONE);
                case C -> Arrays.asList(BlockStateProperties.NORTH_REDSTONE, BlockStateProperties.WEST_REDSTONE, BlockStateProperties.EAST_REDSTONE, BlockStateProperties.SOUTH_REDSTONE);
                case E -> List.of(BlockStateProperties.EAST_REDSTONE);
                case SW -> Arrays.asList(BlockStateProperties.SOUTH_REDSTONE, BlockStateProperties.WEST_REDSTONE);
                case S -> List.of(BlockStateProperties.SOUTH_REDSTONE);
                case SE -> Arrays.asList(BlockStateProperties.SOUTH_REDSTONE, BlockStateProperties.EAST_REDSTONE);
            };

            int minValue = 2;
            for (Property<RedstoneSide> p : props) {
                if (state.getValue(p) == RedstoneSide.NONE) {
                    minValue = 0;
                    break;
                }
                else if (state.getValue(p) == RedstoneSide.SIDE) {
                    minValue = 1;
                }
            }

            RedstoneSide side = switch (minValue) {
                case 0 -> RedstoneSide.SIDE;
                case 1 -> RedstoneSide.UP;
                default -> RedstoneSide.NONE;
            };
            for (Property<RedstoneSide> p : props) {
                state = state.setValue(p, side);
            }
            playSound(level, pos, SoundEvents.FURNACE_FIRE_CRACKLE);
            return state;
        }
        else if (properties.contains(RailBlock.SHAPE)) {
            // Rail separated into 3x3 squares
            //  csc  c = curved rail, around that corner
            //  srs  s = straight rail, in that direction
            //  csc  r = rotate rail
            // If the rail is already the correct straight,
            // clicking that s again will raise it on that side
            CardinalDirection dir = CardinalDirection.fromUnitXZ(x, z);
            RailShape currentShape = state.getValue(RailBlock.SHAPE);
            RailShape newShape = switch (dir) {
                case NW -> RailShape.NORTH_WEST;
                case N, S -> RailShape.NORTH_SOUTH;
                case NE -> RailShape.NORTH_EAST;
                case W, E -> RailShape.EAST_WEST;
                case SW -> RailShape.SOUTH_WEST;
                case SE -> RailShape.SOUTH_EAST;
                case C -> switch(currentShape) {
                    case NORTH_SOUTH -> RailShape.EAST_WEST;
                    case EAST_WEST -> RailShape.NORTH_SOUTH;
                    case ASCENDING_EAST -> RailShape.ASCENDING_SOUTH;
                    case ASCENDING_SOUTH -> RailShape.ASCENDING_WEST;
                    case ASCENDING_WEST -> RailShape.ASCENDING_NORTH;
                    case ASCENDING_NORTH -> RailShape.ASCENDING_EAST;
                    case SOUTH_EAST -> RailShape.SOUTH_WEST;
                    case SOUTH_WEST -> RailShape.NORTH_WEST;
                    case NORTH_WEST -> RailShape.NORTH_EAST;
                    case NORTH_EAST -> RailShape.SOUTH_EAST;
                };
            };

            // Toggle ascending if we're clicking the same straight direction
            // Rotate ascended rail if we're clicking a different ascendable direction
            if (
                (newShape == RailShape.EAST_WEST || newShape == RailShape.NORTH_SOUTH)
                && (currentShape == newShape || currentShape.isSlope() && switch (dir) {
                    case N -> currentShape != RailShape.ASCENDING_NORTH;
                    case E -> currentShape != RailShape.ASCENDING_EAST;
                    case W -> currentShape != RailShape.ASCENDING_WEST;
                    case S -> currentShape != RailShape.ASCENDING_SOUTH;
                    default -> false;
                })
            ) {
                newShape = switch (dir) {
                    case N -> RailShape.ASCENDING_NORTH;
                    case E -> RailShape.ASCENDING_EAST;
                    case W -> RailShape.ASCENDING_WEST;
                    default -> RailShape.ASCENDING_SOUTH;
                };
            }
            playSound(level, pos, state.getSoundType().getPlaceSound());
            return state.setValue(RailBlock.SHAPE, newShape);
        }
        else if ((prop=this.getProperty(properties, "facing")) != null) {
            if (prop.getValueClass() != Direction.class) return null;
            BlockState newState = null;
            switch (face) {
                case Direction.DOWN:
                    if (prop.getPossibleValues().contains(Direction.DOWN)) {
                        // Dispensers, hoppers
                        newState = state.setValue((Property<Direction>)prop, Direction.DOWN);
                        break;
                    }
                    // fall-through
                case Direction.UP:
                    if (prop.getPossibleValues().contains(Direction.UP)) {
                        // Dispensers
                        newState = state.setValue((Property<Direction>)prop, Direction.UP);
                    }
                    else if (prop.getPossibleValues().contains(Direction.DOWN)) {
                        // Hoppers
                        newState = state.setValue((Property<Direction>)prop, Direction.DOWN);
                        break;
                    }
                    else if (properties.contains(DoorBlock.HINGE)) {
                        // Change door hinge
                        newState = cycleState(state, DoorBlock.HINGE);
                    }
                    else if (properties.contains(BlockStateProperties.HALF)) {
                        // trapdoors and stairs
                        newState = cycleState(state, BlockStateProperties.HALF);
                    }
                    else {
                        // I don't like the order it cycles in normally
                        newState = state.setValue(
                            (Property<Direction>)prop,
                            switch (state.getValue((Property<Direction>)prop)) {
                                case NORTH -> Direction.EAST;
                                case EAST -> Direction.SOUTH;
                                case SOUTH -> Direction.WEST;
                                default -> Direction.NORTH;
                            }
                        );
                    }
                    break;
                case Direction.NORTH:
                    newState = state.setValue((Property<Direction>)prop, Direction.NORTH);
                    break;
                case Direction.SOUTH:
                    newState = state.setValue((Property<Direction>)prop, Direction.SOUTH);
                    break;
                case Direction.WEST:
                    newState = state.setValue((Property<Direction>)prop, Direction.WEST);
                    break;
                case Direction.EAST:
                    newState = state.setValue((Property<Direction>)prop, Direction.EAST);
                    break;
            }

            if (state.getBlock() instanceof DoorBlock) {
                // If there's an attached door half, also change it
                DoubleBlockHalf check;
                BlockPos otherPos;
                switch (state.getValue(DoorBlock.HALF)) {
                    case LOWER -> {
                        check = DoubleBlockHalf.UPPER;
                        otherPos = pos.above();
                    }
                    case UPPER -> {
                        check = DoubleBlockHalf.LOWER;
                        otherPos = pos.below();
                    }
                    default -> { return newState; }
                };
                BlockState other = level.getBlockState(otherPos);
                if (other.getBlock() instanceof DoorBlock && other.getValue(DoorBlock.HALF) == check) {
                    level.setBlock(
                        otherPos,
                        other.setValue(DoorBlock.FACING, newState.getValue(DoorBlock.FACING))
                            .setValue(DoorBlock.HINGE, newState.getValue(DoorBlock.HINGE)),
                        18
                    );
                }
            }
            else if (
                state.getBlock() instanceof BedBlock
                && state.getValue(BedBlock.FACING) != newState.getValue(BedBlock.FACING)
            ) {
                BedPart check;
                BlockPos currentPos, newPos;
                switch (state.getValue(BedBlock.PART)) {
                    case FOOT -> {
                        check = BedPart.HEAD;
                        currentPos = pos.relative(state.getValue(BedBlock.FACING));
                        newPos = pos.relative(newState.getValue(BedBlock.FACING));
                    }
                    case HEAD -> {
                        check = BedPart.FOOT;
                        currentPos = pos.relative(state.getValue(BedBlock.FACING).getOpposite());
                        newPos = pos.relative(newState.getValue(BedBlock.FACING).getOpposite());
                    }
                    default -> { return newState; }
                };

                BlockState other = level.getBlockState(currentPos);
                if (other.getBlock() instanceof BedBlock && other.getValue(BedBlock.PART) == check) {
                    if (!level.getBlockState(newPos).canBeReplaced()) {
                        playSound(level, pos, SoundEvents.CHEST_LOCKED);
                        return null; // don't rotate
                    }
                    level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 18);
                    level.setBlock(newPos, other.setValue(BedBlock.FACING, newState.getValue(BedBlock.FACING)), 18);
                }
            }
            playSound(level, pos, SoundEvents.PINK_PETALS_STEP);
            return newState;
        }
        else if (properties.contains(SlabBlock.TYPE)) {
            SlabType type = state.getValue(SlabBlock.TYPE);
            playSound(level, pos, state.getSoundType().getPlaceSound());
            return switch (type) {
                case TOP -> state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                case BOTTOM -> state.setValue(SlabBlock.TYPE, SlabType.TOP);
                default -> null;
            };
        }
        else if (properties.contains(BlockStateProperties.AXIS)) {
            // Chains
            playSound(level, pos, state.getSoundType().getPlaceSound());
            return state.setValue(BlockStateProperties.AXIS, switch(face) {
                case Direction.EAST, Direction.WEST -> Direction.Axis.X;
                case Direction.NORTH, Direction.SOUTH -> Direction.Axis.Z;
                default -> Direction.Axis.Y;
            });
        }
        else if (properties.contains(BlockStateProperties.ROTATION_16)) {
            // Heads, signs, hanging signs, banners
            // basing it on player position was annoying to play with
            playSound(level, pos, SoundEvents.PINK_PETALS_STEP);
            return cycleState(state, BlockStateProperties.ROTATION_16);
        }
        else if (properties.contains(PointedDripstoneBlock.TIP_DIRECTION)) {
            playSound(level, pos, state.getSoundType().getPlaceSound());
            return cycleState(state, PointedDripstoneBlock.TIP_DIRECTION);
        }
        else if (checkAgain) {
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

    private BlockState removeFromInner(BlockState state, Direction facing, Direction middle, Direction tail, StairsShape outer) {
        if (facing == middle) return state.setValue(StairBlock.SHAPE, outer);
        if (facing == tail) return state.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
        return null;
    }

    private BlockState removeFromInnerLeft(BlockState state, Direction facing, Direction turner, Direction middle, Direction tail) {
        if (facing == turner) return state.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT).setValue(StairBlock.FACING, turner.getCounterClockWise());
        return this.removeFromInner(state, facing, middle, tail, StairsShape.OUTER_RIGHT);
    }

    private BlockState removeFromInnerRight(BlockState state, Direction facing, Direction turner, Direction middle, Direction tail) {
        if (facing == turner) return state.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT).setValue(StairBlock.FACING, turner.getClockWise());
        return this.removeFromInner(state, facing, middle, tail, StairsShape.OUTER_LEFT);
    }

    private BlockState removeFromStraight(BlockState state, Direction facing, Direction left, Direction right) {
        if (facing == left) return state.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT);
        if (facing == right) return state.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT);
        return null;
    }

    private BlockState addToOuterLeft(BlockState state, Direction facing, Direction turner, Direction middle, Direction tail) {
        if (facing == turner) return state.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT).setValue(StairBlock.FACING, turner.getCounterClockWise());
        return this.removeFromInner(state, facing, middle, tail, StairsShape.INNER_RIGHT);
    }

    private BlockState addToOuterRight(BlockState state, Direction facing, Direction turner, Direction middle, Direction tail) {
        if (facing == turner) return state.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT).setValue(StairBlock.FACING, turner.getClockWise());
        return this.removeFromInner(state, facing, middle, tail, StairsShape.INNER_LEFT);
    }

    private BlockState addToStraight(BlockState state, Direction facing, Direction left, Direction right) {
        if (facing == left) return state.setValue(StairBlock.SHAPE, StairsShape.INNER_LEFT);
        if (facing == right) return state.setValue(StairBlock.SHAPE, StairsShape.INNER_RIGHT);
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

    @Override
    public boolean actionOnBlock(Player player, final @NonNull BlockPos pos, final @NonNull Direction direction) {
        /* Left-click handles other state changes:
           * toggle between ground and wall blocks (torches, heads, signs)
           * toggle between half/full door/bed
           * attached (hanging signs)
         */
        if (player.isSpectator()) {
            return false;
        }
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        switch (block) {
            case WallTorchBlock _ -> {
                level.setBlock(pos, Blocks.TORCH.defaultBlockState(), 18);
                playSound(level, pos, state.getSoundType().getPlaceSound());
            }
            case TorchBlock _ -> {
                level.setBlock(pos, Blocks.WALL_TORCH.defaultBlockState(), 18);
                playSound(level, pos, state.getSoundType().getPlaceSound());
            }
            // TODO: copy redstone state
            case RedstoneWallTorchBlock _ ->{
                level.setBlock(pos, Blocks.REDSTONE_TORCH.defaultBlockState(), 18);
                playSound(level, pos, state.getSoundType().getPlaceSound());
            }
            case RedstoneTorchBlock _ -> {
                level.setBlock(pos, Blocks.REDSTONE_WALL_TORCH.defaultBlockState(), 18);
                playSound(level, pos, state.getSoundType().getPlaceSound());
            }
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
                playSound(level, pos, newState.getSoundType().getPlaceSound());
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
                playSound(level, pos, newState.getSoundType().getPlaceSound());
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
                playSound(level, pos, newState.getSoundType().getPlaceSound());
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
                playSound(level, pos, newState.getSoundType().getPlaceSound());
            }
            case CeilingHangingSignBlock _ -> {
                // Should this switch to WallHangingSignBlock instead?
                BlockState newState = cycleState(state, CeilingHangingSignBlock.ATTACHED);
                level.setBlock(pos, newState, 18);
                playSound(level, pos, SoundEvents.CHAIN_PLACE);
            }
            case BedBlock _ -> {
                switch (state.getValue(BedBlock.PART)) {
                    case FOOT -> {
                        BlockPos headPos = pos.relative(state.getValue(BedBlock.FACING));
                        BlockState head = level.getBlockState(headPos);
                        if (head.getBlock() instanceof BedBlock && head.getValue(BedBlock.PART) == BedPart.HEAD) {
                            level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 18);
                            playSound(level, pos, state.getSoundType().getBreakSound());
                        }
                        else if (head.canBeReplaced()) {
                            head = state.setValue(BedBlock.PART, BedPart.HEAD);
                            level.setBlock(headPos, head, 18);
                            playSound(level, pos, state.getSoundType().getPlaceSound());
                        }
                        else {
                            playSound(level, pos, SoundEvents.CHEST_LOCKED);
                        }
                    }
                    case HEAD -> {
                        BlockPos footPos = pos.relative(state.getValue(BedBlock.FACING).getOpposite());
                        BlockState foot = level.getBlockState(footPos);
                        if (foot.getBlock() instanceof BedBlock && foot.getValue(BedBlock.PART) == BedPart.FOOT) {
                            level.setBlock(footPos, Blocks.AIR.defaultBlockState(), 18);
                            playSound(level, pos, state.getSoundType().getBreakSound());
                        }
                        else if (foot.canBeReplaced()) {
                            foot = state.setValue(BedBlock.PART, BedPart.FOOT);
                            level.setBlock(footPos, foot, 18);
                            playSound(level, pos, state.getSoundType().getPlaceSound());
                        }
                        else {
                            playSound(level, pos, SoundEvents.CHEST_LOCKED);
                        }
                    }
                }
            }
            case DoorBlock _ -> {
                switch (state.getValue(DoorBlock.HALF)) {
                    case LOWER -> {
                        BlockPos upperPos = pos.above();
                        BlockState upper = level.getBlockState(upperPos);
                        if (upper.getBlock() instanceof DoorBlock && upper.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                            level.setBlock(upperPos, Blocks.AIR.defaultBlockState(), 18);
                            playSound(level, pos, state.getSoundType().getBreakSound());
                        }
                        else if (upper.canBeReplaced()) {
                            upper = state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
                            level.setBlock(upperPos, upper, 18);
                            playSound(level, pos, state.getSoundType().getPlaceSound());
                        }
                        else {
                            playSound(level, pos, state.getSoundType().getFallSound());
                        }
                    }
                    case UPPER -> {
                        BlockPos lowerPos = pos.below();
                        BlockState lower = level.getBlockState(lowerPos);
                        if (lower.getBlock() instanceof DoorBlock && lower.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                            level.setBlock(lowerPos, Blocks.AIR.defaultBlockState(), 18);
                            playSound(level, pos, state.getSoundType().getBreakSound());
                        }
                        else if (lower.canBeReplaced()) {
                            lower = state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
                            level.setBlock(lowerPos, lower, 18);
                            playSound(level, pos, state.getSoundType().getPlaceSound());
                        }
                        else {
                            playSound(level, pos, state.getSoundType().getFallSound());
                        }
                    }
                }
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

    private void playSound(Level level, BlockPos pos, SoundEvent sound) {
        if (!level.isClientSide()) {
            SoundType soundType = level.getBlockState(pos).getSoundType();
            level.playSound(null, pos, sound, SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        }
    }

    private static <T extends Comparable<T>> BlockState cycleState(final BlockState state, final Property<T> property) {
        return state.setValue(property, getRelative(property.getPossibleValues(), state.getValue(property)));
    }

    private static <T> T getRelative(final Iterable<T> collection, final @Nullable T current) {
        return (T)Util.findNextInIterable(collection, current);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    Item.BASE_ATTACK_DAMAGE_ID,
                    (double)-1.0F,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ),
                EquipmentSlotGroup.MAINHAND
            ).add(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(
                    ROTATE_RANGE_MODIFIER_ID,
                    (double)50.0F,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
            ).build();
    }
}
