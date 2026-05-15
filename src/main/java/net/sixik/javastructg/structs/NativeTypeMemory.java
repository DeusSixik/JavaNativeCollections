package net.sixik.javastructg.structs;

import sun.misc.Unsafe;

public interface NativeTypeMemory<T> extends NativeType {

    void readFromMemory(Unsafe unsafe, long offset, T outElement);

    void writeToMemory(Unsafe unsafe, long offset, T element);
}
