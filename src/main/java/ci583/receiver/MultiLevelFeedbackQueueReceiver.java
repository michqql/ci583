package ci583.receiver;
/**
 * The Multi-Level Feedback Queue Receiver. This receiver manages two lists of processes, which are YOUNG
 * and OLD. To schedule the next process it does the following:
 * + If the list of YOUNG processes is not empty, take the next process, allow it to run then
 * put it at the end of the list of OLD processes (unless the state of
 * process is TERMINATED).
 * + If the list of YOUNG processes is empty, take the next process from the list of OLD processes, allow it
 * to run then put it at the end of the list of YOUNG processes (unless the state of
 * process is TERMINATED).
 *
 * @author Jim Burton
 */

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MultiLevelFeedbackQueueReceiver extends ModRegReceiver {

    private final ArrayList<ModuleRegister> young = new ArrayList<>();
    private final ArrayList<ModuleRegister> old = new ArrayList<>();
    private boolean takeFromYoung = true;

    /**
     * Constructs a multi-level feedback queue receiver. The constructor needs to call the constructor of the
     * superclass then initialise the two lists for young and old processes.
     * @param quantum
     */
    public MultiLevelFeedbackQueueReceiver(long quantum) {
        super(quantum);
    }

    /**
     * Adds a new process to the list of young processes.
     * @param m
     */
    @Override
    public void enqueue(ModuleRegister m) {
        young.add(m); // adds last
    }

    /**
     * Schedule the module registration processes. This method needs to:
     * + create an empty list which will hold the completed processes. This will be the
     *   return value of the method.
     * + while one of the queues is not empty:
     *   - if the list of YOUNG processes is not empty, take the next process and get its State.
     *   - if the state is NEW, start the process then sleep for QUANTUM milliseconds
     *     then put the process at the back of the list of OLD processes.
     *   - if the state is TERMINATED, add it to the results list.
     *   - if the state is anything else then interrupt the process to wake it up then
     *     sleep for QUANTUM milliseconds, then put the process at the back of the queue.
     *
     *   - if the list of YOUNG processes is empty, do the same except take the process from the
     *     list of OLD processes and, after it does its 'work' put it at the end of the list of
     *     YOUNG processes.
     *  + when both lists are empty, return the list of completed processes.
     * @return
     */
    @Override
    public List<ModuleRegister> startRegistration() {
        ArrayList<ModuleRegister> results = new ArrayList<>();

        while (!(young.isEmpty() && old.isEmpty())) {
            ModuleRegister process;
            ArrayList<ModuleRegister> returningQueue;

            if (young.isEmpty()) {
                // Take from start of old,
                // which shouldn't be empty if this loop is running
                process = old.remove(0);
                returningQueue = young;
            } else {
                process = young.remove(0);
                returningQueue = old;
            }

            switch (process.getState()) {
                case NEW -> {
                    process.start();
                    returningQueue.add(process);
                    sleepIgnoreException(QUANTUM);
                }
                case TERMINATED -> results.add(process);
                default -> {
                    process.interrupt();
                    returningQueue.add(process);
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
        ImGui.begin("Multi-level feedback queue (young & old)");

        if (ImGui.beginTable("mlfqqueue", Math.max(young.size(), old.size()) + 2,
                ImGuiTableFlags.Borders)) {
            // Young queue
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Young:");

            synchronized (young) {
                Collection<ModuleRegister> youngCopy = new ArrayList<>(young);
                for(ModuleRegister register : youngCopy) {
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
                enqueue(new ModuleRegister("P" + (young.size() + 1), 5000));
            }

            // Old queue
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Old:");

            synchronized (old) {
                Collection<ModuleRegister> oldCopy = new ArrayList<>(old);
                for(ModuleRegister register : oldCopy) {
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

            ImGui.endTable();
        }

        ImGui.end();
    }

    @Override
    public void imGuiReset() {
        selectedRegister = null;
    }
}
