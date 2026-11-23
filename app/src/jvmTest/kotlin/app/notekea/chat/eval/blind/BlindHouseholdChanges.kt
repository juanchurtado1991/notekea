package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHouseholdChanges {
    val all = listOf(
        change(
            task = "add 'buy rock salt' to winter prep",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Winter Prep", "rock salt")
        ),
        change(
            task = "mark the water bill as paid",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Bills and Subscriptions", "Water")
        ),
        change(
            task = "set the status of the 2025 tax return to assessed",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Tax Return 2025", "assessed")
        ),
        change(
            task = "tag our insurance and loan notes that renew before april 2027 " +
                "with #renewal-soon",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Car Insurance", "Mortgage", "renewal-soon"),
            lacks = listOf("Home Insurance Policy")
        ),
        change(
            task = "add to the garage sale log that the next sale is planned for May 2027",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            holds = listOf("Garage Sale Log", "May 2027")
        ),
        change(
            task = "make a note called Reno Contacts with Daniel's and our insurance broker's " +
                "phone and email",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("Reno Contacts", "416-555-0161", "416-555-0133")
        ),
        change(
            task = "move the 2025 tax return into an Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Tax Return 2025", "Archive")
        ),
        change(
            task = "rename winter prep to Winter Prep 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Winter Prep 2026")
        ),
        change(
            task = "add 'book a call with the accountant before Feb' to the tax documents " +
                "checklist",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Tax Documents 2026", "accountant")
        ),
        change(
            task = "update lisbon saved to 4,480 in savings goals",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Savings Goals", "4,480")
        ),
        change(
            task = "write a note called Debts with what we still owe on the mortgage and the car " +
                "loan, and the total",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "write_note", "propose"),
            holds = listOf("Debts", "548,912.37", "14,236.55"),
            computed = listOf("563148.92")
        ),
        change(
            task = "add the renovation tag to appliance orders",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Appliance Orders", "renovation")
        )
    )
}
