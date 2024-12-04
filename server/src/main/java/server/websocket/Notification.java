package webSocketMessages;

public class Notification {
    public enum Type {
        CONNECT,
        MOVE,
        DISCONNECT
    }

    private final Type type;
    private final String message;

    public Notification(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("{\"type\":\"%s\",\"message\":\"%s\"}", type, message);
    }
}
