package core.engine.ai

import core.engine.Node
import core.engine.Engine
import core.engine.LRUCache
import core.engine.Move
import core.engine.ai.evaluator.MaterialEvaluator
import core.engine.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Thread.sleep


class AlphaBetaIDPlayer(player: Int, val maxDepth: Int = 8) : Player(player, Type.AI) {

    data class GameState(
        val value: Int,
        val flag: Flag,
        val depth: Int,
        val bestMaxMove: Move?,
        val bestMinMove: Move?
    ) {
        enum class Flag { Exact, LowerBound, UpperBound }
    }

    val transpositionTable = LruCache<String, GameState>(1000)
    var evaluator = MaterialEvaluator()

    override fun evaluateState(node: Node): Int {

        return evaluator.evaluateState(node, player)
    }

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int, isMax: Boolean): Int {

        var mAlpha = alpha
        var mBeta = beta

//        val entry = transpositionTable[node.state.toStr()]
//        if (entry != null && entry.depth >= depth) {
//            when (entry.flag) {
//                GameState.Flag.Exact -> {
//
//                    return entry.value
//                }
//                GameState.Flag.LowerBound -> {
//
//                    mAlpha = max(alpha, entry.value)
//                }
//
//                GameState.Flag.UpperBound -> {
//
//                    mBeta = min(beta, entry.value)
//
//                }
//
//            }
//        }

//        node.player = node.player.otherPlayer()
        if (depth == 0 || node.isTerminalState()) {
            return evaluateState(node)
        }


        var value = Int.MIN_VALUE


        var moves = node.getMoves()

        if (isMax) {
            value = Int.MIN_VALUE

            for (move in moves) {
                val child = node.getNodeForMove(move)
                val score = doAlphaBeta(child, depth - 1, mAlpha, mBeta, false)
                if (score > value) {
                    node.bestMove = move
                }
                value = max(
                    value,
                    score
                )

                if (score >= mBeta)
                    break
                mAlpha = max(mAlpha, value)

            }


            return value

        } else {
            value = Int.MAX_VALUE

            moves = moves.asReversed()

            for (move in moves) {


                val child = node.getNodeForMove(move)
                val score = doAlphaBeta(child, depth - 1, mAlpha, mBeta, true)

//                if (value < score) {
//                    node.bestMove = move
//                }
                value = min(
                    value,
                    score
                )
                if (value <= mAlpha)
                    break

                mBeta = min(mAlpha, value)

            }

            return value

        }


    }

    override fun getMove(engine: Engine): Move {

        val timeStamp = System.currentTimeMillis()

        val parentNode = Node(
            engine.board, engine, engine.playerTurn, maxDepth, true, Int.MIN_VALUE, Int.MIN_VALUE
        )


        var depth = 0
        var bestMove: Move? = null
        val job = CoroutineScope(Dispatchers.IO).launch {
            while (System.currentTimeMillis() - timeStamp < 2000) {
                depth++
                doAlphaBeta(
                    parentNode, depth, Int.MIN_VALUE, Int.MAX_VALUE, true
                )
                println("depth $depth")
                bestMove = parentNode.bestMove

            }
        }

        sleep(2000)
        job.cancel()

        println("It took ${System.currentTimeMillis() - timeStamp} ${bestMove?.type}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}