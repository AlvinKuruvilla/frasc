package frasc;

/// Data structure needed for decompressing strings - read-only and thus can be shared between multiple decompressing threads.
public class FSSTDecoder {
    long version;
    char zeroTerminated;
    char[] len = new char[255];
    long[] symbol = new long[256];
}
