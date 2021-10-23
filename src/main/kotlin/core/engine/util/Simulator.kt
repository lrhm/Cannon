package core.engine.util

import core.engine.Board
import core.engine.Engine
import core.engine.Move
import core.engine.ai.AlphaBetaIDPlayer
import core.engine.ai.Player
import core.engine.ai.evaluator.MaterialEvaluator
import core.engine.otherPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random
import kotlin.random.nextUInt

class Simulator {


//    val winnerWeights = mutableListOf<MaterialEvaluator>()
//
//    fun doNextTurn() {
//
//        if (engine.hasPlayerLost()) {
//
////            if (engine.playerTurn == 0) {
////                winnerWeights.add(engine.playerTwo.evaluator)
////            } else
////                winnerWeights.add(engine.playerOne.evaluator)
//            reset()
//            return
//
//        }
//
//
//        if (engine.playerTurn == 0 && engine.playerOne.type == Player.Type.AI) {
//
//            val move = engine.playerOne.getMove(engine)
//            move.applyMove(engine.board, engine.playerTurn)
//            println("moving ${move.type} ${move.from} ${move.to}")
//            engine.turnsPassed++
//            engine.playerTurn = engine.playerTurn.otherPlayer()
////            board = engine.board.copy()
//            return
//
//        }
//
//        if (engine.playerTurn == 1 && engine.playerOne.type == Player.Type.AI) {
//
//            val move = engine.playerTwo.getMove(engine)
//            move.applyMove(engine.board, engine.playerTurn)
//
//
//            engine.turnsPassed++
//            engine.playerTurn = engine.playerTurn.otherPlayer()
//
////            board = engine.board.copy()
//            return
//
//        }
//
//
//    }
//
//    fun reset() {
//
//        println("game ended")
//        engine.board = Board()
//        engine.turnsPassed = 0
//        engine.playerTurn = 0
////        board = engine.board
////        isGameStarted = false
//
//        if (winnerWeights.size == 4) {
//
//
//            val newEvaluator = winnerWeights[0].mutateAndCombine(winnerWeights[3])
//
//            val otherEval = winnerWeights[1].mutateAndCombine(winnerWeights[2])
//            engine.playerOne.evaluator = newEvaluator
////            engine.playerTwo.evaluator = otherEval
//            winnerWeights.removeAll { true }
//
//            println("new weights are found, start again ")
//            println("${newEvaluator.weights}")
//            println("${otherEval.weights}")
//
//
//        }
//        startGame()
//    }
//
//    fun startGame() {
//
//
//        CoroutineScope(Dispatchers.Main).launch {
//
////            if (isGameStarted.not())
////                return@launch
//            doNextTurn()
////            delay(100)
//            startGame()
//
//
//        }
//
//
//    }


    fun simulateGame(first: AlphaBetaIDPlayer, second: AlphaBetaIDPlayer) {

        println("simulate a game")
        CoroutineScope(Dispatchers.Default).launch {
            val engine = Engine()
            first.player = 0
            second.player = 1
            engine.playerOne = first
            engine.playerTwo = second

            while (engine.hasPlayerLost().not()) {
                engine.playATurn()
            }

            var winner = engine.getWinnerAI()
            val loser = engine.getLooserAI()
            println("OneGameFinished, winner is ${winner.player}. $finishdMatches")
            println("Board is ")
            engine.board.printBoard()
            println("weights are ${winner.evaluator.weights}")
            winStatistics[winner] = if (winStatistics[winner] != null) winStatistics[winner]!! + 1 else 1
            winStatistics[loser] = if (winStatistics[loser] != null) winStatistics[loser]!! - 1 else -1


            onGoingMatches--
            finishdMatches++
            if (finishdMatches < maxMatch) {
                startAnotherRandomMatch()
            } else {
                println("Finished the matches, now we should mutate and start the pool again $onGoingMatches")
                if (onGoingMatches == 0)
                    continiuePlayoutOrMutate()
            }
        }
    }

    init {
//        generateAIPool()
//        startRandomMatches()
    }


    fun startAnotherRandomMatch() {
        onGoingMatches++
        val first = winStatistics.keys.random(rand)
        var second = winStatistics.keys.random(rand)
        while (first == second)
            second = winStatistics.keys.random(rand)
        CoroutineScope(Dispatchers.Default).launch {
            simulateGame(first, second)

        }
    }

