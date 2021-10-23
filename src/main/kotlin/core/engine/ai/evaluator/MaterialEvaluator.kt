package core.engine.ai.evaluator

import core.engine.*
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextInt

val random = Random(System.currentTimeMillis())

class MaterialEvaluator(
    val weights: List<Int> = arrayListOf(
        3, -2, //moves
        2, -2, //cannons
        35, -40, //pawns
        5, -4, //shoot
        5, -4, //capture
        3, -3, //possible shots
        8, 6, // number pawn in attack vs defend
        2 // random
        , 6// singe move distance
    )
) : Evaluator {


    fun mutateAndCombine1(other: MaterialEvaluator): MaterialEvaluator {
        val newWeights = weights.mapIndexed { index, i ->
            (i + other.weights[index] + if (i < 0) random.nextInt(-4..0) else random.nextInt(0..4)) / 2
        }

        return MaterialEvaluator(newWeights)

    }


    fun mutateAndCombine2(other: MaterialEvaluator): MaterialEvaluator {
        val newWeights = weights.mapIndexed { index, i ->
            (i + other.weights[index] + random.nextInt(-4..4)) / 2
        }

        return MaterialEvaluator(newWeights)

    }


//    val features = arrayListOf(0, 1)


    override fun evaluateState(node: Node, player: Int, move: Move?): Int {

        val state = node.state
        node.calcMoves()

        val myMoves = if (node.player == player) node.myMoves else node.enemyMoves

        val enemyMoves = if (node.player == player) node.enemyMoves else node.myMoves

        if (myMoves.isEmpty() || state.isTownDead(player) || state.pawnCount(player) == 0) // total lost
            return -30000

        if (state.isTownDead((player.otherPlayer()))) // total win
            return 30000
        if (state.pawnCount(player.otherPlayer()) == 0 || enemyMoves.isEmpty())
            return 20000

        // losing state
        if (node.state.toStr() == node.engine.secondLastState?.toStr() || node.state.toStr() == node.engine.thirdLastState?.toStr())
            return -30000

        val shootCnt = myMoves.filter { it.type == Move.Type.Shoot }.size
        var eShootCnt = enemyMoves.filter { it.type == Move.Type.Shoot }.size
//        if (eShootCnt == 0)
//            eShootCnt = 1

        val captureCnt = myMoves.filter { it.type == Move.Type.Capture }.size
        var eCaptureCnt = enemyMoves.filter { it.type == Move.Type.Capture }.size

//        if (eCaptureCnt == 0)
//            eCaptureCnt = 1

        val myCannons = state.numberOfCannons(player)

        var enemyCannons = state.numberOfCannons(player.otherPlayer())

//        if (enemyCannons == 0)
//            enemyCannons = 1

        val possibleShots = state.numberOfPossibleShots(player)
        var enemyPossibleShots = state.numberOfPossibleShots(player.otherPlayer())

//        if (enemyPossibleShots == 0)
//            enemyPossibleShots = 1

        var ePawnCount = state.pawnCount(
            player.otherPlayer()
        )
//        if (ePawnCount == 0)
//            ePawnCount = 1

        val distanceToEnemy = if (weights[12] == 0) 0.0 else -1 * state.calcDistanceToEnemyTown(player)
        val distanceToTown = if (weights[13] == 0) 0.0 else -1 * state.calcDistanceToMyTown(player)

//        val numberOfAttackingPawns =  0//state.numberOfMyPawnsInIthStepOfEnemyTown(player,4)
//        val numberOfDeffendingPawns = 0//state.getHowCloseAreWeDefending(player)

        val features = arrayListOf(
            myMoves.size, enemyMoves.size,
            myCannons, enemyCannons,
            state.pawnCount(player), ePawnCount,
            shootCnt, eShootCnt,
            captureCnt, eCaptureCnt,
            possibleShots, enemyPossibleShots,
            distanceToEnemy.roundToInt(), distanceToTown.roundToInt(),
            random.nextInt(5),
            if (move != null && weights[15] != 0) state.calcMoveDistanceTowardEnemy(player, move).roundToInt() else 0
        )


//        val weights = arrayListOf(
//            3, -2,
//            4, -2,
//            20, -20,
//            5, -4,
//            5, -4,
//            6, -6,
//            2
//        )

        val res = features.reduceIndexed { index, acc, i -> acc + i * weights[index] }

//        println("distances are ${distanceToEnemy} $distanceToTown $res")
//        println("evaluation is ${res}")
//        println("Attack val ${numberOfAttackingPawns} ${numberOfDeffendingPawns}")


        return res
    }


//    fun evaluateStateZ(player: Int, state: Board): Int {
//        val myMoves = getPossibleMoves(player, state)
//
//        val enemyMoves = getPossibleMoves(player.otherPlayer(), state)
//
//        if (myMoves.isEmpty() || state.isTownDead(player) || state.pawnCount(player) == 0) // total lost
//            return -300
//
//        if (state.isTownDead((player.otherPlayer())) || state.pawnCount(player.otherPlayer()) == 0 || enemyMoves.isEmpty()) // total win
//            return 300
//
//        val shootCnt = myMoves.filter { it.type == Move.Type.Shoot }.size
//        var eShootCnt = enemyMoves.filter { it.type == Move.Type.Shoot }.size
//        if (eShootCnt == 0)
//            eShootCnt = 1
//
//        val captureCnt = myMoves.filter { it.type == Move.Type.Capture }.size
//        var eCaptureCnt = enemyMoves.filter { it.type == Move.Type.Capture }.size
//
//        if (eCaptureCnt == 0)
//            eCaptureCnt = 1
//
//        val myCannons = state.numberOfCannons(player)
//
//        var enemyCannons = state.numberOfCannons(player.otherPlayer())
//
//        if (enemyCannons == 0)
//            enemyCannons = 1
//
//        val possibleShots = state.numberOfPossibleShots(player)
//        var enemyPossibleShots = state.numberOfPossibleShots(player.otherPlayer())
//
//        if (enemyPossibleShots == 0)
//            enemyPossibleShots = 1
//
//        var result =
//            ((myMoves.size.toFloat() / enemyMoves.size) * 2 + 6 * (myCannons.toFloat() / enemyCannons) +
//                    100 * (state.pawnCount(player).toFloat() / state.pawnCount(player.otherPlayer())) +
//                    (6 * (shootCnt.toFloat() / eShootCnt))
//                    + 2 * (captureCnt.toFloat() / eCaptureCnt)
//                    + 4 * (possibleShots / enemyPossibleShots)
//                    + 1 * (random).nextInt(4)
////                    + 1 * (progressTowardEnemyTown)
//                    )
//
//        return result.roundToInt()
//
//
//    }

}