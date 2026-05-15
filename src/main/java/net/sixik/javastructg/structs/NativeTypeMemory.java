package net.sixik.javastructg.structs;

import sun.misc.Unsafe;

public interface NativeTypeMemory<T> extends NativeType {

    void readFromMemory(Unsafe unsafe, long offset, T outElement);

    void writeToMemory(Unsafe unsafe, long offset, T element);

    default long hash(T element) {
        return element.hashCode();
    }

    default boolean equals(T left, T right) {
        return left.equals(right);
    }

    default boolean supportsHashMemory() {
        return false;
    }

    default long hashMemory(Unsafe unsafe, long offset) {
        throw new UnsupportedOperationException("hashMemory is not implemented");
    }

    default boolean supportsEqualsMemory() {
        return false;
    }

    default boolean equalsMemory(Unsafe unsafe, long offset, T value) {
        throw new UnsupportedOperationException("equalsMemory is not implemented");
    }
}
