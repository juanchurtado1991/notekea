package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * The nine exams written blind on 2026-11-17 (`docs/eval-blind.md`), each a vault of one invented person and the
 * tasks a person like them would type. Never tuned on: they are measured before a change to the loop and after it,
 * and their failures are read only by kind.
 */
internal object BlindExams {
    class Exam(
        val id: String,
        val vault: String,
        val cases: List<AgentWideCase>
    )

    val all = listOf(
        Exam(
            id = "tech-lead",
            vault = BlindTechLeadCases.VAULT,
            cases = BlindTechLeadCases.all
        ),
        Exam(
            id = "grad-student",
            vault = BlindGradStudentCases.VAULT,
            cases = BlindGradStudentCases.all
        ),
        Exam(
            id = "restaurante",
            vault = BlindRestauranteCases.VAULT,
            cases = BlindRestauranteCases.all
        ),
        Exam(
            id = "household",
            vault = BlindHouseholdCases.VAULT,
            cases = BlindHouseholdCases.all
        ),
        Exam(
            id = "familia",
            vault = BlindFamiliaCases.VAULT,
            cases = BlindFamiliaCases.all
        ),
        Exam(
            id = "freelance-writer",
            vault = BlindFreelanceWriterCases.VAULT,
            cases = BlindFreelanceWriterCases.all
        ),
        Exam(
            id = "health-log",
            vault = BlindHealthLogCases.VAULT,
            cases = BlindHealthLogCases.all
        ),
        Exam(
            id = "inmuebles",
            vault = BlindInmueblesCases.VAULT,
            cases = BlindInmueblesCases.all
        ),
        Exam(
            id = "hobbies",
            vault = BlindHobbiesCases.VAULT,
            cases = BlindHobbiesCases.all
        )
    )
}
