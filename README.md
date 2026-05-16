# JavaStruct

JavaStruct is an experimental Java library for high-performance off-heap data structures with low GC pressure.

This project is primarily built for the real-world performance needs of the Minecraft mod `Generator Accelerator`. That is the main design center of the library: hot loops, repeated operations, strict memory control, and practical throughput over generic convenience.

In short:

- data lives outside the normal Java heap;
- GC has less object churn to deal with;
- APIs are allowed to be specialized when that improves hot-path speed;
- the project is benchmark-driven, not abstraction-driven.

JavaStruct is not trying to replace all of `java.util`. It is a toolbox for performance-critical code.

## Why this library exists

JavaStruct was created to support workloads where standard heap-based collections can become a bottleneck, especially inside systems similar to `Generator Accelerator`:

- large repeated scans and lookups;
- many temporary objects in hot paths;
- GC pressure that becomes visible in frame time or tick time;
- workloads where off-heap storage and specialized memory layouts can pay off.

If your goal is "the most convenient collection API", the JDK is usually enough.

If your goal is "squeeze more predictable performance out of a hot subsystem", JavaStruct is the type of library this project is aiming to be.

## What the project currently provides

The library currently has several native/off-heap building blocks.

### 1. Native arrays

Base class:

- `src/main/java/net/sixik/javastructg/structs/arrays/NativeArray.java`

Typed array implementations:

- `NativeByteArray`
- `NativeBooleanArray`
- `NativeShortArray`
- `NativeCharArray`
- `NativeIntArray`
- `NativeFloatArray`
- `NativeLongArray`
- `NativeDoubleArray`
- `NativeObjectArray<T>`

These arrays are the core off-heap containers in the project. They allocate aligned native memory, expose raw pointers when needed, and let you work with primitive data without creating normal heap arrays for every hot operation.

Useful related helpers:

- cursor APIs such as `NativeIntCursor`, `NativeFloatCursor`, `NativeShortCursor`
- slice APIs such as `NativeIntSlice`, `NativeFloatSlice`, `NativeShortSlice`

Typical use cases:

- fast bulk copy from Java arrays into native memory;
- raw pointer processing in tight loops;
- compact storage for large primitive buffers;
- object storage through custom struct serialization with `NativeObjectArray<T>`.

Examples:

- `src/main/java/net/sixik/javastructg/examples/ArrayExample.java`
- `src/main/java/net/sixik/javastructg/examples/StructExample.java`

### 2. Native object layout support

Core files:

- `src/main/java/net/sixik/javastructg/structs/NativeStructLayout.java`
- `src/main/java/net/sixik/javastructg/structs/NativeTypeMemory.java`
- `src/main/java/net/sixik/javastructg/structs/NativeStructCursor.java`

This layer lets you describe how a Java object is written to and read from off-heap memory.

Main idea:

- `NativeStructLayout` defines field offsets and packed layout;
- `NativeTypeMemory<T>` defines how to write, read, hash, and compare values;
- `NativeObjectArray<T>` and `NativeObjectSet<T>` then use that memory model directly.

This is the piece that makes JavaStruct more than just "native primitive arrays".

### 3. Native sets

Set implementations currently include:

- `NativeObjectSet<T>`
- `NativeHashSet<T>`
- primitive sets such as `NativeIntSet`, `NativeLongSet`, `NativeByteSet`, `NativeBooleanSet`, `NativeCharSet`, `NativeShortSet`, `NativeFloatSet`, `NativeDoubleSet`

Two set variants matter most right now:

#### `NativeObjectSet<T>`

Path:

- `src/main/java/net/sixik/javastructg/structs/sets/NativeObjectSet.java`

Use it when you need:

- safe equality semantics;
- off-heap object storage;
- behavior conceptually closer to `HashSet`;
- optional prehashed operations when the same keys are queried many times.

Important API:

- `add`
- `contains`
- `remove`
- `addPrehashed`
- `containsPrehashed`
- `removePrehashed`

#### `NativeHashSet<T>`

Path:

- `src/main/java/net/sixik/javastructg/structs/sets/NativeHashSet.java`

Use it when you need:

- maximum lookup or insertion speed;
- hash-based identity only;
- a very cheap structure where hash collisions are acceptable for the workload.

Important tradeoff:

- `NativeHashSet<T>` is intentionally lossy;
- equality is not full object equality;
- different objects with the same hash may be treated as the same entry.

So the short rule is:

- need correctness -> `NativeObjectSet<T>`
- need correctness plus repeated lookups with reusable hashes -> `NativeObjectSet<T>` with prehashed API
- need raw speed and can accept collision risk -> `NativeHashSet<T>`

Examples:

- `src/main/java/net/sixik/javastructg/examples/ObjectSetExample.java`
- `src/main/java/net/sixik/javastructg/examples/HashSetExample.java`

### 4. Raw primitive memory helpers

Core file:

- `src/main/java/net/sixik/javastructg/structs/NativeRawPrimitives.java`

