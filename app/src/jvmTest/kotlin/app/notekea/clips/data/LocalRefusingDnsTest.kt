package app.notekea.clips.data

import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A page is never fetched from an address that is no public site (the code review of 2026-11-13): a provider's
 * shared space, "this network", the reserved top and an IPv4 address written inside an IPv6 one passed as public.
 */
class LocalRefusingDnsTest {
    @Test
    fun `reserved ranges and private addresses inside IPv6 are local, a public address is not`() {
        val addresses = listOf(
            "100.64.0.1",
            "100.127.255.254",
            "0.1.2.3",
            "198.18.0.1",
            "240.0.0.1",
            "::ffff:10.0.0.1",
            "::ffff:127.0.0.1",
            "10.0.0.1",
            "100.63.0.1",
            "93.184.216.34",
            "::ffff:93.184.216.34",
            "2606:2800:220:1:248:1893:25c8:1946"
        )

        assertEquals(
            expected = List(size = LOCAL) { true } + List(size = PUBLIC) { false },
            actual = addresses.map { LocalRefusingDns.isLocal(address = InetAddress.getByName(it)) }
        )
    }

    private companion object {
        const val LOCAL = 8
        const val PUBLIC = 4
    }
}
