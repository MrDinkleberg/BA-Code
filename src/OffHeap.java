import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class OffHeap implements OffHeapAccess {

    public long startaddress;   //Adresse des OffHeaps
    private long nextaddress;
    private long size;          //Groesse des OffHeaps


    public Unsafe unsafe;

    public OffHeap(long size) throws NoSuchFieldException, IllegalAccessException {
        this.unsafe = initUnsafe();
        this.size = size;
        this.startaddress = this.unsafe.allocateMemory(size);
        this.nextaddress = startaddress;
        unsafe.setMemory(this.startaddress, this.size, (byte) 0);
    }



    private Unsafe initUnsafe() throws NoSuchFieldException, IllegalAccessException {  //Funktion zur Initialisierung der Unsafe Instanz
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    public void freeMemory(long address){
        unsafe.freeMemory(address);
    }   //Gibt den allozierten Speicher frei

    @Override
    public long writeInt(int value){        //Schreibt die übergebene Integer-Variable in den OffHeap
        unsafe.putInt(nextaddress, value);
        long address = nextaddress;
        nextaddress += Integer.BYTES;
        return address;
    }

    @Override
    public long writeLong(long value){      //Schreibt die übergebene Long-Variable in den OffHeap
        unsafe.putLong(nextaddress, value);
        long address = nextaddress;
        nextaddress += Long.BYTES;
        return address;
    }

    @Override
    public long writeDouble(double value){      //Schreibt die übergebene Double-Variable in den OffHeap
        unsafe.putDouble(nextaddress, value);
        long address = nextaddress;
        nextaddress += Double.BYTES;
        return address;
    }

    @Override
    public long writeChar(char value){      //Schreibt die übergebene Char-Variable in den OffHeap
        unsafe.putChar(nextaddress, value);
        long address = nextaddress;
        nextaddress += Character.BYTES;
        return address;
    }

    @Override
    public long writeByte(byte value){      //Schreibt die übergebene Byte-Variable in den OffHeap
        unsafe.putByte(nextaddress, value);
        long address = nextaddress;
        nextaddress += Byte.BYTES;
        return address;
    }

    @Override
    public long writeShort(short value){        //Schreibt die übergebene Short-Variable in den OffHeap
        unsafe.putShort(nextaddress, value);
        long address = nextaddress;
        nextaddress += Short.BYTES;
        return address;
    }

    @Override
    public long writeFloat(float value){        //Schreibt die übergebene Float-Variable in den OffHeap
        unsafe.putFloat(nextaddress, value);
        long address = nextaddress;
        nextaddress += Float.BYTES;
        return address;
    }

    @Override
    public long writeBoolean(boolean value){        //Schreibt die übergebene Boolean-Variable in den OffHeap
        unsafe.putBoolean(null, nextaddress, value);
        long address = nextaddress;
        nextaddress += 1;
        return address;
    }

    @Override
    public int readInt(long address){   //Gibt die an der übergebenen Adresse gespeicherte Integer-Variable zurueck
        return unsafe.getInt(address);
    }

    @Override
    public long readLong(long address){ //Gibt die an der übergebenen Adresse gespeicherte Integer-Variable zurueck
        return unsafe.getLong(address);
    }

    @Override
    public double readDouble(long address){ //Gibt die an der übergebenen Adresse gespeicherte Double-Variable zurueck
        return unsafe.getDouble(address);
    }

    @Override
    public char readChar(long address){ //Gibt die an der übergebenen Adresse gespeicherte Char-Variable zurueck
        return unsafe.getChar(address);
    }

    @Override
    public byte readByte(long address){ //Gibt die an der übergebenen Adresse gespeicherte Byte-Variable zurueck
        return unsafe.getByte(address);
    }

    @Override
    public short readShort(long address){   //Gibt die an der übergebenen Adresse gespeicherte Short-Variable zurueck
        return unsafe.getShort(address);
    }

    @Override
    public float readFloat(long address){   //Gibt die an der übergebenen Adresse gespeicherte Float-Variable zurueck
        return unsafe.getFloat(address);
    }

    @Override
    public boolean readBoolean(long address){   //Gibt die an der übergebenen Adresse gespeicherte Boolean-Variable zurueck
        return unsafe.getBoolean(null, address);
    }

}
