import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.concurrent.*;

public class Benchmarks {

    public static void main(String[] args) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException, IOException, ClassNotFoundException {
        benchmarkInit(1000000000L, 1);
        benchmarkInit(1000000000L, 10);
        benchmarkInit(1000000000L, 20);
        benchmarkWrites(1000000000L, 1, 1000);
        benchmarkWrites(1000000000L, 10, 1000);
        benchmarkWrites(1000000000L, 20, 1000);
        benchmarkWritesAndReads(1000000000L, 1, 1000);
        benchmarkWritesAndReads(1000000000L, 10, 1000);
        benchmarkWritesAndReads(1000000000L, 20, 1000);
    }




    public static void benchmarkInit(long size, int segments) throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        long starttime = System.nanoTime();
        MemoryManager memoryManager = new MemoryManager(size, segments);

        long endtime = System.nanoTime();

        System.out.println("Time to complete " + ((double) (endtime - starttime)/1000000) + " milliseconds");

        memoryManager.cleanup();

    }
    public static  void benchmarkWrites(long size, int segments, int writes) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments);
        BenchmarkObject64 object = new BenchmarkObject64();
        long[] addresses = new long[writes];

        ExecutorService es = Executors.newCachedThreadPool();

        long starttime = System.nanoTime();

        for(int i = 0; i < writes; i++){
            Callable<Long> task = () -> memoryManager.allocate(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);


        long endtime = System.nanoTime();

        System.out.println("Time to complete " + ((double)(endtime - starttime)/1000000) + " milliseconds");

        memoryManager.cleanup();
    }

    public static  void benchmarkWritesAndReads(long size, int segments, int writes) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException, IOException, ClassNotFoundException {

        MemoryManager memoryManager = new MemoryManager(size, segments);
        BenchmarkObject64 object = new BenchmarkObject64();

        long[] addresses = new long[writes];
        BenchmarkObject64[] objects = new BenchmarkObject64[writes];

        ExecutorService es = Executors.newCachedThreadPool();

        long starttime = System.nanoTime();

        for(int i = 0; i < writes; i++){
            Callable<Long> task = () -> memoryManager.allocate(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);

        es = Executors.newCachedThreadPool();

        for(int i = 0; i < writes; i++){
            long address = addresses[i];
            Callable<byte[]> task = () -> memoryManager.readObject(address);
            Future<byte[]> future = es.submit(task);
            objects[i] = deserialize(future.get());
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);

        long endtime = System.nanoTime();

        System.out.println("Time to complete " + ((double)(endtime - starttime)/1000000) + " milliseconds");

        memoryManager.cleanup();
    }

    private static BenchmarkObject64 deserialize(byte[] object) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream((object));
        ObjectInput in = new ObjectInputStream(bis);
        return (BenchmarkObject64) in.readObject();

    }


}
