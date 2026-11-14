package app.notekea.clips.data

import okhttp3.Dns
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * The lookup OkHttp connects with, refusing a host that resolves to this computer or its local network, so an
 * address checked once cannot turn local when it is looked up again to connect (DNS rebinding).
 */
internal class LocalRefusingDns(private val system: Dns = Dns.SYSTEM) : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val addresses = system.lookup(hostname)
        if (addresses.any { isLocal(address = it) }) throw Refused(host = hostname)

        return addresses
    }

    /** A host refused for being local: an [UnknownHostException], so OkHttp gives up on it at once. */
    class Refused(host: String) : UnknownHostException("$host is on this computer or its network")

    companion object {
        private const val UNIQUE_LOCAL = 0xfc
        private const val BYTE = 0xff
        private const val IPV4_BYTES = 4
        private const val MAPPED_ZEROS = 10
        private const val SHARED_FIRST = 100
        private val SHARED_SECOND = 64..127
        private const val BENCHMARK_FIRST = 198
        private val BENCHMARK_SECOND = 18..19
        private const val RESERVED_FIRST = 240
        private const val UNIQUE_LOCAL_MASK = 0xfe

        fun isLocal(address: InetAddress): Boolean {
            val isUniqueLocal = address is Inet6Address &&
                (address.address[0].toInt() and UNIQUE_LOCAL_MASK) == UNIQUE_LOCAL

            return address.isLoopbackAddress ||
                address.isSiteLocalAddress ||
                address.isLinkLocalAddress ||
                address.isAnyLocalAddress ||
                address.isMulticastAddress ||
                isUniqueLocal ||
                isReserved(address = address)
        }

        // Ranges Java does not call local and no public page lives in: a provider's shared space (100.64/10),
        // "this network" (0/8), benchmarks (198.18/15), the reserved top (240/4) and IPv4 written inside IPv6.
        private fun isReserved(address: InetAddress): Boolean {
            val bytes = address.address.map {
                it.toInt() and BYTE
            }

            if (address is Inet6Address) {
                val isMapped =
                    bytes.take(n = MAPPED_ZEROS).all { it == 0 } &&
                        bytes[MAPPED_ZEROS] == BYTE &&
                        bytes[MAPPED_ZEROS + 1] == BYTE

                val inner = bytes.takeLast(n = IPV4_BYTES)
                val within = ByteArray(size = IPV4_BYTES) { inner[it].toByte() }

                return isMapped && isLocal(address = InetAddress.getByAddress(within))
            }

            val (first, second) = bytes

            return first == 0 ||
                (first == SHARED_FIRST && second in SHARED_SECOND) ||
                (first == BENCHMARK_FIRST && second in BENCHMARK_SECOND) ||
                first >= RESERVED_FIRST
        }
    }
}
