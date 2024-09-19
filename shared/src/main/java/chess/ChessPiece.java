package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor; // Store the color
        this.type = type;             // Store the type
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceMovesCalculator calculator;
        switch (this.type) {
            case KING -> calculator = new KingMovesCalculator();
            //case QUEEN -> calculator = new QueenMovesCalculator();
            //case PAWN -> calculator = new PawnMovesCalculator();
            //case BISHOP -> calculator = new BishopMovesCalculator();
            //case ROOK -> calculator = new RookMovesCalculator();
            //case KNIGHT -> calculator = new KnightMovesCalculator();
            default -> throw new IllegalStateException("Unexpected value: " + this.type);
        }
        return calculator.pieceMoves(board, myPosition);
    }

    public interface PieceMovesCalculator {
        Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
    }

    public class KingMovesCalculator implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            List<ChessMove> possibleMoves = new ArrayList<>();
            int[][] directions = {
                    {1, 0},   // Up
                    {-1, 0},  // Down
                    {0, 1},   // Right
                    {0, -1},  // Left
                    {1, 1},   // Up-Right
                    {1, -1},  // Up-Left
                    {-1, 1},  // Down-Right
                    {-1, -1}  // Down-Left
            };
            int currentRow = myPosition.getRow();
            int currentColumn = myPosition.getColumn();

            for (int[] direction : directions) {
                int newRow = currentRow + direction[0];
                int newCol = currentColumn + direction[1];
                if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                    if (pieceAtNewPosition == null || pieceAtNewPosition.getTeamColor() != ChessPiece.this.getTeamColor()) {
                        possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            return possibleMoves;
        }
    }
/**
 public class QueenMovesCalculator implements PieceMovesCalculator {

@Override public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

}
}

 public class KnightMovesCalculator implements PieceMovesCalculator {

@Override public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
return List.of();
}
}

 public class BishopMovesCalculator implements PieceMovesCalculator {

@Override public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
return List.of();

}
}

 public class PawnMovesCalculator implements PieceMovesCalculator {

@Override public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
return List.of();

}
}

 public class RookMovesCalculator implements PieceMovesCalculator {

@Override public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
return List.of();

}
}*/
}

