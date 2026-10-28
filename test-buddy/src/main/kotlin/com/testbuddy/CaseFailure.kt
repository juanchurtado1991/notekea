package com.testbuddy

/** A case's check that did not hold, with a message a tester can read ("expected to see …, it did not appear"). */
public class CaseFailure(message: String) : AssertionError(message)
