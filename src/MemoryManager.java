import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    public SegmentHeader[] segmentlist;

    public MemoryManager(long size, int segments) throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        this.offheapsize = Math.max(size, segments * (MAX_BLOCK_SIZE + 2));
        this.offHeapAccess = new OffHeap(offheapsize);
        addressoffset = ((OffHeap) offHeapAccess).startaddress;
        segmentlist = new SegmentHeader[segments];
        this.segments = segments;
        createSegments(segments);
    }

    public void createSegments(int segments) throws InterruptedException {
        segmentsize = (int) offheapsize/segments;
        ExecutorService es = Executors.newCachedThreadPool();
        long segmentstart = 0;

        for(int i = 0; i < segments; i++){
            SegmentHeader segment = new SegmentHeader(segmentstart, segmentsize);
            segmentstart += segmentsize + 1;
            segmentlist[i] = segment;
            es.execute(() -> initSegment(segment));
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);
    }

    private void initSegment(SegmentHeader segment){
        long stamp = segment.lock.writeLock();
        try {
            long address = segment.startaddress;
            long previousblock = 0;                                     //Pointer für Verkettung der freien Bloecke
            long nextblock = address + (MAX_BLOCK_SIZE + 2);
            segment.changeListAnchor(segment.findExactBlockList(MAX_BLOCK_SIZE), address + 1);  //setzt Anker fuer Freie-Bloecke-Liste
            byte markervalue = getFreeBlockMarkerValue(MAX_BLOCK_SIZE);

            while (segment.endaddress - address >= MAX_BLOCK_SIZE + 1) {    //erstellt freie Bloecke maximaler Groesse und verkettet sie in die Liste
                writeMarkerLowerBits(address, markervalue);
                address++;
                createFreeBlock(address, MAX_BLOCK_SIZE, nextblock, previousblock);
                previousblock = address;
                if(segment.endaddress - address >= (MAX_BLOCK_SIZE + 1) * 2){
                    nextblock = address + (MAX_BLOCK_SIZE + 1);
                } else {
                    nextblock = 0;
                }
                address += MAX_BLOCK_SIZE;
                writeMarkerUpperBits(address, markervalue);
            }

            int remainingsize = (int) ((segment.endaddress - 1) - address);     //erstellt einen Block der restlichen Groesse
            if (remainingsize > 0 && address + remainingsize < segment.endaddress) {
                byte marker = getFreeBlockMarkerValue(remainingsize);
                if(marker == 15){
                    writeMarkerLowerBits(address, marker);
                    writeMarkerUpperBits(address + 1, marker);
                }
                else if(marker == 0){
                    writeMarkerLowerBits(address, marker);
                    address++;
                    writeLengthField(address, remainingsize, 1);
                    address += remainingsize +1;
                    writeMarkerUpperBits(address, marker);
                }
                else {
                    writeMarkerLowerBits(address, marker);
                    address++;
                    int blocklist = segment.findExactBlockList(remainingsize);
                    segment.freeblocks[blocklist] = address;
                    createFreeBlock(address, remainingsize, 0, 0);
                    address += remainingsize;
                    writeMarkerUpperBits(address, marker);
                }
            }
        } finally {
            segment.lock.unlockWrite(stamp);
        }
    }

    public void createFreeBlock(long address, int size, long next, long prev){
        int lengthfieldsize = getFreeBlockMarkerValue(size);
        writeLengthField(address, size, lengthfieldsize);
        writeAddressField(address + lengthfieldsize, next);
        writeAddressField(address + lengthfieldsize + ADDRESS_SIZE, prev);
        writeLengthField(address + size - lengthfieldsize, size, lengthfieldsize);

    }

    public long allocate(Serializable object) throws IOException {
        byte[] objectbytearray = serialize(object);                 //serialisiert das Objekt in ein Byte Array
        byte usedmarkervalue = getUsedBlockMarkerValue(objectbytearray.length);       //Markerwert des allozierten Speichers
        int lengthfieldsize = usedmarkervalue - 8;  //berechnet Laengenfeld fuer belegten Block
        int size = objectbytearray.length + 2 * lengthfieldsize;
        long address;

        SegmentHeader segment = findSegment();
        long stamp = segment.lock.writeLock();
        try {
            segment.usedspace += size;
            int blocklist = segment.findFittingBlockList(size);         //Freispeicher-Liste in der passenden Groesse
            address = segment.getListAnchor(blocklist);
            long nextfreeblock = getNextFreeBlock(address);
            int freelengthfieldsize = readMarkerLowerBits(nextfreeblock - 1);
            writeAddressField(nextfreeblock + freelengthfieldsize + ADDRESS_SIZE, 0);
            segment.changeListAnchor(blocklist, nextfreeblock);         //setzt den naechsten freien Block als Listenanker

            int blocksize = readLengthField(address, readMarkerLowerBits(address - 1));    //Groesse des angeforderten Blocks
            int newblocksize = blocksize - size;                       //Groesse des neuen freien Blocks

            long newblockaddress = address + size + 1;                  //Adresse des neuen freien Blocks

            writeMarkerLowerBits(address - 1, usedmarkervalue);
            writeLengthField(address, objectbytearray.length, lengthfieldsize);
            writeByteArray(address + lengthfieldsize, objectbytearray, objectbytearray.length);
            writeLengthField(address + lengthfieldsize + objectbytearray.length, objectbytearray.length, lengthfieldsize);
            createNewTrailerMarker(address + size, usedmarkervalue);
            if(newblocksize > 0) {
                cutFreeBlock(segment, newblockaddress, newblocksize); //erstellt aus ueberschuessigem Speicher neuen freien Block
            }
        } finally {
            segment.lock.unlockWrite(stamp);
        }
        return address;


    }

    public long allocateSerialized(byte[] object) {
        byte usedmarkervalue = getUsedBlockMarkerValue(object.length);       //Markerwert des allozierten Speichers
        int lengthfieldsize = usedmarkervalue - 8;  //berechnet Laengenfeld fuer belegten Block
        int size = object.length + 2 * lengthfieldsize;
        long address;

        SegmentHeader segment = findSegment();
        long stamp = segment.lock.writeLock();
        try {
            segment.usedspace += size;
            int blocklist = segment.findFittingBlockList(size);         //Freispeicher-Liste in der passenden Groesse
            address = segment.getListAnchor(blocklist);
            long nextfreeblock = getNextFreeBlock(address);
            int freelengthfieldsize = readMarkerLowerBits(nextfreeblock - 1);
            writeAddressField(nextfreeblock + freelengthfieldsize + ADDRESS_SIZE, 0);
            segment.changeListAnchor(blocklist, nextfreeblock);         //setzt den naechsten freien Block als Listenanker

            int blocksize = readLengthField(address, readMarkerLowerBits(address - 1));    //Groesse des angeforderten Blocks
            int newblocksize = blocksize - size;                       //Groesse des neuen freien Blocks

            long newblockaddress = address + size + 1;                  //Adresse des neuen freien Blocks

            writeMarkerLowerBits(address - 1, usedmarkervalue);
            writeLengthField(address, object.length, lengthfieldsize);
            writeByteArray(address + lengthfieldsize, object, object.length);
            writeLengthField(address + lengthfieldsize + object.length, object.length, lengthfieldsize);
            createNewTrailerMarker(address + size, usedmarkervalue);
            if(newblocksize > 0) {
                cutFreeBlock(segment, newblockaddress, newblocksize); //erstellt aus ueberschuessigem Speicher neuen freien Block
            }
        } finally {
            segment.lock.unlockWrite(stamp);
        }
        return address;


    }

    public void deallocate(long address){
        SegmentHeader segment = getSegmentByAddress(address);

        if(segment != null) {
            long stamp = segment.lock.writeLock();
            try {
                int lengthfieldsize = readMarkerLowerBits(address - 1) - 8;
                int freeblocksize = readLengthField(address, lengthfieldsize) + 2 * lengthfieldsize;
                segment.usedspace -= freeblocksize;
                long freeblockstart = address;
                long previousblock;

                if (isBlockInSegment(freeblockstart - 2, segment) && isPreviousBlockFree(freeblockstart) && freeblocksize < MAX_BLOCK_SIZE) {
                    previousblock = getPreviousBlock(freeblockstart);
                    int prevmarker = readMarkerLowerBits(previousblock - 1);
                    if (prevmarker == 0) {
                        int prevsize = readLengthField(previousblock, 1);
                        if (freeblocksize + prevsize + 1 <= MAX_BLOCK_SIZE) {
                            freeblocksize += prevsize;
                            freeblockstart = previousblock;
                        }
                    } else if (prevmarker == 15) {
                        int prevsize = 1;
                        if (freeblocksize + prevsize + 1 <= MAX_BLOCK_SIZE) {
                            freeblocksize += prevsize;
                            freeblockstart = previousblock;
                        }
                    } else {
                        int prevsize = readLengthField(previousblock, prevmarker);
                        if (freeblocksize + prevsize + 1 <= MAX_BLOCK_SIZE) {
                            removeBlockFromFreeBlockList(previousblock);
                            freeblocksize += prevsize;
                            freeblockstart = previousblock;
                        }
                    }
                }

                long nextblock = getNextBlock(address);


                if (isBlockInSegment(nextblock, segment) && isNextBlockFree(address) && freeblocksize < MAX_BLOCK_SIZE) {
                    int nextmarker = readMarkerLowerBits(nextblock - 1);
                    if (nextmarker == 0) {
                        int nextblocksize = readLengthField(nextblock, 1);
                        if (freeblocksize + nextblocksize + 1 <= MAX_BLOCK_SIZE) {
                            freeblocksize += nextblocksize + 1;
                        }
                    } else if (nextmarker == 15) {
                        int nextblocksize = 1;
                        if (freeblocksize + nextblocksize + 1 <= MAX_BLOCK_SIZE) {
                            freeblocksize += nextblocksize + 1;
                        }
                    } else {
                        int nextblocksize = readLengthField(nextblock, nextmarker);
                        if (freeblocksize + nextblocksize <= MAX_BLOCK_SIZE) {
                            freeblocksize += nextblocksize + 1;
                            removeBlockFromFreeBlockList(nextblock);
                        }
                    }


                }

                int freeblocklist = segment.findExactBlockList(freeblocksize);
                long listanchor = segment.getListAnchor(freeblocklist);
                byte markervalue = getFreeBlockMarkerValue(freeblocksize);
                writeMarkerLowerBits(freeblockstart - 1, markervalue);
                writeMarkerUpperBits(freeblockstart + freeblocksize + 1, markervalue);
                createFreeBlock(freeblockstart, freeblocksize, listanchor, 0);
                segment.changeListAnchor(freeblocklist, freeblockstart);
            }finally {
                segment.lock.unlockWrite(stamp);
            }
        }
        else {
            System.out.println("Unknown Address");
        }

    }

    public void writeObject(long address, Serializable object) throws IOException {
        SegmentHeader segment = getSegmentByAddress(address);
        if(segment != null) {
            long stamp = segment.lock.writeLock();
            try {
                int lengthfieldsize = readMarkerLowerBits(address - 1) - 8;
                int blocksize = readLengthField(address, lengthfieldsize);
                byte[] objectarray = serialize(object);
                if (objectarray.length != blocksize) {
                    System.out.println("Object is of different size");
                } else {
                    writeAddressByteArray(address + lengthfieldsize, objectarray);
                }
            } finally {
                segment.lock.unlockWrite(stamp);
            }
        }
    }

    public void writeSerialized(long address, byte[] object) {
        SegmentHeader segment = getSegmentByAddress(address);
        if(segment != null) {
            long stamp = segment.lock.writeLock();
            try {
                int lengthfieldsize = readMarkerLowerBits(address - 1) - 8;
                int blocksize = readLengthField(address, lengthfieldsize);
                if (object.length != blocksize) {
                    System.out.println("Object is of different size");
                } else {
                    writeAddressByteArray(address + lengthfieldsize, object);
                }
            } finally {
                segment.lock.unlockWrite(stamp);
            }
        }
    }

    public byte[] readObject(long address){
        SegmentHeader segment = getSegmentByAddress(address);
        byte[] object = null;
        if(segment!=null) {
            long stamp = segment.lock.tryOptimisticRead();
            for (int i = 0; i < 3; i++) {
                int lengthfieldsize = readMarkerLowerBits(address - 1);
                if (lengthfieldsize >= 1 && lengthfieldsize <= 3) {
                    System.out.println("No object at this address");
                    return null;
                } else {
                    lengthfieldsize -= 8;
                }
                int objectsize = readLengthField(address, lengthfieldsize);
                object = readByteArray(address + lengthfieldsize, objectsize);
                if (segment.lock.validate(stamp)) {
                    return object;
                }
            }
            stamp = segment.lock.readLock();
            try {
                int lengthfieldsize = readMarkerLowerBits(address - 1);
                if (lengthfieldsize >= 1 && lengthfieldsize <= 3) {
                    System.out.println("No object at this address");
                    return null;
                } else {
                    lengthfieldsize -= 8;
                }
                int objectsize = readLengthField(address, lengthfieldsize);
                object = readByteArray(address + lengthfieldsize, objectsize);

            } finally {
                segment.lock.unlockRead(stamp);
            }
        }
        return object;
    }

    private void cutFreeBlock(SegmentHeader segment, long newblockaddress, int newblocksize){

        byte markervalue = getFreeBlockMarkerValue(newblocksize);

        if(markervalue == 15){
            writeMarkerLowerBits(newblockaddress - 1, markervalue);
            writeMarkerUpperBits(newblockaddress, markervalue);
        }
        else if(markervalue == 0){
            writeMarkerLowerBits(newblockaddress - 1, markervalue);
            writeLengthField(newblockaddress, newblocksize, 1);
            writeLengthField(newblockaddress + newblocksize - 1, newblocksize, 1);
            writeMarkerUpperBits(newblockaddress + newblocksize, markervalue);
        }
        else {
            int blocklist = segment.findExactBlockList(newblocksize);
            long listanchor = segment.getListAnchor(blocklist);
            if (listanchor != 0) {
                int lengthfieldsize = readMarkerLowerBits(listanchor - 1);
                writeAddressField(listanchor + lengthfieldsize + ADDRESS_SIZE, newblockaddress);
            }
            segment.changeListAnchor(blocklist, newblockaddress);


            writeMarkerLowerBits(newblockaddress - 1, markervalue);
            createFreeBlock(newblockaddress, newblocksize, listanchor, 0);
            writeMarkerUpperBits(newblockaddress + newblocksize, markervalue);
        }

    }

    private byte[] serialize(Serializable object) throws IOException {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }




    //Bloecke

    public long getNextBlock(long address){
        int marker = readMarkerLowerBits(address - 1);
        int blocksize;
        if(marker == 0){
            blocksize = readLengthField(address, 1);
            return address + blocksize + 1;
        } else if(marker == 15){
            return address + 1;
        } else {
            if (isBlockUsed(address)) marker -= 8;
            blocksize = readLengthField(address, marker);
            if (isBlockUsed(address)) {
                return address + blocksize + 2 * (marker - 8) + 1;
            }
            return address + blocksize + 1;
        }
    }

    public long getPreviousBlock(long address){
        int marker = readMarkerUpperBits(address - 1);
        int blocksize;
        if(marker == 0) {
            blocksize = readLengthField(address - 2, 1);
            return address - blocksize - 1;
        } else if(marker == 15){
            return address - 1;
        }
        else {
            if (marker >= 9 && marker <= 11) {
                marker -= 8;
            }
            blocksize = readLengthField((address - 1 - marker), marker);
            if (isPreviousBlockUsed(address)) return address - blocksize - 2 * marker - 1;
            else return address - blocksize - 1;
        }
    }


    public boolean isBlockInSegment(long address, SegmentHeader segment){
        return ((address >= segment.startaddress) && (address <= segment.endaddress));
    }

    public boolean isPreviousBlockFree(long address){
        int prevmarker = readMarkerUpperBits(address-1);
        return ((prevmarker >=1 && prevmarker <= 3) || prevmarker == 0 || prevmarker == 15);
    }

    public boolean isPreviousBlockUsed(long address){
        int prevmarker = readMarkerUpperBits(address-1);
        return ((prevmarker >=9 && prevmarker <= 11));
    }


    public boolean isNextBlockFree(long address){
        int lengthfieldsize = readMarkerLowerBits(address - 1);
        if(isBlockUsed(address)){
            lengthfieldsize -= 8;
        }
        int blocksize = readLengthField(address, lengthfieldsize);
        if(isBlockUsed(address)){
            blocksize += 2 * lengthfieldsize;
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
        int index = 0;

        for(int i = 0; i < segments; i++){
            if(segmentlist[i].usedspace > segmentlist[index].usedspace) index = i;
        }
        return segmentlist[index];
    }

    private SegmentHeader getSegmentByAddress(long address){
        for(int i = 0; i < segments; i++){
            if(address >= segmentlist[i].startaddress && address < segmentlist[i].endaddress){
                return segmentlist[i];
            }
        }

        return null;
    }

    //Laengenfeld

    public void writeLengthField(long address, int size, int fieldsize){

        byte[] buffer = ByteBuffer.allocate(Integer.BYTES).putInt(size).array();
        int counter = 0;
        for(int i = Integer.BYTES - fieldsize; i < Integer.BYTES; i++){
            offHeapAccess.writeByte(address + counter + addressoffset, buffer[i]);
            counter++;
        }

        //byte[] convert = new byte[fieldsize];

        //System.arraycopy(buffer, 0, convert, 0, fieldsize);
        //writeByteArray(address, buffer, fieldsize);
        /*for(int i = 0; i < fieldsize; i++){
            byte convert = (byte) (size >>> i * 8);
            offHeapAccess.writeByte(address + i + addressoffset, convert);
        }*/
    }

    public int readLengthField(long address, int lengthfieldsize){
        byte[] convert = new byte[Integer.BYTES];
        int counter = 0;
        for(int i = Integer.BYTES - lengthfieldsize; i < Integer.BYTES; i++){
            convert[i] = offHeapAccess.readByte(address + counter + addressoffset);
            counter++;
        }
        return ByteBuffer.allocate(Integer.BYTES).put(convert).flip().getInt();

        /*int value = 0;
        for(int i = 0; i < lengthfieldsize; i++){
            value = value | offHeapAccess.readByte(address + i + addressoffset);
            value = value << 8;
        }
        return value;*/


    }

    //Adresslogik

    public byte[] convertAddressToByteArray(long value){
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

    public long convertByteArrayToAddress(byte[] value){
        return ByteBuffer.allocate(Long.BYTES).put(value).flip().getLong();
    }

    public void writeAddressField(long address, long value){

        byte[] bytearray = convertAddressToByteArray(value);
        writeAddressByteArray(address, bytearray);

    }

    public long readAddressField(long address){

        int counter = 0;

        byte[] bytearray = new byte[Long.BYTES];
        for(int i = Long.BYTES - ADDRESS_SIZE - 1; i < Long.BYTES; i++){
            bytearray[i] = offHeapAccess.readByte(address + counter + addressoffset);
            counter++;
        }
        return convertByteArrayToAddress(bytearray);

    }

    private long getNextFreeBlock(long address){
        int lengthfieldsize = readMarkerLowerBits(address-1);
        return readAddressField(address + lengthfieldsize);
    }

    private long getPreviousFreeBlock(long address){
        int lengthfieldsize = readMarkerLowerBits(address-1);
        return readAddressField(address + lengthfieldsize + ADDRESS_SIZE);
    }

    private void removeBlockFromFreeBlockList(long address){
        SegmentHeader segment = getSegmentByAddress(address);
        long nextblock = getNextFreeBlock(address);
        int nextblocklengthfield = readMarkerLowerBits(nextblock - 1);
        long prevblock = getPreviousBlock(address);
        writeAddressField(nextblock + nextblocklengthfield + ADDRESS_SIZE, prevblock);
        int prevblocklengthfield = readMarkerLowerBits(prevblock - 1);
        writeAddressField(prevblock + prevblocklengthfield, nextblock);

    }




    //Markerlogik

    public int readMarkerUpperBits(long address){
        byte marker = offHeapAccess.readByte(address + addressoffset);
        int value = marker & 0xFF;
        return (byte) (value >>> 4);
    }

    public int readMarkerLowerBits(long address){
        byte marker = offHeapAccess.readByte(address + addressoffset);
        int value = marker & 0xFF;
        return (byte) (value & 0xF);
    }

    public void writeMarkerUpperBits(long address, byte value){
        int lowerbits = readMarkerLowerBits(address);
        byte marker = (byte) (value << 4);
        marker = (byte) (marker | lowerbits);
        offHeapAccess.writeByte(address + addressoffset, marker);
    }

    public void writeMarkerLowerBits(long address, byte value){
        byte marker = offHeapAccess.readByte(address + addressoffset);
        marker = (byte) (marker & 0xF0);
        marker = (byte) (marker | value);
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

    public void writeByte(long address, byte value){
        offHeapAccess.writeByte(address + addressoffset, value);
    }


    public void writeByteArray(long address, byte[] value, int length){
        for(int i = 0; i < value.length; i++){
            offHeapAccess.writeByte(address + i + addressoffset, value[i]);
        }
    }

    public void writeAddressByteArray(long address, byte[] value){
        int counter = 0;
        for(int i = Long.BYTES - ADDRESS_SIZE - 1; i < Long.BYTES; i++){
            offHeapAccess.writeByte(address + counter + addressoffset, value[i]);
            counter++;
        }
    }



    public byte[] readByteArray(long address, int size){
        byte[] value = new byte[size];
        for(int i = 0; i < size; i++){
            value[i] = offHeapAccess.readByte(address + i + addressoffset);
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
        offHeapAccess.freeMemory(addressoffset);
    }


}
