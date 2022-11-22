package frasc;

import java.math.BigInteger;

public class Symbol {
    static final int FSST_CODE_BITS = 9;
    static final int FSST_HASH_LOG2SIZE = 10;
    static final int FSST_CODE_MAX = (int) ((long) 1 << FSST_CODE_BITS);
    static final int FSST_CODE_MASK = (int) (FSST_CODE_MAX - (long) 1);
    static final int FSST_LEN_BITS = 12;
    static final int FSST_CODE_BASE = (int) (long) 256; /* first 256 codes [0,255] are pseudo codes: escaped bytes */
    private static final String prime = new String("2971215073");
    static final BigInteger FSST_HASH_PRIME = new BigInteger(prime);
    static final int FSST_SHIFT = 15;

    int maxLength = 0;
    long value = 0;
    long icl;

    Symbol() {
        this.icl = 0;
        this.value = 0;

    }

    Symbol(int c, int code) {
        // NOTE: This needs to be checked
        this.icl = 1 << 28;
        c = code << 16;
        code = 56;
        this.value = c;
    }

    Symbol(char input, int len) {
        this.value = 0;
        if (len >= 8) {
            len = 8;
        }
        this.setCodeLength(FSST_CODE_MAX, len);
    }

    Symbol(char begin, char end) {
        this(begin, (int) end - begin);
    }

    void setCodeLength(int code, int len) {
        icl = (len << 28) | (code << 16) | ((8 - len) * 8);
    }

    int length() {
        return (int) (icl >> 28);
    }

    int code() {
        return (int) ((icl >> 16) & FSST_CODE_MASK);
    }

    int ignoredBits() {
        return (int) icl;
    }

    int first() {
        assert (length() >= 1);
        return (int) (0xFF & this.value);
    }

    int first2() {
        assert (length() >= 2);
        return (int) (0xFFFF & this.value);
    }

    private BigInteger FSST_HASH(long w) {
        BigInteger conv = new BigInteger(String.valueOf(w));
        return (((conv).multiply(FSST_HASH_PRIME)).xor((((conv).multiply(FSST_HASH_PRIME)).shiftRight(FSST_SHIFT))));
    }

    public BigInteger hash() {
        long v = 0xFFFFFF & this.value;
        return FSST_HASH(v);
    }
}

// two phases of compression, before and after optimize():
//
// (1) to encode values we probe (and maintain) three datastructures:
// - u16 byteCodes[65536] array at the position of the next byte (s.length==1)
// - u16 shortCodes[65536] array at the position of the next twobyte pattern
// (s.length==2)
// - Symbol hashtable[1024] (keyed by the next three bytes, ie for s.length>2),
// this search will yield a u16 code, it points into Symbol symbols[]. You
// always find a hit, because the first 256 codes are
// pseudo codes representing a single byte these will become escapes)
//
// (2) when we finished looking for the best symbol table we call optimize() to
// reshape it:
// - it renumbers the codes by length (first symbols of length 2,3,4,5,6,7,8;
// then 1 (starting from byteLim are symbols of length 1)
// length 2 codes for which no longer suffix symbol exists (< suffixLim) come
// first among the 2-byte codes
// (allows shortcut during compression)
// - for each two-byte combination, in all unused slots of shortCodes[], it
// enters the byteCode[] of the symbol corresponding
// to the first byte (if such a single-byte symbol exists). This allows us to
// just probe the next two bytes (if there is only one
// byte left in the string, there is still a terminator-byte added during
// compression) in shortCodes[]. That is, byteCodes[]
// and its codepath is no longer required. This makes compression faster. The
// reason we use byteCodes[] during symbolTable construction
// is that adding a new code/symbol is expensive (you have to touch shortCodes[]
// in 256 places). This optimization was
// hence added to make symbolTable construction faster.
//
// this final layout allows for the fastest compression code, only currently
// present in compressBulk
// in the hash table, the icl field contains (low-to-high)
// ignoredBits:16,code:12,length:4
class QSymbol {
    static final int FSST_SAMPLETARGET = (1 << 14);
    static final int FSST_SAMPLEMAXSZ = (int) ((long) 2 * FSST_SAMPLETARGET);
    // high bits of icl (len=8,code=FSST_CODE_MASK) indicates free bucket
    static final int FSST_ICL_FREE = ((15 << 28) | (((int) Symbol.FSST_CODE_MASK) << 16));

