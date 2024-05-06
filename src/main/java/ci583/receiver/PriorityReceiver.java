package ci583.receiver;
/**
 * The Priority Receiver. This receiver takes the next process from the head of a priority queue
 * (an instance of java.util.PriorityQueue, allows it to run then puts it back into the queue (unless
 * the state of process is TERMINATED). Thus, the Priority Receiver is identical to the Round Robin
 * receiver apart from the fact that processes have a priority (HIGH, MED or LOW) and are held in a
 * priority queue.
 *
 * @author Jim Burton
 */
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;

import java.util.*;

public class PriorityReceiver extends ModRegReceiver {

    private final PriorityQueue<ModuleRegister> queue;

    /**
     * Constructs a new Priority Scheduler. The constructor needs to call the constructor of the
     * superclass then initialise the priority queue. To initialise the priority queue, first define
     * a Comparator that compares two processes. The comparator should return -1 if two processes have
     * the same priority. This is so that when a process is added to the queue it ends up behind any
     * other processes with the same priority. If the two priorities are not equal, the Comparator should
     * return -1 if p1 is less than p2, and 1 if p1 is greater than p2.
     * @param quantum
     */
    public PriorityReceiver(long quantum) {
      super(quantum);
        // Comparator to sort in descending order, higher priorities go first
        final Comparator<ModuleRegister> comparator = (p1, p2) ->
                p1.getPriority() == p2.getPriority() ? -1 : p1.getPriority() - p2.getPriority();
        this.queue = new PriorityQueue<>(comparator);
    }

    @Override
    public void enqueue(ModuleRegister m) {
        queue.offer(m);
    }

    /**
     * Schedule the processes. This method needs to:
     * + create an empty list which will hold the completed processes. This will be the
     *   return value of the method.
     * + while the queue is not empty:
     *   - use the priority queue's `poll` method to take the next process from the queue and get its State.
     *   - if the state is NEW, start the process then sleep for QUANTUM milliseconds
     *     then put the process at the back of the queue.
     *   - if the state is TERMINATED, add it to the results list.
     *   - if the state is anything else then interrupt the process to wake it up then
     *     sleep for QUANTUM milliseconds, then put the process at the back of the queue.
     *  + when the queue is empty, return the list of completed processes.
     * @return
     */
    @Override
    public List<ModuleRegister> startRegistration() {
        ArrayList<ModuleRegister> orderedResults = new ArrayList<>();

        while(!queue.isEmpty()) {
            // PriorityQueue#peek is used here instead of poll (remove), as otherwise the GUI
            // cannot see the first element on the scheduler
            ModuleRegister process = queue.peek(); // O(1) time complexity
            switch(process.getState()) {
                case NEW -> {
                    process.start();
                    process.startWork();
                    sleepIgnoreException(QUANTUM);
                    process.stopWork();

                    queue.poll(); // Remove the first element - O(log n) due to sift down operation
                    queue.offer(process); // Adds back to queue - O(log n) due to sift up operation
                }
                case TERMINATED -> {
                    orderedResults.add(process);
                    queue.poll(); // Remove the first element - O(log n) due to sift down operation
                }
                default -> {
                    process.startWork();
                    sleepIgnoreException(QUANTUM);
                    process.stopWork();

                    queue.poll(); // Remove the first element - O(log n) due to sift down operation
                    queue.offer(process); // Adds back to queue - O(log n) due to sift up operation
                }
            }
        }

        return orderedResults;
    }

    // Gui code
    @Override
    public void imGuiDraw() {
        ImGui.begin("Priority");

        if (ImGui.beginTable("priorityqueue", queue.size() + 2, ImGuiTableFlags.Borders)) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Queue:");

            synchronized (queue) {
                Collection<ModuleRegister> copy = new ArrayList<>(queue);
                for(ModuleRegister register : copy) {
                    ImGui.tableNextColumn();

                    ImGui.text(register.getName() + " Pri(" + register.getPriority() + ") " +
                            (register.isExecuting() ? "Executing" : "Runnable"));

                    // Display work done
                    if(register.getState() != Thread.State.NEW) {
                        ImGui.text(register.getWorkCompleted() + " / " + register.getWork());
                    }

                    // Slider to configure work done amount
                    // Menu to configure priority
                    if(register.getState() == Thread.State.NEW) {
                        ImGui.separator();
                        ImGui.text("Work:");
                        ImGui.sameLine();
                        int[] wrapper = {(int) register.getTotalWorkToDo()};
                        if(ImGui.sliderInt("##work" + register.getName(), wrapper, 1000, 60000)) {
                            register.setWorkToDo(wrapper[0]);
                        }

                        ModuleRegister.Priority[] priorityWrapper =
                                { ModuleRegister.Priority.fromValue(register.getPriority()) };
                        ImGui.pushID("priority" + register.getName());
                        if(ImGui.beginMenu("Priority")) {
                            for(ModuleRegister.Priority priority :
                                    ModuleRegister.Priority.values()) {
                                if(ImGui.menuItem(priority.name(), "", false,
                                        priority != priorityWrapper[0])) {
                                    register.setPriority(priority.getVal());
                                }
                            }
                            ImGui.endMenu();
                        }
                        ImGui.popID();
                    }
                }
            }

            ImGui.tableNextColumn();
            if(ImGui.button("+", ImGui.getColumnWidth(), 0)) {
                enqueue(new ModuleRegister("P" + (queue.size() + 1), 5000));
            }

            ImGui.endTable();
        }

        ImGui.end();
    }

    @Override
    public void imGuiReset() {
    }
}
