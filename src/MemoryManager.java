public class MemoryManager {

    private OffHeap offHeap;

    public MemoryManager(long size) throws NoSuchFieldException, IllegalAccessException {
        this.offHeap = new OffHeap(size);
    }

    public long writeInt(int value){
        return offHeap.writeInt(value);
    }

    public long writeLong(long value){
        return offHeap.writeLong(value);
    }

    public long writeDouble(double value){
        return offHeap.writeDouble(value);
    }

    public long writeChar(char value){
        return offHeap.writeChar(value);
    }

    public long writeByte(byte value){
        return offHeap.writeByte(value);
    }

    public long writeShort(short value){
        return offHeap.writeShort(value);
    }

    public long writeFloat(float value){
        return offHeap.writeFloat(value);
    }

    public long writeBoolean(boolean value){
        return offHeap.writeBoolean(value);
    }

    public int readInt(long address){
        return offHeap.readInt(address);
    }

    public long readLong(long address){
        return offHeap.readLong(address);
    }

    public double readDouble(long address){
        return offHeap.readDouble(address);
    }

    public char readChar(long address){
        return offHeap.readChar(address);
    }

    public byte readByte(long address){
        return offHeap.readByte(address);
    }

    public short readShort(long address){
        return offHeap.readShort(address);
    }

    public float readFloat(long address){
        return offHeap.readFloat(address);
    }

    public boolean readBoolean(long address){
        return offHeap.readBoolean(address);
    }

    public void cleanup(){
        offHeap.freeMemory(offHeap.startaddress);
    }


}
