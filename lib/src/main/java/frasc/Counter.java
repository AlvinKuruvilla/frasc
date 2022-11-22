package frasc;

public class Counter {
    static final int FSST_CODE_BITS = 9;
    static final int FSST_CODE_MAX = (int) ((long) 1 << FSST_CODE_BITS);
    // NOTE: There is a NNONOPT_FSST ifdef in the c++ code, that we need to account
    // for somehow
    int[] count1 = new int[FSST_CODE_MAX];

}
