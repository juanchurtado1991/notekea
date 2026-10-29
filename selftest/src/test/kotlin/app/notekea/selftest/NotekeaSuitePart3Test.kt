package app.notekea.selftest

import com.testbuddy.TestBuddyJUnit
import com.testbuddy.shard

/** Part 3 of the automated cases of Notekea's Self-Test; the parts run at the same time, each in its own JVM. */
class NotekeaSuitePart3Test : TestBuddyJUnit(
    suite = NotekeaSuite.suite.shard(index = 2, count = NotekeaSuiteParts.COUNT)
)
