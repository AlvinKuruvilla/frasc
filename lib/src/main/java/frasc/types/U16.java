package frasc.types;

import com.google.common.primitives.Ints;

public class U16 {
    private final static short ZERO_VAL = 0;
    public final static U16 ZERO = new U16(ZERO_VAL);

    private static final short NO_MASK_VAL = (short) 0xFFff;
    public final static U16 NO_MASK = new U16(NO_MASK_VAL);
    public static final U16 FULL_MASK = ZERO;

    public static int f(final short i) {
        return i & 0xffff;
    }

    public static short t(final int l) {
        return (short) l;
    }

    public static int normalize(int value) {
        return value & 0xFFFF;
    }

    private final short raw;

    private U16(short raw) {
        this.raw = raw;
    }

    public static U16 of(int value) {
        return ofRaw(t(value));
    }

    public static U16 ofRaw(short raw) {
        if (raw == ZERO_VAL)
            return ZERO;
        return new U16(raw);
    }

    public int getValue() {
        return f(raw);
    }

    public short getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return String.format("0x%04x", raw);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + raw;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        U16 other = (U16) obj;
        if (raw != other.raw)
            return false;
        return true;
    }

    public int getLength() {
        return 2;
    }

    public U16 applyMask(U16 mask) {
        return ofRaw((short) (raw & mask.raw));
    }

    public int compareTo(U16 o) {
        return Ints.compare(f(raw), f(o.raw));
    }

}
