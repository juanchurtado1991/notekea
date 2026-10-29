package app.notekea.selftest

import com.testbuddy.TestBuddyJUnit

/** Every automated case of Notekea's Self-Test, as a test named after its ID, in CI on the three systems. */
class NotekeaSuiteTest : TestBuddyJUnit(suite = NotekeaSuite.suite)
