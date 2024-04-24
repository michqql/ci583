package ci583.receiver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class ShortestJobFirstReceiver extends ModRegReceiver {

    private final TreeSet<ModuleRegister> jobs;

    public ShortestJobFirstReceiver() {
        Comparator<ModuleRegister> comparator = (c1, c2) -> (int) (c1.getWorkToDo() - c2.getWorkToDo());
        jobs = new TreeSet<>(comparator);
    }

    @Override
    public void enqueue(ModuleRegister m) {

    }

    @Override
    public List<ModuleRegister> startRegistration() {
        List<ModuleRegister> result = new ArrayList<>();
        return result;
    }

    @Override
    public void imGuiDraw() {

    }

    @Override
    public void imGuiReset() {

    }
}
