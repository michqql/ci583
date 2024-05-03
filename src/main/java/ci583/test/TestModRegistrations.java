package ci583.test;

/**
 * Tests for the CI583 Registration assignment. When your code passes these tests you can be
 * fairly confident it is along the right lines, but passing the tests does not imply that
 * your code is perfect.
 *
 * @author Jim Burton
 */

import ci583.receiver.*;
import org.junit.Before;
import org.junit.Test;


import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class TestModRegistrations {
    @Before
    public void setUp() {
    }

    @Test
    public void testRoundRobinReceiver() {
        ModRegReceiver r = new RoundRobinReceiver(100);
        r.enqueue(new ModuleRegister("P1", 5000));
        r.enqueue(new ModuleRegister("P2", 3000));
        r.enqueue(new ModuleRegister("P3", 1000));
        r.enqueue(new ModuleRegister("P4", 4000));

        Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
        assertEquals("[P3, P2, P4, P1]", Arrays.toString(names.toArray()));
    }

    @Test
    public void testRoundRobinReceiver2() {
        ModRegReceiver r = new RoundRobinReceiver(100);
        r.enqueue(new ModuleRegister("P1", 1000));
        r.enqueue(new ModuleRegister("P2", 2000));
        r.enqueue(new ModuleRegister("P3", 3000));
        r.enqueue(new ModuleRegister("P4", 4000));

        Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
        assertEquals("[P1, P2, P3, P4]", Arrays.toString(names.toArray()));
    }

    @Test
    public void testRoundRobinReceiver3() {
        ModRegReceiver r = new RoundRobinReceiver(100);
        r.enqueue(new ModuleRegister("P1", 6000));
        r.enqueue(new ModuleRegister("P2", 2000));
        r.enqueue(new ModuleRegister("P3", 4000));
        r.enqueue(new ModuleRegister("P4", 2000));

        Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
        assertEquals("[P2, P4, P3, P1]", Arrays.toString(names.toArray()));
    }

    @Test
    public void testRoundRobinReceiver4() {
        ModRegReceiver r = new RoundRobinReceiver(100);
        r.enqueue(new ModuleRegister("P1", 500));
        r.enqueue(new ModuleRegister("P2", 300));
        r.enqueue(new ModuleRegister("P3", 400));
        r.enqueue(new ModuleRegister("P4", 200));

        Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
        assertEquals("[P2, P4, P1, P3]", Arrays.toString(names.toArray()));
    }

    @Test
    public void testRoundRobinReceiverMultiple() {
        for(int i = 0; i < 10; i++) {
            ModRegReceiver r = new RoundRobinReceiver(100);
            r.enqueue(new ModuleRegister("P1", 5000));
            r.enqueue(new ModuleRegister("P2", 3000));
            r.enqueue(new ModuleRegister("P3", 1000));
            r.enqueue(new ModuleRegister("P4", 4000));

            Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
            assertEquals("[P3, P2, P4, P1]", Arrays.toString(names.toArray()));
        }
    }

    @Test
    public void testPriorityReceiver() {
        ModRegReceiver r = new PriorityReceiver(100);
        r.enqueue(new ModuleRegister("P1", 2000, ModuleRegister.Priority.MED));
        r.enqueue(new ModuleRegister("P2", 3000, ModuleRegister.Priority.LOW));
        r.enqueue(new ModuleRegister("P3", 4000, ModuleRegister.Priority.MED));
        r.enqueue(new ModuleRegister("P4", 4000, ModuleRegister.Priority.HIGH));
        r.enqueue(new ModuleRegister("P5", 4000, ModuleRegister.Priority.LOW));
        r.enqueue(new ModuleRegister("P6", 4000, ModuleRegister.Priority.HIGH));

        Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
        assertEquals("[P6, P4, P3, P1, P2, P5]", Arrays.toString(names.toArray()));
    }

    @Test
    public void testMultiLevelFeedbackQueueReceiver() {
        ModRegReceiver r = new MultiLevelFeedbackQueueReceiver(100);
        r.enqueue(new ModuleRegister("P1", 2000));
        r.enqueue(new ModuleRegister("P2", 3000));
        r.enqueue(new ModuleRegister("P3", 4000));
        r.enqueue(new ModuleRegister("P4", 4000));
        r.enqueue(new ModuleRegister("P5", 4000));
        r.enqueue(new ModuleRegister("P6", 4000));

        Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
        assertEquals("[P1, P2, P4, P5, P6, P3]", Arrays.toString(names.toArray()));
    }

    // Extra schedulers
    @Test
    public void testShortestJobFirstReceiver() {
        ModRegReceiver r = new ShortestJobFirstReceiver(100);
        r.enqueue(new ModuleRegister("P1", 2000));
        r.enqueue(new ModuleRegister("P2", 3000));
        r.enqueue(new ModuleRegister("P3", 4000));
        r.enqueue(new ModuleRegister("P4", 2000));
        r.enqueue(new ModuleRegister("P5", 4000));
        r.enqueue(new ModuleRegister("P6", 1000));

        Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
        assertEquals("[P6, P1, P4, P2, P3, P5]", Arrays.toString(names.toArray()));
    }

    @Test
    public void testFirstComeFirstServeReceiver() {
        ModRegReceiver r = new FirstComeFirstServeReceiver(100);
        r.enqueue(new ModuleRegister("P1", 2000));
        r.enqueue(new ModuleRegister("P2", 3000));
        r.enqueue(new ModuleRegister("P3", 4000));
        r.enqueue(new ModuleRegister("P4", 2000));
        r.enqueue(new ModuleRegister("P5", 4000));
        r.enqueue(new ModuleRegister("P6", 1000));

        Stream<String> names = r.startRegistration().stream().map(ModuleRegister::getName);
        assertEquals("[P1, P2, P3, P4, P5, P6]", Arrays.toString(names.toArray()));
    }
}
