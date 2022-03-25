package minesweeper

import kotlin.random.Random

class Command(val name: String, val position: Pair<Int, Int>)

fun getCommand(): Command {
    print("Set/unset mines marks or claim a cell as free:")
    val elements = readLine()!!.split(" ")
    return Command(elements[2], Pair(elements[0].toInt() - 1, elements[1].toInt() - 1))
}

fun run(width: Int, height: Int) {

    val marks = mutableSetOf<Pair<Int, Int>>()
    val openCells = mutableSetOf<Pair<Int, Int>>()

    print("How many mines do you want on the field?")
    val mineCount = readLine()!!.toInt()

    val zeroField = createZeroField(width, height)
    printField(zeroField, marks, openCells)

    val field: Array<Array<Int>>
    while (true) {

        val command = getCommand()

        when (command.name) {

            "mine" -> {
                markSetUnset(command.position.first, command.position.second, marks)
                printField(zeroField, marks, openCells)
            }

            "free" -> {
                field = createField(width, height, mineCount, command.position.first, command.position.second)
                fillCounts(field)
                openCell(command.position, field, openCells, marks)
                break
            }
        }
    }

    while (true) {

        printField(field, marks, openCells)

        val command = getCommand()
        val x = command.position.first
        val y = command.position.second

        if (field[y][x] > 0 && openCells.contains(Pair(x, y))) {
            println("There is a number here!")
            continue
        }

        when (command.name) {

            "mine" -> {
                markSetUnset(x, y, marks)
            }

            "free" -> {
                when {
                    field[y][x] == -1 -> {
                        printAllOpened(field, openCells)
                        println("You stepped on a mine and failed!")
                        return
                    }

                    field[y][x] >= 0 -> {
                        openCell(Pair(x, y), field, openCells, marks)
                    }
                }
            }
        }

        if (checkForGameOver(field, marks)) {
            printField(field, marks, openCells)
            println("Congratulations! You found all the mines!")
            return
        }
    }
}

fun printAllOpened(field: Array<Array<Int>>, openCells: Set<Pair<Int, Int>>) {

    print(" │")
    for (i in 1..field[0].size) print(i)
    println("│")
    println("—│${"—".repeat(field[0].size)}│")

    for (y in 0..field.lastIndex) {
        print("${y + 1}|")
        for (x in 0..field[y].lastIndex) {
            val cellValue = field[y][x]
            print(
                when {
                    cellValue == -1 -> "X"
                    !openCells.contains(Pair(x, y)) -> "."
                    cellValue == 0 -> "/"
                    else -> cellValue.toString()
                }
            )
        }
        println("│")
    }
    println("—│${"—".repeat(field[0].size)}│")
}

fun getNeighbors(centralCell: Pair<Int, Int>, xBound: Int, yBound: Int): List<Pair<Int, Int>> {

    val x = centralCell.first
    val y = centralCell.second

    return listOf(
        Pair(x, y - 1),
        Pair(x + 1, y - 1),
        Pair(x + 1, y),
        Pair(x + 1, y + 1),
        Pair(x, y + 1),
        Pair(x - 1, y + 1),
        Pair(x - 1, y),
        Pair(x - 1, y - 1)
    )
        .filter { coords ->
            coords.first >= 0
                    && coords.second >= 0
                    && coords.first <= xBound
                    && coords.second <= yBound
        }
}

fun openCell(position: Pair<Int, Int>, field: Array<Array<Int>>, openCells: MutableSet<Pair<Int, Int>>, marks: MutableSet<Pair<Int, Int>>) {

    if (field[position.second][position.first] == -1) return

    marks.remove(position)
    openCells.add(position)

    when {

        field[position.second][position.first] > 0  -> return

        field[position.second][position.first] == 0  -> {

            val neighbors = getNeighbors(position, field[0].lastIndex, field.lastIndex)
                .filter { cell -> !openCells.contains(cell) }


            if (neighbors.isEmpty()) return

            for (cell in neighbors) {

                val xc = cell.first
                val yc = cell.second

                if (field[yc][xc] >= 0) {
                    openCell(Pair(xc, yc), field, openCells, marks)
                }
            }
        }
    }

}

fun checkForGameOver(field: Array<Array<Int>>, marks: Set<Pair<Int, Int>>): Boolean {

    var matchCount = 0
    var mineCount = 0

    for (y in 0..field.lastIndex) {
        for (x in 0..field[y].lastIndex) {
            if (field[y][x] == -1) {
                mineCount++
                if (marks.contains(Pair(x, y)))
                    matchCount++
            }
        }
    }

    return mineCount == marks.size && matchCount == mineCount
}

fun printField(field: Array<Array<Int>>, marks: Set<Pair<Int, Int>>, openCells: Set<Pair<Int, Int>>) {

    print(" │")
    for (i in 1..field[0].size) print(i)
    println("│")
    println("—│${"—".repeat(field[0].size)}│")

    for (y in 0..field.lastIndex) {
        print("${y + 1}|")
        for (x in 0..field[y].lastIndex) {
            val cellValue = field[y][x]
            print(
                when {
                    marks.contains(Pair(x, y)) -> "*"
                    !openCells.contains(Pair(x, y)) -> "."
                    cellValue == 0 -> "/"
                    cellValue == -1 -> "."
                    else -> cellValue.toString()
                }
            )
        }
        println("│")
    }
    println("—│${"—".repeat(field[0].size)}│")
}

fun markSetUnset(x: Int, y: Int, marks: MutableSet<Pair<Int, Int>>) {
    val markPosition = Pair(x, y)
    if (marks.contains(markPosition)) {
        marks.remove(markPosition)
    } else {
        marks.add(markPosition)
    }
}

fun fillCounts(field: Array<Array<Int>>) {
    for (y in 0..field.lastIndex) {
        for (x in 0..field[y].lastIndex) {
            if (field[y][x] == -1) continue
            field[y][x] = minesInNeighbors(x, y, field)
        }
    }
}

fun minesInNeighbors(x: Int, y: Int, field: Array<Array<Int>>): Int {

    var count = 0

    val neighbors = listOf(
        Pair(x, y - 1),
        Pair(x + 1, y - 1),
        Pair(x + 1, y),
        Pair(x + 1, y + 1),
        Pair(x, y + 1),
        Pair(x - 1, y + 1),
        Pair(x - 1, y),
        Pair(x - 1, y - 1)
    )

    val width = field[0].size
    val height = field.size

    for (idx in 0..neighbors.lastIndex) {
        val xn = neighbors[idx].first
        val yn = neighbors[idx].second
        if (xn < 0 || xn >= width || yn < 0 || yn >= height) continue
        if (field[yn][xn] == -1) count++
    }

    return count
}

fun createZeroField(width: Int, height: Int): Array<Array<Int>> {
    return Array(height) { Array(width) { 0 } }
}

fun createField(width: Int, height: Int, mineCount: Int, beginX: Int, beginY: Int): Array<Array<Int>> {

    val set = mutableSetOf<Int>()
    while (set.size < mineCount) {
        val number = Random.nextInt(0, width * height)
        if (number % width == beginX && number / width == beginY) continue
        set.add(number)
    }

    val field = Array(height) { Array(width) { 0 } }
    for (element in set) field[element / width][element % width] = -1

    return field
}

fun main() {

    run(9, 9)
}
