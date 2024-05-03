package ci583.gui;

import ci583.receiver.*;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiCol;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Window extends Application {

    private static final String WINDOW_TITLE = "Module Register";
    private static ImFont JETBRAINS_FONT;

    private final RoundRobinReceiver roundRobinReceiver = new RoundRobinReceiver(100);
    private final PriorityReceiver priorityReceiver = new PriorityReceiver(100);
    private final MultiLevelFeedbackQueueReceiver multiLevelFeedbackQueueReceiver =
            new MultiLevelFeedbackQueueReceiver(100);

    private final ShortestJobFirstReceiver shortestJobFirstReceiver = new ShortestJobFirstReceiver(100);

    private final FirstComeFirstServeReceiver firstComeFirstServeReceiver = new FirstComeFirstServeReceiver(100);

    private final LinkedHashMap<String, ModRegReceiver> selectMap = new LinkedHashMap<>(){{
        put("Round Robin", roundRobinReceiver);
        put("Priority Queue", priorityReceiver);
        put("Multi-level Feedback Queue (young & old)", multiLevelFeedbackQueueReceiver);
        //put("Multi-level Feedback Queue (implementation)", () -> {});
        put("Shortest Job First", shortestJobFirstReceiver);
        put("First Come First Serve", firstComeFirstServeReceiver);
    }};

    // The running schedulers
    private final HashMap<Class<? extends ModRegReceiver>, ModRegReceiver> selectedReceivers =
            new HashMap<>();
    private boolean running;

    @Override
    protected void configure(Configuration config) {
        config.setTitle(WINDOW_TITLE);
    }

    @Override
    public void process() {
        ImGui.pushFont(JETBRAINS_FONT);

        mainMenuBar();
        selectedReceivers.forEach((aClass, modRegReceiver) ->
                modRegReceiver.imGuiDraw());

        ImGui.popFont();
    }

    private void mainMenuBar() {
        if(ImGui.beginMainMenuBar()) {
            // Allow user to select a scheduler
            if(ImGui.beginMenu("Select Scheduler")) {
                selectMap.forEach((name, modRegReceiver) -> {
                    if(ImGui.menuItem(name)) {
                        toggleReceiver(modRegReceiver);
                    }
                });
                ImGui.endMenu();
            }

            ImGui.separator();

            if(ImGui.menuItem("Start", "", running, !running)) {
                // Start the receivers
                startSchedulers();
            }

            ImGui.separator();

            if(ImGui.beginMenu("Run Options")) {
                // Quantum
                ImGui.text("Quantum:");
                ImGui.sameLine();
                // Reset button and styling
                ImGui.pushStyleColor(ImGuiCol.Button, 0.6f, 0.2f, 0.2f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.7f, 0.2f, 0.2f, 1.0f);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.5f, 0.1f, 0.1f, 1.0f);
                if(ImGui.button("Reset")) {
                    ModRegReceiver.setQUANTUM(100);
                }
                ImGui.popStyleColor();
                ImGui.popStyleColor();
                ImGui.popStyleColor();
                // Slider
                int[] wrapper = { (int) ModRegReceiver.getQUANTUM() };
                if(ImGui.sliderInt("##quantum", wrapper, 10, 1000)) {
                    ModRegReceiver.setQUANTUM(wrapper[0]);
                }

                ImGui.separator();

                ImGui.endMenu();
            }

            ImGui.separator();

            if(ImGui.beginMenu("Load Tests")) {
                if(ImGui.menuItem("Default JUnit Tests")) {
                    loadJUnitTests();
                }

                if(ImGui.menuItem("Long and Equal Tests")) {
                    loadLongAndEqualTests();
                }

                ImGui.endMenu();
            }

            ImGui.endMainMenuBar();
        }
    }

    private void toggleReceiver(ModRegReceiver receiver) {
        if(selectedReceivers.remove(receiver.getClass()) == null) {
            selectedReceivers.put(receiver.getClass(), receiver);
        }
    }

    private void startSchedulers() {
        if(running) return;

        running = true;
        // New thread for each scheduler, so that they can run simultaneously
        AtomicInteger resultCount = new AtomicInteger();
        for(ModRegReceiver scheduler : selectedReceivers.values()) {
            Thread thread = new Thread(() -> {
                scheduler.startRegistration();
                schedulerFinishedRunning(resultCount.incrementAndGet());
                scheduler.imGuiReset();
            });
            thread.start();
        }
    }

    private void schedulerFinishedRunning(int count) {
        if(count >= selectedReceivers.size()) {
            running = false;
        }
    }

    private void loadJUnitTests() {
        selectedReceivers.clear();

        // Round-robin
        ModRegReceiver r = new RoundRobinReceiver(100);
        r.enqueue(new ModuleRegister("P1", 5000));
        r.enqueue(new ModuleRegister("P2", 3000));
        r.enqueue(new ModuleRegister("P3", 1000));
        r.enqueue(new ModuleRegister("P4", 4000));
        selectedReceivers.put(r.getClass(), r);

        // Priority
        r = new PriorityReceiver(100);
        r.enqueue(new ModuleRegister("P1", 2000, ModuleRegister.Priority.MED));
        r.enqueue(new ModuleRegister("P2", 3000, ModuleRegister.Priority.LOW));
        r.enqueue(new ModuleRegister("P3", 4000, ModuleRegister.Priority.MED));
        r.enqueue(new ModuleRegister("P4", 4000, ModuleRegister.Priority.HIGH));
        r.enqueue(new ModuleRegister("P5", 4000, ModuleRegister.Priority.LOW));
        r.enqueue(new ModuleRegister("P6", 4000, ModuleRegister.Priority.HIGH));
        selectedReceivers.put(r.getClass(), r);

        // Multi-level feedback queue (young & old)
        r = new MultiLevelFeedbackQueueReceiver(100);
        r.enqueue(new ModuleRegister("P1", 2000));
        r.enqueue(new ModuleRegister("P2", 3000));
        r.enqueue(new ModuleRegister("P3", 4000));
        r.enqueue(new ModuleRegister("P4", 4000));
        r.enqueue(new ModuleRegister("P5", 4000));
        r.enqueue(new ModuleRegister("P6", 4000));
        selectedReceivers.put(r.getClass(), r);

        // Shortest job first
        r = new ShortestJobFirstReceiver(100);
        r.enqueue(new ModuleRegister("P1", 2000));
        r.enqueue(new ModuleRegister("P2", 3000));
        r.enqueue(new ModuleRegister("P3", 4000));
        r.enqueue(new ModuleRegister("P4", 2000));
        r.enqueue(new ModuleRegister("P5", 4000));
        r.enqueue(new ModuleRegister("P6", 1000));
        selectedReceivers.put(r.getClass(), r);

        // First come first serve
        r = new FirstComeFirstServeReceiver(100);
        r.enqueue(new ModuleRegister("P1", 2000));
        r.enqueue(new ModuleRegister("P2", 3000));
        r.enqueue(new ModuleRegister("P3", 4000));
        r.enqueue(new ModuleRegister("P4", 2000));
        r.enqueue(new ModuleRegister("P5", 4000));
        r.enqueue(new ModuleRegister("P6", 1000));
        selectedReceivers.put(r.getClass(), r);

    }

    private void loadLongAndEqualTests() {
        selectedReceivers.clear();

        // RR
        ModRegReceiver r = new RoundRobinReceiver(100);
        r.enqueue(new ModuleRegister("P1", 19000));
        r.enqueue(new ModuleRegister("P2", 29000));
        r.enqueue(new ModuleRegister("P3", 35000));
        r.enqueue(new ModuleRegister("P4", 6000));
        r.enqueue(new ModuleRegister("P5", 40000));
        r.enqueue(new ModuleRegister("P6", 44000));
        selectedReceivers.put(r.getClass(), r);

        // P
        r = new PriorityReceiver(100);
        r.enqueue(new ModuleRegister("P1", 19000));
        r.enqueue(new ModuleRegister("P2", 29000));
        r.enqueue(new ModuleRegister("P3", 35000));
        r.enqueue(new ModuleRegister("P4", 6000));
        r.enqueue(new ModuleRegister("P5", 40000));
        r.enqueue(new ModuleRegister("P6", 44000));
        selectedReceivers.put(r.getClass(), r);

        // MLFQ
        r = new MultiLevelFeedbackQueueReceiver(100);
        r.enqueue(new ModuleRegister("P1", 19000));
        r.enqueue(new ModuleRegister("P2", 29000));
        r.enqueue(new ModuleRegister("P3", 35000));
        r.enqueue(new ModuleRegister("P4", 6000));
        r.enqueue(new ModuleRegister("P5", 40000));
        r.enqueue(new ModuleRegister("P6", 44000));
        selectedReceivers.put(r.getClass(), r);

        r = new ShortestJobFirstReceiver(100);
        r.enqueue(new ModuleRegister("P1", 19000));
        r.enqueue(new ModuleRegister("P2", 29000));
        r.enqueue(new ModuleRegister("P3", 35000));
        r.enqueue(new ModuleRegister("P4", 6000));
        r.enqueue(new ModuleRegister("P5", 40000));
        r.enqueue(new ModuleRegister("P6", 44000));
        selectedReceivers.put(r.getClass(), r);

        r = new FirstComeFirstServeReceiver(100);
        r.enqueue(new ModuleRegister("P1", 19000));
        r.enqueue(new ModuleRegister("P2", 29000));
        r.enqueue(new ModuleRegister("P3", 35000));
        r.enqueue(new ModuleRegister("P4", 6000));
        r.enqueue(new ModuleRegister("P5", 40000));
        r.enqueue(new ModuleRegister("P6", 44000));
        selectedReceivers.put(r.getClass(), r);
    }

    public static void main(String[] args) {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        JETBRAINS_FONT = io.getFonts().addFontFromFileTTF("jetbrains_mono.ttf", 25);

        launch(new Window());
    }
}
