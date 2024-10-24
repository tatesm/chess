package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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
            case QUEEN -> calculator = new QueenMovesCalculator();
            case PAWN -> calculator = new PawnMovesCalculator();
            case BISHOP -> calculator = new BishopMovesCalculator();
            case ROOK -> calculator = new RookMovesCalculator();
            case KNIGHT -> calculator = new KnightMovesCalculator();
            default -> throw new IllegalStateException("Unexpected value: " + this.type);
        }
        return calculator.pieceMoves(board, myPosition);
    }

    public interface PieceMovesCalculator {
        Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
    }

    public abstract static class DirectionalMovesCalculator implements PieceMovesCalculator {

        protected Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, int[][] directions) {
            List<ChessMove> possibleMoves = new ArrayList<>();
            int currentRow = myPosition.getRow();
            int currentColumn = myPosition.getColumn();

            for (int[] direction : directions) {
                int newRow = currentRow;
                int newCol = currentColumn;

                while (true) {
                    newRow += direction[0];
                    newCol += direction[1];


                    if (newRow < 1 || newRow > 8 || newCol < 1 || newCol > 8) {
                        break;
                    }

                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);


                    if (pieceAtNewPosition == null) {
                        possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                    } else {
                        if (pieceAtNewPosition.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                            possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break; // Stop when hitting another piece
                    }
                }
            }

            return possibleMoves;
        }
    }


    public class KingMovesCalculator implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            List<ChessMove> possibleMovesKing = new ArrayList<>();
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
                        possibleMovesKing.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            return possibleMovesKing;
        }
    }

    public static class QueenMovesCalculator extends DirectionalMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

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
            return calculateMoves(board, myPosition, directions);
        }
    }

    public class KnightMovesCalculator implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            List<ChessMove> possibleMovesKnight = new ArrayList<>();
            int[][] directions = {
                    {2, 1},   // 2 Up, 1 Right
                    {2, -1},  // 2 Up, 1 Left
                    {-2, 1},  // 2 Down, 1 Right
                    {-2, -1}, // 2 Down, 1 Left
                    {1, 2},   // 1 Up, 2 Right
                    {1, -2},  // 1 Up, 2 Left
                    {-1, 2},  // 1 Down, 2 Right
                    {-1, -2}  // 1 Down, 2 Left
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
                        possibleMovesKnight.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            return possibleMovesKnight;
        }
    }

    public static class RookMovesCalculator extends DirectionalMovesCalculator {

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            int[][] directions = {
                    {1, 0},   // Up
                    {-1, 0},  // Down
                    {0, 1},   // Right
                    {0, -1},  // Left
            };
            return calculateMoves(board, myPosition, directions);
        }
    }

    public static class BishopMovesCalculator extends DirectionalMovesCalculator {

        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

            int[][] directions = {
                    {1, 1},   // Up-Right
                    {1, -1},  // Up-Left
                    {-1, 1},  // Down-Right
                    {-1, -1}  // Down-Left
            };
            return calculateMoves(board, myPosition, directions);
        }
    }


    public class PawnMovesCalculator implements PieceMovesCalculator {
        @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
            List<ChessMove> possibleMovesPawn = new ArrayList<>();
            int currentRow = myPosition.getRow();
            int currentColumn = myPosition.getColumn();


            int direction;
            int startingRow;
            if (ChessPiece.this.getTeamColor() == ChessGame.TeamColor.WHITE) {
                direction = 1; // Move up
                startingRow = 2;
            } else {
                direction = -1; // Move down
                startingRow = 7;
            }


            checkMoves(board, myPosition, possibleMovesPawn, currentRow, currentColumn, direction);


            if (currentRow == startingRow) {
                int newRow = currentRow + (2 * direction);
                ChessPosition newPosition = new ChessPosition(newRow, currentColumn);
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                ChessPiece pieceInFront = board.getPiece(new ChessPosition(currentRow + direction, currentColumn));
                if (pieceAtNewPosition == null && pieceInFront == null) {
                    possibleMovesPawn.add(new ChessMove(myPosition, newPosition, null));
                }
            }

            return possibleMovesPawn;
        }

        private void checkMoves(ChessBoard board, ChessPosition myPosition, List<ChessMove> possibleMovesPawn,
                                int currentRow, int currentColumn, int direction) {
            int[][] directions = {
                    {direction, 0},   // Forward
                    {direction, 1},   // Up-Right or Down-Right
                    {direction, -1}    // Up-Left or Down-Left
            };

            for (int[] dir : directions) {
                int newRow = currentRow + dir[0];
                int newCol = currentColumn + dir[1];


                if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                    ChessPosition newPosition = new ChessPosition(newRow, newCol);
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);


                    if (dir[1] == 0 && pieceAtNewPosition == null) {
                        addPromotionMoves(myPosition, possibleMovesPawn, newPosition, newRow);
                    }


                    if (dir[1] != 0 && pieceAtNewPosition != null && pieceAtNewPosition.getTeamColor() != ChessPiece.this.getTeamColor()) {
                        addPromotionMoves(myPosition, possibleMovesPawn, newPosition, newRow);
                    }
                }
            }
        }

        private void addPromotionMoves(ChessPosition myPosition, List<ChessMove> possibleMovesPawn, ChessPosition newPosition, int newRow) {
            if (newRow == 8 || newRow == 1) {
                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
            } else {
                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, null));
            }
        }
    }


}

