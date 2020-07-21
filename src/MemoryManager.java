import java.util.ArrayList;

public class MemoryManager {

    //public static final long OFFHEAP_SIZE = 1000000000L;
    public static final int NUMBER_OF_SEGMENTS = 10;
    public static final int NUMBER_OF_BLOCKS = 100;
    public static final int ADDRESS_SIZE = 5;
    public static final long MAX_BLOCK_SIZE = 16000000L;



    private OffHeapAccess offHeapAccess;
    private long offheapaddress;
    private long offheapsize;
    private long segmentsize;
    public ArrayList<SegmentHeader> segmentlist;

    public MemoryManager(long size) throws NoSuchFieldException, IllegalAccessException {
        this.offHeapAccess = new OffHeap(size);
        this.offheapsize = size;
        segmentlist = new ArrayList<>();
        createSegments(NUMBER_OF_SEGMENTS);
    }

    public void createSegments(int segments){
        segmentsize = offheapsize/segments;
        int maxblocksize = (int) segmentsize / NUMBER_OF_BLOCKS;

        for(int i = 0; i < segments; i++){
            segmentlist.add(new SegmentHeader(offheapaddress + segmentsize * i, maxblocksize, segmentsize));
            createBlocks(segmentlist.get(i));
        }
    }

    private void createBlocks(SegmentHeader segment){
        long address = segment.startaddress;
        offHeapAccess.writeByte((byte) 0, address);                 //Erstellt Byte damit Schleife funktioniert
        long previousblock = 0;                                     //Pointer für Verkettung der freien Bloecke
        long nextblock = address += (segment.maxblocksize + 1);
        segment.freeblocks[segment.findFittingBlock(segment.maxblocksize)] = segment.startaddress+1; //Verbesserungsbedarf

        while(segment.endaddress - address >= segment.maxblocksize+1){
            writeMarkerLowerBits(address, getMarkerValue(segment.maxblocksize));
            address++;
            createFreeBlock(address, segment.maxblocksize, nextblock, previousblock);
            previousblock = address;
            address += segment.maxblocksize;
            nextblock = address += (segment.maxblocksize + 1);
            createNewTrailerMarkerByte(address, getMarkerValue(segment.maxblocksize));
        }

        long remainingsize = (segment.endaddress - 1) - address;
        writeMarkerLowerBits(address, getMarkerValue(remainingsize));
        address++;
        createFreeBlock(address, remainingsize, 0, previousblock);
        createNewTrailerMarkerByte(address, getMarkerValue(remainingsize));
    }

    private int readMarkerUpperBits(long address){
        byte marker = offHeapAccess.readByte(address);
        return marker >>> 4;
    }

    private int readMarkerLowerBits(long address){
        byte marker = offHeapAccess.readByte(address);
        return marker & 0xF;
    }

    private void writeMarkerUpperBits(long address, byte value){
        int lowerbits = readMarkerLowerBits(address);
        byte marker = (byte) (value << 4);
        marker += lowerbits;
        offHeapAccess.writeByte(marker, address);
    }

    private void writeMarkerLowerBits(long address, byte value){
        byte marker = offHeapAccess.readByte(address);
        marker = (byte) (marker & 0xF0);
        marker += value;
        offHeapAccess.writeByte(marker, address);
    }

    private void createNewTrailerMarkerByte(long address, byte value){
        value = (byte) (value << 4);
        offHeapAccess.writeByte(value, address);
    }

    private byte getMarkerValue(long size){
        if(size < 256) return 1;
        else if(size < 65536) return 2;
        else return 3;
    }


    private SegmentHeader findSegment(){
        return segmentlist.get(0);
    }

    private SegmentHeader getSegmentByAddress(long address){
        for(int i = 0; i < NUMBER_OF_SEGMENTS; i++){
            if(address >= offheapaddress + i * segmentsize && address < offheapaddress + (i+1) * segmentsize){
                return segmentlist.get(i);
            }
        }

        return null;
    }





    private void createFreeBlock(long address, long size, long next, long prev){
        int lengthfieldsize = getLengthFieldSize(size);
        writeLengthField(address, size, lengthfieldsize);
        address += lengthfieldsize;
        writeAddress(address, next);
        address += ADDRESS_SIZE;
        writeAddress(address, prev);
        address = address + size - lengthfieldsize;
        writeLengthField(address, size, lengthfieldsize);

    }

    private int getLengthFieldSize(long size){
        if(size < 256) return 1;
        else if (size < 65536) return 2;
        else return 3;
    }

    private void writeLengthField(long address, long size, int fieldsize){

        /*switch(fieldsize){

            case 3:
                byte selectedbyte = (byte) ((size & 0xFF0000) >>> 16);
                offHeapAccess.writeByte(selectedbyte, address);
                selectedbyte = (byte) ((size & 0xFF00) >>> 8);
                offHeapAccess.writeByte(selectedbyte, address+1);
                selectedbyte = (byte) (size & 0xFF);
                offHeapAccess.writeByte(selectedbyte, address+2);

            case 2:
                selectedbyte = (byte) ((size & 0xFF00) >>> 8);
                offHeapAccess.writeByte(selectedbyte, address);
                selectedbyte = (byte) ((size & 0xFF));
                offHeapAccess.writeByte(selectedbyte, address+1);

            case 1:
                offHeapAccess.writeByte((byte) size, address);
        }*/

        int counter = 0;
        for(int i = fieldsize - 1; i >= 0; i--) {
            byte selectedbyte = (byte) (size >>> i * 8);
            offHeapAccess.writeByte(selectedbyte, address + counter);
            counter++;
        }
    }

    private void writeAddress(long address, long value){

        int counter = 0;

        for(int i = ADDRESS_SIZE - 1; i >= 0; i--){
            byte selectedbyte = (byte) (address >>> i * 8);
            offHeapAccess.writeByte(selectedbyte, address + counter);
            counter++;
        }
    }

    //Aufruf der Zugriffsfunktionen des implementierten OffHeaps

    public long writeInt(int value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeInt(value, segment.getFreeBlock(Integer.SIZE));
    }

    public long writeLong(long value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeLong(value, segment.getFreeBlock(Long.SIZE));
    }

    public long writeDouble(double value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeDouble(value, segment.getFreeBlock(Double.SIZE) );
    }

    public long writeChar(char value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeChar(value, segment.getFreeBlock(Character.SIZE));
    }

    public long writeByte(byte value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeByte(value, segment.getFreeBlock(Byte.SIZE));
    }

    public long writeShort(short value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeShort(value, segment.getFreeBlock(Short.SIZE));
    }

    public long writeFloat(float value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeFloat(value, segment.getFreeBlock(Float.SIZE));
    }

    public long writeBoolean(boolean value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeBoolean(value, segment.getFreeBlock(1));
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
