import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class OffHeap {

    public long startaddress;
    private long nextaddress;
    private long size;


    public Unsafe unsafe;

    public OffHeap(long size) throws NoSuchFieldException, IllegalAccessException {
        this.unsafe = initUnsafe();
        this.size = size;
        this.startaddress = this.unsafe.allocateMemory(size);
        this.nextaddress = startaddress;
        unsafe.setMemory(this.startaddress, this.size, (byte) 0);
    }



    private Unsafe initUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    public void freeMemory(long address){
        unsafe.freeMemory(address);
    }

    public long writeInt(int value){
        unsafe.putByte(nextaddress, (byte) value);
        long address = nextaddress;
        nextaddress += Integer.BYTES;

        return address;
    }



}
