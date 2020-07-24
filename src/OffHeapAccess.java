// Interface zur Implementierung des Zugriffs auf den OffHeap.
// Die Funktionen sollen dazu dienen dazu, Primitive Datentypen/Objekte in den OffHeap zu schreiben oder zu lesen.


public interface OffHeapAccess {

    long writeInt(long address, int value);

    long writeLong(long address, long value);

    long writeDouble(long address, double value);

    long writeChar(long address, char value);

    long writeByte(long address, byte value);

    long writeShort(long address, short value);

    long writeFloat(long address, float value);

    long writeBoolean(long address, boolean value);

    int readInt(long address);

    long readLong(long address);

    double readDouble(long address);

    char readChar(long address);

    byte readByte(long address);

    short readShort(long address);

    float readFloat(long address);

    boolean readBoolean(long address);
}
