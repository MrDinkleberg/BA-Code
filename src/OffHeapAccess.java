// Interface zur Implementierung des Zugriffs auf den OffHeap.
// Die Funktionen sollen dazu dienen dazu, Primitive Datentypen/Objekte in den OffHeap zu schreiben oder zu lesen.


public interface OffHeapAccess {

    long writeInt(int value);

    long writeLong(long value);

    long writeDouble(double value);

    long writeChar(char value);

    long writeByte(byte value);

    long writeShort(short value);

    long writeFloat(float value);

    long writeBoolean(boolean value);

    int readInt(long address);

    long readLong(long address);

    double readDouble(long address);

    char readChar(long address);

    byte readByte(long address);

    short readShort(long address);

    float readFloat(long address);

    boolean readBoolean(long address);
}
