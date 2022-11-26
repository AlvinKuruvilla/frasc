package frasc.zstd.huffman;

import java.util.Arrays;
import frasc.zstd.NodeTable;

public class HuffmanCompressionTableWorkspace {
    public final NodeTable nodeTable = new NodeTable((2 * Huffman.MAX_SYMBOL_COUNT - 1)); // number of nodes in binary
                                                                                          // tree with MAX_SYMBOL_COUNT
                                                                                          // leaves

    public final short[] entriesPerRank = new short[Huffman.MAX_TABLE_LOG + 1];
    public final short[] valuesPerRank = new short[Huffman.MAX_TABLE_LOG + 1];

    // for setMaxHeight
    public final int[] rankLast = new int[Huffman.MAX_TABLE_LOG + 2];

    public void reset() {
        Arrays.fill(entriesPerRank, (short) 0);
        Arrays.fill(valuesPerRank, (short) 0);
    }

}
