package ci583.receiver;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;

import java.util.*;

/**
 * <p>Proper implementation of a MLFQ following
 * <a href="https://pages.cs.wisc.edu/~remzi/OSTEP/cpu-sched-mlfq.pdf">
 *     University of Wisconsin: MLFQ Scheduling
 * </a>
 * </p>
 * <p>
 *     There are a set of rules that should be followed, these are:
 *     <ol>
 *         <li>If Priority(A) > Priority(B): A runs and B doesn't</li>
 *         <li>If Priority(A) = Priority(B): A & B run in round robin</li>
 *         <li>When a job enters, it is placed at the highest priority (topmost queue)</li>
 *         <li>Once a job uses up its allotted time (regardless if it gives up the CPU), its
 *         priority is reduced</li>
 *         <li>After some period of time <em>S</em>, move all jobs to the topmost queue</li>
 *     </ol>
 * </p>
 */
public class MultiLevelFeedbackQueueRealReceiver extends ModRegReceiver {

    private long S = 10000;
    private final LinkedList<ArrayDeque<ModuleRegister>> queues;

    public MultiLevelFeedbackQueueRealReceiver(long quantum) {
        super(quantum);
        queues = new LinkedList<>();
    }

    @Override
    public void enqueue(ModuleRegister m) {

    }

    @Override
    public List<ModuleRegister> startRegistration() {
        ArrayList<ModuleRegister> result = new ArrayList<>();

        // The last time the loop ran
        long lastTime = System.currentTimeMillis();
        long dt = 0; // To count towards S

        // Iterate until we break out
        while(true) {
            // Firstly, check if dt > S, if so promote all to top
            dt += (System.currentTimeMillis() - lastTime);
            if(dt > S) {
                dt = 0;
                ArrayDeque<ModuleRegister> top = queues.getFirst();
                for(int i = 1; i < queues.size(); i++) {
                    var q = queues.get(i);
                    ModuleRegister p;
                    while((p = q.pollFirst()) != null) {
                        top.offer(p);
                    }
                }
            }
            lastTime = System.currentTimeMillis();

            // Variable to track if all queues are empty
            // If all queues are empty, all processes have been processed and the loop can break
            // If a queue is not empty, this will become false
            boolean empty = true;

            // Variable to track process to give CPU time to
            ModuleRegister register = null;
            // Variables to track current and descendant queues
            Queue<ModuleRegister> curr = null;
            Queue<ModuleRegister> down = null;

            // Iterate over the queues starting from the first (the highest priority)
            Iterator<ArrayDeque<ModuleRegister>> iterator = queues.iterator();
            while(iterator.hasNext()) {
                var q = iterator.next();

                if(!q.isEmpty()) {
                    // If this point is reached, this is the highest queue with a process
                    empty = false;

                    register = q.peek();
                    curr = q;
                    down = iterator.hasNext() ? iterator.next() : curr; // If this queue is the
                    // lowest priority, process cannot descend further so should round-robin in
                    // this bottom queue
                    break;
                }
            }

            if(empty) {
                break;
            }

            switch (register.getState()) {
                case TERMINATED:
                    result.add(register);
                    curr.poll(); // Polled variable will be the register (as this was
                    // retrieved using peekFirst)
                    break;

                case NEW:
                    register.start();
                default:
                    register.startWork();
                    // Check if this register yields the CPU time
                    if(register.hasYieldedCPU()) {
                        register.stopWork();
                        // Keep register in same queue
                        curr.poll();
                        curr.offer(register);
                    } else {
                        sleepIgnoreException(QUANTUM);
                        register.stopWork();
                        // Demote register
                        curr.poll();
                        down.offer(register);
                    }
                    break;
            }
        }

        System.out.println(result);
        return result;
    }

    // Gui code
    @Override
    public void imGuiDraw() {
        ImGui.begin("Multi-level feedback queue");

        int maxElements = 0, totalElements = 0;
        for (ArrayDeque<ModuleRegister> q : queues) {
            maxElements = Math.max(q.size(), maxElements);
            totalElements += q.size();
        }

        if(ImGui.beginTable("mlfqr", maxElements + 2, ImGuiTableFlags.Borders)) {
            for (int i = 0; i < queues.size(); i++) {
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.text("Queue[" + i + "]");

                ArrayDeque<ModuleRegister> queue = queues.get(i);
                Collection<ModuleRegister> copy = new ArrayList<>(queue);
                for(ModuleRegister register : copy) {
                    if(register == null) break;
                    ImGui.tableNextColumn();

                    ImGui.text(register.getName() + " - " +
                            (register.isExecuting() ? "Executing" : "Runnable"));

                    if(register.getState() != Thread.State.NEW) {
                        ImGui.text(register.getWorkCompleted() + " / " + register.getWork());
                        ImGui.text(((int) (register.getInteractiveThreadChance() * 100)) + "% " +
                                "yield chance");
                    }

                    // Slider to configure work done amount
                    if(register.getState() == Thread.State.NEW) {
                        ImGui.text("Work: ");
                        ImGui.sameLine();
                        int[] wrapper = {(int) register.getTotalWorkToDo()};
                        if(ImGui.sliderInt("##work" + register.getName(), wrapper, 1000, 60000)) {
                            register.setWorkToDo(wrapper[0]);
                        }

                        ImGui.text("Yield: ");
                        ImGui.sameLine();
                        wrapper = new int[]{ (int) (register.getInteractiveThreadChance() * 100) };
                        if(ImGui.sliderInt("##interactive" + register.getName(), wrapper, 1, 99)) {
                            register.setInteractiveThreadChance((double) wrapper[0] / 100D);
                        }
                    }
                }

                ImGui.tableNextColumn();
                ImGui.pushID("##plus" + i);
                if(ImGui.button("+", ImGui.getColumnWidth(), 0)) {
                    queue.push(new ModuleRegister("P" + (totalElements + 1), 5000, 0.5));
                }
                ImGui.popID();
            }

            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            if(ImGui.button("+ Queue", ImGui.getColumnWidth(), 0)) {
                queues.addLast(new ArrayDeque<>());
            }

            ImGui.endTable();
        }

        ImGui.end();
    }

    @Override
    public void imGuiReset() {

    }
}
