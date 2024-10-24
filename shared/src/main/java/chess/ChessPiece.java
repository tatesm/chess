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

    public abstract class DirectionalMovesCalculator implements PieceMovesCalculator {

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

    public class QueenMovesCalculator extends DirectionalMovesCalculator {
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
            List<ChessMove> possibleMoves_Knight = new ArrayList<>();
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
                        possibleMoves_Knight.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
            return possibleMoves_Knight;
        }
    }

    public class RookMovesCalculator extends DirectionalMovesCalculator {

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

    public class BishopMovesCalculator extends DirectionalMovesCalculator {

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
            if (ChessPiece.this.getTeamColor() == ChessGame.TeamColor.WHITE) {
                int[][] directions = {
                        {1, 0}, // Up
                        {1, 1},   // Up-Right
                        {1, -1},  // Up-Left
                };
                int currentRow = myPosition.getRow();
                int currentColumn = myPosition.getColumn();
                for (int[] direction : directions) {
                    int newRow = currentRow + direction[0];
                    int newCol = currentColumn + direction[1];

                    if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                        ChessPosition newPosition = new ChessPosition(newRow, newCol);
                        ChessPiece pieceAtNewPosition = board.getPiece(newPosition);

                        if (direction[1] == 0 && pieceAtNewPosition == null) {
                            if (newRow == 8) {
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                            } else {
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, null));
                            }
                        }
                        if (direction[1] != 0 && pieceAtNewPosition != null && pieceAtNewPosition.getTeamColor() != ChessPiece.this.getTeamColor()) {
                            if (newRow == 8) {
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
                if (currentRow == 2) {
                    int newRow = currentRow + 2;
                    ChessPosition newPosition = new ChessPosition(newRow, currentColumn);
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                    ChessPiece pieceInFront = board.getPiece(new ChessPosition(currentRow + 1, currentColumn));
                    if (pieceAtNewPosition == null && pieceInFront == null) {
                        possibleMovesPawn.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            } else {
                int[][] directions = {
                        {-1, 0}, // Down
                        {-1, 1},   // Down-Right
                        {-1, -1},  // Up-Left
                };
                int currentRow = myPosition.getRow();
                int currentColumn = myPosition.getColumn();


                for (int[] direction : directions) {
                    int newRow = currentRow + direction[0];
                    int newCol = currentColumn + direction[1];
                    //Reg forward and capture pieces
                    if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                        ChessPosition newPosition = new ChessPosition(newRow, newCol);
                        ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                        if (direction[1] == 0 && pieceAtNewPosition == null) {
                            if (newRow == 1) {
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                            } else {
                                possibleMovesPawn.add(new ChessMove(myPosition, newPosition, null));
                            }
                        }

                        if (direction[1] != 0 && pieceAtNewPosition != null && pieceAtNewPosition.getTeamColor() != ChessPiece.this.getTeamColor()) {
                            if (newRow == 1) {
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
                if (currentRow == 7) {
                    int newRow = currentRow - 2;
                    ChessPosition newPosition = new ChessPosition(newRow, currentColumn);
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                    ChessPiece pieceInFront = board.getPiece(new ChessPosition(currentRow - 1, currentColumn));
                    if (pieceAtNewPosition == null && pieceInFront == null) {
                        possibleMovesPawn.add(new ChessMove(myPosition, newPosition, null));
                    }
                }

            }
            return possibleMovesPawn;
        }
    }


}

