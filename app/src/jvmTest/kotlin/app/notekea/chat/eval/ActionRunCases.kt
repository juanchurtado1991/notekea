package app.notekea.chat.eval

import java.io.File

/** A request for [ActionRunChatModelTest]: what is asked, the note open if any, and what the vault shows after. */
class ActionRunCase(
    val request: String,
    val open: String? = null,
    val check: (vault: File) -> String?
)

/** The requests run end to end, each with what the files must hold once it is done. */
object ActionRunCases {
    private const val DUE = "2026-11-20"
    private const val FLOUR = "Buy flour for the arepas"
    private const val BREAD = "Créame notas a partir de este texto:\n\n" +
        "El pan nació en Mesopotamia hace unos diez mil años, cuando se molían granos de trigo y cebada entre piedras " +
        "y la masa se cocía sobre piedras calientes. Era un pan plano, sin levadura.\n\n" +
        "Los egipcios descubrieron la fermentación hacia el año 3000 antes de Cristo: una masa olvidada al sol creció " +
        "y dio un pan más ligero. Construyeron los primeros hornos de barro y pagaban a los obreros de las pirámides " +
        "con pan y cerveza.\n\n" +
        "En Roma el pan fue un asunto del Estado. Había más de trescientas panaderías en la ciudad, los panaderos se " +
        "organizaban en un gremio y el gobierno repartía grano gratis a los ciudadanos pobres, la annona.\n\n" +
        "En la Edad Media el color del pan marcaba la clase social: el pan blanco era para los nobles y el pan negro, " +
        "de centeno, para los campesinos. Los molinos de agua y de viento cambiaron la forma de moler.\n\n" +
        "Hoy el pan se hace en fábricas con levadura comercial, pero la masa madre ha vuelto: muchos panaderos " +
        "dejan fermentar la masa más de doce horas para un pan con más sabor y que se conserva mejor."

    val all = listOf(
        ActionRunCase(request = "create a note Groceries in Recipes with a shopping list for arepas") { vault ->
            val text = File(vault, "Recipes/Groceries.md").takeIf { it.isFile }?.readText()

            when {
                text == null -> "Recipes/Groceries.md was not written"
                "- " !in text && "1. " !in text -> "the shopping list has no list"
                else -> null
            }
        },
        ActionRunCase(request = "write a summary of my book notes in a new note called Books summary") { vault ->
            val text = File(vault, "Books/Books summary.md").takeIf { it.isFile }?.readText()
            val words = text?.split(Regex(pattern = "\\s+"))?.size ?: 0

            when {
                text == null -> "Books/Books summary.md was not written"
                "[[" !in text -> "the summary links none of its books"
                words !in 80..800 -> "the summary of 6 notes has $words words"
                else -> null
            }
        },
        ActionRunCase(request = "write a note Focus vs habits comparing Deep Work and Atomic Habits in a table") { vault ->
            val text = vault
                .walkTopDown()
                .firstOrNull {
                    it.name == "Focus vs habits.md"
                }
                ?.readText()

            if (text == null) {
                "Focus vs habits.md was not written"
            } else if ("|" !in text) {
                "no table"
            } else {
                null
            }
        },
        ActionRunCase(request = "tag every note in Recipes with food") { vault ->
            val untagged = File(vault, "Recipes")
                .listFiles()
                .orEmpty()
                .filter {
                    "food" !in it.readText()
                }

            if (untagged.isEmpty()) null else "not tagged: ${untagged.map { it.name }}"
        },
        ActionRunCase(request = "set the rating of Sapiens to 4") { vault ->
            if ("rating: 4" in File(vault, "Books/Sapiens.md").readText()) null else "Sapiens has no rating 4"
        },
        ActionRunCase(request = "add a task to buy tomato seeds in Garden plan due $DUE") { vault ->
            val text = File(vault, "Projects/Garden plan.md").readText()
            val isDated = Regex(pattern = "- \\[ ] .*tomato.*$DUE").containsMatchIn(input = text)
            if (isDated) null else "no dated task"
        },
        ActionRunCase(request = "add 'call the plumber' to today's note") { vault ->
            val found = File(vault, "Journal/Daily")
                .listFiles()
                .orEmpty()
                .any {
                    "plumber" in it.readText()
                }

            if (found) null else "today's note does not say it"
        },
        ActionRunCase(request = "mark the task '$FLOUR' in Tasks as done") { vault ->
            if ("- [x] $FLOUR" in File(vault, "Guides/Tasks.md").readText()) null else "the task is still open"
        },
        ActionRunCase(request = "rename Pancakes to Banana pancakes") { vault ->
            if (File(vault, "Recipes/Banana pancakes.md").isFile) null else "Pancakes was not renamed"
        },
        ActionRunCase(request = "create a folder Archive and move Retro October into it") { vault ->
            if (File(vault, "Archive/Retro October.md").isFile) null else "Retro October is not in Archive"
        },
        ActionRunCase(request = "link Arepas to Weekly menu") { vault ->
            if ("[[Weekly menu]]" in File(vault, "Recipes/Arepas.md").readText()) null else "Arepas does not link it"
        },
        ActionRunCase(
            request = "make a new note Lisbon summary that summarizes this note",
            open = "Trip to Lisbon"
        ) {
            if (it.walkTopDown().any { file -> file.name == "Lisbon summary.md" }) null else "no summary note"
        },
        ActionRunCase(request = BREAD) { vault ->
            val folder = vault.walkTopDown().firstOrNull {
                it.isDirectory &&
                    it.listFiles()?.size?.let { n -> n >= 3 } == true &&
                    it.name !in OLD_FOLDERS
            }

            when {
                folder == null -> "no folder of notes was made"
                folder.listFiles().orEmpty().none { "[[" in it.readText() } -> "the notes are not linked"
                else -> null
            }
        }
    )

    private val OLD_FOLDERS =
        setOf("Sample Vault", "Books", "Guides", "Ideas", "Journal", "Daily", "Meetings", "Projects", "Recipes", "Templates", "Assets", ".obsidian")
}
