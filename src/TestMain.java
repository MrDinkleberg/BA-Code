import java.io.IOException;

public class TestMain {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException {

        MemoryManager memoryManager = new MemoryManager(100000000L, 10);

        TestObject testobj = new TestObject(200, 300, "Hallo");

        long address = memoryManager.allocate(testobj);
        System.out.println(address);
        memoryManager.deallocate(address);




    }

}
