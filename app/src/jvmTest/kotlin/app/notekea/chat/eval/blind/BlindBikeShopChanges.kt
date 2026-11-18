package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Bike Shop vault, each one clear from the words and the notes. */
internal object BlindBikeShopChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to T-1038: call Lars when the pads arrive",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("T-1038", "call")
        ),
        AgentWideCase(
            task = "set the status of T-1052 to waiting-supplier",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("T-1052", "waiting-supplier")
        ),
        AgentWideCase(
            task = "tag every repair ticket that has an unpaid invoice with #unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("T-1036", "T-1047", "T-1049"),
            lacks = listOf("T-1031", "T-1041", "T-1043", "T-1052")
        ),
        AgentWideCase(
            task = "put in today's log: Velo Noord delivery came in, PO-231 complete",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-11", "PO-231")
        ),
        AgentWideCase(
            task = "the spokes order PO-233 came in today, mark it received",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("PO-233", "received")
        ),
        AgentWideCase(
            task = "make a note called Lars Bakker account with his tickets and what he owes me",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lars Bakker account", "T-1031", "T-1047", "47.50")
        ),
        AgentWideCase(
            task = "move the warranty policy into a new Policies folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Warranty policy", "Policies")
        ),
        AgentWideCase(
            task = "rename T-1053 so it has the customer's name in it like the other tickets",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("T-1053", "Sem Vos")
        ),
        AgentWideCase(
            task = "set Lars de Wit's preferred contact to whatsapp",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lars de Wit", "whatsapp")
        ),
        AgentWideCase(
            task = "the waterland loop moves to saturday the 14th, update the ride list",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Waterland")
        ),
        AgentWideCase(
            task = "tag all my customers who ride an e-bike with #ebike",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lars de Wit", "Pieter Mol"),
            lacks = listOf("Lars Bakker", "Femke", "Noor")
        ),
        AgentWideCase(
            task = "add a truing stand to the tools wishlist",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tools wishlist", "truing stand")
        ),
        AgentWideCase(
            task = "Lars Bakker just paid INV-304, mark it paid",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("INV-304", "paid")
        )
    )
}
