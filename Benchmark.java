import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A multi-threaded benchmark class that performs a computationally intensive task
 * for a fixed duration to measure the total throughput of a system's CPU cores.
 */
public class Benchmark {

    // A volatile boolean flag to signal all running threads to stop.
    // Volatile ensures that changes to this variable are visible across all threads.
    private static volatile boolean isRunning = true;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting multi-threaded benchmark...");

        // Determine the number of available CPU cores to the JVM.
        // This will be the number of threads we spawn.
        int coreCount = Runtime.getRuntime().availableProcessors();
        System.out.println("Detected " + coreCount + " available processor core(s). Creating one thread per core.");

        // The target duration for the benchmark in seconds.
        final int DURATION_SECONDS = 60;

        // An ExecutorService to manage our pool of threads.
        ExecutorService executor = Executors.newFixedThreadPool(coreCount);
        
        // An AtomicLong to safely count the total number of operations across all threads.
        // This prevents race conditions when multiple threads try to update the counter simultaneously.
        AtomicLong totalOperations = new AtomicLong(0);

        long startTime = System.nanoTime();

        // Create and submit a worker task for each core.
        for (int i = 0; i < coreCount; i++) {
            executor.submit(() -> {
                // Each thread runs this loop until the main thread sets isRunning to false.
                while (isRunning) {
                    calculateFactorial(500);
                    totalOperations.incrementAndGet(); // Atomically increment the total counter.
                }
            });
        }

        System.out.println("Benchmark running for approximately " + DURATION_SECONDS + " seconds...");

        // Let the benchmark run for the specified duration.
        Thread.sleep(DURATION_SECONDS * 1000);

        // Signal all threads to stop their work.
        isRunning = false;

        // Gracefully shut down the executor. It will wait for currently running tasks to finish.
        executor.shutdown();
        // Wait for all threads in the pool to terminate.
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long endTime = System.nanoTime();
        double actualDurationSeconds = (endTime - startTime) / 1_000_000_000.0;

        System.out.println("\nBenchmark finished.");
        System.out.println("-----------------------------------------");
        System.out.printf("Threads used: %d\n", coreCount);
        System.out.printf("Total operations completed: %,d\n", totalOperations.get());
        System.out.printf("Actual execution time: %.2f seconds\n", actualDurationSeconds);
        System.out.printf("Operations per second (Score): %,.2f\n", totalOperations.get() / actualDurationSeconds);
        System.out.println("-----------------------------------------");
    }

    /**
     * Calculates the factorial of a non-negative integer n.
     * Uses BigInteger to handle arbitrarily large numbers and to ensure the calculation
     * is computationally significant for benchmarking.
     * @param n The number to calculate the factorial of.
     * @return The factorial of n as a BigInteger.
     */
    public static BigInteger calculateFactorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial is not defined for negative numbers.");
        }
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }
}