package core.engine

import core.engine.*
import core.engine.ai.*
import core.engine.ai.evaluator.MaterialEvaluator
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

class Engine {
//
//    companion object{
//        instance = En
//    }
//
//    3, -2, //moves
//    2, -2, //cannons
//    35, -40, //pawns
//    5, -4, //shoot
//    5, -4, //capture
//    3, -3, //possible shots
//    8, 6, // number pawn in attack vs defend
//    2 // random
//    , 6// singe move distance

    var board = Board()
    var playerOne = AlphaBetaIDPlayer(
        0, MaterialEvaluator(
            listOf(

//                3, -2, 4, -4, 35, -40, 0, -4, 0, -4, 0, -3, 8, 6, 2, 0
                0, 0, 2, -1, 20, -21, 0, -1, 0, 0, 0, 0, 0, 0, 1, 0

//                3, -2, 2, -2, 45, -46, 5, -4, 5, -4, 3, -3, 8, 6, 2, 5

//                4, -3, //moves
//                8, -7, //cannons
//                30, -27, //pawns
//                8, -7, //shoot
//                5, -4, //capture
//                0, 0, //possible shots
//                0, 0, // number pawn in attack vs defend
//                2 // random
//                , 0// singe move distance

            )
        )
    )
    var playerTwo = AlphaBetaIDPlayer(
        1, MaterialEvaluator(
            listOf(

                0, 0, 2, -1, 20, -21, 0, -1, 0, 0, 0, 0, 0, 0, 1, 0


//                3, -2, 4, -4, 35, -40, 0, -4, 0, -4, 0, -3, 8, 6, 2, 0

//                3, -2, 2, -2, 35, -40, 5, -4, 5, -4, 3, -3, 8, 6, 2, 5
//                9, -14, 12, -9, 48, -47, 15, -9, 16, -13, 10, -7, 19, 13, 9, 13
//                3, -2, 2, -2, 45, -48, 4, -3, 3, -2, 1, -1, 3, 2, 2, 1

//                3, -2, 4, -3, 45, -46, 4, -3, 3, -2, 1, -1, 3, 2, 2, 1

//                3, -2, 2, -2, 45, -46, 5, -4, 5, -4, 3, -3, 8, 6, 2, 5
//                4, -3, //moves
//                5, -5, //cannons
//                35, -40, //pawns
//                8, -7, //shoot
//                5, -4, //capture
//                6, -6, //possible shots
//                0, 0, // number pawn in attack vs defend
//                2 // random
//                , 1// singe move distance


            )
        )
    )
    var playerTurn = 0
    var turnsPassed = 0


    var lastState: Board? = null
    var secondLastState: Board? = null
    var thirdLastState: Board? = null

    init {
//        board.lateInit()
//        playerTurn = 1
//        turnsPassed = 31
    }

    fun getWinnerAI(): AlphaBetaIDPlayer {

        if (playerTurn == 0)
            return playerTwo
        if (playerTurn == 1)
            return playerOne

        return playerOne
    }

    fun getLooserAI(): AlphaBetaIDPlayer {

        if (playerTurn == 0)
            return playerOne
        if (playerTurn == 1)
            return playerTwo

        return playerOne
    }

    fun getPlayerForTurn(): Player {
        if (playerTurn == 0)
            return playerOne
        return playerTwo
    }

    fun placeTowns() {
        placeTownForPlayer(3)
        placeTownForPlayer(4)

        turnsPassed += 2
    }

    fun hasPlayerLost(): Boolean {

        if (turnsPassed <= 2)
            return false


//        val player = getPlayerForTurn()
        val row = if (playerTurn == 0) 9 else 0

        var isTownDead = true
        for (i in 1..8) {
            if (board.board[row][i] == 3 + playerTurn)
                isTownDead = false
        }

        if (isTownDead)
            return true

        if (getPossibleMoves(playerTurn).isEmpty())
            return true


        return false

    }

    fun startGame() {

//        if(playerTurn == 0 && )
    }

    fun playATurn() {


//        readLine()

        if (hasPlayerLost())
            return

        val move = if (playerTurn == 0)
            playerOne.getMove(this@Engine)
        else playerTwo.getMove(this@Engine)

        thirdLastState = secondLastState?.copy()
        secondLastState = lastState?.copy()
        lastState = board.copy()

        move.applyMove(board, playerTurn)

        move.from.printChessNotation()
        move.to.printChessNotation()

        turnsPassed++
        playerTurn = playerTurn.otherPlayer()


//            if (hasPlayerLost())
//                return@coroutineScope


    }

