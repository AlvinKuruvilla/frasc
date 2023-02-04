package frasc;

/// Data structure needed for decompressing strings - read-only and thus can be shared between multiple decompressing threads.
public class FSSTDecoder {
    long version;
    char zeroTerminated;
    char[] len = new char[255];
    long[] symbol = new long[256];

    FSSTDecoder() {
    }

    int import_from_buffer(int[] buffer) {
        return 0;
    }
}
