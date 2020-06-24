import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;

class OffHeapTest {

    MemoryManager memoryManager;

    @Test
    void readWriteInt() throws NoSuchFieldException, IllegalAccessException {
        memoryManager = new MemoryManager(1000);
        int test = Integer.MAX_VALUE;
        long address = memoryManager.writeInt(test);
        assertEquals(test, memoryManager.readInt(address));
        memoryManager.cleanup();
    }

    @Test
    void readWriteLong() throws NoSuchFieldException, IllegalAccessException {
        memoryManager = new MemoryManager(1000);
        long test = Long.MAX_VALUE;
        long address = memoryManager.writeLong(test);
        assertEquals(test, memoryManager.readLong(address));
        memoryManager.cleanup();
    }

    @Test
    void readWriteDouble() throws NoSuchFieldException, IllegalAccessException {
        memoryManager = new MemoryManager(1000);
        double test = Double.MAX_VALUE;
        long address = memoryManager.writeDouble(test);
        assertEquals(test, memoryManager.readDouble(address));
        memoryManager.cleanup();
    }

    @Test
    void readWriteChar() throws NoSuchFieldException, IllegalAccessException {
        memoryManager = new MemoryManager(1000);
        char test = 'A';
        long address = memoryManager.writeChar(test);
        assertEquals('A', memoryManager.readChar(address));
        memoryManager.cleanup();
    }

    @Test
    void readWriteByte() throws NoSuchFieldException, IllegalAccessException {
        memoryManager = new MemoryManager(1000);
        byte test = Byte.MAX_VALUE;
        long address = memoryManager.writeByte(test);
        assertEquals(test, memoryManager.readByte(address));
        memoryManager.cleanup();
    }

    @Test
    void readWriteShort() throws NoSuchFieldException, IllegalAccessException {
        memoryManager = new MemoryManager(1000);
        short test = Short.MAX_VALUE;
        long address = memoryManager.writeShort(test);
        assertEquals(test, memoryManager.readShort(address));
        memoryManager.cleanup();
    }

    @Test
    void readWriteFloat() throws NoSuchFieldException, IllegalAccessException {
        memoryManager = new MemoryManager(1000);
        float test = Float.MAX_VALUE;
        long address = memoryManager.writeFloat(test);
        assertEquals(test, memoryManager.readFloat(address));
        memoryManager.cleanup();
    }

    @Test
    void readWriteBoolean() throws NoSuchFieldException, IllegalAccessException {
        memoryManager = new MemoryManager(1000);
        boolean test = false;
        long address = memoryManager.writeBoolean(test);
        assertEquals(test, memoryManager.readBoolean(address));
        memoryManager.cleanup();
    }
}