    // ignoredBits is (8-length)*8, which is the amount of high bits to zero in the
    // input word before comparing with the hashtable key
    // ..it could of course be computed from len during lookup, but storing it
    // precomputed in some loose bits is faster
    //
    // the gain field is only used in the symbol queue that sorts symbols on gain

    Symbol symbol;
    int gain;

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /*
         * Check if o is an instance of Complex or not
         * "null instanceof [type]" also returns false
         */
        if (!(o instanceof QSymbol)) {
            return false;
        }
        QSymbol other = (QSymbol) o;
        return this.symbol.value == other.symbol.value && this.symbol.length() == other.symbol.length();
    }
}

class SymbolTable {
    static final int hashTabSize = 1 << Symbol.FSST_HASH_LOG2SIZE;
    int[] shortCodes = new int[65536];
    int[] byteCodes = new int[256];
    Symbol[] symbols = new Symbol[Symbol.FSST_CODE_MAX];
    Symbol[] hashTab = new Symbol[hashTabSize];
    int nSymbols; // amount of symbols in the map (max 255)
    int suffixLim; // codes higher than this do not have a longer suffix
    int terminator; // code of 1-byte symbol, that can be used as a terminator during compression
    boolean zeroTerminated; // whether we are expecting zero-terminated strings (we then also produce
                            // zero-terminated compressed strings)
    int lenHisto[] = new int[Symbol.FSST_CODE_BITS]; // lenHisto[x] is the amount of symbols of byte-length (x+1) in
                                                     // this symbolTable

    SymbolTable() {
        this.nSymbols = 0;
        this.suffixLim = Symbol.FSST_CODE_MAX;
        this.terminator = 0;
        this.zeroTerminated = false;
        for (int i = 0; i < 256; i++) {
            symbols[i] = new Symbol(i, i | (1 << Symbol.FSST_LEN_BITS)); // pseudo symbols
        }
        Symbol unused = new Symbol((int) 0, Symbol.FSST_CODE_MASK); // single-char symbol, exception code
        for (int i = 256; i < Symbol.FSST_CODE_MAX; i++) {
            symbols[i] = unused; // we start with all symbols unused
        }
        // empty hash table
        Symbol s = new Symbol();
        s.value = 0;
        s.icl = QSymbol.FSST_ICL_FREE; // marks empty in hashtab
        for (int i = 0; i < hashTabSize; i++)
            hashTab[i] = s;

        // fill byteCodes[] with the pseudo code all bytes (escaped bytes)
        for (int i = 0; i < 256; i++)
            byteCodes[i] = (1 << Symbol.FSST_LEN_BITS) | i;

        // fill shortCodes[] with the pseudo code for the first byte of each two-byte
        // pattern
        for (int i = 0; i < 65536; i++) {
            shortCodes[i] = (1 << Symbol.FSST_LEN_BITS) | (i & 255);
        }
    }

    public void clear() {
        for (int i = Symbol.FSST_CODE_BASE; i < Symbol.FSST_CODE_BASE + nSymbols; i++) {
            if (symbols[i].length() == 1) {
                int val = symbols[i].first();
                byteCodes[val] = (1 << Symbol.FSST_LEN_BITS) | val;
            } else if (symbols[i].length() == 2) {
                int val = symbols[i].first2();
                shortCodes[val] = (1 << Symbol.FSST_LEN_BITS) | (val & 255);
            } else {
                BigInteger tableSize = new BigInteger(String.valueOf(hashTabSize - 1));
                BigInteger idx = symbols[i].hash().and(tableSize);
                hashTab[idx.intValue()].value = 0;
                hashTab[idx.intValue()].icl = QSymbol.FSST_ICL_FREE; // marks empty in hashtab
            }
        }
        nSymbols = 0; // no need to clean symbols[] as no symbols are used
    }

