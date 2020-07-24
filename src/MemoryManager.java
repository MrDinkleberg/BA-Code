import java.io.*;
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
            initSegment(segmentlist.get(i));
        }
    }

    private void initSegment(SegmentHeader segment){
        long address = segment.startaddress;
        offHeapAccess.writeByte(address, (byte) 0);                 //Erstellt Byte damit Schleife funktioniert
        long previousblock = 0;                                     //Pointer für Verkettung der freien Bloecke
        long nextblock = address += (segment.maxblocksize + 1);
        segment.freeblocks[segment.findFittingBlockList(segment.maxblocksize)] = segment.startaddress+1; //Verbesserungsbedarf

        while(segment.endaddress - address >= segment.maxblocksize+1){
            writeMarkerLowerBits(address, getFreeBlockMarkerValue(segment.maxblocksize));
            address++;
            createFreeBlock(address, segment.maxblocksize, nextblock, previousblock);
            previousblock = address;
            address += segment.maxblocksize;
            nextblock = address += (segment.maxblocksize + 1);
            createNewTrailerMarkerByte(address, getFreeBlockMarkerValue(segment.maxblocksize));
        }

        long remainingsize = (segment.endaddress - 1) - address;
        writeMarkerLowerBits(address, getFreeBlockMarkerValue(remainingsize));
        address++;
        createFreeBlock(address, remainingsize, 0, previousblock);
        createNewTrailerMarkerByte(address, getFreeBlockMarkerValue(remainingsize));
    }

    private void createFreeBlock(long address, long size, long next, long prev){
        int lengthfieldsize = getLengthFieldSize(size);
        writeLengthField(address, size, lengthfieldsize);
        address += lengthfieldsize;
        writeAddressField(address, next);
        address += ADDRESS_SIZE;
        writeAddressField(address, prev);
        address = address + size - lengthfieldsize;
        writeLengthField(address, size, lengthfieldsize);

    }

    public long allocate(Serializable object) throws IOException {
        byte[] objectbytearray = serialize(object);                 //serialisiert das Objekt in ein Byte Array
        long size = objectbytearray.length;

        SegmentHeader segment = findSegment();                      //verbesserungsbedarf
        int blocklist = segment.findFittingBlockList(size);         //Freispeicher-Liste in der passenden Groesse
        long address = segment.getBlock(blocklist);
        long nextfreeblock = getNextFreeBlock(address);
        writePreviousFreeBlock(nextfreeblock, 0);
        segment.changeListAnchor(blocklist, nextfreeblock);         //setzt den naechsten freien Block als Listenanker

        long blocksize = getBlockSize(address);                     //Groesse des angeforderten Blocks
        long newblocksize = blocksize - size;                       //Groesse des neuen freien Blocks
        long newblockaddress = address + size + 1;                  //Adresse des neuen freien Blocks
        byte usedmarkervalue = getUsedBlockMarkerValue(size);       //Markerwert des allozierten Speichers

        writeMarkerLowerBits(address-1, usedmarkervalue);
        writeByteArray(address, objectbytearray);
        createNewTrailerMarkerByte(address + size, usedmarkervalue);

        cutFreeBlock(segment, newblockaddress, newblocksize);

        return address;
    }

    private void cutFreeBlock(SegmentHeader segment, long newblockaddress, long newblocksize){

        int blocklist = segment.findFittingBlockList(newblocksize);
        long listanchor = segment.getBlock(blocklist);
        writePreviousFreeBlock(listanchor, newblockaddress);
        segment.changeListAnchor(blocklist, newblockaddress);

        byte markervalue = getFreeBlockMarkerValue(newblocksize);
        writeMarkerLowerBits(newblockaddress-1, markervalue);
        createFreeBlock(newblockaddress, newblocksize, listanchor, 0);
        writeMarkerUpperBits(newblockaddress + newblocksize + 1, markervalue);


    }

    private byte[] serialize(Serializable object) throws IOException {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    private boolean isPreviousBlockFree(long address){
        int prevmarker = readMarkerUpperBits(address-1);
        return (prevmarker >=1 && prevmarker <= 3);
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
            offHeapAccess.writeByte(address + counter, selectedbyte);
            counter++;
        }
    }

    private long getBlockSize(long address){
        int lengthfieldsize = readMarkerLowerBits(address-1);
        long blocksize = 0;
        for(int i = 0; i < lengthfieldsize; i++){
            blocksize = blocksize | offHeapAccess.readByte(address);
            blocksize = blocksize << 8;
        }
        return blocksize;
    }

    private void writeAddressField(long address, long value){

        int counter = 0;

        for(int i = ADDRESS_SIZE - 1; i >= 0; i--){
            byte selectedbyte = (byte) (value >>> i * 8);
            offHeapAccess.writeByte(address + counter, selectedbyte);
            counter++;
        }
    }

    private long readAddressField(long address){
        long value = 0;

        for(int i = 0; i < ADDRESS_SIZE; i++){
            byte read = offHeapAccess.readByte(address + i);
            value = value | read;
            value = value << 8;
        }

        return value;

    }

    private long getNextFreeBlock(long address){
        int lengthfieldsize = readMarkerLowerBits(address-1);
        return readAddressField(address + lengthfieldsize);
    }

    private void writeNextFreeBlock(long address, long value){
        int lengthfieldsize = readMarkerLowerBits(address-1);
        writeAddressField(address + lengthfieldsize, value);
    }

    private long getPreviousFreeBlock(long address){
        int lengthfieldsize = readMarkerLowerBits(address-1);
        return readAddressField(address + lengthfieldsize + ADDRESS_SIZE);
    }

    private void writePreviousFreeBlock(long address, long value){
        int lengthfieldsize = readMarkerLowerBits(address-1);
        writeAddressField(address + lengthfieldsize + ADDRESS_SIZE, value);
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
        offHeapAccess.writeByte(address, marker);
    }

    private void writeMarkerLowerBits(long address, byte value){
        byte marker = offHeapAccess.readByte(address);
        marker = (byte) (marker & 0xF0);
        marker += value;
        offHeapAccess.writeByte(address, marker);
    }

    private void createNewTrailerMarkerByte(long address, byte value){
        value = (byte) (value << 4);
        offHeapAccess.writeByte(address, value);
    }

    private byte getFreeBlockMarkerValue(long size){

        if(size == 1) return 15;
        else if(size < 12) return 0;
        else if(size < 256) return 1;
        else if(size < 65536) return 2;
        else return 3;
    }

    private byte getUsedBlockMarkerValue(long size){
        if(size < 256) return 9;
        else if(size < 65536) return 10;
        else return 11;
    }



    //Aufruf der Zugriffsfunktionen des implementierten OffHeaps

    public long writeInt(int value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeInt(segment.getFreeBlock(Integer.SIZE), value);
    }

    public long writeLong(long value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeLong(segment.getFreeBlock(Long.SIZE), value);
    }

    public long writeDouble(double value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeDouble(segment.getFreeBlock(Double.SIZE), value);
    }

    public long writeChar(char value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeChar(segment.getFreeBlock(Character.SIZE), value);
    }

    public long writeByte(byte value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeByte(segment.getFreeBlock(Byte.SIZE), value);
    }

    public long writeShort(short value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeShort(segment.getFreeBlock(Short.SIZE), value);
    }

    public long writeFloat(float value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeFloat(segment.getFreeBlock(Float.SIZE), value);
    }

    public long writeBoolean(boolean value){
        SegmentHeader segment = findSegment();
        return offHeapAccess.writeBoolean(segment.getFreeBlock(1), value);
    }

    public void writeByteArray(long address, byte[] value){
        for(int i = 0; i < value.length; i++){
            offHeapAccess.writeByte(address + i, value[i]);
        }
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
