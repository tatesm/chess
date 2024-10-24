package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;


    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTurn = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currentTurn;
    }


    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;

    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);

        Collection<ChessMove> allowedMoves = new ArrayList<>();
        for (ChessMove move : allMoves) {
            if (isAllowed(move)) {

                allowedMoves.add(move);
            }

        }

        return allowedMoves;
    }

    private boolean isAllowed(ChessMove move) {

        ChessBoard boardCopy = new ChessBoard();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null) {
                    ChessPiece newPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    boardCopy.addPiece(position, newPiece);
                }
            }
        }


        ChessPiece pieceToMove = boardCopy.getPiece(move.getStartPosition());
        if (pieceToMove == null) {
            return false;
        }
        boardCopy.addPiece(move.getStartPosition(), null);
        boardCopy.addPiece(move.getEndPosition(), pieceToMove);


        ChessBoard originalBoard = this.board;
        this.board = boardCopy;


        ChessGame.TeamColor teamColor = pieceToMove.getTeamColor();
        boolean isInCheck = isInCheck(teamColor);


        this.board = originalBoard;


        return !isInCheck;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {


        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at the starting position.");
        }

        if (piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("It's not " + piece.getTeamColor() + "'s turn.");
        }
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move!");
        }


        board.addPiece(move.getStartPosition(), null);


        ChessPiece.PieceType promotion = move.getPromotionPiece();
        if (promotion != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {

            piece = new ChessPiece(piece.getTeamColor(), promotion);
        }


        board.addPiece(move.getEndPosition(), piece);


        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return new ChessPosition(row, col);
                }
            }
        }
        return null;
    }


    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);


        if (kingPosition == null) {
            return false;
        }


        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(currentPosition);


                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }


                Collection<ChessMove> opponentMoves = piece.pieceMoves(board, currentPosition);


                for (ChessMove move : opponentMoves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }


        return false;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(currentPosition);


                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }


                Collection<ChessMove> possibleMoves = piece.pieceMoves(board, currentPosition);


                for (ChessMove move : possibleMoves) {
                    if (isAllowed(move)) {
                        return false;
                    }
                }
            }
        }


        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {

        if (isInCheck(teamColor)) {
            return false;
        }
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition currentPosition = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(currentPosition);


                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }


                Collection<ChessMove> possibleMoves = piece.pieceMoves(board, currentPosition);


                for (ChessMove move : possibleMoves) {
                    if (isAllowed(move)) {
                        return false;
                    }
                }
            }
        }


        return true;
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
