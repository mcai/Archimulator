package archimulator.incubator.noc;

public enum  Direction {
    LOCAL,
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public Direction getReflexDirection() {
        switch (this) {
            case LOCAL:
                return LOCAL;
            case NORTH:
                return SOUTH;
            case EAST:
                return WEST;
            case SOUTH:
                return NORTH;
            case WEST:
                return EAST;
            default:
                throw new IllegalArgumentException(String.format("%s", this));
        }
    }
}
