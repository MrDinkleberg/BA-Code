import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class OffHeap implements OffHeapAccess {

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

    @Override
    public long writeInt(int value){
        unsafe.putInt(nextaddress, value);
        long address = nextaddress;
        nextaddress += Integer.BYTES;
        return address;
    }

    @Override
    public long writeLong(long value){
        unsafe.putLong(nextaddress, value);
        long address = nextaddress;
        nextaddress += Long.BYTES;
        return address;
    }

    @Override
    public long writeDouble(double value){
        unsafe.putDouble(nextaddress, value);
        long address = nextaddress;
        nextaddress += Double.BYTES;
        return address;
    }

    @Override
    public long writeChar(char value){
        unsafe.putChar(nextaddress, value);
        long address = nextaddress;
        nextaddress += Character.BYTES;
        return address;
    }

    @Override
    public long writeByte(byte value){
        unsafe.putByte(nextaddress, value);
        long address = nextaddress;
        nextaddress += Byte.BYTES;
        return address;
    }

    @Override
    public long writeShort(short value){
        unsafe.putShort(nextaddress, value);
        long address = nextaddress;
        nextaddress += Short.BYTES;
        return address;
    }

    @Override
    public long writeFloat(float value){
        unsafe.putFloat(nextaddress, value);
        long address = nextaddress;
        nextaddress += Float.BYTES;
        return address;
    }

    @Override
    public long writeBoolean(boolean value){
        unsafe.putBoolean(null, nextaddress, value);
        long address = nextaddress;
        nextaddress += 1;
        return address;
    }

    @Override
    public int readInt(long address){
        return unsafe.getInt(address);
    }

    @Override
    public long readLong(long address){
        return unsafe.getLong(address);
    }

    @Override
    public double readDouble(long address){
        return unsafe.getDouble(address);
    }

    @Override
    public char readChar(long address){
        return unsafe.getChar(address);
    }

    @Override
    public byte readByte(long address){
        return unsafe.getByte(address);
    }

    @Override
    public short readShort(long address){
        return unsafe.getShort(address);
    }

    @Override
    public float readFloat(long address){
        return unsafe.getFloat(address);
    }

    @Override
    public boolean readBoolean(long address){
        return unsafe.getBoolean(null, address);
    }

}
