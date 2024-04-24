package ci583.receiver;
/**
 * The abstract superclass for all Receivers. A Receiver accepts a series of Processes
 * and allows them to run to completion according to its specific strategy (e.g.
 * Round Robin, High/Low Priority or Multi-level Feedack Queue).
 *
 * @author Jim Burton
 */

import java.util.List;


public abstract class ModRegReceiver {

    /** The time quantum for which each module registration process will run before being
     * put back to sleep.
     */
    protected static long QUANTUM = 100; // The amount of processing time each task gets allocated

    public static long getQUANTUM() {
        return QUANTUM;
    }

    public static void setQUANTUM(long QUANTUM) {
        ModRegReceiver.QUANTUM = QUANTUM;
    }

    public ModRegReceiver() {}

    /**
     * Creates a Module registration receiver with the given time quantum.
     * @param quantum
     */
    public ModRegReceiver(long quantum) {
        QUANTUM = quantum;
    }

    /**
     * Add a process to  the queue of precesses, for registering a module to a student .
     * @param m
     */
    public abstract void enqueue(ModuleRegister m);

    /**
     * Start registering modules.
     * @return
     */
    public abstract List<ModuleRegister> startRegistration();

    /**
     * Sleeps the current thread for the specified time, returning false if an interrupt exception was thrown
     *
     * @param timeMs the time to sleep in milliseconds
     */
    protected void sleepIgnoreException(long timeMs) {
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException ignore) {}
    }

    /**
     * Called by the Window class to render the current state of the scheduler
     */
    public abstract void imGuiDraw();

    /**
     * Called by the Window class to reset the gui state of the scheduler
     */
    public abstract void imGuiReset();
}
