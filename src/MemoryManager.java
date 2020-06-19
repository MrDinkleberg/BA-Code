public class MemoryManager {

    private OffHeap offHeap;

    public MemoryManager(long size) throws NoSuchFieldException, IllegalAccessException {
        this.offHeap = new OffHeap(size);
    }

    public long writeInt(int value){
        return offHeap.writeInt(value);
    }

    public int readInt(long address){
        return offHeap.readInt(address);
    }


    public void cleanup(){
        offHeap.freeMemory(offHeap.startaddress);
    }


}
