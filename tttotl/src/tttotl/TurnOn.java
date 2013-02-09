package tttotl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * @author erac
 *
 * The plugs are controlled by a simple code sent by modulating a 433 MHz signal.
 * A transmitter chip wired to a GPIO port on a Raspberry Pi can be used to send the 
 * signal - all you have to do is to wiggle the output up and down the right way, and 
 * with the right timing.
 * 
 * The protocol to be sent is as follows:
 *  >  Send a preamble of OFF for 13ms, followed by a sync pulse of ON for 0.5ms.
 *     The payload is then sent as one-bit per state-change,
 *     where level for 1.5ms signifies a 1 and 0.5ms signifies a 0.
 *  >  The payload is 48 bits and identifies a channel, a code within that channel,
 *     and whether to send on or off.
 *
 * The channel is the least significant 16 bits as follows
 *      Channel 1  0x3335
 *      Channel 2  0x3353
 *      Channel 3  0x3533
 *      Channel 4  0x5333
 *
 * The code is the next 16 bits
 *      Code 1     0x3335
 *      Code 2     0x3353
 *      Code 3     0x3533
 *      Code 4     0x5333
 *
 * On and off are the highest 16 bits
 *      On         0x3333
 *      Off        0x5333
 *
 * So sending
 *      Channel 4 Code 2 On  is 0x3333 0x3353 0x5333
 *      Channel 1 Code 3 Off is 0x5333 0x3335 0x3533
 *
 * The payload is sent least significant bit first.
 */

public class TurnOn {
    
    private final Outputter outputter;

    enum ChannelOrCode {
        one(0x3335),
        two(0x3353),
        three(0x3533),
        four(0x5333);
        
        private final long value;

        ChannelOrCode(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }
    
    enum ChannelGoal {
        on(0x3333),
        off(0x5333);
        
        private final long value;

        ChannelGoal(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }
    
    public interface DigOut {   // minimal i/f to simplify testing
        void setState(boolean on);
    }
    
    public static class SimplePin implements DigOut {
        private final GpioPinDigitalOutput pin;

        SimplePin(GpioPinDigitalOutput pin) {
            this.pin = pin;
        }

        @Override
        public void setState(boolean state) {
            pin.setState(state);
        }
    }
    
    public static class Sequencer implements Iterable<Boolean> {
        private int count;
        private long value;
        
        Sequencer(ChannelOrCode channel, ChannelOrCode code, ChannelGoal onOff) {
            this(onOff.getValue() << 32 | code.getValue() << 16 | channel.getValue(), 48);
        }

        Sequencer(long value, int count) {      // for test
            this.value = value;
            this.count = count;
        }    
            
        @Override
        public Iterator<Boolean> iterator() {
            return new SequenceIterator(value, count);  // create discardable iterator
        }
        
        
        public long getValue() {
            return value;
        }
    }
    
    static class SequenceIterator implements Iterator<Boolean> {
        private long value;
        private int count;

        SequenceIterator(long value, int count) {
            this.value = value;
            this.count = count;
        }

        @Override
        public boolean hasNext() {
            return count > 0;
        }

        @Override
        public Boolean next() {
            if (hasNext()) {
                Boolean result = (value & 1) == 1 ? Boolean.TRUE : Boolean.FALSE;
                value >>= 1;
                count--;
                return result;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Lazy programmer...");
        }
    }

    public static class Outputter {
        private final DigOut pin;

        public Outputter(DigOut pin) {
            this.pin = pin;
        }

        public void output(Sequencer sequence) {
            setStateThenWait(false, 13e6);
            setStateThenWait(true, 5e5);
            boolean output = false;
            for (Boolean state : sequence) {
                setStateThenWait(output, state ? 15e5 : 5e5);
                output = !output;
            }
        }
        
        private void setStateThenWait(boolean state, double delay) {
            long endTime = System.nanoTime() + (long)delay;
            pin.setState(state);
            while (System.nanoTime() < endTime) { /* spin hard */ }
        }
    }

    /**
     * @param args Takes two arguments:
     *  channel: one two three four
     *  goal: on off
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            help();
        } else {
            GpioController gpio = GpioFactory.getInstance();
            GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08, "MyLED", PinState.LOW);
            try {
                ChannelOrCode channel = ChannelOrCode.valueOf(args[0]);
                ChannelOrCode code = ChannelOrCode.valueOf(args[1]);
                ChannelGoal goal = ChannelGoal.valueOf(args[2]);
                System.out.format("Turning channel %s %s %s\n", channel, code, goal);
                System.out.format("Turning channel %x %x %x\n", channel.getValue(), code.getValue(), goal.getValue());

                TurnOn turnOn = new TurnOn(new Outputter(new SimplePin(pin)));
                Sequencer sequence = new Sequencer(channel, code, goal);

                for (int i = 0; i < 10; i++) {
                    turnOn.apply(sequence);
                }
            } catch (IllegalArgumentException x) {
                help();
            }
        }
    }

    public TurnOn(Outputter outputter) {
        this.outputter = outputter;
    }
    
    public void apply(Sequencer sequence) {
        outputter.output(sequence);
    }

    private static void help() {
        System.out.println(
            "Usage: java -cp ... tttotl.TurnOf <channel> <code> <goal>\n" +
            "where\n" +
            "\tchannel is one of one, two, three, or four\n" +
            "\tcode is one of one, two, three, or four\n" +
            "\tgoal is one of on or off");
    }
}
