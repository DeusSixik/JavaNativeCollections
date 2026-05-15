package net.sixik.javastructg.structs;

public interface NativeType {

    long sizeof();

    default void freeMemory() { }
}
