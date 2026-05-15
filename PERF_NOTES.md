# Performance Notes

This project now exposes several different access patterns for native primitive arrays.
They have very different overhead profiles.

Important:

- The fast paths are intentionally unchecked.
- If you pass a bad index, bad length, or overlapping ranges where overlap is not expected, behavior is undefined.
- `freeMemory()` is always your responsibility.

## Which API to use

| Goal                                  | Recommended API                                                           | Why                                            |
|---------------------------------------|---------------------------------------------------------------------------|------------------------------------------------|
| Fastest heap -> native copy           | `NativeRawPrimitives.copy*FromArray(...)` or `Native*Array.copyFrom(...)` | Bulk `Unsafe.copyMemory`                       |
| Fastest native -> heap copy           | `NativeRawPrimitives.copy*ToArray(...)` or `Native*Array.copyTo(...)`     | Bulk `Unsafe.copyMemory`                       |
| Fastest full-array fill               | `NativeRawPrimitives.fill*(...)` or `Native*Array.fill(...)`              | Replicated memory pattern, no per-element loop |
| Fastest sequential read               | Raw pointer loop from `ptr()`                                             | Lowest overhead in hot loop                    |
| Fast sub-range work                   | `slice(...)` / `writeSlice(...)`                                          | View without copy, lower overhead than cursor  |
| Sequential ergonomic read/write       | `cursor()` / `writeCursor(...)`                                           | Cleaner than manual pointer arithmetic         |
| Peak throughput writes of many values | Bulk load + raw loop + bulk store                                         | Avoids `set(i, value)` in hot path             |

## Recommended pattern

Use native arrays as memory owners, then switch to raw address arithmetic inside the hot loop.

```java
NativeIntArray nativeArray = new NativeIntArray(input.length);
try {
    long ptr = nativeArray.ptr();
    int count = input.length;

    NativeRawPrimitives.copyIntsFromArray(ptr, input);

    long current = ptr;
    for (int i = 0; i < count; i++, current += Integer.BYTES) {
        int value = UNSAFE.getInt(current);
        UNSAFE.putInt(current, value * 2);
    }

    int[] output = new int[count];
    NativeRawPrimitives.copyIntsToArray(ptr, output);
    return output;
} finally {
    nativeArray.freeMemory();
}
```

See `src/main/java/net/sixik/javastructg/Example.java` for a concrete version of this pattern.

## API tiers

### 1. Raw helpers

Use `src/main/java/net/sixik/javastructg/structs/NativeRawPrimitives.java` when:

- you already have `ptr()` and length
- you want the least abstraction in the hot path
- you are okay with manual control

Best for:

- bulk load/store
- fill
- summation/reduction
- custom kernels over off-heap memory

### 2. Slice views

Use `slice(...)`, `tailSlice(...)`, `writeSlice(...)` when:

- you want a sub-range view without copying
- you want lower overhead than cursor
- you still want object-level API instead of raw `Unsafe`

Best for:

- windowed processing
- chunk-based algorithms
- passing a sub-range around without allocating

### 3. Cursors

Use `cursor()` / `writeCursor(...)` when:

- you want sequential code with explicit advancement
- readability matters more than absolute top speed

Best for:

- simple serialization-style loops
- code that benefits from `rewind()` / `seek()`

### 4. Direct array methods

Use `get(i)` / `set(i, value)` when:

- the loop is not the bottleneck
- you want the simplest direct API

Do not use this path for the hottest code if maximum throughput matters.

## Struct layouts

Use `NativeStructLayout` when you define native object layouts.

Best practice:

- describe offsets through `NativeStructLayout.Builder`
- let the builder insert padding for aligned fields
- use `StringField` for fixed-capacity length-prefixed strings
- use the computed `layout.sizeof()` instead of hand-written constants

Example:

```java
NativeStructLayout.Builder builder = NativeStructLayout.builder();
long xOffset = builder.intField();
long yOffset = builder.intField();
long zOffset = builder.intField();
NativeStructLayout.StringField nameField = builder.intLengthPrefixedStringField(32);
NativeStructLayout layout = builder.build();
```

This gives you:

- stable offsets
- aligned primitive fields
- padded final struct size
- less risk of accidental misaligned layouts like `short` followed by `float` at offset `2`

See:

- `src/main/java/net/sixik/javastructg/TestStruct.java`
- `src/main/java/net/sixik/javastructg/StructExample.java`
- `src/main/java/net/sixik/javastructg/structs/NativeStructLayout.java`

### Struct strings

If the struct contains a bounded string, prefer `StringField` over manual length/data arithmetic.

It handles:

- length field offset
- char data offset
- max character count
- `read(...)`
- `write(...)`

This is the intended pattern for `fixed-capacity string + primitive fields` structs.

## Practical rules

- Prefer bulk operations over per-element writes.
- Prefer slice over cursor if you need a sub-range and still care about speed.
- Prefer raw pointer loops over all other read paths when benchmarking says the loop is hot.
- Use `try/finally` around every native allocation.
- Keep the number of heap <-> native transitions low.

## Current mental model

- `Native*Array` owns memory.
- `slice` is the low-overhead view.
- `cursor` is the ergonomic sequential tool.
- `NativeRawPrimitives` is the fastest reusable low-level layer.
- `Example` shows the intended high-performance pattern.
