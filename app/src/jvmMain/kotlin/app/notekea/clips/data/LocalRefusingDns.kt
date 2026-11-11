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
        private const val UNIQUE_LOCAL_MASK = 0xfe

        fun isLocal(address: InetAddress): Boolean {
            val isUniqueLocal = address is Inet6Address &&
                (address.address[0].toInt() and UNIQUE_LOCAL_MASK) == UNIQUE_LOCAL

            return address.isLoopbackAddress ||
                address.isSiteLocalAddress ||
                address.isLinkLocalAddress ||
                address.isAnyLocalAddress ||
                address.isMulticastAddress ||
                isUniqueLocal
        }
    }
}
