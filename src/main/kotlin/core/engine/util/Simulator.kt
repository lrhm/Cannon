package core.engine.util

import core.engine.Board
import core.engine.Engine
import core.engine.Move
import core.engine.ai.Player
import core.engine.ai.evaluator.MaterialEvaluator
import core.engine.otherPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Simulator {

    val engine = Engine

    val winnerWeights = mutableListOf<MaterialEvaluator>()

    fun doNextTurn() {

        if (engine.hasPlayerLost()) {

            if (engine.playerTurn == 0) {
                winnerWeights.add(engine.playerTwo.evaluator)
            } else
                winnerWeights.add(engine.playerOne.evaluator)
            reset()
            return

        }


        if (engine.playerTurn == 0 && engine.playerOne.type == Player.Type.AI) {

            val move = engine.playerOne.getMove(engine)
            move.applyMove(engine.board, engine.playerTurn)
            println("moving ${move.type} ${move.from} ${move.to}")
            engine.turnsPassed++
            engine.playerTurn = engine.playerTurn.otherPlayer()
//            board = engine.board.copy()
            return

        }

        if (engine.playerTurn == 1 && engine.playerOne.type == Player.Type.AI) {

            val move = engine.playerTwo.getMove(engine)
            move.applyMove(engine.board, engine.playerTurn)


            engine.turnsPassed++
            engine.playerTurn = engine.playerTurn.otherPlayer()

//            board = engine.board.copy()
            return

        }


    }

    fun reset() {

        println("game ended")
        engine.board = Board()
        engine.turnsPassed = 0
        engine.playerTurn = 0
//        board = engine.board
//        isGameStarted = false

        if (winnerWeights.size == 4) {


            val newEvaluator = winnerWeights[0].mutateAndCombine(winnerWeights[3])

            val otherEval = winnerWeights[1].mutateAndCombine(winnerWeights[2])
            engine.playerOne.evaluator = newEvaluator
            engine.playerTwo.evaluator = otherEval
            winnerWeights.removeAll { true }

            println("new weights are found, start again ")
            println("${newEvaluator.weights}")
            println("${otherEval.weights}")


        }
        startGame()
    }

    fun startGame() {


        CoroutineScope(Dispatchers.Main).launch {

//            if (isGameStarted.not())
//                return@launch
            doNextTurn()
//            delay(100)
            startGame()


        }


    }
}