package frasc.zstd;

import static frasc.zstd.Constants.MAX_LITERALS_LENGTH_SYMBOL;
import static frasc.zstd.Constants.MAX_OFFSET_CODE_SYMBOL;

import frasc.zstd.fse.FseCompressionTable;

import static frasc.zstd.Constants.MAX_MATCH_LENGTH_SYMBOL;

public class SequenceEncodingContext {
        private static final int MAX_SEQUENCES = Math.max(MAX_LITERALS_LENGTH_SYMBOL, MAX_MATCH_LENGTH_SYMBOL);

        public final FseCompressionTable literalLengthTable = new FseCompressionTable(
                        Constants.LITERAL_LENGTH_TABLE_LOG,
                        MAX_LITERALS_LENGTH_SYMBOL);
        public final FseCompressionTable offsetCodeTable = new FseCompressionTable(Constants.OFFSET_TABLE_LOG,
                        MAX_OFFSET_CODE_SYMBOL);
        public final FseCompressionTable matchLengthTable = new FseCompressionTable(Constants.MATCH_LENGTH_TABLE_LOG,
                        MAX_MATCH_LENGTH_SYMBOL);

        public final int[] counts = new int[MAX_SEQUENCES + 1];
        public final short[] normalizedCounts = new short[MAX_SEQUENCES + 1];

}
