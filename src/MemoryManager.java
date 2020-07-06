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
            segmentlist.add(new SegmentHeader(offheapaddress + segmentsize * i, maxblocksize, segmentsize));
        }
    }

    private void createBlocks(SegmentHeader segment){
        long address = segment.startaddress;
        segment.freeblocks[31] = segment.startaddress+1;
        while(segment.endaddress - address >= segment.maxblocksize+1){


            address += segment.maxblocksize;
        }
    }

    private SegmentHeader findSegment(){
        return segmentlist.get(0);
    }






    //Aufruf der Zugriffsfunktionen des implementierten OffHeaps

    public long writeInt(int value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeInt(value, segment.findFreeBlock());
    }

    public long writeLong(long value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeLong(value, segment.findFreeBlock());
    }

    public long writeDouble(double value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeDouble(value, segment.findFreeBlock() );
    }

    public long writeChar(char value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeChar(value, segment.findFreeBlock());
    }

    public long writeByte(byte value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeByte(value, segment.findFreeBlock());
    }

    public long writeShort(short value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeShort(value, segment.findFreeBlock());
    }

    public long writeFloat(float value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeFloat(value, segment.findFreeBlock());
    }

    public long writeBoolean(boolean value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeBoolean(value, segment.findFreeBlock());
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
