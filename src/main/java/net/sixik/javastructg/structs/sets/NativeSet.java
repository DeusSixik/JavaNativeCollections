package net.sixik.javastructg.structs.sets;

import net.sixik.javastructg.structs.NativeType;
import net.sixik.javastructg.utils.NativeUtils;

public abstract class NativeSet implements NativeType {

    /*
        Состояние слота в hash table:

        EMPTY   - слот ни разу не использовался.
                  Если мы дошли до EMPTY при поиске, значит элемента точно нет.

        USED    - в слоте лежит живой ключ.

        DELETED - ключ отсюда удалили, но цепочка probing через этот слот
                  все еще может продолжаться. Поэтому при contains/remove
                  на DELETED останавливаться нельзя.
     */
    protected static final byte EMPTY = 0;
    protected static final byte USED = 1;
    protected static final byte DELETED = 2;


    /*
        capacity  - количество слотов в таблице.
        mask      - capacity - 1, используется вместо % для быстрого вычисления индекса.
        size      - количество реально живых элементов.
        usedSlots - количество слотов, которые не EMPTY (USED + DELETED).

        usedSlots нужен отдельно, потому что tombstone'ы портят probing.
        Даже если size маленький, большое число DELETED может сделать таблицу медленной.
     */
    protected int capacity;
    protected int size;
    protected int mask;
    protected int usedSlots;

    public NativeSet(int initialCapacity) {
        this.capacity = NativeUtils.nextPowerOfTwo(Math.max(8, initialCapacity));
        this.mask = capacity - 1;
        this.size = 0;
        this.usedSlots = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int capacity() {
        return capacity;
    }

    protected void ensureCapacityForInsert() {
        if(usedSlots * 4 >= capacity * 3) {
            rehash(capacity * 2);
        }
    }

    protected abstract void rehash(int newCapacity);

    public void clear() {
        size = 0;
        usedSlots = 0;
    }

    @Override
    public void freeMemory() {
        capacity = 0;
        mask = 0;
        size = 0;
        usedSlots = 0;
    }
}
