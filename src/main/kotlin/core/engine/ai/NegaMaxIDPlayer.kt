package core.engine.ai

import core.engine.Node
import core.engine.util.LruCache
import src.main.kotlin.core.engine.Engine
import src.main.kotlin.core.engine.Move
import java.lang.Integer.max
import java.lang.Integer.min


class NegaMaxIDPlayer(player: Int, val maxDepth: Int = 6) : Player(player, Type.AI) {

    data class GameState(val value: Int, val flag: Flag, val depth: Int) {
        enum class Flag { Exact, LowerBound, UpperBound }
    }

    val transpositionHash = LruCache<Int, GameState>(500)

    fun doAlphaBeta(node: Node, depth: Int, alpha: Int, beta: Int): Int {


        var score = Int.MIN_VALUE
        var value = Int.MIN_VALUE

        var mAlpha = alpha
        var mBeta = beta


        var entry = transpositionHash[node.hashCode()]
        if (entry != null) {

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
            return node.evaluateState()

        val moves = node.getMoves()


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

        entry = GameState(score, flag, depth)

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

        var d = 1
        while (d <= 6 && System.currentTimeMillis() - startTime < 1000) {
            println("ID ${d} starts")
            doAlphaBeta(parentNode, d, Int.MIN_VALUE, Int.MAX_VALUE)
            d++
        }

        transpositionHash.evictAll()


        println("It took ${System.currentTimeMillis() - timeStamp} and now map is ${transpositionHash.size()}")
//        println("node is $parentNode with value $node children ${parentNode.bestMove}")

        if (parentNode.bestMove == null)
            return engine.getPossibleMoves(engine.playerTurn).random()
        return parentNode.bestMove!!

        val moves = engine.getPossibleMoves(player)

        return moves.random()
    }


}
