package app.notekea.chat.eval.blind

/** The blind pool's first 25 exams, abogado to food-bank: [BlindPool] holds them in this order. */
internal object BlindPoolFirst {
    val all = listOf(
        // A solo lawyer in Monterrey.
        exam(
            id = "abogado",
            vault = "Blind Abogado",
            BlindAbogadoAnswers.all,
            BlindAbogadoChanges.all,
            BlindAbogadoAsks.all
        ),
        // The director of a language academy in Sevilla.
        exam(
            id = "academia-idiomas",
            vault = "Blind Academia Idiomas",
            BlindAcademiaIdiomasAnswers.all,
            BlindAcademiaIdiomasChanges.all,
            BlindAcademiaIdiomasAsks.all,
            isSealed = true
        ),
        // A beekeeper in Granada.
        exam(
            id = "apicultor",
            vault = "Blind Apicultor",
            BlindApicultorAnswers.all,
            BlindApicultorChanges.all,
            BlindApicultorAsks.all
        ),
        // A Seattle high school senior applying to universities.
        exam(
            id = "applicant",
            vault = "Blind Applicant",
            BlindApplicantAnswers.all,
            BlindApplicantChanges.all,
            BlindApplicantAsks.all
        ),
        // An architect in Santiago with four residential projects.
        exam(
            id = "arquitecta",
            vault = "Blind Arquitecta",
            BlindArquitectaAnswers.all,
            BlindArquitectaChanges.all,
            BlindArquitectaAsks.all
        ),
        // Amateur astronomer and astronomy club treasurer in Arizona.
        exam(
            id = "astronomy",
            vault = "Blind Astronomy",
            BlindAstronomyAnswers.all,
            BlindAstronomyChanges.all,
            BlindAstronomyAsks.all
        ),
        // A bike mechanic's repair shop in Amsterdam.
        exam(
            id = "bike-shop",
            vault = "Blind Bike Shop",
            BlindBikeShopAnswers.all,
            BlindBikeShopChanges.all,
            BlindBikeShopAsks.all
        ),
        // A Buenos Aires wedding planner running three weddings at once.
        exam(
            id = "bodas",
            vault = "Blind Bodas",
            BlindBodasAnswers.all,
            BlindBodasChanges.all,
            BlindBodasAsks.all
        ),
        // A coffee farm in Tarrazú.
        exam(
            id = "cafetal",
            vault = "Blind Cafetal",
            BlindCafetalAnswers.all,
            BlindCafetalChanges.all,
            BlindCafetalAsks.all
        ),
        // The activities coordinator of a Leeds care home.
        exam(
            id = "care-home",
            vault = "Blind Care Home",
            BlindCareHomeAnswers.all,
            BlindCareHomeChanges.all,
            BlindCareHomeAsks.all,
            isSealed = true
        ),
        // A daughter in Ohio caring for both elderly parents.
        exam(
            id = "caregiver",
            vault = "Blind Caregiver",
            BlindCaregiverAnswers.all,
            BlindCaregiverChanges.all,
            BlindCaregiverAsks.all
        ),
        // A carpenter in Bilbao making custom furniture.
        exam(
            id = "carpintero",
            vault = "Blind Carpintero",
            BlindCarpinteroAnswers.all,
            BlindCarpinteroChanges.all,
            BlindCarpinteroAsks.all
        ),
        // The director of a community choir in Cardiff.
        exam(
            id = "choir",
            vault = "Blind Choir",
            BlindChoirAnswers.all,
            BlindChoirChanges.all,
            BlindChoirAsks.all,
            isSealed = true
        ),
        // The volunteer treasurer of a neighbourhood football club in Montevideo.
        exam(
            id = "club-futbol",
            vault = "Blind Club Futbol",
            BlindClubFutbolAnswers.all,
            BlindClubFutbolChanges.all,
            BlindClubFutbolAsks.all,
            isSealed = true
        ),
        // Guadalajara taxi driver.
        exam(
            id = "conductor",
            vault = "Blind Conductor",
            BlindConductorAnswers.all,
            BlindConductorChanges.all,
            BlindConductorAsks.all,
            isSealed = true
        ),
        // Manager of a small agave and corn cooperative in Oaxaca, Mexico.
        exam(
            id = "cooperativa",
            vault = "Blind Cooperativa",
            BlindCooperativaAnswers.all,
            BlindCooperativaChanges.all,
            BlindCooperativaAsks.all
        ),
        // A freelance data scientist in Berlin.
        exam(
            id = "data-consultant",
            vault = "Blind Data Consultant",
            BlindDataConsultantAnswers.all,
            BlindDataConsultantChanges.all,
            BlindDataConsultantAsks.all
        ),
        // A dog trainer and border collie breeder in Eugene, Oregon.
        exam(
            id = "dog-trainer",
            vault = "Blind Dog Trainer",
            BlindDogTrainerAnswers.all,
            BlindDogTrainerChanges.all,
            BlindDogTrainerAsks.all
        ),
        // A Perth electrician.
        exam(
            id = "electrician",
            vault = "Blind Electrician",
            BlindElectricianAnswers.all,
            BlindElectricianChanges.all,
            BlindElectricianAsks.all
        ),
        // An amateur mountaineer in Boulder planning Mera Peak.
        exam(
            id = "expedition",
            vault = "Blind Expedition",
            BlindExpeditionAnswers.all,
            BlindExpeditionChanges.all,
            BlindExpeditionAsks.all,
            isSealed = true
        ),
        // A pharmacist who owns a small pharmacy in Valparaíso, Chile.
        exam(
            id = "farmacia",
            vault = "Blind Farmacia",
            BlindFarmaciaAnswers.all,
            BlindFarmaciaChanges.all,
            BlindFarmaciaAsks.all
        ),
        // A film student in Los Angeles making a thesis short.
        exam(
            id = "film-student",
            vault = "Blind Film Student",
            BlindFilmStudentAnswers.all,
            BlindFilmStudentChanges.all,
            BlindFilmStudentAsks.all,
            isSealed = true
        ),
        // A Keys charter captain.
        exam(
            id = "fishing-charter",
            vault = "Blind Fishing Charter",
            BlindFishingCharterAnswers.all,
            BlindFishingCharterChanges.all,
            BlindFishingCharterAsks.all,
            isSealed = true
        ),
        // A florist in Barcelona with a shop and event work.
        exam(
            id = "floristeria",
            vault = "Blind Floristeria",
            BlindFloristeriaAnswers.all,
            BlindFloristeriaChanges.all,
            BlindFloristeriaAsks.all
        ),
        // A food bank volunteer coordinator in Philadelphia.
        exam(
            id = "food-bank",
            vault = "Blind Food Bank",
            BlindFoodBankAnswers.all,
            BlindFoodBankChanges.all,
            BlindFoodBankAsks.all
        )
    )
}
