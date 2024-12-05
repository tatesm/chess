package websocket.messages;

import model.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    private final ServerMessageType serverMessageType;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    // Subclass to handle LOAD_GAME messages
    public static class LoadGameMessage extends ServerMessage {
        private final GameData game;

        public LoadGameMessage(GameData game) {
            super(ServerMessageType.LOAD_GAME);
            this.game = game;
        }

        public GameData getGame() {
            return game;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LoadGameMessage)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            LoadGameMessage that = (LoadGameMessage) o;
            return Objects.equals(getGame(), that.getGame());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), getGame());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
