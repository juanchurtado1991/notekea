package com.testbuddy

/**
 * One of [count] parts of the suite, by turns: case 0 to part 0, case 1 to part 1… so each part gets cases of every
 * feature and the parts take about as long. Parts run in JVMs of their own, at the same time (`TestBuddyJUnit`
 * classes with a part each). The manual cases, and the cases [together] picks, stay in part 0.
 */
public fun Suite.shard(
    index: Int,
    count: Int,
    together: (AutomatedCase) -> Boolean = { false }
): Suite {
    require(count > 0 && index in 0 until count) { "Part $index of $count does not exist" }

    val shardManual = if (index == 0) manual else emptyList()
    val (kept, rest) = automated.partition(together)
    val first = if (index == 0) kept else emptyList()

    return withCases(
        automated = first + rest.filterIndexed { position, _ ->
            position % count == index
        },
        manual = shardManual
    )
}
