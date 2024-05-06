package ci583.receiver;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;

import java.util.*;

public class ShortestJobFirstReceiver extends ModRegReceiver {

    private final TreeSet<ModuleRegister> jobs;

    public ShortestJobFirstReceiver(long quantum) {
        super(quantum);
        final Comparator<ModuleRegister> comparator =
                (c1, c2) -> c1.getRemainingWorkToDo() == c2.getRemainingWorkToDo() ?
                        1 : (int) (c1.getRemainingWorkToDo() - c2.getRemainingWorkToDo());
        jobs = new TreeSet<>(comparator);
    }

    @Override
    public void enqueue(ModuleRegister m) {
        jobs.add(m);
    }

    @Override
    public List<ModuleRegister> startRegistration() {
        List<ModuleRegister> results = new ArrayList<>();

        while(jobs.size() > 0) {
            ModuleRegister process = jobs.first(); // Time complexity is only as great as the
            // height of the tree
            assert process != null; // Can't be null as jobs.size() > 0
            switch (process.getState()) {
                case NEW -> {
                    process.start();
                    process.startWork();
                    sleepIgnoreException(QUANTUM);
                    process.stopWork();

                    jobs.pollFirst(); // O(log n)
                    jobs.add(process); // O(log n)
                }
                case TERMINATED -> {
                    jobs.pollFirst(); // O(log n)
                    results.add(process);
                }
                default -> {
                    process.startWork();
                    sleepIgnoreException(QUANTUM);
                    process.stopWork();

                    jobs.pollFirst(); // O(log n)
                    jobs.add(process); // O(log n)
                }
            }
        }

        return results;
    }

    // Gui code
    @Override
    public void imGuiDraw() {
        ImGui.begin("Shortest Job First");

        if (ImGui.beginTable("sjf", jobs.size() + 2, ImGuiTableFlags.Borders)) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Jobs:");

            synchronized (jobs) {
                Collection<ModuleRegister> copy = new ArrayList<>(jobs);
                for(ModuleRegister register : copy) {
                    ImGui.tableNextColumn();

                    ImGui.text(register.getName() + " - " +
                                    (register.isExecuting() ? "Executing" : "Runnable"));
                    ImGui.text(register.getRemainingWorkToDo() + "ms remaining");

                    // Display work done
                    if(register.getState() != Thread.State.NEW) {
                        ImGui.text(register.getWorkCompleted() + " / " + register.getWork());
                    } else {
                        // Thread state is NEW
                        // Slider to configure work done amount
                        int[] wrapper = {(int) register.getTotalWorkToDo()};
                        if(ImGui.sliderInt("##work" + register.getName(), wrapper, 1000, 60000)) {
                            register.setWorkToDo(wrapper[0]);
                        }
                    }
                }
            }

            ImGui.tableNextColumn();
            if(ImGui.button("+", ImGui.getColumnWidth(), 0)) {
                enqueue(new ModuleRegister("P" + (jobs.size() + 1), 5000));
            }

            ImGui.endTable();
        }

        ImGui.end();
    }

    @Override
    public void imGuiReset() {
    }
}
