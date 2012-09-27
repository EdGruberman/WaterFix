package edgruberman.bukkit.waterfix;

import java.util.HashMap;
import java.util.Map;

/** individual block levels of fluids (water/lava) */
public enum FluidLevel {

    /** entire block is filled */
    FULL(0x0),

    /** one unit less than full */
    ALMOST_FULL(0x1),

    /** two units less than full */
    EVEN_MORE(0x2),

    /** three units less than full */
    SOME_MORE(0x3),

    /** half full and half empty */
    HALF(0x4),

    /** three units away from empty */
    SOME_LESS(0x5),

    /** two units away from empty */
    EVEN_LESS(0x6),

    /** one unit away from empty */
    ALMOST_EMPTY(0x7);



    /** first three bits */
    public final static byte DATA_MASK = 0x7;

    private final static Map<Byte, FluidLevel> BY_DATA = new HashMap<Byte, FluidLevel>();

    static {
        for (final FluidLevel level : FluidLevel.values()) FluidLevel.BY_DATA.put(level.data, level);
    }



    private final byte data;

    private FluidLevel(final int data) {
        this.data = (byte) data;
    }

    /** @return value associated with the {@link #DATA_MASK first three bits} of block data */
    public byte getData() {
        return this.data;
    }

    /** @return {@link FluidLevel} representing the {@link #DATA_MASK first three bits} of the given data value or null if none match */
    public static FluidLevel getByData(final byte data) {
        return FluidLevel.BY_DATA.get((byte) (data & FluidLevel.DATA_MASK));
    }

}
