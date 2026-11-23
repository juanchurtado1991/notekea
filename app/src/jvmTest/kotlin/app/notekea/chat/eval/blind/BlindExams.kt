package app.notekea.chat.eval.blind

/**
 * The nine exams written blind on 2026-11-17 (`docs/eval-blind.md`), each a vault of one invented person and the
 * tasks a person like them would type. Never tuned on: they are measured before a change to the loop and after it,
 * and their failures are read only by kind.
 */
internal object BlindExams {
    val all = listOf(
        // A fintech team's tech lead.
        exam(
            id = "tech-lead",
            vault = "Blind Tech Lead",
            BlindTechLeadAnswers.all,
            BlindTechLeadChanges.all,
            BlindTechLeadAsks.all
        ),
        // A second-year marine biology PhD student.
        exam(
            id = "grad-student",
            vault = "Blind Grad Student",
            BlindGradStudentAnswers.all,
            BlindGradStudentChanges.all,
            BlindGradStudentAsks.all
        ),
        // Guadalajara restaurateur.
        exam(
            id = "restaurante",
            vault = "Blind Restaurante",
            BlindRestauranteAnswers.all,
            BlindRestauranteChanges.all,
            BlindRestauranteAsks.all
        ),
        // Toronto couple, money+home.
        exam(
            id = "household",
            vault = "Blind Household",
            BlindHouseholdAnswers.all,
            BlindHouseholdChanges.all,
            BlindHouseholdAsks.all
        ),
        // A parent in Bogotá with a family of five.
        exam(
            id = "familia",
            vault = "Blind Familia",
            BlindFamiliaAnswers.all,
            BlindFamiliaChanges.all,
            BlindFamiliaAsks.all
        ),
        // Orla Keane, freelance journalist in Dublin.
        exam(
            id = "freelance-writer",
            vault = "Blind Freelance Writer",
            BlindFreelanceWriterAnswers.all,
            BlindFreelanceWriterChanges.all,
            BlindFreelanceWriterAsks.all
        ),
        // Denver runner, knee rehab.
        exam(
            id = "health-log",
            vault = "Blind Health Log",
            BlindHealthLogAnswers.all,
            BlindHealthLogChanges.all,
            BlindHealthLogAsks.all
        ),
        // A Madrid property manager running 8 rental flats for five owners.
        exam(
            id = "inmuebles",
            vault = "Blind Inmuebles",
            BlindInmueblesAnswers.all,
            BlindInmueblesChanges.all,
            BlindInmueblesAsks.all
        ),
        // A retired teacher in Lisbon: trips, book club, Japanese, garden, photography, volunteering.
        exam(
            id = "hobbies",
            vault = "Blind Hobbies",
            BlindHobbiesAnswers.all,
            BlindHobbiesChanges.all,
            BlindHobbiesAsks.all
        )
    )
}
