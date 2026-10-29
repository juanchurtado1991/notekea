package com.testbuddy

/**
 * One of [count] parts of the suite, by turns: case 0 to part 0, case 1 to part 1… so each part gets cases of every
 * feature and the parts take about as long. Parts run in JVMs of their own, at the same time (`TestBuddyJUnit`
 * classes with a part each). The manual cases stay in part 0.
 */
public fun Suite.shard(
    index: Int,
    count: Int
): Suite {
    require(count > 0 && index in 0 until count) { "Part $index of $count does not exist" }
    return Suite(
        appName = appName,
        version = version,
        automated = automated.filterIndexed { position, _ -> position % count == index },
        manual = if (index == 0) manual else emptyList(),
        window = window,
        dataSources = dataSources,
        backUp = backUp,
        content = content
    )
}
