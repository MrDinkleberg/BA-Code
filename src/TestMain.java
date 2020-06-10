public class TestMain {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {

        MemoryManager memoryManager = new MemoryManager(1000000000);

        int testIntMax = Integer.MAX_VALUE;
        int testIntMin = Integer.MIN_VALUE;

        long address = memoryManager.writeInt(testIntMax);

        System.out.println("store "+ testIntMax + " at " + address );

        address = memoryManager.writeInt(testIntMin);

        System.out.println("store "+ testIntMin + " at " + address);

        memoryManager.cleanup();



    }

}
