

public class MemoryManager {


        private OffHeap offHeap;

        public MemoryManager(long size) throws NoSuchFieldException, IllegalAccessException {
                this.offHeap = new OffHeap(size);
        }





}
