import manager.*;

public class Main {
    static TaskManager taskManager;

    public static void main(String[] args) {
        System.out.println("Поехали!");
        taskManager = Managers.getDefault();
    }
}
