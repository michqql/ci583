package ci583.receiver;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableFlags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FirstComeFirstServeReceiver extends ModRegReceiver {

    private final ArrayList<ModuleRegister> list;

    public FirstComeFirstServeReceiver(long quantum) {
        super(quantum);
        this.list = new ArrayList<>();
    }

    @Override
    public void enqueue(ModuleRegister m) {
        list.add(m);
    }

    @Override
    public List<ModuleRegister> startRegistration() {
        List<ModuleRegister> results = new ArrayList<>();

        while(list.size() > 0) {
            ModuleRegister process = list.get(0);
            assert process != null; // Can't be null as jobs.size() > 0
            switch (process.getState()) {
                case NEW -> {
                    process.start();
                    sleepIgnoreException(QUANTUM);
                }
                case TERMINATED -> {
                    list.remove(0);
                    results.add(process);
                }
                default -> {
                    process.interrupt();
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
        ImGui.begin("First Come First Server");

        if (ImGui.beginTable("fcfs", list.size() + 2, ImGuiTableFlags.Borders)) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Processes:");

            synchronized (list) {
                Collection<ModuleRegister> copy = new ArrayList<>(list);
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
                        ImGui.text(register.workDone() + " / " + register.getTotalWorkToDo());
                    }

                    // Slider to configure work done amount
                    if(register.getState() == Thread.State.NEW) {
                        int[] wrapper = {(int) register.getTotalWorkToDo()};
                        if(ImGui.sliderInt("##work" + register.getName(), wrapper, 1000, 60000)) {
                            register.setWorkToDo(wrapper[0]);
                        }
                    }
                }
            }

            ImGui.tableNextColumn();
            if(ImGui.button("+", ImGui.getColumnWidth(), 0)) {
                enqueue(new ModuleRegister("P" + (list.size() + 1), 5000));
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
