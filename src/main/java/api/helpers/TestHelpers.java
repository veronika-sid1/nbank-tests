package api.helpers;

public class TestHelpers {
    public static void repeat(int count, Runnable action) {
        for (int i = 0; i < count; i++) {
            action.run();
        }
    }
}
