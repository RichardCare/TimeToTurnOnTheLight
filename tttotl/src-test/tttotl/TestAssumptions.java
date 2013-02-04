package tttotl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class TestAssumptions {

    // Show that size of long is sufficient to represent entire required sequence of bits
    @Test
    public void testLongSize() {
        assertEquals(64, Long.SIZE);    // so I don't have to worry about merging ints
    }

    // Decode a long (effectively) to a sequence of 48 bits 
    @Test
    public void testBitSerialiseLS48Bits() {
        long value = 0x0123456789ABCDEFL;
        //               F   E   D   C   B   A   9   8   7   6   5   4, don't decode ms 16 bits
        assertEquals("111101111011001111010101100100011110011010100010", serialise(value));
    }
    
    /* Evaluate the speed (always risky) of the CPU - worst delta is better than 0.5 millis
     * Empirically the slowest delta is on the second iteration.
     * Core i5
     *   [411, 28326, 1231, 821, 411, 821, 821, 410, 821, 821, 411, 821, 410, 821, 821, ...
     *   [410, 3284, 821, 822, 821, 820, 411, 821, 821, 411, 821, 821, 410, 821, 821, ...
     *   Max deltas: 28326 3284
     * RasPi
     *   [12000, 327986, 19000, 10999, 10000, 10999, 9000, 9000, 8999, 9000, 8999, ...
     *   [10999, 25999, 13000, 9999, 10000, 9000, 9999, 9000, 10999, 9000, 9000, 9999, ...
     *   Max deltas: 327986 25999
     */
    @Test
    public void testTiming() {
        final int count = 100;
        List<Pair> deltaNanos0 = getDeltaNanos(count);
        System.out.println(deltaNanos0);
        assertTrue("resolution worse than 0.2 millis " + Collections.max(deltaNanos0).nano, Collections.max(deltaNanos0).nano < 200000L);

        List<Pair> deltaNanos1 = getDeltaNanosL(count);
        System.out.println(deltaNanos1);
        assertTrue("resolution worse than 0.2 millis " + Collections.max(deltaNanos1).nano, Collections.max(deltaNanos0).nano < 200000L);
        System.out.format("Max deltas: %d %d\n", Collections.max(deltaNanos0).nano, Collections.max(deltaNanos1).nano);

        // Unrolled getting of time to confirm time & loop resolution
        List<Pair> deltaNanos2 = getDeltaNanosU(count);
//        System.out.println(deltaNanos2);
        List<Pair> processed = postProcess(deltaNanos2);
//        System.out.println(deltaNanos2);
        System.out.println(processed);
    }

    private String serialise(long value) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < 48; i++) {
            buffer.append((value & 1) == 1? '1' : '0');
            value >>= 1;
        }
        return buffer.toString();
    }

    // most Java like
    private List<Pair> getDeltaNanosL(final int count) {
        List<Pair> result = new ArrayList<Pair>(count);

        long lastTime = System.nanoTime();
        for (int i = 0; i < count; i++) {
            long t;
            int iters = 0;
            while ((t = System.nanoTime()) == lastTime) { ++iters; /* spin hard */ }
            result.add(new Pair(t - lastTime, iters));
            lastTime = t;
        }
        return result;
    }
    
    // work with pre-populated array
    private List<Pair> getDeltaNanos(final int count) {
        Pair[] result = new Pair[count];
        for (int i = 0; i < count; i++) {
            result[i] = new Pair();
        }

        long lastTime = System.nanoTime();
        for (int i = 0; i < count; i++) {
            long t;
            int iters = 0;
            while ((t = System.nanoTime()) == lastTime) { ++iters; /* spin hard */ }
            result[i].set(t - lastTime, iters);
            lastTime = t;
        }
        return Arrays.asList(result);
    }

    // unroll polling
    private List<Pair> getDeltaNanosU(final int count) {
        Pair[] array = new Pair[count];

        int i = 0;
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        array[i++] = new Pair(System.nanoTime(), 0);
        
        // set all times wrt the first
        long t0 = array[0].nano;
        for (i = 0; i < array.length; i++) {
            array[i].nano -= t0;
        }
        return Arrays.asList(array);
    }

    private List<Pair> postProcess(List<Pair> input) {
        ArrayList<Pair> result = new ArrayList<Pair>(input.size());
        // remove pairs with duplicate times, count up the duplicates 
        for (Pair pair : input) {
            if (!result.isEmpty()) {
                Pair last = result.get(result.size() - 1);
                if (last.nano == pair.nano) {
                    last.iters++;
                    continue;
                }
            }
            pair.iters = 1;
            result.add(pair);
        }
        // change all times to incremental delta
        long lastTime = 0;
        for (Pair pair : input) {
            long temp = pair.nano;
            pair.nano -= lastTime;
            lastTime = temp;
        }
        return result;
    }

static class Pair implements Comparable<Pair> {
    long nano;
    int iters;
    
    Pair() {
    }
    
    Pair(long nano, int iters) {
        this.nano = nano;
        this.iters = iters;
    }
    
    void set(long nano, int iters) {
        this.nano = nano;
        this.iters = iters;
    }

    @Override
    public int compareTo(Pair other) {
        return Long.compare(nano, other.nano);
    }

    @Override
    public String toString() {
        return String.format("P[%d, %d]", iters, nano);
    }
}

}
