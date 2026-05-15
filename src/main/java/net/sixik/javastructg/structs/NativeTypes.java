package net.sixik.javastructg.structs;

public interface NativeTypes {
    int BYTE    = 1;
    int BOOLEAN = 1;
    int SHORT   = 2;
    int CHAR    = 2;
    int INT     = 4;
    int FLOAT   = 4;
    int LONG    = 8;
    int DOUBLE  = 8;
    int UUID    = CHAR * 36;
    int BLOCK_POS = INT * 3;


    static int sizeof(String string) {
        return CHAR * string.length();
    }
}
