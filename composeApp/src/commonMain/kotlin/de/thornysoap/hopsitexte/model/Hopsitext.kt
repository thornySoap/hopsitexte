package de.thornysoap.hopsitexte.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Class containing all business logic for the application.
 */
@Stable
class Hopsitext {
    companion object {
        const val DEFAULT_BELA_INDEX = 0
        const val DEFAULT_AMIRA_INDEX = 1
        val DefaultJumpValues = (('a'..'z') + listOf('ä', 'ö', 'ü', 'ß') zip 1..30).toMap()

        val Saver = Saver<Hopsitext, String>(
            save = { it.save() },
            restore = { Hopsitext().apply { load(it) } },
        )
    }

    // Can potentially be configured
    var jumpValues: Map<Char, Int> = DefaultJumpValues
    var initialBelaIndex: Int = DEFAULT_BELA_INDEX
    var initialAmiraIndex: Int = DEFAULT_AMIRA_INDEX

    val lines: SnapshotStateList<Line> = mutableStateListOf(Line(""))
    var information: Information by mutableStateOf(Information())
        private set

    private var breakCalculationAt: Int? = null
    var calculationJob: Job? by mutableStateOf(null)
        private set

    /**
     * Add [textLines] (including current line) to text at [lineI].
     */
    fun addNewLines(lineI: Int, textLines: List<String>) {
        lines[lineI] = lines[lineI].copy(text = textLines.last())
        // Multiple new lines can be inserted by pasting text from clipboard
        lines.addAll(
            index = lineI,
            elements = textLines.dropLast(1).map { Line(it) },
        )
    }

    /**
     * Remove line break before line with index [lineI].
     */
    fun deleteLineBreak(lineI: Int) {
        val before = lines[lineI - 1]
        val current = lines[lineI]
        lines[lineI] = current.copy(
            text = before.text + current.text,
            belaInitial = before.belaInitial,
            amiraInitial = before.amiraInitial,
        )
        lines.removeAt(lineI - 1)
    }

    /**
     * Performs the calculation of Bela's and Amira's respective jumps.
     * @param fromLine starting from the specified line
     * @param minimumToLines calculate until minimum reached. After that break if
     * newly calculated jumps of one line match it's previous ones
     */
    suspend fun performHopsiCalculation(
        fromLine: Int,
        minimumToLines: Int?,
    ) {
        coroutineScope {
            calculationJob?.let {
                // Cannot forcefully stop calculation as edit might have happened on a not yet updated line
                breakCalculationAt = fromLine
                it.join()
                breakCalculationAt = null
            }

            calculationJob = launch(Dispatchers.Default) {
                val lines = lines.toMutableList()
                val information = information

                if (fromLine >= lines.size)
                    return@launch

                val relevantLinesWithIndex = lines.asSequence().withIndex().drop(fromLine)

                // The line containing the "fatal character". Needn't be the first one containing a union jump
                var unionSourceLine: Int? = null
                var belaTotalJumps = information.belaTotalJumps
                var amiraTotalJumps = information.amiraTotalJumps
                // Algorithm of the 2nd Junioraufgabe
                var charIndex = 0
                var nextBelaI = initialBelaIndex.takeIf { fromLine == 0 }
                    ?: lines[fromLine].belaInitial
                    ?: throw IllegalStateException("No belaInitial at line $fromLine")
                var nextAmiraI = initialAmiraIndex.takeIf { fromLine == 0 }
                    ?: lines[fromLine].amiraInitial
                    ?: throw IllegalStateException("No amiraInitial at line $fromLine")

                calc@ for ((lineI, line) in relevantLinesWithIndex) {
                    // Initial character indices relative to line beginning
                    val belaInitial = nextBelaI - charIndex
                    val amiraInitial = nextAmiraI - charIndex

                    var lineSymbolI = 0
                    val belaJumps = mutableSetOf<Int>()
                    val amiraJumps = mutableSetOf<Int>()
                    val unionJumps = mutableSetOf<Int>()

                    for (c in line.text.lowercase()) {
                        // In case of cancellation, e.g. if requesting new calculation
                        if (breakCalculationAt != null && lineI > breakCalculationAt!!)
                            break@calc

                        if (c in jumpValues) {
                            if (nextBelaI == charIndex && nextAmiraI == charIndex)
                                unionJumps.add(lineSymbolI)
                            else if (nextBelaI == charIndex)
                                belaJumps.add(lineSymbolI)
                            else if (nextAmiraI == charIndex)
                                amiraJumps.add(lineSymbolI)

                            if (charIndex == nextBelaI)
                                nextBelaI += jumpValues[c]!!
                            if (charIndex == nextAmiraI)
                                nextAmiraI += jumpValues[c]!!

                            if (nextBelaI == nextAmiraI && (unionSourceLine == null || lineI < unionSourceLine))
                                unionSourceLine = lineI

                            charIndex++
                        }

                        // Need to keep track of simple symbol index for display instructions
                        lineSymbolI++
                    }

                    // Assuming only lines included in `minimumLines` have changed
                    if (minimumToLines != null && lineI >= minimumToLines &&
                        belaInitial == line.belaInitial && amiraInitial == line.amiraInitial
                    ) break@calc

                    belaTotalJumps += belaJumps.size - line.belaJumps.size +
                            unionJumps.size - line.unionJumps.size
                    amiraTotalJumps += amiraJumps.size - line.amiraJumps.size +
                            unionJumps.size - line.unionJumps.size

                    lines[lineI] = line.copy(
                        belaInitial = belaInitial,
                        amiraInitial = amiraInitial,
                        belaJumps = belaJumps,
                        amiraJumps = amiraJumps,
                        unionJumps = unionJumps,
                        unionSource = false,
                    )
                }

                if (unionSourceLine == null &&
                    information.unionSourceLine != null && information.unionSourceLine < fromLine
                ) {
                    unionSourceLine = information.unionSourceLine
                }

                unionSourceLine?.let {
                    lines[it] = lines[it].copy(unionSource = true)
                }

                // Write operation to `MutableState` on main thread
                withContext(Dispatchers.Main) {
                    this@Hopsitext.lines.clear()
                    this@Hopsitext.lines.addAll(lines)

                    this@Hopsitext.information = information.copy(
                        completelyAnalyzed = false,
                        unionSourceLine = unionSourceLine,
                        belaTotalJumps = belaTotalJumps,
                        amiraTotalJumps = amiraTotalJumps,
                    )
                }
            }

            calculationJob?.invokeOnCompletion {
                if (breakCalculationAt == null)
                    calculationJob = null
            }
        }
    }

