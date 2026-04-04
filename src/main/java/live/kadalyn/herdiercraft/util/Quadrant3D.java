package live.kadalyn.herdiercraft.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

public enum Quadrant3D {
    NW_BOTTOM,
    NE_BOTTOM,
    SW_BOTTOM,
    SE_BOTTOM,
    NW_TOP,
    NE_TOP,
    SW_TOP,
    SE_TOP;

    public static Quadrant3D fromUnitXYZ(double x, double y, double z) {
        if (y < 0.5) {
            if (x < 0.5) {
                if (z < 0.5) return NW_BOTTOM;
                else return SW_BOTTOM;
            } else {
                if (z < 0.5) return NE_BOTTOM;
                else return SE_BOTTOM;
            }
        } else {
            if (x < 0.5) {
                if (z < 0.5) return NW_TOP;
                else return SW_TOP;
            } else {
                if (z < 0.5) return NE_TOP;
                else return SE_TOP;
            }
        }
    }

    public boolean isBottom() {
        return switch (this) {
            case NW_BOTTOM, NE_BOTTOM, SW_BOTTOM, SE_BOTTOM -> true;
            default -> false;
        };
    }

    public boolean isTop() {
        return switch (this) {
            case NW_TOP, NE_TOP, SW_TOP, SE_TOP -> true;
            default -> false;
        };
    }

    public Boolean isFilled(BlockState state) {
        if (state.getBlock() instanceof StairBlock) {
            Half half = state.getValue(StairBlock.HALF);
            // facing+shape fills:
            // straight: fills both on facing
            // inner_*: fills both on facing plus
            //   _left: counter-clockwise facing (E -> NW)
            //   _right: clockwise facing (E -> SW)
            // outer_left: fills only counter-clockwise of facing (E -> NE)
            // outer_right: fills only clockwise of facing (E -> SE)
            return switch(half) {
                case Half.BOTTOM -> this.isBottom() || switch(this) {
                    case NW_TOP -> this.stairFillsNW(state);
                    case NE_TOP -> this.stairFillsNE(state);
                    case SW_TOP -> this.stairFillsSW(state);
                    case SE_TOP -> this.stairFillsSE(state);
                    default -> false; // unreachable
                };
                case Half.TOP -> this.isTop() || switch(this) {
                    case NW_BOTTOM -> this.stairFillsNW(state);
                    case NE_BOTTOM -> this.stairFillsNE(state);
                    case SW_BOTTOM -> this.stairFillsSW(state);
                    case SE_BOTTOM -> this.stairFillsSE(state);
                    default -> false; // unreachable
                };
            };
        }
        return null;
    }

    private boolean stairFillsNW(BlockState state) {
        Direction facing = state.getValue(StairBlock.FACING);
        StairsShape shape = state.getValue(StairBlock.SHAPE);
        return switch (shape) {
            case STRAIGHT -> switch (facing) {
                case NORTH, WEST -> true;
                default -> false;
            };
            case INNER_LEFT -> facing != Direction.SOUTH;
            case INNER_RIGHT -> facing != Direction.EAST;
            case OUTER_LEFT -> facing == Direction.NORTH;
            case OUTER_RIGHT -> facing == Direction.WEST;
        };
    }

    private boolean stairFillsNE(BlockState state) {
        Direction facing = state.getValue(StairBlock.FACING);
        StairsShape shape = state.getValue(StairBlock.SHAPE);
        return switch (shape) {
            case STRAIGHT -> switch (facing) {
                case NORTH, EAST -> true;
                default -> false;
            };
            case INNER_LEFT -> facing != Direction.WEST;
            case INNER_RIGHT -> facing != Direction.SOUTH;
            case OUTER_LEFT -> facing == Direction.EAST;
            case OUTER_RIGHT -> facing == Direction.NORTH;
        };
    }

    private boolean stairFillsSW(BlockState state) {
        Direction facing = state.getValue(StairBlock.FACING);
        StairsShape shape = state.getValue(StairBlock.SHAPE);
        return switch (shape) {
            case STRAIGHT -> switch (facing) {
                case SOUTH, WEST -> true;
                default -> false;
            };
            case INNER_LEFT -> facing != Direction.EAST;
            case INNER_RIGHT -> facing != Direction.NORTH;
            case OUTER_LEFT -> facing == Direction.WEST;
            case OUTER_RIGHT -> facing == Direction.SOUTH;
        };
    }

    private boolean stairFillsSE(BlockState state) {
        Direction facing = state.getValue(StairBlock.FACING);
        StairsShape shape = state.getValue(StairBlock.SHAPE);
        return switch (shape) {
            case STRAIGHT -> switch (facing) {
                case SOUTH, EAST -> true;
                default -> false;
            };
            case INNER_LEFT -> facing != Direction.NORTH;
            case INNER_RIGHT -> facing != Direction.WEST;
            case OUTER_LEFT -> facing == Direction.SOUTH;
            case OUTER_RIGHT -> facing == Direction.EAST;
        };
    }
}
