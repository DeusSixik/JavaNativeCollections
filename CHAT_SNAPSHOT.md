# Chat Snapshot

Этот файл можно приложить в новый чат как контекст по проекту `JavaStruct`.

## Что уже сделано

- Добавлено выравнивание off-heap памяти в `src/main/java/net/sixik/javastructg/structs/arrays/NativeArray.java`
  - `MEMORY_ALIGNMENT = 64`
  - хранятся `rawMemoryAddress` и `memoryAddress`
- Добавлены raw helper'ы для примитивов в `src/main/java/net/sixik/javastructg/structs/NativeRawPrimitives.java`
- Реализованы primitive set:
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeIntSet.java`
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeLongSet.java`
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeByteSet.java`
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeShortSet.java`
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeCharSet.java`
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeFloatSet.java`
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeDoubleSet.java`
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeBooleanSet.java`
- Реализован `NativeObjectSet`:
  - `src/main/java/net/sixik/javastructg/structs/sets/NativeObjectSet.java`
- Расширен контракт `NativeTypeMemory`:
  - `hash(T element)`
  - `equals(T left, T right)`
  - `supportsHashMemory()`
  - `hashMemory(Unsafe unsafe, long offset)`
  - `supportsEqualsMemory()`
  - `equalsMemory(Unsafe unsafe, long offset, T value)`
- В `NativeObjectArray` добавлен `addressAt(int index)`
- В `NativeUtils` есть `hashCombine(long seed, long hash)`
- Сделан fast-path для `NativeObjectSet`:
  - probing может использовать `equalsMemory(...)`
  - rehash может использовать `hashMemory(...)` и `Unsafe.copyMemory(...)` без промежуточной десериализации объекта

## Примеры

- `src/main/java/net/sixik/javastructg/IntSetExample.java`
- `src/main/java/net/sixik/javastructg/examples/ObjectSetExample.java`
- `src/main/java/net/sixik/javastructg/examples/StructExample.java`

## Тесты

- `src/test/java/NativeIntSetTest.java`
- `src/test/java/NativeLongSetTest.java`
- `src/test/java/NativeAdditionalSetsTest.java`
- `src/test/java/IntSetExampleTest.java`
- `src/test/java/ObjectSetExampleTest.java`
- `src/test/java/StructExampleTest.java`

На момент сохранения `.\gradlew test` проходил успешно.

## JMH benchmark'и

- `src/jmh/java/net/sixik/javastructg/NativeLongSetJmhBenchmark.java`
- `src/jmh/java/net/sixik/javastructg/NativeSetJmhBenchmark.java`
- также уже существовали:
  - `src/jmh/java/net/sixik/javastructg/NativeArrayJmhBenchmark.java`
  - `src/jmh/java/net/sixik/javastructg/NativePrimitiveArrayJmhBenchmark.java`

На момент сохранения:

- `.\gradlew jmhClasses` проходил
- `.\gradlew jmhJar` проходил
- `build/reports/jmh/results.json` существовал и был проанализирован

## Ключевые выводы по benchmark'ам set

Источник: `build/reports/jmh/results.json`

- `NativeIntSet` и `NativeLongSet`
  - сильны в `add/remove`
  - `contains hit` обычно быстрее `HashSet`
  - `contains miss` обычно слабее `HashSet`
- `NativeShortSet` и `NativeCharSet`
  - очень хорошие `add/remove`
  - `contains miss` на больших размерах заметно проседает
- `NativeFloatSet` и `NativeDoubleSet`
  - `add/remove` часто хорошие
  - `contains miss` заметно слабее `HashSet`
- `NativeByteSet`
  - на маленьких наборах очень конкурентный
  - `remove` особенно сильный
- `NativeBooleanSet`
  - почти не имеет смысла как perf-структура, потому что всего 2 уникальных значения
- `NativeObjectSet`
  - сейчас заметно проигрывает обычному `HashSet`
  - главные причины: дорогой object-case, строки, `equalsMemory`, probing для структур

## Важные пользовательские предпочтения

- Приоритет: минимальный overhead и максимальная скорость
- Пользователь явно согласен отказываться от safety-check'ов ради perf
- Стиль работы: идти по шагам, делать по очереди
- Если нужен review, отвечать в стиле code review:
  - findings first
  - по severity
  - с указанием файлов/строк

## Важные технические замечания

- Для `NativeObjectSet` рекомендованный паттерн:
  - хеш не через `T.hashCode()`, а через `NativeTypeMemory.hash(T)`
  - сравнение не через `T.equals()`, а через `NativeTypeMemory.equals(...)`
  - fast-path через `equalsMemory(...)`
  - fast-path при rehash через `hashMemory(...)`
- `hashMemory(...)` корректен только если представление в памяти каноническое
  - особенно важно для строк, padding и хвостов fixed-size полей

## Последний активный запрос пользователя

Пользователь попросил:

- сохранить snapshot текущего чата в директории проекта
- чтобы потом можно было приложить этот файл в новый чат и быстро восстановить контекст