    boolean hashInsert(Symbol s) {
        int idx = s.hash().intValue() & (hashTabSize - 1);
        boolean taken = (hashTab[idx].icl < QSymbol.FSST_ICL_FREE);
        if (taken)
            return false; // collision in hash table
        hashTab[idx].icl = s.icl;
        BigInteger offset = new BigInteger(String.valueOf("0xFFFFFFFFFFFFFFFF"));
        hashTab[idx].value = s.value & (offset.shiftRight((int) s.icl)).intValue();
        return true;
    }

    boolean add(Symbol s) {
        assert (Symbol.FSST_CODE_BASE + nSymbols < Symbol.FSST_CODE_MAX);
        int len = s.length();
        s.setCodeLength(Symbol.FSST_CODE_BASE + nSymbols, len);
        if (len == 1) {
            byteCodes[s.first()] = Symbol.FSST_CODE_BASE + nSymbols + (1 << Symbol.FSST_LEN_BITS); // len=1
                                                                                                   // (<<FSST_LEN_BITS)
        } else if (len == 2) {
            shortCodes[s.first2()] = Symbol.FSST_CODE_BASE + nSymbols + (2 << Symbol.FSST_LEN_BITS); // len=2
                                                                                                     // (<<FSST_LEN_BITS)
        } else if (!hashInsert(s)) {
            return false;
        }
        symbols[Symbol.FSST_CODE_BASE + nSymbols++] = s;
        lenHisto[len - 1]++;
        return true;
    }

    /// Find longest expansion, return code (= position in symbol table)
    int findLongestSymbol(Symbol s) {
        int idx = s.hash().intValue() & (hashTabSize - 1);
        BigInteger offset = new BigInteger("0xFFFFFFFFFFFFFFFF");
        if (hashTab[idx].icl <= s.icl
                && hashTab[idx].value == (s.value & (offset.intValue() >> ((int) hashTab[idx].icl)))) {
            return (int) ((hashTab[idx].icl >> 16) & Symbol.FSST_CODE_MASK); // matched a long symbol
        }
        if (s.length() >= 2) {
            int code = shortCodes[s.first2()] & Symbol.FSST_CODE_MASK;
            if (code >= Symbol.FSST_CODE_BASE)
                return code;
        }
        return byteCodes[s.first()] & Symbol.FSST_CODE_MASK;
    }

    int findLongestSymbol(int cur, int end) {
        Symbol symbol = new Symbol(cur, end);
        return findLongestSymbol(symbol); // represent the string as a temporary symbol
    }

