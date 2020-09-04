import java.util.Random;
import java.util.concurrent.*;

public class Benchmarks {

    public static void main(String[] args) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {
        int mode = Integer.parseInt(args[0]);
        long size = Long.parseLong(args[1]) * 100000000;
        int initblocksize = Integer.parseInt(args[2]) * 1000000;
        int testitertions = Integer.parseInt(args[3]);
        double duration = 0;

        System.out.println(args[1] + "00MB Memory");

        switch (mode){
            case 1:

                System.out.println("Init benchmarks");

                System.out.println("1 segment:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkInit(size, 1, initblocksize);
                System.out.println("Average Time (" + testitertions +  "runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("2 segments:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkInit(size, 2, initblocksize);
                System.out.println("Average Time (" + testitertions +  " runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("4 segments:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkInit(size, 4, initblocksize);
                System.out.println("Average Time (" + testitertions +  " runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("6 segments:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkInit(size, 6, initblocksize);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("12 segments:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkInit(size, 12, initblocksize);
                System.out.println("Average Time (" + testitertions + " runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("24 segments:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkInit(size, 24, initblocksize);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                break;
            case 2:
                System.out.println("Allocation benchmarks");

                duration = 0;
                System.out.println("1 segment, 5000 allocations:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkAllocations(size, 1, initblocksize, 1000);
                System.out.println("Average Time (" + testitertions + " runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("2 segments, 5000 allocations:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkAllocations(size, 2, initblocksize, 1000);
                System.out.println("Average Time (" + testitertions + " runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("4 segments, 5000 allocations:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkAllocations(size, 4, initblocksize, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("6 segment, 5000 allocations:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkAllocations(size, 6, initblocksize, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("12 segment, 5000 allocations:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkAllocations(size, 12, initblocksize, 5000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("24 segment, 5000 allocations:");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkAllocations(size, 24, initblocksize, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                break;
            case 3:
                System.out.println("Reads");

                duration = 0;
                System.out.println("1 segment, 1000 reads");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkReads(size, 1, initblocksize, 1000, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("2 segments, 1000 reads");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkReads(size, 2, initblocksize, 1000, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("4 segments, reads");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkReads(size, 4, initblocksize, 1000, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("6 segment, 1000 reads");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkReads(size, 6, initblocksize, 1000, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("12 segment, 1000 reads");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkReads(size, 12, initblocksize, 100, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("24 segment, reads");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkReads(size, 24, initblocksize, 1000, 1000);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                break;
            case 4:
                System.out.println("Reads and writes, same chance for reads and writes");

                duration = 0;
                System.out.println("1 segment, 1000 accesses");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 1, initblocksize, 1000, 1);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("2 segments, 1000 accesses");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 2, initblocksize, 1000, 1);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("4 segments, 1000 accesses");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 4, initblocksize, 1000, 1);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("6 segments, 1000 accesses");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 6, initblocksize, 1000, 1);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("12 segment, 1000 accesses");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 12, initblocksize, 1000, 1);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("24 segment, 1000 accesses");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 24, initblocksize, 1000, 1);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                break;
            case 5:
                System.out.println("Writes and reads");

                duration = 0;
                System.out.println("1 segment, 1000 iterations, 4 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 1, initblocksize, 1000, 4);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("2 segment, 1000 iterations, 4 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 2, initblocksize, 1000, 4);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("4 segment, 1000 iterations, 4 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 4, initblocksize, 1000, 4);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("6 segment, 1000 iterations, 4 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 6, initblocksize, 1000, 4);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("12 segment, 1000 iterations, 4 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 12, initblocksize, 1000, 4);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("24 segment, 1000 iterations, 4 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 24, initblocksize, 1000, 4);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                break;

            case 6:
                System.out.println("Writes and reads");

                duration = 0;
                System.out.println("1 segment, 1000 iterations, 10 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 1, initblocksize, 1000, 10);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("2 segment, 1000 iterations, 10 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 2, initblocksize, 1000, 10);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("4 segment, 1000 iterations, 10 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 4, initblocksize, 1000, 10);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("6 segment, 1000 iterations, 10 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 6, initblocksize, 1000, 10);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("12 segment, 1000 iterations, 10 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 12, initblocksize, 1000, 10);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                duration = 0;
                System.out.println("24 segment, 1000 iterations, 10 reads per write");
                for(int i = 0; i < testitertions; i++)
                    duration += benchmarkWritesAndReads(size, 24, initblocksize, 1000, 10);
                System.out.println("Average Time ("+ testitertions +" runs): " + duration/(double) testitertions);

                break;
        }
    }




    public static double benchmarkInit(long size, int segments, int initblocksize) throws IllegalAccessException, InterruptedException, NoSuchFieldException {
        long starttime = System.nanoTime();
        MemoryManager memoryManager = new MemoryManager(size, segments, initblocksize);

        long endtime = System.nanoTime();

        double duration = (endtime - starttime)/1000000.0;

        //System.out.println("Time to complete " + duration  + " milliseconds");

        memoryManager.cleanup();

        return duration;

    }
    public static double benchmarkAllocations(long size, int segments, int initblocksize, int writes) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments, initblocksize);
        byte[] object = new byte[10000];
        long[] addresses = new long[writes];

        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        long starttime = System.nanoTime();

        for(int i = 0; i < writes; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.HOURS);


        long endtime = System.nanoTime();

        double duration = (endtime - starttime)/1000000.0;

        //System.out.println("Time to complete " + duration + " milliseconds");

        memoryManager.cleanup();

        return duration;
    }

    public static double benchmarkReads(long size, int segments, int initblocksize, int writes, int reads) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments, initblocksize);
        byte[] object = new byte[500000];

        long[] addresses = new long[writes];
        byte[][] objects = new byte[reads][500000];

        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        for(int i = 0; i < writes; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.HOURS);

        es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        long starttime = System.nanoTime();

        for(int i = 0; i < reads; i++){
            long address = addresses[i];
            Callable<byte[]> task = () -> memoryManager.readObject(address);
            Future<byte[]> future = es.submit(task);
            objects[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.HOURS);

        long endtime = System.nanoTime();

        double duration = (endtime - starttime)/1000000.0;

        //System.out.println("Time to complete " + duration + " milliseconds");

        memoryManager.cleanup();

        return duration;

    }

    public static double benchmarkWritesAndReads(long size, int segments, int initblocksize, int iterations, int readsperwrite) throws IllegalAccessException, InterruptedException, NoSuchFieldException, ExecutionException {

        MemoryManager memoryManager = new MemoryManager(size, segments, initblocksize);
        byte[] object = new byte[500000];
        long[] addresses = new long[iterations];
        byte[][] objects = new byte[iterations][500000];
        Random addressselector = new Random();
        Random accessselector = new Random();
        int writecounter= iterations;
        int readcounter = iterations * readsperwrite;
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for(int i = 0; i < iterations; i++){
            Callable<Long> task = () -> memoryManager.allocateSerialized(object);
            Future<Long> future = es.submit(task);
            addresses[i] = future.get();
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.HOURS);

        es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        long starttime = System.nanoTime();


        for(int i = 0; i < iterations; i++) {
            int access = accessselector.nextInt(1 + readsperwrite);
            if(access == 0 && writecounter == 0) access = 1;
            if(access != 0 && readcounter == 0) access = 0;
            if(access == 0){
                writecounter--;
                long addressw = addresses[addressselector.nextInt(iterations)];
                Runnable wtask = () -> memoryManager.writeSerialized(addressw, object);
                es.submit(wtask);
            }else {
                for (int j = 0; j < readsperwrite; j++) {
                    readcounter--;
                    long addressr = addresses[addressselector.nextInt(iterations)];
                    Callable<byte[]> rtask = () -> memoryManager.readObject(addressr);
                    Future<byte[]> rfuture = es.submit(rtask);
                    objects[i] = rfuture.get();
                }
            }
        }

        es.shutdown();
        es.awaitTermination(1, TimeUnit.HOURS);


        long endtime = System.nanoTime();

        double duration = (endtime - starttime)/1000000.0;

        //System.out.println("Time to complete " + duration + " milliseconds");

        memoryManager.cleanup();

        return duration;
    }


}
