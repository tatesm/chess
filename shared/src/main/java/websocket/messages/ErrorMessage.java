package websocket.messages;

import websocket.messages.ServerMessage;

import java.util.Objects;

public class ErrorMessage extends ServerMessage {
    private final String errorMessage;

    public ErrorMessage(String errorMessage) {
        super(ServerMessage.ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorMessage)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ErrorMessage that = (ErrorMessage) o;
        return Objects.equals(getErrorMessage(), that.getErrorMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getErrorMessage());
    }
}