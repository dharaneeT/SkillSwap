abstract class Session {
    abstract void start();

    void end() {
        System.out.println("Session ended");
    }
}