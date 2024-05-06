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
            ModuleRegister process = list.get(0); // O(1) time complexity
            assert process != null; // Can't be null as jobs.size() > 0
            switch (process.getState()) {
                case NEW -> {
                    process.start();
                    process.startWork();
                    sleepIgnoreException(QUANTUM);
                    process.stopWork();
                }
                case TERMINATED -> {
                    list.remove(0); // O(n) time complexity as greater elements shifted down
                    results.add(process);
                }
                default -> {
                    process.startWork();
                    sleepIgnoreException(QUANTUM);
                    process.stopWork();
                }
            }
        }

        return results;
    }

    // Gui code
    @Override
    public void imGuiDraw() {
        ImGui.begin("First Come First Serve");

        if (ImGui.beginTable("fcfs", list.size() + 2, ImGuiTableFlags.Borders)) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.text("Processes:");

            synchronized (list) {
                Collection<ModuleRegister> copy = new ArrayList<>(list);
                for(ModuleRegister register : copy) {
                    ImGui.tableNextColumn();

                    ImGui.text(register.getName());

                    // Display work done
                    if(register.getState() != Thread.State.NEW) {
                        ImGui.text(register.getWorkCompleted() + " / " + register.getWork());
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
    }
}
