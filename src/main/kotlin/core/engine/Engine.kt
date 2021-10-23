package core.engine

import core.engine.ai.*
import core.engine.ai.evaluator.MaterialEvaluator

class Engine {


    var board = GameState()
    var playerOne: Player = AlphaBetaIDPlayer(
        0, MaterialEvaluator(
            listOf(

                0, 0, 2, -1, 20, -20, 0, -1, 0, 0, 0, 0, 0, 0, 1, 0

            )
        )
    )
    var playerTwo: Player = AlphaBetaIDPlayer(
        1, MaterialEvaluator(
            listOf(

                0, 0, 2, -1, 20, -21, 0, -1, 0, 0, 0, 0, 0, 0, 1, 0

            )
        )
    )
    var playerTurn = 0
    var turnsPassed = 0


    var lastState: GameState? = null
    var secondLastState: GameState? = null
    var thirdLastState: GameState? = null


    fun getWinnerAI(): Player {

        if (playerTurn == 0)
            return playerTwo
        if (playerTurn == 1)
            return playerOne

        return playerOne
    }

    fun getLooserAI(): Player {

        if (playerTurn == 0)
            return playerOne
        if (playerTurn == 1)
            return playerTwo

        return playerOne
    }


    fun hasPlayerLost(): Boolean {

        if (turnsPassed <= 2)
            return false


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


    }

    fun getPossibleMoves(player: Int, board: GameState = this.board): List<Move> {

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

    fun getTownPlaceMove(board: GameState, player: Int): MutableList<Move> {
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

    fun getShootMoves(board: GameState, player: Int): MutableList<Move> {
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

    fun getSlideMoves(board: GameState, player: Int): MutableList<Move> {

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

    fun getRetreatMoves(board: GameState, player: Int): MutableList<Move> {
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


                                }
                            } catch (e: Exception) {
                            }

                        }

                }
            }


        return moves

    }

    fun getForwardMoves(board: GameState, player: Int): MutableList<Move> {

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

    fun getCaptureMoves(board: GameState, player: Int): MutableList<Move> {
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



