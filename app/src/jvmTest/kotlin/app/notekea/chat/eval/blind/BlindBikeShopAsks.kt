package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Bike Shop vault cannot settle, then look-alikes whose words settle them. */
internal object BlindBikeShopAsks {
    val all = listOf(
        ask(
            task = "change Lars's phone number to 06-28374615",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Bakker", "de Wit")
        ),
        ask(
            task = "add a task to chase Lars about his unpaid invoice",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Bakker", "de Wit")
        ),
        ask(
            task = "push Femke's appointment back an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("fitting", "brake check", "winter service")
        ),
        ask(
            task = "add a line to the price list: prices go up 5% on 1 January",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("parts", "rental")
        ),
        ask(
            task = "add 'helmet' to the checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("rental", "ride")
        ),
        ask(
            task = "the Velo Noord order came in, mark it received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("PO-231", "PO-234")
        ),
        ask(
            task = "move the group ride to the saturday before",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Waterland", "Gooi", "Christmas")
        ),
        ask(
            task = "mark the e-bike repair ticket as done",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Mol", "de Wit")
        ),
        change(
            task = "rental price list: e-bike is 34 a day now",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("34")
        ),
        change(
            task = "move Femke's brake check to 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Femke Jansen", "17:00")
        ),
        change(
            task = "both Velo Noord orders came in today, mark them received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("PO-231", "PO-234")
        ),
        change(
            task = "add a frame pump to my ride kit checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("pump"),
            lacks = listOf("handover")
        ),
        change(
            task = "add a task to chase the Lars with the cargo bike about his unpaid invoice",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Lars de Wit")
        )
    )
}
