import java.io.IOException;

public class TestMain {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException {

        MemoryManager memoryManager = new MemoryManager(500000000L, 10);
        System.out.println("init");

        TestObject testobj = new TestObject(200, 300, "Hallo");

        long address = memoryManager.allocate(testobj);
        System.out.println("allocated");
        //System.out.println(testobj);
        //System.out.println(memoryManager.readObject(address));
        //System.out.println(address);
        TestObject testobj2 = new TestObject(200, 300, "Hallo");
        TestObject testobj3 = new TestObject(Long.MAX_VALUE, Integer.MAX_VALUE, "Hallo");
        TestObject testobj4 = new TestObject(Long.MIN_VALUE, Integer.MIN_VALUE, "Hallo");
        TestObject testobj5 = new TestObject(0, 0, "");
        TestObject testobj6 = new TestObject(200, 300, "Hallo");
        long address2 = memoryManager.allocate(testobj2);
        //System.out.println(address2);
        long address3 = memoryManager.allocate(testobj3);
        //System.out.println(address3);
        long address4 = memoryManager.allocate(testobj4);
        //System.out.println(address4);
        long address5 = memoryManager.allocate(testobj5);
        memoryManager.writeObject(address, testobj6);
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


        /*byte[] test = memoryManager.convertAddressToByteArray(1048576);
        for(int i = 0; i < test.length; i++){
            System.out.println(test[i]);
        }
        System.out.println(memoryManager.convertByteArrayToAddress(test));*/


        //memoryManager.writeAddressField(1, 5);

        //memoryManager.writeLengthField(2, 4096, 2);
        //System.out.println(memoryManager.readLengthField(2, 2));

        //memoryManager.writeMarkerUpperBits(1, (byte) 15);
        //memoryManager.writeMarkerLowerBits(1, (byte) 3);
        //System.out.println(memoryManager.readByte(1));
        //System.out.println(memoryManager.readMarkerUpperBits(1));
        //System.out.println(memoryManager.readMarkerLowerBits(1));


        //System.out.println(memoryManager.readAddressField(1));
        //SegmentHeader segment = memoryManager.segmentlist.get(1);
        //System.out.println(segment.startaddress);
        //System.out.println(memoryManager.isBlockInSegment(segment.startaddress, segment));
        //System.out.println(memoryManager.isBlockInSegment(segment.startaddress - 1, segment));

        /*memoryManager.writeMarkerUpperBits(10, (byte)1);
        memoryManager.isBlockInSegment(1, memoryManager.segmentlist.get(0));
        System.out.println(memoryManager.readMarkerUpperBits(10));
        memoryManager.writeMarkerLowerBits(10, (byte) 9);
        System.out.println(memoryManager.readMarkerLowerBits(10));
        memoryManager.writeLengthField(11, 250, 1);
        System.out.println(memoryManager.readLengthField(11, 1));
        memoryManager.writeLengthField(262, 250, 1);
        System.out.println(memoryManager.readLengthField(262, 1));
        memoryManager.writeMarkerUpperBits(263, (byte) 9);
        System.out.println(memoryManager.readMarkerUpperBits(263));
        memoryManager.writeMarkerLowerBits(263, (byte) 9);
        System.out.println(memoryManager.readMarkerLowerBits(263));


        System.out.println(memoryManager.getPreviousBlock(264));
        //memoryManager.writeByte(2, (byte) 0);
        //memoryManager.writeByte(1, (byte) 0xFF);
        //System.out.println(0 | memoryManager.readByte(2));
        //System.out.println( (byte) 0xFE);*/

        memoryManager.cleanup();



    }

}