    fun playGame() {


        placeTowns()

        while (true) {
            val player = turnsPassed.mod(2)
            val moves = getPossibleMoves(player, board)

            val move = moves.random()

//            println("player ${player} moves from ${move.from} to ${move.to} ")

            move.applyMove(board, playerTurn)
            board.printBoard()

            turnsPassed++
            readLine()
        }
        //apply move


    }

    fun placeTownForPlayer(townIdx: Int) {

        print("Please enter the position of the town for Player ${townIdx - 2}\n")
        val townOneInput = readLine()

        val pos = townOneInput?.split(" ")?.map { it.toInt() }
        if (pos.isNullOrEmpty())
            print("Invalid, DO again")
        board.board[pos!![0]][pos[1]] = townIdx


    }

    fun getPossibleMoves(player: Int, board: Board = this.board): List<Move> {

//        val set = mutableSetOf(
//
//            getSlideMoves(board, player),
//            getCaptureMoves(board, player),
//
//            )

//        println("get possible move for turn ${playerTurn}")

        if (turnsPassed < 2)
            return getTownPlaceMove(board, player)

        val all = mutableListOf<Move>()
        all.addAll(getShootMoves(board, player))
        all.addAll(getCaptureMoves(board, player))
        all.addAll(getSlideMoves(board, player))
        all.addAll(getForwardMoves(board, player))
        all.addAll(getRetreatMoves(board, player))


        return all

    }

    fun getTownPlaceMove(board: Board, player: Int): MutableList<Move> {
        val moves = mutableListOf<Move>()


        for (i in 1..8)
            moves.add(
                Move(
                    Move.Type.PlaceTown, Position(-1, -1),
                    Position
                        (
                        if (player == 0) 9 else 0, i

                    )
                )
            )


        return moves
    }

    fun getShootMoves(board: Board, player: Int): MutableList<Move> {
        val moves = mutableListOf<Move>()
        for (i in 0..9)
            for (j in 0..9) {

                if (board.board[i][j].isFriendlySoldier(player)) {

                    for (k in -1..1)
                        for (l in -1..1) {
//                            if (l == -1 && k == 0)
//                                continue
//                            if (l == 1 && k == 0)
//                                continue
                            try {
                                if (board.board[i + k][j + l].isFriendlySoldier(player) and
                                    board.board[i + 2 * k][j + 2 * l].isFriendlySoldier(player)
                                    and board.board[i + 3 * k][j + 3 * l].isEmpty()
                                ) {
                                    //slide can happen now

                                    if (board.board[i + 4 * k][j + 4 * l].isEnemy(player))
                                        moves.add(
                                            Move(Move.Type.Shoot, Position(i, j), Position(i + 4 * k, j + 4 * l))
                                        )


                                    if (board.board[i + 4 * k][j + 4 * l].isEmpty() and board.board[i + 5 * k][j + 5 * l].isEnemy(
                                            player
                                        )
                                    )
                                        moves.add(
                                            Move(Move.Type.Shoot, Position(i, j), Position(i + 5 * k, j + 5 * l))
                                        )

                                }
                            } catch (e: Exception) {
                            }

                        }


                }

            }


        return moves
    }

    fun getSlideMoves(board: Board, player: Int): MutableList<Move> {

        val moves = mutableListOf<Move>()

        for (i in 0..9)
            for (j in 0..9) {

                if (board.board[i][j].isFriendlySoldier(player)) {

//                    println("soldier at $i $j")
                    for (k in -1..1)
                        for (l in -1..1) {
//                            if (l == -1 && k == 0)
//                                continue
//                            if (l == 1 && k == 0)
//                                continue
                            try {
//                                println("$k $l ${i+k} ${j+l}")
                                if (board.board[i + k][j + l].isFriendlySoldier(player) and
                                    board.board[i + 2 * k][j + 2 * l].isFriendlySoldier(player)
                                    and board.board[i + 3 * k][j + 3 * l].isEmpty()
                                ) {
                                    //slide can happen now
                                    moves.add(
                                        Move(Move.Type.Slide, Position(i, j), Position(i + 3 * k, j + 3 * l))
                                    )
                                }
                            } catch (e: Exception) {
                            }

                        }


                }

            }


        return moves
    }

