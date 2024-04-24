package ci583.receiver;
/**
 * The Round Robin Module Registration Receiver. This receiver takes the next process from the head of a list,
 * allows it to run then puts it back at the end of the list (unless the state of process is
 * TERMINATED).
 *
 * @author Jim Burton
 */

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoundRobinReceiver extends ModRegReceiver {

    private final ArrayList<ModuleRegister> queue;

    /**
     * Create a new RRReceiver with the given quantum. The constructor needs to call the constructor
     * of the superclass, then initialise the list of processes.
     * @param quantum amount of time to run RRReceiver
     */
    public RoundRobinReceiver(long quantum) {
      super(quantum);
      this.queue = new ArrayList<>();
    }

    /**
     * Add a ModuleRegister process to the queue, to be scheduled for registration
     */
    @Override
    public void enqueue(ModuleRegister m) {
        // Add the object to the end of the queue
        queue.add(m);
    }

    /**
     * Schedule the processes, start registration. This method needs to:
     * + create an empty list which will hold the completed processes. This will be the
     *   return value of the method.
     * + while the queue is not empty:
     *   - take the next process from the queue and get its State.
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
        ArrayList<ModuleRegister> results = new ArrayList<>();

        while(!queue.isEmpty()) {
            ModuleRegister process = queue.remove(0);
            switch (process.getState()) {
                case NEW -> {
                    process.start();
                    queue.add(process);
                    sleepIgnoreException(QUANTUM);
                }
                case TERMINATED -> {
                    results.add(process);
                }
                default -> {
                    process.interrupt();
                    queue.add(process);
                    sleepIgnoreException(QUANTUM);
                }
            }
        }

        return results;
    }

    // Gui code
    private ModuleRegister selectedRegister;

    @Override
    public void imGuiDraw() {
        ImGui.begin("Round Robin");

        if (ImGui.beginTable("roundrobinqueue", queue.size() + 2, ImGuiTableFlags.Borders)) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Queue:");

            synchronized (queue) {
                Collection<ModuleRegister> copy = new ArrayList<>(queue);
                for(ModuleRegister register : copy) {
                    ImGui.tableNextColumn();

                    if(selectedRegister != null) {
                        // Only make a button for the selected register
                        if(selectedRegister.equals(register)) {
                            ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.8f, 0.2f, 1.0f);
                            if(ImGui.button(register.getName(), ImGui.getColumnWidth(), 0)) {
                                selectedRegister = null;
                            }
                            ImGui.popStyleColor();
                        } else {
                            ImGui.text(register.getName());
                        }
                    } else {
                        // Style the button
                        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 0.0f, 0.0f, 1.0f);

                        if(ImGui.button(register.getName(), ImGui.getColumnWidth(), 0)) {
                            selectedRegister = register;
                        }
                        ImGui.popStyleColor();
                    }

                    // Display work done
                    if(register.getState() != Thread.State.NEW &&
                            register.getState() != Thread.State.TERMINATED) {
                        ImGui.text(register.workDone() + " / " + register.getWorkToDo());
                    }

                    // Slider to configure work done amount
                    if(register.getState() == Thread.State.NEW) {
                        int[] wrapper = {(int) register.getWorkToDo()};
                        if(ImGui.sliderInt("##work" + register.getName(), wrapper, 1000, 60000)) {
                            register.setWorkToDo(wrapper[0]);
                        }
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
        selectedRegister = null;
    }
}
