// Interface zur Implementierung des Zugriffs auf den OffHeap.
// Die Funktionen sollen dazu dienen dazu, Primitive Datentypen/Objekte in den OffHeap zu schreiben oder zu lesen.


public interface OffHeapAccess {

    long writeInt(int value, long address);

    long writeLong(long value, long address);

    long writeDouble(double value, long address);

    long writeChar(char value, long address);

    long writeByte(byte value, long address);

    long writeShort(short value, long address);

    long writeFloat(float value, long address);

    long writeBoolean(boolean value, long address);

    int readInt(long address);

    long readLong(long address);

    double readDouble(long address);

    char readChar(long address);

    byte readByte(long address);

    short readShort(long address);

    float readFloat(long address);

    boolean readBoolean(long address);
}
