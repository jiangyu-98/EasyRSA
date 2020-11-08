package jiangyu;

public class PerformanceTest {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        final int repeat = 100000000;
        long cnt = 2;
        for (int i = 0; i < repeat; i++) {
            cnt += 1;
        }

        long endTime = System.nanoTime();

        System.out.println((endTime - startTime) / 1.0 / repeat);
    }
}
