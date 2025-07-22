import java.math.BigInteger;

/**
 * A simple benchmark class that performs a computationally intensive task
 * for a fixed duration to measure performance.
 */
public class Benchmark {

    public static void main(String[] args) {
        System.out.println("Starting benchmark...");
        
        // The target duration for the benchmark in seconds.
        final long DURATION_SECONDS = 60;
        final long DURATION_NANOS = DURATION_SECONDS * 1_000_000_000L;

        long operations = 0;
        long startTime = System.nanoTime();
        long elapsedTime = 0;

        System.out.println("Running computationally intensive task for approximately " + DURATION_SECONDS + " seconds.");
        System.out.println("This will test CPU performance under emulation (if applicable).");

        // Loop until the desired duration has passed.
        while (elapsedTime < DURATION_NANOS) {
            // Perform a reasonably complex calculation.
            // Calculating the factorial of a moderately large number using BigInteger is a good candidate.
            // The result is not stored to avoid consuming memory; we are only interested in the CPU work.
            calculateFactorial(500); 
            
            operations++;
            elapsedTime = System.nanoTime() - startTime;
        }

        // Get the final time and calculate the actual duration.
        long finalTime = System.nanoTime();
        double actualDurationSeconds = (finalTime - startTime) / 1_000_000_000.0;

        System.out.println("\nBenchmark finished.");
        System.out.println("-----------------------------------------");
        System.out.printf("Total operations completed: %,d\n", operations);
        System.out.printf("Actual execution time: %.2f seconds\n", actualDurationSeconds);
        System.out.printf("Operations per second (Score): %,.2f\n", operations / actualDurationSeconds);
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
            // This check is good practice but won't be hit in the current benchmark loop.
            throw new IllegalArgumentException("Factorial is not defined for negative numbers.");
        }
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            // The multiplication of BigInteger objects is the core of the workload.
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }
}