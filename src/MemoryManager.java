import java.util.ArrayList;

public class MemoryManager {

    public static final long OFFHEAP_SIZE = 1000000000;


    private OffHeapAccess offHeapAccess;
    private long offheapaddress;
    public ArrayList<SegmentHeader> segmentlist;

    public MemoryManager(long size) throws NoSuchFieldException, IllegalAccessException {
        this.offHeapAccess = new OffHeap(size);
        segmentlist = new ArrayList<>();
    }

    public void createSegments(int segments, int maxblocksize){
        long segmentsize = OFFHEAP_SIZE/segments;

        for(int i = 0; i < segments; i++){
            segmentlist.add(new SegmentHeader(offheapaddress + i * segmentsize));
        }
    }






    //Aufruf der Zugriffsfunktionen des implementierten OffHeaps

    public long writeInt(int value){
        return offHeapAccess.writeInt(value);
    }

    public long writeLong(long value){
        return offHeapAccess.writeLong(value);
    }

    public long writeDouble(double value){
        return offHeapAccess.writeDouble(value);
    }

    public long writeChar(char value){
        return offHeapAccess.writeChar(value);
    }

    public long writeByte(byte value){
        return offHeapAccess.writeByte(value);
    }

    public long writeShort(short value){
        return offHeapAccess.writeShort(value);
    }

    public long writeFloat(float value){
        return offHeapAccess.writeFloat(value);
    }

    public long writeBoolean(boolean value){
        return offHeapAccess.writeBoolean(value);
    }

    public int readInt(long address){
        return offHeapAccess.readInt(address);
    }

    public long readLong(long address){
        return offHeapAccess.readLong(address);
    }

    public double readDouble(long address){
        return offHeapAccess.readDouble(address);
    }

    public char readChar(long address){
        return offHeapAccess.readChar(address);
    }

    public byte readByte(long address){
        return offHeapAccess.readByte(address);
    }

    public short readShort(long address){
        return offHeapAccess.readShort(address);
    }

    public float readFloat(long address){
        return offHeapAccess.readFloat(address);
    }

    public boolean readBoolean(long address){
        return offHeapAccess.readBoolean(address);
    }

    public void cleanup(){
        ((OffHeap)offHeapAccess).freeMemory(offheapaddress);
    }


}
