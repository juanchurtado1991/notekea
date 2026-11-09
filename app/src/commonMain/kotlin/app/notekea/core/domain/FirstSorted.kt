package app.notekea.core.domain

/**
 * The first [n] elements as `sortedWith(comparator).take(n)` gives them, equal elements in their order here included,
 * without sorting the rest: a search that shows ten results out of thousands keeps only ten in order.
 */
fun <T> Iterable<T>.firstSorted(
    n: Int,
    comparator: Comparator<in T>
): List<T> {
    val first = ArrayList<T>(n.coerceAtLeast(minimumValue = 0))
    if (n <= 0) return first
    forEach { element ->
        val isFull = first.size == n
        if (isFull && comparator.compare(element, first.last()) >= 0) return@forEach
        // After every element that is not greater, so equal elements keep their order, as a stable sort does.
        var low = 0
        var high = first.size
        while (low < high) {
            val middle = (low + high) / 2
            if (comparator.compare(first[middle], element) <= 0) low = middle + 1 else high = middle
        }
        first.add(index = low, element = element)
        if (first.size > n) first.removeAt(index = first.lastIndex)
    }
    return first
}
