package archimulator.uncore.net.basic;

/**
 * Direction.
 *
 * @author Min Cai
 */
public enum Direction {
    LOCAL,
    LEFT,
    RIGHT,
    UP,
    DOWN;

    public Direction opposite() {
        switch (this) {
            case LOCAL:
                return LOCAL;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            default:
                throw new IllegalArgumentException(this + "");
        }
    }
}
