package ci583.receiver;

/**
 * A class representing a process for the CI583 Modules Registration assignment. Process is a subclass of Thread.
 * When the thread runs, the 'work' that it does is to sleep repeatedly.
 */
public class ModuleRegister extends Thread {

    /** The amount of 'work' this process has to do, in milliseconds. */
    private long work;
    /** The time at which this process was started, in milliseconds. */
    private long timeStarted;
    /** Contains the name and other details of this process. */
    private String statusDescription;

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
        statusDescription = String.format("[%s %d] INCOMPLETE %d", pid, getPriority(), work);
    }

    /**
     * The run method sleeps repeatedly until the 'work' is done.
     */
    @SuppressWarnings("BusyWait")
    public void run() {
        timeStarted = System.currentTimeMillis();
        while(workDone() < work) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
        updateStatus();
    }

    /**
     * Only call once this process has completed its execution
     * Sets a string describing this process and how long it took to complete.
     */
    private void updateStatus() {
        statusDescription = String.format("[%s %d] COMPLETE: %d/%d", getName(), getPriority(),
                work, workDone());
    }

    /**
     * @return The length of time, in milliseconds, that this process has been working.
     */
    public long workDone() {
        return System.currentTimeMillis() - timeStarted;
    }

    public long getRemainingWorkToDo() {
        if(timeStarted > 0) {
            return timeStarted + work - System.currentTimeMillis();
        } else {
            return work; // If not started, return the work amount
        }
    }

    public long getTotalWorkToDo() {
        return work;
    }

    public void setWorkToDo(long work) {
        this.work = work;
    }

    /**
     * Returns the contents of `status'.
     * @return
     */
    public String toString() {
        return statusDescription;
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