    // rationale for finalize:
    // - during symbol table construction, we may create more than 256 codes, but
    // bring it down to max 255 in the last makeTable()
    // consequently we needed more than 8 bits during symbol table contruction, but
    // can simplify the codes to single bytes in finalize()
    // (this feature is in fact lo longer used, but could still be exploited: symbol
    // construction creates no more than 255 symbols in each pass)
    // - we not only reduce the amount of codes to <255, but also *reorder* the
    // symbols and renumber their codes, for higher compression perf.
    // we renumber codes so they are grouped by length, to allow optimized scalar
    // string compression (byteLim and suffixLim optimizations).
    // - we make the use of byteCode[] no longer necessary by inserting single-byte
    // codes in the free spots of shortCodes[]
    // Using shortCodes[] only makes compression faster. When creating the
    // symbolTable, however, using shortCodes[] for the single-byte
    // symbols is slow, as each insert touches 256 positions in it. This
    // optimization was added when optimizing symbolTable construction time.
    //
    // In all, we change the layout and coding, as follows..
    //
    // before finalize():
    // - The real symbols are symbols[256..256+nSymbols>. As we may have nSymbols >
    // 255
    // - The first 256 codes are pseudo symbols (all escaped bytes)
    //
    // after finalize():
    // - table layout is symbols[0..nSymbols>, with nSymbols < 256.
    // - Real codes are [0,nSymbols>. 8-th bit not set.
    // - Escapes in shortCodes have the 8th bit set (value: 256+255=511). 255
    // because the code to be emitted is the escape byte 255
    // - symbols are grouped by length: 2,3,4,5,6,7,8, then 1 (single-byte codes
    // last)
    // the two-byte codes are split in two sections:
    // - first section contains codes for symbols for which there is no longer
    // symbol (no suffix). It allows an early-out during compression
    //
    // finally, shortCodes[] is modified to also encode all single-byte symbols
    // (hence byteCodes[] is not required on a critical path anymore).
    //
    void finalize(int zeroTerminated) {
        assert (nSymbols <= 255);
        int[] newCode = new int[256];
        int[] rsum = new int[8];
        int byteLim = nSymbols - (lenHisto[0] - zeroTerminated);

        // compute running sum of code lengths (starting offsets for each length)
        rsum[0] = byteLim; // 1-byte codes are highest
        rsum[1] = zeroTerminated;
        for (int i = 1; i < 7; i++)
            rsum[i + 1] = rsum[i] + lenHisto[i];

        // determine the new code for each symbol, ordered by length (and splitting
        // 2byte symbols into two classes around suffixLim)
        suffixLim = rsum[1];
        symbols[newCode[0] = 0] = symbols[256]; // keep symbol 0 in place (for zeroTerminated cases only)

        for (int i = zeroTerminated, j = rsum[2]; i < nSymbols; i++) {
            Symbol s1 = symbols[Symbol.FSST_CODE_BASE + i];
            int len = s1.length();
            int opt = (len == 2) ? 1 : 0 * nSymbols;
            if (opt == 0) {
                int first2 = s1.first2();
                for (int k = 0; k < opt; k++) {
                    Symbol s2 = symbols[Symbol.FSST_CODE_BASE + k];
                    if (k != i && s2.length() > 1 && first2 == s2.first2()) // test if symbol k is a suffix of s
                        opt = 0;
                }
                // By putting 'opt' by itself I think the code was implicitly checking if it was
                // true in the ternary operator
                // so by doing 'opt == 0' I believe we achieve the same effect
                newCode[i] = opt == 0 ? suffixLim++ : --j; // symbols without a larger suffix have a code < suffixLim
            } else
                newCode[i] = rsum[len - 1]++;
            s1.setCodeLength(newCode[i], len);
            symbols[newCode[i]] = s1;
        }
        // renumber the codes in byteCodes[]
        for (int i = 0; i < 256; i++)
            if ((byteCodes[i] & Symbol.FSST_CODE_MASK) >= Symbol.FSST_CODE_BASE)
                byteCodes[i] = newCode[(int) byteCodes[i]] + (1 << Symbol.FSST_LEN_BITS);
            else
                byteCodes[i] = 511 + (1 << Symbol.FSST_LEN_BITS);

        // renumber the codes in shortCodes[]
        for (int i = 0; i < 65536; i++)
            if ((shortCodes[i] & Symbol.FSST_CODE_MASK) >= Symbol.FSST_CODE_BASE)
                shortCodes[i] = newCode[(int) shortCodes[i]] + (shortCodes[i] & (15 << Symbol.FSST_LEN_BITS));
            else
                shortCodes[i] = byteCodes[i & 0xFF];

        // replace the symbols in the hash table
        for (int i = 0; i < hashTabSize; i++)
            if (hashTab[i].icl < QSymbol.FSST_ICL_FREE)
                hashTab[i] = symbols[newCode[(int) hashTab[i].code()]];
    }
};
