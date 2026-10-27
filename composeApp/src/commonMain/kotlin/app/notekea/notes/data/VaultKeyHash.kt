package app.notekea.notes.data

/**
 * The part of a settings key that names one vault: its path hashed with 64-bit FNV-1a, in hexadecimal. Computed here
 * so it is the same on every platform and run, and settings of one vault never reach another.
 */
object VaultKeyHash {
    private const val BYTE_MASK = 0xFFL
    private const val FNV_OFFSET_BASIS = -0x340d631b7bdddcdbL
    private const val FNV_PRIME = 0x100000001b3L
    private const val HEX_RADIX = 16

    fun of(
        vaultPath: String
    ): String = vaultPath.encodeToByteArray()
        .fold(initial = FNV_OFFSET_BASIS) { hash, byte -> (hash xor (byte.toLong() and BYTE_MASK)) * FNV_PRIME }
        .toULong()
        .toString(radix = HEX_RADIX)
}
