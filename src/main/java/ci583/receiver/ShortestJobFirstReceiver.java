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
            ModuleRegister process = jobs.pollFirst();
            assert process != null; // Can't be null as jobs.size() > 0
            switch (process.getState()) {
                case NEW -> {
                    process.start();
                    jobs.add(process);
                    sleepIgnoreException(QUANTUM);
                }
                case TERMINATED -> {
                    results.add(process);
                }
                default -> {
                    process.interrupt();
                    jobs.add(process);
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
        ImGui.begin("Shortest Job First");

        if (ImGui.beginTable("sjf", jobs.size() + 2, ImGuiTableFlags.Borders)) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Jobs:");

            synchronized (jobs) {
                Collection<ModuleRegister> copy = new ArrayList<>(jobs);
                for(ModuleRegister register : copy) {
                    ImGui.tableNextColumn();

                    if(selectedRegister != null) {
                        // Only make a button for the selected register
                        if(selectedRegister.equals(register)) {
                            ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.8f, 0.2f, 1.0f);
                            if(ImGui.button(register.getName() + ", " + register.getRemainingWorkToDo(),
                                    ImGui.getColumnWidth(),
                                    0)) {
                                selectedRegister = null;
                            }
                            ImGui.popStyleColor();
                        } else {
                            ImGui.text(register.getName() + ", " + register.getRemainingWorkToDo());
                        }
                    } else {
                        // Style the button
                        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 0.0f, 0.0f, 1.0f);

                        if(ImGui.button(register.getName() + ", " + register.getRemainingWorkToDo(),
                                ImGui.getColumnWidth(), 0)) {
                            selectedRegister = register;
                        }
                        ImGui.popStyleColor();
                    }

                    // Display work done
                    if(register.getState() != Thread.State.NEW &&
                            register.getState() != Thread.State.TERMINATED) {
                        ImGui.text(register.workDone() + " / " + register.getTotalWorkToDo());
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
        selectedRegister = null;
    }
}
