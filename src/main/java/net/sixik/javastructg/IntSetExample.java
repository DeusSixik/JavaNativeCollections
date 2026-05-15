package net.sixik.javastructg;

import net.sixik.javastructg.structs.sets.NativeIntSet;

public final class IntSetExample {

    private IntSetExample() {
    }

    public static int countUnique(int[] values) {
        NativeIntSet set = new NativeIntSet(values.length * 2);
        try {
            for (int value : values) {
                set.add(value);
            }
            return set.size();
        } finally {
            set.freeMemory();
        }
    }

    public static int[] uniqueInEncounterOrder(int[] values) {
        NativeIntSet seen = new NativeIntSet(values.length * 2);
        try {
            int[] out = new int[values.length];
            int outSize = 0;

            for (int value : values) {
                if (seen.add(value)) {
                    out[outSize++] = value;
                }
            }

            int[] result = new int[outSize];
            System.arraycopy(out, 0, result, 0, outSize);
            return result;
        } finally {
            seen.freeMemory();
        }
    }

    public static boolean containsAny(int[] haystack, int[] needles) {
        NativeIntSet set = new NativeIntSet(haystack.length * 2);
        try {
            for (int value : haystack) {
                set.add(value);
            }

            for (int needle : needles) {
                if (set.contains(needle)) {
                    return true;
                }
            }

            return false;
        } finally {
            set.freeMemory();
        }
    }
}
