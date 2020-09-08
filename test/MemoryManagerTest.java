import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MemoryManagerTest  {

    MemoryManager memoryManager;


    @Test
    void getNextBlock() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1, 1);

        //Erstellung von 3 300 Byte grossen freien Bloecken an den Adressen 1, 302 und 603
        memoryManager.writeMarkerLowerBits(0, (byte) 2);
        memoryManager.writeLengthField(1, 300, 2);
        memoryManager.writeLengthField(299, 300, 2);
        memoryManager.writeMarkerUpperBits(301, (byte) 2);

        memoryManager.writeMarkerLowerBits(301, (byte) 2);
        memoryManager.writeLengthField(302, 300, 2);
        memoryManager.writeLengthField(600, 300, 2);
        memoryManager.writeMarkerUpperBits(602, (byte) 2);

        memoryManager.writeMarkerLowerBits(602, (byte) 2);
        memoryManager.writeLengthField(603, 300, 2);
        memoryManager.writeLengthField(901, 300, 2);
        memoryManager.writeMarkerUpperBits(903, (byte) 2);

        long nextblock = memoryManager.getNextBlock(302);
        assertEquals(603, nextblock);

        memoryManager.cleanup();


    }

    @Test
    void getPreviousBlock() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1, 1);

        //Erstellung von 3 300 Byte grossen freien Bloecken an den Adressen 1, 302 und 603
        memoryManager.writeMarkerLowerBits(0, (byte) 2);
        memoryManager.writeLengthField(1, 300, 2);
        memoryManager.writeLengthField(299, 300, 2);
        memoryManager.writeMarkerUpperBits(301, (byte) 2);

        memoryManager.writeMarkerLowerBits(301, (byte) 2);
        memoryManager.writeLengthField(302, 300, 2);
        memoryManager.writeLengthField(600, 300, 2);
        memoryManager.writeMarkerUpperBits(602, (byte) 2);

        memoryManager.writeMarkerLowerBits(602, (byte) 2);
        memoryManager.writeLengthField(603, 300, 2);
        memoryManager.writeLengthField(901, 300, 2);
        memoryManager.writeMarkerUpperBits(903, (byte) 2);

        long prevblock = memoryManager.getPreviousBlock(302);
        assertEquals(1, prevblock);

        memoryManager.cleanup();
    }

    @Test
    void isPreviousBlockFree() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        //Erstellung von 3 300 Byte grossen freien Bloecken an den Adressen 1, 302 und 603
        memoryManager.writeMarkerLowerBits(0, (byte) 2);
        memoryManager.writeLengthField(1, 300, 2);
        memoryManager.writeLengthField(299, 300, 2);
        memoryManager.writeMarkerUpperBits(301, (byte) 2);

        memoryManager.writeMarkerLowerBits(301, (byte) 2);
        memoryManager.writeLengthField(302, 300, 2);
        memoryManager.writeLengthField(600, 300, 2);
        memoryManager.writeMarkerUpperBits(602, (byte) 2);

        memoryManager.writeMarkerLowerBits(602, (byte) 2);
        memoryManager.writeLengthField(603, 300, 2);
        memoryManager.writeLengthField(901, 300, 2);
        memoryManager.writeMarkerUpperBits(903, (byte) 2);

        assertTrue(memoryManager.isPreviousBlockFree(302));

        memoryManager.cleanup();
    }

    @Test
    void isPreviousBlockUsed() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        //Erstellung von 2 300 Byte grossen freien Bloecken an den Adressen 302 und 603
        //und einem belegten Block bei 1
        memoryManager.writeMarkerLowerBits(0, (byte) 10);
        memoryManager.writeLengthField(1, 296, 2);
        memoryManager.writeLengthField(299, 296, 2);
        memoryManager.writeMarkerUpperBits(301, (byte) 10);

        memoryManager.writeMarkerLowerBits(301, (byte) 2);
        memoryManager.writeLengthField(302, 300, 2);
        memoryManager.writeLengthField(600, 300, 2);
        memoryManager.writeMarkerUpperBits(602, (byte) 2);

        memoryManager.writeMarkerLowerBits(602, (byte) 2);
        memoryManager.writeLengthField(603, 300, 2);
        memoryManager.writeLengthField(901, 300, 2);
        memoryManager.writeMarkerUpperBits(903, (byte) 2);

        assertTrue(memoryManager.isPreviousBlockUsed(302));

        memoryManager.cleanup();

    }

    @Test
    void isNextBlockFree() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        //Erstellung von 3 300 Byte grossen freien Bloecken an den Adressen 1, 302 und 603
        memoryManager.writeMarkerLowerBits(0, (byte) 2);
        memoryManager.writeLengthField(1, 300, 2);
        memoryManager.writeLengthField(299, 300, 2);
        memoryManager.writeMarkerUpperBits(301, (byte) 2);

        memoryManager.writeMarkerLowerBits(301, (byte) 2);
        memoryManager.writeLengthField(302, 300, 2);
        memoryManager.writeLengthField(600, 300, 2);
        memoryManager.writeMarkerUpperBits(602, (byte) 2);

        memoryManager.writeMarkerLowerBits(602, (byte) 2);
        memoryManager.writeLengthField(603, 300, 2);
        memoryManager.writeLengthField(901, 300, 2);
        memoryManager.writeMarkerUpperBits(903, (byte) 2);

        assertTrue(memoryManager.isNextBlockFree(302));

        memoryManager.cleanup();
    }

    @Test
    void readLengthField() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        //Bereich wird mit 1en gefuellt
        for(int i = 0; i < 10; i++){
            memoryManager.writeByte(i, (byte) 0xFF);
        }

        memoryManager.writeLengthField(5, 65536, 3);
        assertEquals(65536, memoryManager.readLengthField(5, 3));

        memoryManager.cleanup();
    }

    @Test
    void readAddressField() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        for(int i = 0; i < 10; i++){
            memoryManager.writeByte(i, (byte) 0xFF);
        }

        memoryManager.writeAddressField(3, 500);
        assertEquals(500, memoryManager.readAddressField(3));

        memoryManager.cleanup();

    }

    @Test
    void readMarkerUpperBits() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        for(int i = 0; i < 10; i++){
            memoryManager.writeByte(i, (byte) 0xFF);
        }

        memoryManager.writeMarkerUpperBits(5, (byte) 3);
        assertEquals(3, memoryManager.readMarkerUpperBits(5));

        memoryManager.cleanup();

    }

    @Test
    void readMarkerLowerBits() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        for(int i = 0; i < 10; i++){
            memoryManager.writeByte(i, (byte) 0xFF);
        }

        memoryManager.writeMarkerLowerBits(5, (byte) 3);
        assertEquals(3, memoryManager.readMarkerLowerBits(5));

        memoryManager.cleanup();

    }

    @Test
    void markerValues() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        for(int i = 0; i < 10; i++){
            memoryManager.writeByte(i, (byte) 0xFF);
        }

        memoryManager.writeMarkerLowerBits(5, (byte) 3);
        memoryManager.writeMarkerUpperBits(5, (byte) 2);
        assertEquals(3, memoryManager.readMarkerLowerBits(5));
        assertEquals(2, memoryManager.readMarkerUpperBits(5));

        memoryManager.cleanup();

    }

    @Test
    void readByteArray() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(10000, 1);

        for(int i = 0; i < 10; i++){
            memoryManager.writeByte(i, (byte) 0xFF);
        }

        byte[] array = new byte[5];

        for(int i = 0; i < 5; i++){
            array[i] = 5;
        }

        memoryManager.writeByteArray(3, array);
        assertArrayEquals(array, memoryManager.readByteArray(3, 5));

        memoryManager.cleanup();

    }


    @Test
    void allocate() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(100000, 1, 1000);

        byte[] object = new byte[64];
        Arrays.fill(object, (byte) 1);


        long address = memoryManager.allocateSerialized(object);

        assertArrayEquals(object, memoryManager.readObject(address));

        memoryManager.cleanup();

    }

    @Test
    void writeObject() throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        memoryManager = new MemoryManager(100000, 1, 1000);

        byte[] object = new byte[64];
        Arrays.fill(object, (byte) 1);


        long address = memoryManager.allocateSerialized(object);

        byte[] newobject = new byte[64];
        Arrays.fill(newobject, (byte) 2);
        memoryManager.writeSerialized(address, newobject);

        assertArrayEquals(newobject, memoryManager.readObject(address));

        memoryManager.cleanup();

    }
}