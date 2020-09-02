import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

public class Benchmarks {

    public static void main(String[] args) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {
        System.out.println("Init benchmarks");
        System.out.println("1GB, 1 segment:");
        benchmarkInit(1000000000L, 1);
        System.out.println("1GB, 10 segments:");
        benchmarkInit(1000000000L, 10);
        System.out.println("1GB, 20 segments:");
        benchmarkInit(1000000000L, 20);

        System.out.println("Write benchmarks, 1GB memory");
        System.out.println("1 segment, 1000 writes:");
        benchmarkWrites(1000000000L, 1, 1000);
        System.out.println("10 segments, 1000 writes:");
        benchmarkWrites(1000000000L, 10, 1000);
        System.out.println("20 segments, 1000 writes:");
        benchmarkWrites(1000000000L, 20, 1000);

        System.out.println("Reads after writes, 1GB memory");
        System.out.println("1 segment, 1000 writes/reads");
        benchmarkWritesAndReads(1000000000L, 1, 1000);
        System.out.println("10 segments, 1000 writes/reads");
        benchmarkWritesAndReads(1000000000L, 10, 1000);
        System.out.println("20 segments, 1000 writes/reads");
        benchmarkWritesAndReads(1000000000L, 20, 1000);

        System.out.println("Mixed reads and writes, 1GB memory");
        System.out.println("1 segment, 1000 reads/writes");
        benchmarkWritesRandomReads(1000000000L, 1, 1000);
        System.out.println("10 segment, 1000 reads/writes");
        benchmarkWritesRandomReads(1000000000L, 10, 1000);
        System.out.println("20 segment, 1000 reads/writes");
        benchmarkWritesRandomReads(1000000000L, 20, 1000);
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
        byte[] object = new byte[64];
        long[] addresses = new long[writes];

        ExecutorService es = Executors.newCachedThreadPool();

        long starttime = System.nanoTime();

        for(int i = 0; i < writes; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);


        long endtime = System.nanoTime();

        System.out.println("Time to complete " + ((double)(endtime - starttime)/1000000) + " milliseconds");

        memoryManager.cleanup();
    }

    public static  void benchmarkWritesAndReads(long size, int segments, int writes) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments);
        byte[] object = new byte[64];

        long[] addresses = new long[writes];
        byte[][] objects = new byte[writes][64];

        ExecutorService es = Executors.newCachedThreadPool();

        long starttime = System.nanoTime();

        for(int i = 0; i < writes; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
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
            objects[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);

        long endtime = System.nanoTime();

        System.out.println("Time to complete " + ((double)(endtime - starttime)/1000000) + " milliseconds");

        memoryManager.cleanup();
    }

    public static  void benchmarkWritesRandomReads(long size, int segments, int iterations) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments);
        byte[] object = new byte[64];
        ArrayList<Long> addresses = new ArrayList<>();
        byte[][] objects = new byte[iterations][64];
        Random rand = new Random();
        ExecutorService es = Executors.newCachedThreadPool();

        long starttime = System.nanoTime();

        for(int i = 0; i < iterations; i++) {
            Callable<Long> wtask = () -> memoryManager.allocateSerialized(object);
            Future<Long> wfuture = es.submit(wtask);
            addresses.add(wfuture.get());
            long address = addresses.get(rand.nextInt(addresses.size()));
            Callable<byte[]> rtask = () -> memoryManager.readObject(address);
            Future<byte[]> rfuture = es.submit(rtask);
            objects[i] = rfuture.get();
        }

        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);


        long endtime = System.nanoTime();

        System.out.println("Time to complete " + ((double)(endtime - starttime)/1000000) + " milliseconds");

        memoryManager.cleanup();
    }


}
