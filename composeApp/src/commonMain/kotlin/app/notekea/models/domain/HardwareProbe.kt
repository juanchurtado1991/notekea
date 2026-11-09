package app.notekea.models.domain

/** Reads the memory and video memory of this machine. */
interface HardwareProbe {
    suspend fun probe(): Hardware
}
