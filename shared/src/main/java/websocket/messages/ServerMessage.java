package websocket.messages;

import model.GameData;

import java.util.Collection;
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

    // Subclass to handle ERROR messages
    public static class ErrorMessage extends ServerMessage {
        private final String errorMessage;

        public ErrorMessage(String errorMessage) {
            super(ServerMessageType.ERROR);
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

    public static class LegalMovesMessage extends ServerMessage {
        private final Collection<String> legalMoves;

        public LegalMovesMessage(Collection<String> legalMoves) {
            super(ServerMessageType.NOTIFICATION); // Use NOTIFICATION for simplicity, or define a new type
            this.legalMoves = legalMoves;
        }

        public Collection<String> getLegalMoves() {
            return legalMoves;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LegalMovesMessage)) return false;
            if (!super.equals(o)) return false;
            LegalMovesMessage that = (LegalMovesMessage) o;
            return Objects.equals(getLegalMoves(), that.getLegalMoves());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), getLegalMoves());
        }
    }

    public static class ResignationMessage extends ServerMessage {
        private final String player;

        public ResignationMessage(String player) {
            super(ServerMessageType.NOTIFICATION);
            this.player = player;
        }

        public String getPlayer() {
            return player;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ResignationMessage)) return false;
            if (!super.equals(o)) return false;
            ResignationMessage that = (ResignationMessage) o;
            return Objects.equals(getPlayer(), that.getPlayer());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), getPlayer());
        }
    }


}
