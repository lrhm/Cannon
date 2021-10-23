package core.engine


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

