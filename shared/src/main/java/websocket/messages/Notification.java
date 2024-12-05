package websocket.messages;

public class Notification {
    public enum Type {
        CONNECT,
        MOVE,
        RESIGN,
        DISCONNECT
    }

    private final Type type;
    private final String message;

    public Notification(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    // Getter methods for Gson or other libraries
    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }


    // Removed toString() to let Gson handle JSON conversion
}
