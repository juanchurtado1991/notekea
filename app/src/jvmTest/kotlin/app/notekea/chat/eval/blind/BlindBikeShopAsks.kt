package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Bike Shop vault cannot settle, then look-alikes whose words settle them. */
internal object BlindBikeShopAsks {
    val all = listOf(
        AgentWideCase(
            task = "change Lars's phone number to 06-28374615",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Bakker", "de Wit")
        ),
        AgentWideCase(
            task = "add a task to chase Lars about his unpaid invoice",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Bakker", "de Wit")
        ),
        AgentWideCase(
            task = "push Femke's appointment back an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("fitting", "brake check", "winter service")
        ),
        AgentWideCase(
            task = "add a line to the price list: prices go up 5% on 1 January",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("parts", "rental")
        ),
        AgentWideCase(
            task = "add 'helmet' to the checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("rental", "ride")
        ),
        AgentWideCase(
            task = "the Velo Noord order came in, mark it received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("PO-231", "PO-234")
        ),
        AgentWideCase(
            task = "move the group ride to the saturday before",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Waterland", "Gooi", "Christmas")
        ),
        AgentWideCase(
            task = "mark the e-bike repair ticket as done",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Mol", "de Wit")
        ),
        AgentWideCase(
            task = "rental price list: e-bike is 34 a day now",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("34")
        ),
        AgentWideCase(
            task = "move Femke's brake check to 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Femke Jansen", "17:00")
        ),
        AgentWideCase(
            task = "both Velo Noord orders came in today, mark them received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("PO-231", "PO-234")
        ),
        AgentWideCase(
            task = "add a frame pump to my ride kit checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("pump"),
            lacks = listOf("handover")
        ),
        AgentWideCase(
            task = "add a task to chase the Lars with the cargo bike about his unpaid invoice",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lars de Wit")
        )
    )
}
