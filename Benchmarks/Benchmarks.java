import java.util.Random;
import java.util.concurrent.*;

public class Benchmarks {

    public static void main(String[] args) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {
        int mode = Integer.parseInt(args[0]);
        long size = Long.parseLong(args[1]) * 1000000000;

        System.out.println(args[1] + "GB Memory");

        switch (mode){
            case 1:
                System.out.println("Init benchmarks");
                System.out.println("1GB, 1 segment:");
                benchmarkInit(size, 1);
                System.out.println("1GB, 10 segments:");
                benchmarkInit(size, 10);
                System.out.println("1GB, 20 segments:");
                benchmarkInit(size, 20);
            case 2:
                System.out.println("Write benchmarks, 1GB memory");
                System.out.println("1 segment, 1000 writes:");
                benchmarkWrites(size, 1, 1000);
                System.out.println("10 segments, 1000 writes:");
                benchmarkWrites(size, 10, 1000);
                System.out.println("20 segments, 1000 writes:");
                benchmarkWrites(size, 20, 1000);
            case 3:
                System.out.println("Reads after writes");
                System.out.println("1 segment, 1000 writes/reads");
                benchmarkReads(size, 1, 1000, 1000);
                System.out.println("10 segments, 1000 writes/reads");
                benchmarkReads(size, 10, 1000, 1000);
                System.out.println("20 segments, 1000 writes/reads");
                benchmarkReads(size, 20, 1000, 1000);
            case 4:
                System.out.println("Mixed reads and writes, 1GB memory");
                System.out.println("1 segment, 1000 reads/writes");
                benchmarkWritesReads(size, 1, 1000);
                System.out.println("10 segment, 1000 reads/writes");
                benchmarkWritesReads(size, 10, 1000);
                System.out.println("20 segment, 1000 reads/writes");
                benchmarkWritesReads(size, 20, 1000);
            case 5:
                System.out.println("Writes and multiple reads");
                System.out.println("1 segment, 1000 iterations, 10 reads per write");
                benchmarkWritesMultipleReads(size, 1, 1000, 10);
                System.out.println("10 segment, 1000 iterations, 10 reads per write");
                benchmarkWritesMultipleReads(size, 10, 1000, 10);
                System.out.println("20 segment, 1000 iterations, 10 reads per write");
                benchmarkWritesMultipleReads(size, 20, 1000, 10);
        }
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

    public static  void benchmarkReads(long size, int segments, int writes, int reads) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments);
        byte[] object = new byte[64];

        long[] addresses = new long[writes];
        byte[][] objects = new byte[reads][64];

        ExecutorService es = Executors.newCachedThreadPool();


        for(int i = 0; i < writes; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);

        es = Executors.newCachedThreadPool();

        long starttime = System.nanoTime();

        for(int i = 0; i < reads; i++){
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

    public static  void benchmarkWritesReads(long size, int segments, int iterations) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments);
        byte[] object = new byte[64];
        long[] addresses = new long[iterations];
        byte[][] objects = new byte[iterations][64];
        Random rand = new Random();
        ExecutorService es = Executors.newCachedThreadPool();


        for(int i = 0; i < iterations; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);

        es = Executors.newCachedThreadPool();

        long starttime = System.nanoTime();


        for(int i = 0; i < iterations; i++) {
            long addressw = addresses[rand.nextInt(iterations)];
            Runnable wtask = () -> memoryManager.writeSerialized(addressw, object);
            es.execute(wtask);
            long addressr = addresses[rand.nextInt(iterations)];
            Callable<byte[]> rtask = () -> memoryManager.readObject(addressr);
            Future<byte[]> rfuture = es.submit(rtask);
            objects[i] = rfuture.get();
        }

        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);


        long endtime = System.nanoTime();

        System.out.println("Time to complete " + ((double)(endtime - starttime)/1000000) + " milliseconds");

        memoryManager.cleanup();
    }

    public static  void benchmarkWritesMultipleReads(long size, int segments, int iterations, int reads) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments);
        byte[] object = new byte[64];
        long[] addresses = new long[iterations];
        byte[][] objects = new byte[iterations][64];
        Random rand = new Random();
        ExecutorService es = Executors.newCachedThreadPool();

        for(int i = 0; i < iterations; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);

        es = Executors.newCachedThreadPool();

        for(int i = 0; i < iterations; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);


        long starttime = System.nanoTime();


        for(int i = 0; i < iterations; i++) {
            long addressw = addresses[rand.nextInt(iterations)];
            Runnable wtask = () -> memoryManager.writeSerialized(addressw, object);
            es.execute(wtask);
            for(int j = 0; j < reads; j++) {
                long addressr = addresses[rand.nextInt(iterations)];
                Callable<byte[]> rtask = () -> memoryManager.readObject(addressr);
                Future<byte[]> rfuture = es.submit(rtask);
                objects[i] = rfuture.get();
            }
        }

        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);


        long endtime = System.nanoTime();

        System.out.println("Time to complete " + ((double)(endtime - starttime)/1000000) + " milliseconds");

        memoryManager.cleanup();
    }


}
