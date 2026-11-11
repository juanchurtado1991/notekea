package app.notekea.autotest

import com.testbuddy.TestBuddyJUnit
import com.testbuddy.shard

/** Part 4 of the automated cases of Notekea's Auto-Test; the parts run at the same time, each in its own JVM. */
class NotekeaSuitePart4Test : TestBuddyJUnit(
    suite = NotekeaSuite.selected().shard(
        index = 3,
        count = NotekeaSuiteParts.COUNT
    )
)
