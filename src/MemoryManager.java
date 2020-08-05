import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class MemoryManager {

    //public static final long OFFHEAP_SIZE = 1000000000L;
    //public static final int NUMBER_OF_SEGMENTS = 10;
    //public static final int NUMBER_OF_BLOCKS = 100;
    public static final int ADDRESS_SIZE = 5;
    public static final int MAX_BLOCK_SIZE = 16000000;



    private OffHeapAccess offHeapAccess;
    private long addressoffset;
    private long offheapsize;
    private long segmentsize;
    private int segments;
    public ArrayList<SegmentHeader> segmentlist;

    public MemoryManager(long size, int segments) throws NoSuchFieldException, IllegalAccessException {
        this.offheapsize = Math.max(size, segments * (MAX_BLOCK_SIZE + 2));
        this.offHeapAccess = new OffHeap(offheapsize);
        addressoffset = ((OffHeap) offHeapAccess).startaddress;
        segmentlist = new ArrayList<>();
        this.segments = segments;
        createSegments(segments);
    }

    public void createSegments(int segments){
        segmentsize = offheapsize/segments;

        for(int i = 0; i < segments; i++){
            SegmentHeader segment = new SegmentHeader(segmentsize * i, segmentsize);
            segmentlist.add(segment);
            initSegment(segment);
        }
    }

    private void initSegment(SegmentHeader segment){
        long address = segment.startaddress;
        //offHeapAccess.writeByte(address, (byte) 0);                 //Erstellt Byte damit Schleife funktioniert
        long previousblock = 0;                                     //Pointer für Verkettung der freien Bloecke
        long nextblock = address + (MAX_BLOCK_SIZE + 2);
        segment.freeblocks[segment.findFittingBlockList(MAX_BLOCK_SIZE)] = segment.startaddress+1; //Verbesserungsbedarf

        while(segment.endaddress - address >= MAX_BLOCK_SIZE+1){
            writeMarkerLowerBits(address, getFreeBlockMarkerValue(MAX_BLOCK_SIZE));
            address++;
            createFreeBlock(address, MAX_BLOCK_SIZE, nextblock, previousblock);
            previousblock = address;
            nextblock = address + (MAX_BLOCK_SIZE + 1);
            address += MAX_BLOCK_SIZE;
            writeMarkerUpperBits(address, getFreeBlockMarkerValue(MAX_BLOCK_SIZE));
        }

        int remainingsize = (int) ((segment.endaddress - 1) - address);
        if(remainingsize > 0) {
            writeMarkerLowerBits(address, getFreeBlockMarkerValue(remainingsize));
            address++;
            int blocklist = segment.findFittingBlockList(remainingsize);
            segment.freeblocks[blocklist] = address;
            createFreeBlock(address, remainingsize, 0, 0);
            address += remainingsize;
            createNewTrailerMarker(address, getFreeBlockMarkerValue(remainingsize));
        }
    }

    private void createFreeBlock(long address, int size, long next, long prev){
        int lengthfieldsize = getFreeBlockMarkerValue(size);
        writeLengthField(address, size, lengthfieldsize);
        writeNextFreeBlock(address, next);
        writePreviousFreeBlock(address, prev);
        writeLengthField(address + size - lengthfieldsize, size, lengthfieldsize);

    }

    public long allocate(Serializable object) throws IOException {
        byte[] objectbytearray = serialize(object);                 //serialisiert das Objekt in ein Byte Array
        byte usedmarkervalue = getUsedBlockMarkerValue(objectbytearray.length);       //Markerwert des allozierten Speichers
        int lengthfieldsize = usedmarkervalue - 8;  //berechnet Laengenfeld fuer belegten Block
        int size = objectbytearray.length + 2 * lengthfieldsize;

        SegmentHeader segment = findSegment();                      //verbesserungsbedarf
        int blocklist = segment.findFittingBlockList(size);         //Freispeicher-Liste in der passenden Groesse
        //System.out.println(blocklist);
        long address = segment.getListAnchor(blocklist);
        //System.out.println(address);
        long nextfreeblock = getNextFreeBlock(address);
        //System.out.println(nextfreeblock);
        writePreviousFreeBlock(nextfreeblock, 0);
        segment.changeListAnchor(blocklist, nextfreeblock);         //setzt den naechsten freien Block als Listenanker

        int blocksize = readLengthField(address);                     //Groesse des angeforderten Blocks
        //System.out.println(blocksize);
        int newblocksize = blocksize - size;                       //Groesse des neuen freien Blocks
        long newblockaddress = address + size + 1;                  //Adresse des neuen freien Blocks

        writeMarkerLowerBits(address-1, usedmarkervalue);
        writeLengthField(address, objectbytearray.length, lengthfieldsize);
        writeByteArray(address + lengthfieldsize, objectbytearray);
        writeLengthField(address + lengthfieldsize + objectbytearray.length, objectbytearray.length, lengthfieldsize);
        createNewTrailerMarker(address + size, usedmarkervalue);
        //System.out.println(newblockaddress + " " + newblocksize);
        cutFreeBlock(segment, newblockaddress, newblocksize);

        return address;
    }

    public void deallocate(long address){
        SegmentHeader segment = getSegmentByAddress(address);
        if(segment != null) {
            int lengthfieldsize = readMarkerLowerBits(address - 1) - 8;
            int freeblocksize = readLengthField(address) + 2 * lengthfieldsize;
            long freeblockstart = address;


            while (isBlockInSegment(address - 2, segment) && freeblocksize < MAX_BLOCK_SIZE && isPreviousBlockFree(freeblockstart)) {
                long previousblock = getPreviousBlock(freeblockstart);
                freeblockstart = previousblock;
                freeblocksize += readLengthField(freeblockstart);
            }

            long nextblock = getNextBlock(address);


            while (isBlockInSegment(nextblock, segment) && freeblocksize < MAX_BLOCK_SIZE && isNextBlockFree(address)) {
                freeblocksize += readLengthField(nextblock);
                nextblock = getNextBlock(nextblock);
            }

            int freeblocklist = segment.findFittingBlockList(freeblocksize);
            long listanchor = segment.getListAnchor(freeblocklist);
            byte markervalue = getFreeBlockMarkerValue(freeblocksize);
            writeMarkerLowerBits(freeblockstart-1, markervalue);
            writeMarkerUpperBits(freeblockstart + freeblocksize + 1, markervalue);
            createFreeBlock(freeblockstart, freeblocksize, listanchor, 0);
            segment.changeListAnchor(freeblocklist, freeblockstart);
        }
        else {
            System.out.println("Unknown Address");
        }

    }

    private void cutFreeBlock(SegmentHeader segment, long newblockaddress, int newblocksize){

        int blocklist = segment.findFittingBlockList(newblocksize);
        long listanchor = segment.getListAnchor(blocklist);
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

    private Serializable deserialize(byte[] value) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream bis = new ByteArrayInputStream(value);
        ObjectInput in = new ObjectInputStream(bis)) {
            return (Serializable) in.readObject();
        }
    }



    //Bloecke

    private long getNextBlock(long address){
        int lengthfieldsize = readMarkerLowerBits(address - 1);
        int blocksize = readLengthField(address);
        if(isBlockUsed(address)){
                return address + blocksize + 2 * (lengthfieldsize - 8) + 1;
        }
        return address + blocksize + 1;
    }

    private long getPreviousBlock(long address){
        System.out.println(address);
        int lengthfieldsize = readMarkerUpperBits(address - 1);
        System.out.println(lengthfieldsize);
        if(isBlockUsed(address)){
            lengthfieldsize -= 8;
        }
        int blocksize = readLengthField(address - 1 -lengthfieldsize);
        if(isBlockUsed(address)) return address - blocksize - 1;
        else return address - blocksize - 2 * lengthfieldsize -1;
    }

    private boolean isBlockInSegment(long address, SegmentHeader segment){
        //long blocksize = readLengthField(address);
        return address >= segment.startaddress && address <= segment.endaddress;
    }

    private boolean isPreviousBlockFree(long address){
        int prevmarker = readMarkerUpperBits(address-1);
        return (prevmarker >=0 && prevmarker <= 3);
    }

    private boolean isNextBlockFree(long address){
        int marker = readMarkerLowerBits(address - 1);
        System.out.println(marker);
        int blocksize = readLengthField(address);
        if(isBlockUsed(address)){
            blocksize += 2 * (marker - 8);
        }
        int nextmarker = readMarkerLowerBits(address + blocksize);
        return (nextmarker >=0 && nextmarker <= 3);
    }

    private boolean isBlockUsed(long address){
        int marker = readMarkerLowerBits(address - 1);
        return (marker >= 9 && marker <= 11);
    }

    //Segmente

    private SegmentHeader findSegment(){
        return segmentlist.get(0);
    }

    private SegmentHeader getSegmentByAddress(long address){
        for(int i = 0; i < segments; i++){
            if(address >= i * segmentsize && address < (i+1) * segmentsize){
                return segmentlist.get(i);
            }
        }

        return null;
    }

    //Laengenfeld

    private void writeLengthField(long address, long size, int fieldsize){

        byte[] buffer = ByteBuffer.allocate(Long.BYTES).putLong(size).array();
        byte[] convert = new byte[fieldsize];

        System.arraycopy(buffer, 0, convert, 0, fieldsize);
        writeByteArray(address, convert);
    }

    private int readLengthField(long address){
        int lengthfieldsize = readMarkerLowerBits(address-1);
        if(isBlockUsed(address)) lengthfieldsize -= 8;

        byte[] convert = new byte[Integer.BYTES];
        System.out.println(lengthfieldsize);
        for(int i = 0; i < lengthfieldsize; i++){
            convert[i] = offHeapAccess.readByte(address + i + addressoffset);
        }
        return ByteBuffer.wrap(convert).getInt();

    }

    //Adresslogik

    private byte[] convertAddressToByteArray(long value){
        return ByteBuffer.allocate(Long.BYTES).putLong(value).order(ByteOrder.BIG_ENDIAN).array();
    }

    private long convertByteArrayToAddress(byte[] value){
        byte[] convertarray = new byte[Long.BYTES];
        System.arraycopy(value, 0, convertarray, 0, ADDRESS_SIZE);
        return ByteBuffer.allocate(Long.BYTES).put(convertarray).flip().getLong();
    }

    private void writeAddressField(long address, long value){

        byte[] bytearray = convertAddressToByteArray(value);
        writeAddressByteArray(address, bytearray);

    }

    private long readAddressField(long address){

        byte[] bytearray = new byte[ADDRESS_SIZE];
        for(int i = 0; i < ADDRESS_SIZE; i++){
            bytearray[i] = offHeapAccess.readByte(address + i + addressoffset);
        }
        return convertByteArrayToAddress(bytearray);

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

    //Markerlogik

    private int readMarkerUpperBits(long address){
        byte marker = offHeapAccess.readByte(address + addressoffset);
        return marker >>> 4;
    }

    private int readMarkerLowerBits(long address){
        byte marker = offHeapAccess.readByte(address + addressoffset);
        return marker & 0xF;
    }

    private void writeMarkerUpperBits(long address, byte value){
        int lowerbits = readMarkerLowerBits(address);
        byte marker = (byte) (value << 4);
        marker += lowerbits;
        offHeapAccess.writeByte(address + addressoffset, marker);
    }

    private void writeMarkerLowerBits(long address, byte value){
        byte marker = offHeapAccess.readByte(address + addressoffset);
        marker = (byte) (marker & 0xF0);
        marker += value;
        offHeapAccess.writeByte(address + addressoffset, marker);
    }

    private void createNewTrailerMarker(long address, byte value){
        value = (byte) (value << 4);
        offHeapAccess.writeByte(address + addressoffset, value);
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


    public void writeByteArray(long address, byte[] value){
        for(int i = 0; i < value.length; i++){
            offHeapAccess.writeByte(address + i + addressoffset, value[i]);
        }
    }

    public void writeAddressByteArray(long address, byte[] value){
        for(int i = 0; i < ADDRESS_SIZE; i++){
            offHeapAccess.writeByte(address + i + addressoffset, value[i]);
        }
    }

    public Serializable readObject(long address) throws IOException, ClassNotFoundException {
        int lengthfieldsize = readMarkerLowerBits(address - 1);
        if(lengthfieldsize >= 1 && lengthfieldsize <= 3) {
            System.out.println("No object at this address");
            return null;
        } else {
            lengthfieldsize -= 8;
        }
        int objectsize = readLengthField(address);
        byte[] object = readByteArray(address, lengthfieldsize, objectsize);
        return deserialize(object);


    }

    public byte[] readByteArray(long address, int lengthfieldsize, int size){
        byte[] value = new byte[size];
        for(int i = 0; i < size; i++){
            value[i] = offHeapAccess.readByte(address + lengthfieldsize + i + addressoffset);
        }
        return value;
    }

    public int readInt(long address){
        return offHeapAccess.readInt(address + addressoffset);
    }

    public long readLong(long address){
        return offHeapAccess.readLong(address + addressoffset);
    }

    public double readDouble(long address){
        return offHeapAccess.readDouble(address + addressoffset);
    }

    public char readChar(long address){
        return offHeapAccess.readChar(address + addressoffset);
    }

    public byte readByte(long address){
        return offHeapAccess.readByte(address + addressoffset);
    }

    public short readShort(long address){
        return offHeapAccess.readShort(address + addressoffset);
    }

    public float readFloat(long address){
        return offHeapAccess.readFloat(address + addressoffset);
    }

    public boolean readBoolean(long address){
        return offHeapAccess.readBoolean(address + addressoffset);
    }

    public void cleanup(){
        ((OffHeap)offHeapAccess).freeMemory(addressoffset);
    }


}
