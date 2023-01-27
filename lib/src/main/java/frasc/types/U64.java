package frasc.types;

import java.math.BigInteger;

import com.google.common.primitives.UnsignedLongs;

public class U64 {
    private static final long UNSIGNED_MASK = 0x7fffffffffffffffL;
    private final static long ZERO_VAL = 0;
    public final static U64 ZERO = new U64(ZERO_VAL);

    private static final long NO_MASK_VAL = 0xFFffFFffFFffFFffL;
    public final static U64 NO_MASK = new U64(NO_MASK_VAL);
    public static final U64 FULL_MASK = ZERO;

    private final long raw;

    protected U64(final long raw) {
        this.raw = raw;
    }

    public static U64 of(long raw) {
        return ofRaw(raw);
    }

    public static U64 ofRaw(final long raw) {
        if (raw == ZERO_VAL)
            return ZERO;
        return new U64(raw);
    }

    public static U64 parseHex(String hex) {
        return new U64(new BigInteger(hex, 16).longValue());
    }

    public long getValue() {
        return raw;
    }

    public BigInteger getBigInteger() {
        BigInteger bigInt = BigInteger.valueOf(raw & UNSIGNED_MASK);
        if (raw < 0) {
            bigInt = bigInt.setBit(Long.SIZE - 1);
        }
        return bigInt;
    }

    @Override
    public String toString() {
        return String.format("0x%016x", raw);
    }

    public static BigInteger f(final long value) {
        BigInteger bigInt = BigInteger.valueOf(value & UNSIGNED_MASK);
        if (value < 0) {
            bigInt = bigInt.setBit(Long.SIZE - 1);
        }
        return bigInt;
    }

    public static long t(final BigInteger l) {
        return l.longValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (raw ^ (raw >>> 32));
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
        U64 other = (U64) obj;
        if (raw != other.raw)
            return false;
        return true;
    }

    public int getLength() {
        return 8;
    }

    public U64 applyMask(U64 mask) {
        return and(mask);
    }

    public int compareTo(U64 o) {
        return UnsignedLongs.compare(raw, o.raw);
    }

    public U64 inverse() {
        return U64.of(~raw);
    }

    public U64 or(U64 other) {
        return U64.of(raw | other.raw);
    }

    public U64 and(U64 other) {
        return ofRaw(raw & other.raw);
    }

    public U64 xor(U64 other) {
        return U64.of(raw ^ other.raw);
    }

    public U64 add(U64 other) {
        return U64.of(this.raw + other.raw);
    }

    public U64 subtract(U64 other) {
        return U64.of(this.raw - other.raw);
    }

}