This layer provides low-level bulk operations for native memory, such as copying and filling primitive ranges. It is useful for tight loops and bulk transforms where per-element abstraction cost matters.

## Quick examples

### Native primitive array

```java
NativeIntArray array = new NativeIntArray(1024);

try {
    array.add(10);
    array.add(20);
    array.add(30);

    int value = array.get(1);
    array.set(1, value * 2);
} finally {
    array.freeMemory();
}
```

### Native object array with custom memory layout

```java
PersonMemory memory = new PersonMemory();
NativeObjectArray<Person> array = new NativeObjectArray<>(128, memory);

try {
    array.set(0, new Person("Alex", 10, 20, 30));

    Person out = new Person();
    array.get(0, out);
} finally {
    array.freeMemory();
}
```

### Safe off-heap set

```java
PersonMemory memory = new PersonMemory();
NativeObjectSet<Person> set = new NativeObjectSet<>(expectedCapacity, memory, Person::new);

try {
    set.add(person);
    boolean exists = set.contains(person);
    boolean removed = set.remove(person);
} finally {
    set.freeMemory();
}
```

### Prehashed safe set

```java
PersonMemory memory = new PersonMemory();
NativeObjectSet<Person> set = new NativeObjectSet<>(expectedCapacity, memory, Person::new);
long hash = memory.hash(person);

try {
    set.addPrehashed(person, hash);
    boolean exists = set.containsPrehashed(person, hash);
    boolean removed = set.removePrehashed(person, hash);
} finally {
    set.freeMemory();
}
```

### Fast hash-only set

```java
PersonMemory memory = new PersonMemory();
NativeHashSet<Person> set = new NativeHashSet<>(expectedCapacity, memory);
long hash = memory.hash(person);

try {
    set.addHash(hash);
    boolean exists = set.containsHash(hash);
    boolean removed = set.removeHash(hash);
} finally {
    set.freeMemory();
}
```

## Benchmark snapshot

To make the project easier to explain to new contributors, the repository now includes a focused "story benchmark" that compares only a few easy-to-understand scenarios.

Command:

```powershell
.\gradlew jmhSetStoryReport
```

Current snapshot from `build/reports/jmh/set-story-summary.txt`:

### Safe repeated lookup of missing keys

- Java `HashSet`: `785.096` full passes/s
- `NativeObjectSet`: `674.197` full passes/s
- `NativeObjectSet` prehashed: `1062.908` full passes/s

Takeaway:

- when safe equality is required and the hash can be reused, `NativeObjectSet` prehashed is about `1.35x` faster than Java `HashSet` in this scenario.

### Safe removal of equal copies

- Java `HashSet`: `149.298` full passes/s
- `NativeObjectSet`: `79.550` full passes/s
- `NativeObjectSet` prehashed: `114.153` full passes/s

Takeaway:

- this is still a weaker case for the current implementation;
- `NativeObjectSet` prehashed is about `1.31x` slower than Java `HashSet` here, while still keeping real equality semantics and off-heap storage.

### Fast repeated hit lookup when collisions are acceptable

- Java `HashSet` baseline: `449.590` full passes/s
- `NativeHashSet` prehashed: `1635.568` full passes/s

Takeaway:

- `NativeHashSet` prehashed is about `3.64x` faster here;
- the price is that it uses hash identity, not full safe equality.

### Fast bulk add of unique keys

- Java `HashSet` baseline: `336.359` full passes/s
- `NativeHashSet` prehashed: `1018.638` full passes/s

Takeaway:

- `NativeHashSet` prehashed is about `3.03x` faster for raw insertion throughput in this dataset.

Important note:

- these numbers are workload-specific;
- they are useful as a current snapshot, not as a universal promise;
- always benchmark against your real workload.

## Tests and focused benchmark entry points

Beginner-friendly story tests:

```powershell
.\gradlew testSetStory
```

Focused benchmark report:

```powershell
.\gradlew jmhSetStoryReport
```

Generated files:

- `build/reports/jmh/set-story-human.txt`
- `build/reports/jmh/set-story-summary.txt`

These are currently the best entry points for explaining the project to another developer without forcing them through the full benchmark suite.

## Key limitations

Before using JavaStruct in production-like code, keep the tradeoffs in mind:

- memory must be released manually with `freeMemory()`;
- this is low-level API territory, not fully managed collection code;
- `NativeHashSet<T>` is intentionally not safe in the same sense as `HashSet`;
- some workloads will still be faster with standard JDK collections;
- the project is still evolving, so APIs may change.

## Project positioning

The honest positioning of JavaStruct is:

- not a general-purpose replacement for the JDK;
- not a convenience-first collection library;
- yes, a serious option for hot subsystems, game code, data generators, performance-sensitive mods, and other throughput-critical components.

The library is designed first for maximum effectiveness in workloads like `Generator Accelerator`, then generalized outward where it makes sense.

## Project status

JavaStruct is under active experimental development.

That means:

- APIs may still change;
- some structures are still being optimized;
- documentation will continue to improve;
- benchmark results matter more than pretty abstractions.