    fun getRetreatMoves(board: Board, player: Int): MutableList<Move> {
        val moves = mutableListOf<Move>()

        val soldierValues = player + 1
        val direction = if (player == 1) -1 else 1

        for (i in 0..9)
            for (j in 0..9) {

                if (board.board[i][j].isFriendlySoldier(player)) {
                    //my soldier

                    for (k in -1..1)
                        for (l in -1..1) {

                            //try catch because index outta bounds
                            try {
                                if (board.board[i + k][l + j].isEnemySoldier(player)) {
                                    // now check if we can retreat

                                    for (z in -1..1) {

                                        try {

                                            if (board.board[i + direction][j + z].isEmpty()
                                                &&
                                                board.board[i + 2 * direction][j + 2 * z].isEmpty()
                                            ) {
                                                moves.add(
                                                    Move(
                                                        Move.Type.Retreat,
                                                        Position(i, j),
                                                        Position(i + 2 * direction, j + 2 * z)
                                                    )
                                                )
                                            }
                                        } catch (e: Exception) {

                                        }
                                    }

//                                    try {
//                                        for (ctr in -1..1)
//                                            if (board.board[i + direction][j + ctr].isEmpty() and board.board[i + 2 * direction][j + ctr * 2].isEmpty()) {
//
//                                                moves.add(
//                                                    Move(
//                                                        Move.Type.Retreat,
//                                                        Position(i, j), Position(i + 2 * direction, j + ctr * 2)
//                                                    )
//                                                )
//                                            }
//
//                                    } catch (e: Exception) {
//                                    }


                                }
                            } catch (e: Exception) {
                            }

                        }

                }
            }


        return moves

    }

    fun getForwardMoves(board: Board, player: Int): MutableList<Move> {

        val moves = mutableListOf<Move>()

        val soldierValues = player + 1
        val direction = if (player == 0) -1 else 1

        for (i in 0..9)
            for (j in 0..9) {
                if (board.board[i][j].isFriendlySoldier(player))
                    for (k in -1..1) {
                        try {
                            val toPos = board.board[i + direction][j + k]
                            if (toPos == 0)
                                moves.add(Move(Move.Type.Forward, Position(i, j), Position(i + direction, j + k)))
                        } catch (e: Exception) {
                        }
                    }
            }

        return moves
    }

    fun getCaptureMoves(board: Board, player: Int): MutableList<Move> {
        val moves = mutableListOf<Move>()

        val soldierValues = player + 1
        val directionD = if (player == 0) -1 else 1
        val directions = listOf(directionD, 0)

        for (i in 0..9)
            for (j in 0..9) {
                if (board.board[i][j] == soldierValues)
                    for (k in -1..1) {
                        for (direction in directions)
                            try {
                                val toPos = board.board[i + direction][j + k]
                                if (toPos.isEnemy(player))
                                    moves.add(Move(Move.Type.Capture, Position(i, j), Position(i + direction, j + k)))
                            } catch (e: Exception) {
                            }
                    }
            }

        return moves
    }


}

fun Int.isEnemySoldier(player: Int): Boolean {
    if (player == 0)
        if (this == 2)
            return true

    if (player == 1)
        if (this == 1)
            return true

    return false
}

fun Int.isEmpty() = this == 0

fun Int.otherPlayer() = (this + 1).mod(2)

fun Int.isFriendlySoldier(player: Int): Boolean {
    if (player == 0)
        if (this == 1)
            return true

    if (player == 1)
        if (this == 2)
            return true

    return false
}

fun Int.isEnemy(player: Int): Boolean {

    if (player == 0) {
        if (this == 2 || this == 4)
            return true
    }

    if (player == 1) {
        if (this == 1 || this == 3)
            return true
    }
    return false
}

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

    fun applyPlaceTownMove(board: Board, player: Int) {

//        println("apply town for player $player")
        if (player == 0) {
            board.board[to.row][to.column] = 3

        } else
            board.board[to.row][to.column] = 4

    }

    fun applyMove(board: Board, player: Int) {


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


data class Position(val row: Int, val column: Int) {
    val i = row
    val j = column

    override fun equals(other: Any?): Boolean {

        if (other is Position)
            return other.row == row && other.column == column
        return super.equals(other)
    }

    val iList = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")

    fun printChessNotation() {

        if (i == -1)
            return
        println(
            "${iList[j]}-${10 - i}"
        )

    }
}