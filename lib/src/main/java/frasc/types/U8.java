package frasc.types;

import com.google.common.primitives.UnsignedBytes;

public class U8 {
    private final static byte ZERO_VAL = 0;
    public final static U8 ZERO = new U8(ZERO_VAL);

    private static final byte NO_MASK_VAL = (byte) 0xFF;
    public static final U8 NO_MASK = new U8(NO_MASK_VAL);
    public static final U8 FULL_MASK = ZERO;

    private final byte raw;

    private U8(byte raw) {
        this.raw = raw;
    }

    public static U8 of(short value) {
        if (value == ZERO_VAL)
            return ZERO;
        if (value == NO_MASK_VAL)
            return NO_MASK;

        return new U8(t(value));
    }

    public static U8 ofRaw(byte value) {
        return new U8(value);
    }

    public static short normalize(short value) {
        return (short) (value & 0xFF);
    }

    public short getValue() {
        return f(raw);
    }

    public byte getRaw() {
        return raw;
    }

    @Override
    public String toString() {
        return String.format("0x%02x", raw);
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
        U8 other = (U8) obj;
        if (raw != other.raw)
            return false;
        return true;
    }

    public static short f(final byte i) {
        return (short) (i & 0xff);
    }

    public static byte t(final short l) {
        return (byte) l;
    }

    public int getLength() {
        return 1;
    }

    public U8 applyMask(U8 mask) {
        return ofRaw((byte) (raw & mask.raw));
    }

    public int compareTo(U8 o) {
        return UnsignedBytes.compare(raw, o.raw);
    }

}
