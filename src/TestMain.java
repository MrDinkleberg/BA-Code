import java.io.IOException;

public class TestMain {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {

        MemoryManager memoryManager = new MemoryManager(100000000L, 10);

        TestObject testobj = new TestObject(200, 300, "Hallo");

        long address = memoryManager.allocate(testobj);
        //System.out.println(testobj);
        //System.out.println(memoryManager.readObject(address));
        //System.out.println(address);
        TestObject testobj2 = new TestObject(200, 300, "Hallo");
        TestObject testobj3 = new TestObject(Long.MAX_VALUE, Integer.MAX_VALUE, "Hallo");
        TestObject testobj4 = new TestObject(Long.MIN_VALUE, Integer.MIN_VALUE, "Hallo");
        TestObject testobj5 = new TestObject(0, 0, "");
        long address2 = memoryManager.allocate(testobj2);
        //System.out.println(address2);
        long address3 = memoryManager.allocate(testobj3);
        //System.out.println(address3);
        long address4 = memoryManager.allocate(testobj4);
        //System.out.println(address4);
        long address5 = memoryManager.allocate(testobj5);
        //System.out.println(address5);
        memoryManager.deallocate(address);
        System.out.println("deallocated");
        memoryManager.deallocate(address2);
        System.out.println("deallocated");
        memoryManager.deallocate(address3);
        System.out.println("deallocated");
        memoryManager.deallocate(address4);
        System.out.println("deallocated");
        memoryManager.deallocate(address5);
        System.out.println("deallocated");
        memoryManager.cleanup();



    }

}