    /**
     * Checks all [lines] and updates [information].
     */
    suspend fun performCompleteAnalysis() {
        withContext(Dispatchers.Default) {
            var firstUnionLine: Int? = null
            var belaTotalJumps = 0
            var amiraTotalJumps = 0
            var totalCharacters = 0

            for ((lineI, line) in lines.withIndex()) {
                if (!isActive) return@withContext

                if (firstUnionLine == null && line.unionJumps.isNotEmpty())
                    firstUnionLine = lineI
                belaTotalJumps += line.belaJumps.size + line.unionJumps.size
                amiraTotalJumps += line.amiraJumps.size + line.unionJumps.size
                totalCharacters += line.text.count { it in jumpValues }
            }

            withContext(Dispatchers.Main) {
                information = Information(
                    completelyAnalyzed = true,
                    unionSourceLine = firstUnionLine,
                    belaTotalJumps = belaTotalJumps,
                    amiraTotalJumps = amiraTotalJumps,
                    totalCharacters = totalCharacters,
                )
            }
        }
    }

    fun save(): String =
        lines.joinToString(separator = "\n") { it.text }

    fun load(text: String) {
        lines.clear()
        lines.addAll(
            // Windows CRLF line breaks :/
            text.split(Regex("\\r?\\n"))
                .mapTo(mutableListOf()) { Line(it) },
        )
    }

    /**
     * A line of the text. Mostly one line corresponds to one paragraph.
     *
     * The [belaInitial] and [amiraInitial] indices are [jumpValues] aware and for internal
     * functioning, while [belaJumps], [amiraJumps] and [unionJumps] are the simple unaware
     * symbol indices of [text] used for display.
     */
    @Stable
    data class Line(
        val text: String,
        internal val belaInitial: Int? = null,
        internal val amiraInitial: Int? = null,
        val belaJumps: Set<Int> = setOf(),
        val amiraJumps: Set<Int> = setOf(),
        val unionJumps: Set<Int> = setOf(),
        val unionSource: Boolean = false,
        val identifier: Any = Random.nextInt(),
    )

    @Immutable
    data class Information(
        val completelyAnalyzed: Boolean = false,
        val unionSourceLine: Int? = null,
        val belaTotalJumps: Int = 0,
        val amiraTotalJumps: Int = 0,
        val totalCharacters: Int = 0,
    )
}
