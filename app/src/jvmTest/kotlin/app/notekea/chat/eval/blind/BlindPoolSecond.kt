package app.notekea.chat.eval.blind

/** The blind pool's last 25 exams, founder to yoga-studio: [BlindPool] holds them after [BlindPoolFirst]. */
internal object BlindPoolSecond {
    val all = listOf(
        // A seed-stage logistics founder in Austin.
        exam(
            id = "founder",
            vault = "Blind Founder",
            BlindFounderAnswers.all,
            BlindFounderChanges.all,
            BlindFounderAsks.all
        ),
        // A solo indie game developer in Montreal shipping a pixel-art game.
        exam(
            id = "game-dev",
            vault = "Blind Game Dev",
            BlindGameDevAnswers.all,
            BlindGameDevChanges.all,
            BlindGameDevAsks.all
        ),
        // A CrossFit gym owner in Denver.
        exam(
            id = "gym-owner",
            vault = "Blind Gym Owner",
            BlindGymOwnerAnswers.all,
            BlindGymOwnerChanges.all,
            BlindGymOwnerAsks.all
        ),
        // A history PhD student in Edinburgh writing on 19th-century Leith–Baltic trade.
        exam(
            id = "history-thesis",
            vault = "Blind History Thesis",
            BlindHistoryThesisAnswers.all,
            BlindHistoryThesisChanges.all,
            BlindHistoryThesisAsks.all
        ),
        // A parent homeschooling three kids in New Zealand.
        exam(
            id = "homeschool",
            vault = "Blind Homeschool",
            BlindHomeschoolAnswers.all,
            BlindHomeschoolChanges.all,
            BlindHomeschoolAsks.all,
            isSealed = true
        ),
        // A Tennessee homesteader with goats, hens and a garden.
        exam(
            id = "homestead",
            vault = "Blind Homestead",
            BlindHomesteadAnswers.all,
            BlindHomesteadChanges.all,
            BlindHomesteadAsks.all
        ),
        // A hostel owner in Cusco.
        exam(
            id = "hostal",
            vault = "Blind Hostal",
            BlindHostalAnswers.all,
            BlindHostalChanges.all,
            BlindHostalAsks.all
        ),
        // Secretary of a Vienna housing coop.
        exam(
            id = "housing-coop",
            vault = "Blind Housing Coop",
            BlindHousingCoopAnswers.all,
            BlindHousingCoopChanges.all,
            BlindHousingCoopAsks.all
        ),
        // A London interior designer's studio vault: three client projects, two clients named Patel.
        exam(
            id = "interior-design",
            vault = "Blind Interior Design",
            BlindInteriorDesignAnswers.all,
            BlindInteriorDesignChanges.all,
            BlindInteriorDesignAsks.all
        ),
        // A careful Melbourne investor's journal of index funds and savings.
        exam(
            id = "investor-journal",
            vault = "Blind Investor Journal",
            BlindInvestorJournalAnswers.all,
            BlindInvestorJournalChanges.all,
            BlindInvestorJournalAsks.all
        ),
        // A Valencia family moving to Toronto.
        exam(
            id = "mudanza",
            vault = "Blind Mudanza",
            BlindMudanzaAnswers.all,
            BlindMudanzaChanges.all,
            BlindMudanzaAsks.all
        ),
        // An ICU nurse in Manchester.
        exam(
            id = "nurse",
            vault = "Blind Nurse",
            BlindNurseAnswers.all,
            BlindNurseChanges.all,
            BlindNurseAsks.all
        ),
        // A baker in Arequipa, Peru.
        exam(
            id = "panaderia",
            vault = "Blind Panaderia",
            BlindPanaderiaAnswers.all,
            BlindPanaderiaChanges.all,
            BlindPanaderiaAsks.all
        ),
        // Atlanta photographer.
        exam(
            id = "photographer",
            vault = "Blind Photographer",
            BlindPhotographerAnswers.all,
            BlindPhotographerChanges.all,
            BlindPhotographerAsks.all
        ),
        // Host of a weekly Toronto interview podcast.
        exam(
            id = "podcast",
            vault = "Blind Podcast",
            BlindPodcastAnswers.all,
            BlindPodcastChanges.all,
            BlindPodcastAsks.all
        ),
        // A PGY-2 internal medicine resident in Boston.
        exam(
            id = "resident-doctor",
            vault = "Blind Resident Doctor",
            BlindResidentDoctorAnswers.all,
            BlindResidentDoctorChanges.all,
            BlindResidentDoctorAsks.all
        ),
        // A game streamer in Bogotá.
        exam(
            id = "streamer",
            vault = "Blind Streamer",
            BlindStreamerAnswers.all,
            BlindStreamerChanges.all,
            BlindStreamerAsks.all
        ),
        // Workshop owner, Medellín.
        exam(
            id = "taller",
            vault = "Blind Taller",
            BlindTallerAnswers.all,
            BlindTallerChanges.all,
            BlindTallerAsks.all
        ),
        // A tattoo artist who owns a small studio in Berlin.
        exam(
            id = "tattoo-studio",
            vault = "Blind Tattoo Studio",
            BlindTattooStudioAnswers.all,
            BlindTattooStudioChanges.all,
            BlindTattooStudioAsks.all
        ),
        // A Chicago high school history teacher with four classes.
        exam(
            id = "teacher",
            vault = "Blind Teacher",
            BlindTeacherAnswers.all,
            BlindTeacherChanges.all,
            BlindTeacherAsks.all
        ),
        // A Leeds indie band's manager-guitarist on a UK tour.
        exam(
            id = "touring-band",
            vault = "Blind Touring Band",
            BlindTouringBandAnswers.all,
            BlindTouringBandChanges.all,
            BlindTouringBandAsks.all,
            isSealed = true
        ),
        // An EN-PT Lisbon translator.
        exam(
            id = "translator",
            vault = "Blind Translator",
            BlindTranslatorAnswers.all,
            BlindTranslatorChanges.all,
            BlindTranslatorAsks.all
        ),
        // A Vermont vet's clinic.
        exam(
            id = "vet-clinic",
            vault = "Blind Vet Clinic",
            BlindVetClinicAnswers.all,
            BlindVetClinicChanges.all,
            BlindVetClinicAsks.all
        ),
        // The owner of a wine shop in Mendoza, Argentina.
        exam(
            id = "vinoteca",
            vault = "Blind Vinoteca",
            BlindVinotecaAnswers.all,
            BlindVinotecaChanges.all,
            BlindVinotecaAsks.all
        ),
        // A yoga studio owner in Sydney.
        exam(
            id = "yoga-studio",
            vault = "Blind Yoga Studio",
            BlindYogaStudioAnswers.all,
            BlindYogaStudioChanges.all,
            BlindYogaStudioAsks.all
        )
    )
}
