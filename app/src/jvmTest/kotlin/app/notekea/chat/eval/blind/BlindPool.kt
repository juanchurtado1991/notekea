package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase

/**
 * The blind pool (`docs/eval-blind.md`): 50 exams written and checked blind on 2026-11-17. Ten are sealed and never
 * measured until the final number; a run measures ten of the others picked at random, and their failures are read
 * only by kind.
 */
internal object BlindPool {
    class Exam(
        val id: String,
        val vault: String,
        val cases: List<AgentWideCase>,
        val isSealed: Boolean
    )

    val all = listOf(
        Exam(
            id = "abogado",
            vault = BlindAbogadoCases.VAULT,
            cases = BlindAbogadoCases.all,
            isSealed = false
        ),
        Exam(
            id = "academia-idiomas",
            vault = BlindAcademiaIdiomasCases.VAULT,
            cases = BlindAcademiaIdiomasCases.all,
            isSealed = true
        ),
        Exam(
            id = "apicultor",
            vault = BlindApicultorCases.VAULT,
            cases = BlindApicultorCases.all,
            isSealed = false
        ),
        Exam(
            id = "applicant",
            vault = BlindApplicantCases.VAULT,
            cases = BlindApplicantCases.all,
            isSealed = false
        ),
        Exam(
            id = "arquitecta",
            vault = BlindArquitectaCases.VAULT,
            cases = BlindArquitectaCases.all,
            isSealed = false
        ),
        Exam(
            id = "astronomy",
            vault = BlindAstronomyCases.VAULT,
            cases = BlindAstronomyCases.all,
            isSealed = false
        ),
        Exam(
            id = "bike-shop",
            vault = BlindBikeShopCases.VAULT,
            cases = BlindBikeShopCases.all,
            isSealed = false
        ),
        Exam(
            id = "bodas",
            vault = BlindBodasCases.VAULT,
            cases = BlindBodasCases.all,
            isSealed = false
        ),
        Exam(
            id = "cafetal",
            vault = BlindCafetalCases.VAULT,
            cases = BlindCafetalCases.all,
            isSealed = false
        ),
        Exam(
            id = "care-home",
            vault = BlindCareHomeCases.VAULT,
            cases = BlindCareHomeCases.all,
            isSealed = true
        ),
        Exam(
            id = "caregiver",
            vault = BlindCaregiverCases.VAULT,
            cases = BlindCaregiverCases.all,
            isSealed = false
        ),
        Exam(
            id = "carpintero",
            vault = BlindCarpinteroCases.VAULT,
            cases = BlindCarpinteroCases.all,
            isSealed = false
        ),
        Exam(
            id = "choir",
            vault = BlindChoirCases.VAULT,
            cases = BlindChoirCases.all,
            isSealed = true
        ),
        Exam(
            id = "club-futbol",
            vault = BlindClubFutbolCases.VAULT,
            cases = BlindClubFutbolCases.all,
            isSealed = true
        ),
        Exam(
            id = "conductor",
            vault = BlindConductorCases.VAULT,
            cases = BlindConductorCases.all,
            isSealed = true
        ),
        Exam(
            id = "cooperativa",
            vault = BlindCooperativaCases.VAULT,
            cases = BlindCooperativaCases.all,
            isSealed = false
        ),
        Exam(
            id = "data-consultant",
            vault = BlindDataConsultantCases.VAULT,
            cases = BlindDataConsultantCases.all,
            isSealed = false
        ),
        Exam(
            id = "dog-trainer",
            vault = BlindDogTrainerCases.VAULT,
            cases = BlindDogTrainerCases.all,
            isSealed = false
        ),
        Exam(
            id = "electrician",
            vault = BlindElectricianCases.VAULT,
            cases = BlindElectricianCases.all,
            isSealed = false
        ),
        Exam(
            id = "expedition",
            vault = BlindExpeditionCases.VAULT,
            cases = BlindExpeditionCases.all,
            isSealed = true
        ),
        Exam(
            id = "farmacia",
            vault = BlindFarmaciaCases.VAULT,
            cases = BlindFarmaciaCases.all,
            isSealed = false
        ),
        Exam(
            id = "film-student",
            vault = BlindFilmStudentCases.VAULT,
            cases = BlindFilmStudentCases.all,
            isSealed = true
        ),
        Exam(
            id = "fishing-charter",
            vault = BlindFishingCharterCases.VAULT,
            cases = BlindFishingCharterCases.all,
            isSealed = true
        ),
        Exam(
            id = "floristeria",
            vault = BlindFloristeriaCases.VAULT,
            cases = BlindFloristeriaCases.all,
            isSealed = false
        ),
        Exam(
            id = "food-bank",
            vault = BlindFoodBankCases.VAULT,
            cases = BlindFoodBankCases.all,
            isSealed = false
        ),
        Exam(
            id = "founder",
            vault = BlindFounderCases.VAULT,
            cases = BlindFounderCases.all,
            isSealed = false
        ),
        Exam(
            id = "game-dev",
            vault = BlindGameDevCases.VAULT,
            cases = BlindGameDevCases.all,
            isSealed = false
        ),
        Exam(
            id = "gym-owner",
            vault = BlindGymOwnerCases.VAULT,
            cases = BlindGymOwnerCases.all,
            isSealed = false
        ),
        Exam(
            id = "history-thesis",
            vault = BlindHistoryThesisCases.VAULT,
            cases = BlindHistoryThesisCases.all,
            isSealed = false
        ),
        Exam(
            id = "homeschool",
            vault = BlindHomeschoolCases.VAULT,
            cases = BlindHomeschoolCases.all,
            isSealed = true
        ),
        Exam(
            id = "homestead",
            vault = BlindHomesteadCases.VAULT,
            cases = BlindHomesteadCases.all,
            isSealed = false
        ),
        Exam(
            id = "hostal",
            vault = BlindHostalCases.VAULT,
            cases = BlindHostalCases.all,
            isSealed = false
        ),
        Exam(
            id = "housing-coop",
            vault = BlindHousingCoopCases.VAULT,
            cases = BlindHousingCoopCases.all,
            isSealed = false
        ),
        Exam(
            id = "interior-design",
            vault = BlindInteriorDesignCases.VAULT,
            cases = BlindInteriorDesignCases.all,
            isSealed = false
        ),
        Exam(
            id = "investor-journal",
            vault = BlindInvestorJournalCases.VAULT,
            cases = BlindInvestorJournalCases.all,
            isSealed = false
        ),
        Exam(
            id = "mudanza",
            vault = BlindMudanzaCases.VAULT,
            cases = BlindMudanzaCases.all,
            isSealed = false
        ),
        Exam(
            id = "nurse",
            vault = BlindNurseCases.VAULT,
            cases = BlindNurseCases.all,
            isSealed = false
        ),
        Exam(
            id = "panaderia",
            vault = BlindPanaderiaCases.VAULT,
            cases = BlindPanaderiaCases.all,
            isSealed = false
        ),
        Exam(
            id = "photographer",
            vault = BlindPhotographerCases.VAULT,
            cases = BlindPhotographerCases.all,
            isSealed = false
        ),
        Exam(
            id = "podcast",
            vault = BlindPodcastCases.VAULT,
            cases = BlindPodcastCases.all,
            isSealed = false
        ),
        Exam(
            id = "resident-doctor",
            vault = BlindResidentDoctorCases.VAULT,
            cases = BlindResidentDoctorCases.all,
            isSealed = false
        ),
        Exam(
            id = "streamer",
            vault = BlindStreamerCases.VAULT,
            cases = BlindStreamerCases.all,
            isSealed = false
        ),
        Exam(
            id = "taller",
            vault = BlindTallerCases.VAULT,
            cases = BlindTallerCases.all,
            isSealed = false
        ),
        Exam(
            id = "tattoo-studio",
            vault = BlindTattooStudioCases.VAULT,
            cases = BlindTattooStudioCases.all,
            isSealed = false
        ),
        Exam(
            id = "teacher",
            vault = BlindTeacherCases.VAULT,
            cases = BlindTeacherCases.all,
            isSealed = false
        ),
        Exam(
            id = "touring-band",
            vault = BlindTouringBandCases.VAULT,
            cases = BlindTouringBandCases.all,
            isSealed = true
        ),
        Exam(
            id = "translator",
            vault = BlindTranslatorCases.VAULT,
            cases = BlindTranslatorCases.all,
            isSealed = false
        ),
        Exam(
            id = "vet-clinic",
            vault = BlindVetClinicCases.VAULT,
            cases = BlindVetClinicCases.all,
            isSealed = false
        ),
        Exam(
            id = "vinoteca",
            vault = BlindVinotecaCases.VAULT,
            cases = BlindVinotecaCases.all,
            isSealed = false
        ),
        Exam(
            id = "yoga-studio",
            vault = BlindYogaStudioCases.VAULT,
            cases = BlindYogaStudioCases.all,
            isSealed = false
        )
    )

    /** The 40 exams a round samples from, measured whole once as the reference (`docs/loop-goal-plan.md`). */
    val unsealed: List<Exam> get() = all.filterNot { it.isSealed }

    /** Ten of the unsealed exams, the same ten for the same [seed]. */
    fun sampleOf(seed: Long): List<Exam> = unsealed
        .shuffled(random = kotlin.random.Random(seed = seed))
        .take(n = SAMPLE)

    val sealed: List<Exam> get() = all.filter { it.isSealed }

    private const val SAMPLE = 10
}
