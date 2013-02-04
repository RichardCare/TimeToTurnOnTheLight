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
 * Decide on a GPIO port - or is that a different one for each team?
 * 
 * What is 'Code' in the above, what does it affect?
 * 
 * How accurate do the timings have to be? 13ms±? 1.5ms±? 0.5ms±? 
 * 
 * An example or two of a bit stream would help, where L == pin low, H == pin high, 
 * nn is time in millis & ... is the other 44 bits:
 * 0xA (i.e. 1010) is 13L 0.5H 0.5L 1.5H 0.5L 1.5H ...
 * 0x3 (i.e. 0011) is 13L 0.5H 1.5L 1.5H 0.5L 0.5H ...
 */
public class TestTurnOn {

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
        final Boolean T = Boolean.TRUE;
        final Boolean F = Boolean.FALSE;
        final long _1 = (long)15e5;
        final long _0 = (long)5e5;


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
                new Result(13e6, F), new Result(5e5, T),  // preamble
                new Result(_0, F), new Result(_1, T), new Result(_0, F), new Result(_1, T), 
                new Result(_1, F), new Result(_0, T), new Result(_0, F), new Result(_1, T) 
        }), pin.getResults());
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
    private static final long PERMISSIBLE_ERROR = (long)4e6;       // stupidly large permissible delta
    final long delta;
    final boolean state;

    Result(double delta, boolean state) {       // convenience for expected results
        this((long)delta, state);
    }

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
            System.out.format("Excessive delta %d at %s v. %s\n", delta - other.delta, this, other);
            return false;
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
