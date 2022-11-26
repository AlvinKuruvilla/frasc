package frasc.zstd;

import java.util.Arrays;

public class NodeTable {
    int[] count;
    short[] parents;
    int[] symbols;
    byte[] numberOfBits;

    public NodeTable(int size) {
        count = new int[size];
        parents = new short[size];
        symbols = new int[size];
        numberOfBits = new byte[size];
    }

    public void reset() {
        Arrays.fill(count, 0);
        Arrays.fill(parents, (short) 0);
        Arrays.fill(symbols, 0);
        Arrays.fill(numberOfBits, (byte) 0);
    }

    public void copyNode(int from, int to) {
        count[to] = count[from];
        parents[to] = parents[from];
        symbols[to] = symbols[from];
        numberOfBits[to] = numberOfBits[from];
    }

    public byte[] getNumberOfBits() {
        return this.numberOfBits;
    }

    public int[] getCount() {
        return this.count;
    }

    public short[] getParents() {
        return this.parents;
    }

    public int[] getSymbols() {
        return this.symbols;
    }
}
