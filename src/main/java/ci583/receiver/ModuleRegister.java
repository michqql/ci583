package ci583.receiver;

import java.util.Random;

/**
 * A class representing a process for the CI583 Modules Registration assignment. Process is a subclass of Thread.
 * When the thread runs, the 'work' that it does is to sleep repeatedly.
 */
public class ModuleRegister extends Thread {

    private static final Random RANDOM = new Random();

    // The amount of 'work' this process has to do, in milliseconds
    private long work;
    // Whether this process is currently being given CPU computation time
    private boolean executing;
    // The time in ms at which work started (used for calculating the amount of processing
    // completed during the current CPU allocation)
    private long workStartTime;
    // The total amount of work in ms which has been completed
    private long workCompleted;

    // Interactive thread - used for MLFQ
    // Interactive process is a process that requires IO (such as waiting for keyboard input)
    // and often yields the CPU waiting for certain events
    // This variable is a percentage chance of how often this process should yield
    private double interactiveThreadChance;

    /**
     * Constructs a new Process with the given name and amount of work to do.
     * @param pid
     * @param work
     */
    public ModuleRegister(String pid, long work) {
        this(pid, work, Priority.MED);
    }

    /**
     * Constructs a new Process with the given name, amount of work to do and priority.
     * @param pid
     * @param work
     * @param p
     */
    public ModuleRegister(String pid, long work, Priority p) {
        this.setName(pid);
        this.work = work;
        setPriority(p.getVal());
    }

    public ModuleRegister(String pid, long work, double interactiveThreadChance) {
        this(pid, work);
        this.interactiveThreadChance = interactiveThreadChance;
    }

    /**
     * <strong>The run method sleeps repeatedly until the 'work' is done.</strong>
     * <p>
     * Changed solution to use time that only increases during given CPU execution
     * rather than {@link System#currentTimeMillis()} as this constantly increases
     * even if the process isn't being given CPU time.
     * </p>
     */
    @SuppressWarnings("BusyWait")
    public void run() {
        while(workCompleted < work) {
            try {
                // Without this sleep call (or some other call such as sys out) it seems the
                // compiler removes this while loop during optimisation
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Indicates that this process has just been given CPU time
     */
    public void startWork() {
        executing = true;
        workStartTime = System.currentTimeMillis();
    }

    /**
     * Indicates that this process is no longer receiving CPU time
     */
    public void stopWork() {
        executing = false;
        workCompleted += (System.currentTimeMillis() - workStartTime);
    }

    /**
     * @return true if this process is currently being given CPU time
     */
    public boolean isExecuting() {
        return executing;
    }

    /**
     * @return amount of work that has been completed
     */
    public long getWorkCompleted() {
        return workCompleted;
    }

    /**
     * @return amount of work required for this process to finish
     */
    public long getWork() {
        return work;
    }

    public double getInteractiveThreadChance() {
        return interactiveThreadChance;
    }

    public void setInteractiveThreadChance(double interactiveThreadChance) {
        this.interactiveThreadChance = interactiveThreadChance;
    }

    public boolean hasYieldedCPU() {
        return RANDOM.nextDouble() <= interactiveThreadChance;
    }

    /**
     * Only call once this process has completed its execution
     * Sets a string describing this process and how long it took to complete.
     */
    private void updateStatus() {

    }

    /**
     * @return The length of time, in milliseconds, that this process has been working.
     */
    public long workDone() {
        return 0;
    }

    public long getRemainingWorkToDo() {
        return work - workCompleted;
    }

    public long getTotalWorkToDo() {
        return work;
    }

    public void setWorkToDo(long work) {
        this.work = work;
    }

    /**
     * @return the contents of `status'.
     */
    public String toString() {
        return getName();
    }

    /** An enum containing three priority values, LOW, MEDIUM and HIGH. */
    public enum Priority {
        HIGH(1), MED(5), LOW(9);

        private final int val;

        Priority(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

        public static Priority fromValue(int val) {
            for(Priority priority : values()) {
                if(priority.val == val) {
                    return priority;
                }
            }

            return null;
        }
    }
}
