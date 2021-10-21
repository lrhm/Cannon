package core.engine.ai

import core.engine.Node
import core.engine.util.LruCache
import core.engine.Engine
import core.engine.Move
import core.engine.ai.evaluator.MaterialEvaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Thread.sleep


class NegaMaxIDPlayer(player: Int, val maxDepth: Int = 6) : Player(player, Type.AI) {
    var evaluator = MaterialEvaluator()

    override fun evaluateState(node: Node): Int {
        return evaluator.evaluateState(node, player)
    }

    data class GameState(val value: Int, val flag: Flag, val depth: Int, val bestMove: Move?) {
        enum class Flag { Exact, LowerBound, UpperBound }
    }

    val transpositionHash = LruCache<Int, GameState>(1000000)

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int): Int {


        var score = Int.MIN_VALUE
        var value = Int.MIN_VALUE

        var mAlpha = alpha
        var mBeta = beta


        var entry = transpositionHash[node.hashCode()]
        if (entry != null && entry.depth >= depth) {

            when (entry.flag) {
                GameState.Flag.Exact -> {

                    return entry.value
                }
                GameState.Flag.LowerBound -> {

                    mAlpha = max(alpha, entry.value)
                }

                GameState.Flag.UpperBound -> {

                    mBeta = min(beta, entry.value)

                }

            }


            if (mAlpha > mBeta)
                return entry.value
        }

        if (depth == 0 || node.isTerminalState())
            return evaluateState(node)

        val moves = node.getMoves()

        if (entry?.bestMove != null) {
            moves.add(0, entry.bestMove!!)
        }

        for (move in moves) {

            val childNode = node.getNodeForMove(move)

            value = -1 * doAlphaBeta(childNode, depth - 1, -mBeta, -mAlpha)


            if (value > score) {
                score = value
                node.bestMove = move
            }

            if (value > alpha)
                mAlpha = score
            if (score >= beta)
                break
        }


        val flag = if (score <= alpha)
            GameState.Flag.UpperBound
        else if (score >= beta)
            GameState.Flag.LowerBound
        else GameState.Flag.Exact

        entry = GameState(score, flag, depth, node.bestMove)

        transpositionHash.put(node.hashCode(), entry)

        return score


    }

    override fun getMove(engine: Engine): Move {

        val timeStamp = System.currentTimeMillis()

        val parentNode = Node(
            engine.board, engine, engine.playerTurn, 0, true, Int.MIN_VALUE, Int.MIN_VALUE
        )
//        val node = doAlphaBeta(
//            parentNode, maxDepth, Int.MIN_VALUE, Int.MAX_VALUE
//        )

        val startTime = System.currentTimeMillis()

//        doAlphaBeta(parentNode, 5, Int.MIN_VALUE, Int.MAX_VALUE)

        var bestMove: Move? = null
        val job = CoroutineScope(Dispatchers.IO).launch {


            var d = 1
            while (System.currentTimeMillis() - startTime < 2000) {
                println("ID ${d} starts")
                doAlphaBeta(parentNode, d, Int.MIN_VALUE, Int.MAX_VALUE)
                bestMove = parentNode.bestMove
                d++
            }
        }

        sleep(2000)

        job.cancel()


//        transpositionHash
        transpositionHash.evictAll()


        println("It took ${System.currentTimeMillis() - timeStamp} and now map is ${bestMove}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}
