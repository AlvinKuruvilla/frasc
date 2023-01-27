package frasc.types;

import com.google.common.primitives.UnsignedInts;

public class U32 {
    private final static int ZERO_VAL = 0;
    public final static U32 ZERO = new U32(ZERO_VAL);

    private static final int NO_MASK_VAL = 0xFFffFFff;
    public final static U32 NO_MASK = new U32(NO_MASK_VAL);
    public static final U32 FULL_MASK = ZERO;

    private final int raw;

    private U32(int raw) {
        this.raw = raw;
    }

    public static U32 of(long value) {
        return ofRaw(U32.t(value));
    }

    public static U32 ofRaw(int raw) {
        if (raw == ZERO_VAL)
            return ZERO;
        if (raw == NO_MASK_VAL)
            return NO_MASK;
        return new U32(raw);
    }

    public long getValue() {
        return f(raw);
    }

    public int getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return String.format("0x%08x", raw);
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
        U32 other = (U32) obj;
        if (raw != other.raw)
            return false;

        return true;
    }

    public static long f(final int i) {
        return i & 0xffffffffL;
    }

    public static int t(final long l) {
        return (int) l;
    }

    public static long normalize(long value) {
        return value & 0xFFFF_FFFFL;
    }

    public int compareTo(U32 o) {
        return UnsignedInts.compare(raw, o.raw);
    }

}