    val rand = Random(System.currentTimeMillis())

    var onGoingMatches = 0
    var maxMatch = 18
    var finishdMatches = 0

    fun continiuePlayoutOrMutate() {

//        if (finishdMatches < 12)
//            startRandomMatches()
//        else {

        finishdMatches = 0

        mutateAndCrossOver()
//        }

    }

    val file = File("weights.txt")

    fun mutateAndCrossOver() {

        val ais = winStatistics.keys.sortedByDescending { winStatistics[it] }

        println("First mutation genZ")

        for (ai in ais) {
            file.appendText("---- Mutation genZ----\n")
            file.appendText("Up is ${winStatistics[ai]} with ${ai.evaluator.weights}\n")
        }
        file.appendText("----End Mutation genZ----\n")


//        ais[6].evaluator = ais[0].evaluator.mutateAndCombine1(ais[1].evaluator)

        for (i in 3..6) {
            val first = rand.nextInt(0, 3)
            var second = rand.nextInt(0, 3)
//            while (second == first)
//                second = rand.nextInt(0, 3)
            ais[i].evaluator = ais[first].evaluator.mutateAndCombine1(ais[second].evaluator)

            winStatistics[ais[i]] = 0
        }

        for (ai in ais) {
            winStatistics[ai] = 0
        }
        startRandomMatches()

    }

    val maxParalledMatches = 14

    fun startRandomMatches() {


        for (i in 0..maxParalledMatches) {
            onGoingMatches++
            val first = winStatistics.keys.random(rand)
            var second = winStatistics.keys.random(rand)
            while (first == second)
                second = winStatistics.keys.random(rand)
            CoroutineScope(Dispatchers.Default).launch {
                simulateGame(first, second)

            }
        }
    }

    var pool = mutableListOf<AlphaBetaIDPlayer>()
    val winStatistics = HashMap<AlphaBetaIDPlayer, Int>()

    fun generateAIPool() {

        pool = mutableListOf()
        pool.add(
            AlphaBetaIDPlayer(
                0, MaterialEvaluator(
                    arrayListOf(
                        3, -2, //moves
                        2, -2, //cannons
                        35, -40, //pawns
                        5, -4, //shoot
                        5, -4, //capture
                        3, -3, //possible shots
                        8, 6, // number pawn in attack vs defend
                        2 // random
                        , 5// singe move distance
                    )
                )
            )
        )

        pool.add(
            AlphaBetaIDPlayer(
                0, MaterialEvaluator(
                    arrayListOf(
                        9, -14, 12, -9, 48, -47, 15, -9, 16, -13, 10, -7, 19, 13, 9, 13
                    )
                )
            )
        )


        pool.add(
            AlphaBetaIDPlayer(
                0, MaterialEvaluator(
                    arrayListOf(
                        24, -29, 25, -21, 64, -55, 27, -16, 27, -22, 22, -17, 31, 25, 23, 22
                    )
                )
            )
        )


        pool.add(
            AlphaBetaIDPlayer(
                0, MaterialEvaluator(
                    arrayListOf(
                        7, -11, 10, -9, 46, -44, 14, -7, 13, -10, 10, -6, 18, 10, 8, 10
                    )
                )
            )
        )


        pool.add(
            AlphaBetaIDPlayer(
                0, MaterialEvaluator(
                    arrayListOf(
                        4, -4, 4, -4, 38, -42, 7, -4, 7, -4, 4, -3, 10, 7, 3, 6
                    )
                )
            )
        )


        pool.add(
            AlphaBetaIDPlayer(
                0, MaterialEvaluator(
                    arrayListOf(
                        19, -25, 20, -19, 60, -51, 24, -14, 24, -18, 19, -13, 27, 21, 18, 19
                    )
                )
            )
        )



        pool.add(
            AlphaBetaIDPlayer(
                0, MaterialEvaluator(
                    arrayListOf(
                        8, -12, 10, -9, 45, -45, 15, -9, 14, -11, 10, -6, 18, 13, 8, 12
                    )
                )
            )
        )

        for (ai in pool)
            winStatistics[ai] = 0


    }
}