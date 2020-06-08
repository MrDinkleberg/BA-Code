import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class OffHeap {

    private long address;
    private long size;


    public Unsafe unsafe;

    public OffHeap(long size) throws NoSuchFieldException, IllegalAccessException {
        this.unsafe = initUnsafe();
        this.size = size;
        this.address = this.unsafe.allocateMemory(size);
        unsafe.setMemory(this.address, this.size, (byte) 0);
    }



    private Unsafe initUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    public void writeInt(int value){

    }



}
