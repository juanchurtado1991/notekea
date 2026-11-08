package app.notekea.models.domain

/** [HardwareProbe] that reports [hardware]. */
class FakeHardwareProbe(
    var hardware: Hardware = Hardware(memoryGb = 16, videoMemoryGb = null)
) : HardwareProbe {
    override suspend fun probe(): Hardware = hardware
}
