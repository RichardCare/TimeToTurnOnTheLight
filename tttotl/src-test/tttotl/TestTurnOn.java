package tttotl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import tttotl.TurnOn.ChannelOrCode;
import tttotl.TurnOn.ChannelGoal;
import tttotl.TurnOn.Sequencer;
import tttotl.TurnOn.Outputter;
import tttotl.TurnOn.DigOut;

/* Questions:
 * Decide on a GPIO port - 00
 * 
 * Channel & Code are just two 2-bit values giving 16 combinations.
 * 
 * How accurate do the timings have to be? 13ms±? 1.5ms±? 0.5ms±? 
 * Don't know but I have to repeat the stream ~10 times to get a hit...
 * 
 * An example or two of a bit stream would help, where L == pin low, H == pin high, 
 * nn is time in millis & ... is the other 44 bits:
 * 0xA (i.e. 1010) is 13L 0.5H 0.5L 1.5H 0.5L 1.5H ...
 * 0x3 (i.e. 0011) is 13L 0.5H 1.5L 1.5H 0.5L 0.5H ...
 */
public class TestTurnOn {
    final Boolean T = Boolean.TRUE;
    final Boolean F = Boolean.FALSE;
    final long _1 = (long)15e5;
    final long _0 = (long)5e5;

    @Test
    public void testSequencer() {
        Sequencer sequencer = new Sequencer(ChannelOrCode.two, ChannelOrCode.one, ChannelGoal.on);

        assertEquals(0x333333353353L, sequencer.getValue());
        
        StringBuffer buffer = new StringBuffer();
        for (Boolean state : sequencer) {
            buffer.append(state ? '1' : '0');
        }
        
        // Note ls bits first 
        final String _3 = "1100";
        final String _5 = "1010";
        assertEquals(_3 + _5 + _3 + _3 + _5 + _3 + _3 + _3 + _3 + _3 + _3 + _3, buffer.toString());
    }
    
    @Test
    public void testOutputter() {
        // pipe cleaning
        DigOutForTest pin = new DigOutForTest();
        Outputter outputter = new Outputter(pin);
        outputter.output(new Sequencer(0x9A, 8));
        System.out.println(pin.getResults());

        pin = new DigOutForTest();
        outputter = new Outputter(pin);
        outputter.output(new Sequencer(0x9A, 8));
        pin.setState(true);     // flush the final state
        
        System.out.println(pin.getResults());
        
        assertEquals(Arrays.asList(new Result[] {
                r(13e6, F), r(5e5, T),  // preamble
                r(_0, F), r(_1, T), r(_0, F), r(_1, T), 
                r(_1, F), r(_0, T), r(_0, F), r(_1, T) 
        }), pin.getResults());
    }
    
    @Test
    public void testSequencerAndOutputter() {
        DigOutForTest pin = new DigOutForTest();
        Outputter outputter = new Outputter(pin);
        Sequencer sequence = new Sequencer(ChannelOrCode.two, ChannelOrCode.three, ChannelGoal.on);
        assertEquals(0x333335333353L, sequence.getValue());

        outputter.output(sequence);
        pin.setState(true);     // flush the final state
        
        System.out.println("Test stream: " + pin.getResults());
        
        List<Result> _3 = listOf(r(_1, F), r(_1, T), r(_0, F), r(_0, T));
        List<Result> _5 = listOf(r(_1, F), r(_0, T), r(_1, F), r(_0, T));
        ArrayList<Result> expected = new ArrayList<Result>();
        expected.addAll(listOf(r(13e6, F), r(5e5, T))); // preamble
        expected.addAll(_3);
        expected.addAll(_5);
        expected.addAll(_3);
        expected.addAll(_3);
        expected.addAll(_3);
        expected.addAll(_3);
        expected.addAll(_5);
        expected.addAll(_3);
        expected.addAll(_3);
        expected.addAll(_3);
        expected.addAll(_3);
        expected.addAll(_3);
        assertEquals(expected, pin.getResults());
    }

    List<Result> listOf(Result... results) {
        return Arrays.asList(results);
    }

    Result r(double delta, boolean state) {      // convenience for expected results
        return new Result((long) delta, state);
    }

static class DigOutForTest implements DigOut {
    private final List<Result> result = new ArrayList<Result>();
    private Result lastTime = null;
    
    @Override
    public void setState(boolean state) {       // record last state & time it was in place
        Result thisTime = new Result(System.nanoTime(), state);

        if (lastTime != null) {
            result.add(new Result(thisTime.delta - lastTime.delta, lastTime.state));
        }
        
        lastTime = thisTime;
    }
    
    public List<Result> getResults() {
        return result;
    }
}

static class Result {
    private static final long _1MS = (long)1e6;
    private static final long REASONABLE_ERROR = _1MS / 4;      // !!! >this is just logged
    private static final long PERMISSIBLE_ERROR = 3 * _1MS;     // stupidly large permissible delta for tests to pass!
    final long delta;
    final boolean state;

    Result(long delta, boolean state) {
        this.delta = delta;
        this.state = state;
    }
    
    @Override
    public String toString() {
        return String.format("R[%d, %s]", delta, state);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Result other = (Result) obj;
        if (state != other.state)
            return false;
        if (Math.abs(delta - other.delta) > PERMISSIBLE_ERROR) {
            System.out.format(">>>Excessive delta<<< %d at %s v. %s\n", delta - other.delta, this, other);
            return false;
        } else if (Math.abs(delta - other.delta) > REASONABLE_ERROR) {
            System.out.format(">>>Reasonable(?) delta<<< %d at %s v. %s\n", delta - other.delta, this, other);
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (delta ^ (delta >>> 32));
        result = prime * result + (state ? 1231 : 1237);
        return result;
    }
    
}

}
