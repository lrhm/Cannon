package core.engine

open class Move(val type: Type, var from: Position, var to: Position) {
    enum class Type {
        Slide,
        Retreat,
        Forward,
        Capture,
        Shoot,
        PlaceTown
    }

    override fun equals(other: Any?): Boolean {

        if (other is Move) {
            return type == other.type && from == other.from && to == other.to
        }

        return super.equals(other)
    }

    fun applyPlaceTownMove(board: GameState, player: Int) {

//        println("apply town for player $player")
        if (player == 0) {
            board.board[to.row][to.column] = 3

        } else
            board.board[to.row][to.column] = 4

    }

    fun applyMove(board: GameState, player: Int) {


        when (type) {
            Type.PlaceTown -> applyPlaceTownMove(board, player)
            Type.Shoot -> {
                board.board[to.row][to.column] = 0
            }
            else -> {
                val temp = board.board[from.row][from.column]
                board.board[from.row][from.column] = 0
                board.board[to.row][to.column] = temp
            }
        }
    }


}