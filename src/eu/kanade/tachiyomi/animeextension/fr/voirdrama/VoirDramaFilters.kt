package eu.kanade.tachiyomi.animeextension.fr.voirdrama

import eu.kanade.tachiyomi.animesource.model.AnimeFilter
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList

object VoirDramaFilters {

    /* ==============================
       ORDER BY
       ============================== */

    class OrderByFilter : AnimeFilter.Select<String>(
        "Trier par",
        arrayOf(
            "Pertinence",
            "Popularité",
            "Derniers ajouts",
            "Alphabet",
            "Note",
            "Vues",
            "Nouveauté",
        ),
        0,
    ) {
        fun toQuery(): String? = when (state) {
            1 -> "trending"
            2 -> "latest"
            3 -> "alphabet"
            4 -> "rating"
            5 -> "views"
            6 -> "new-manga"
            else -> null
        }
    }

    /* ==============================
       TYPE/FORMAT
       ============================== */

    class TypeFilter : AnimeFilter.Select<String>(
        "Format",
        arrayOf("Tous", "TV", "Movie", "TV Short", "OVA", "ONA", "Special"),
        0,
    ) {
        fun toQuery(): String? = when (state) {
            1 -> "TV"
            2 -> "MOVIE"
            3 -> "TV SHORT"
            4 -> "OVA"
            5 -> "ONA"
            6 -> "SPECIAL"
            else -> null
        }
    }

    /* ==============================
       LANGUAGE
       ============================== */

    class LanguageFilter : AnimeFilter.Select<String>(
        "Langue",
        arrayOf("Tous", "VF", "VOSTFR"),
        0,
    ) {
        fun toQuery(): String? = when (state) {
            1 -> "vf" // VF
            2 -> "vostfr" // VOSTFR
            else -> null
        }
    }

    /* ==============================
       YEAR
       ============================== */

    class YearFilter : AnimeFilter.Text("Année de sortie")

    /* ==============================
       STATUS
       ============================== */

    class Status(name: String) : AnimeFilter.CheckBox(name)

    class StatusFilter : AnimeFilter.Group<Status>(
        "Statut",
        listOf(
            Status("Terminé"),
            Status("En cours"),
            Status("Annulé"),
            Status("En pause"),
        ),
    ) {
        fun toQuery(): List<String> {
            val values = mutableListOf<String>()
            if (state[0].state) values.add("end")
            if (state[1].state) values.add("on-going")
            if (state[2].state) values.add("canceled")
            if (state[3].state) values.add("on-hold")
            return values
        }
    }

    /* ==============================
       GENRES
       ============================== */

    class Genre(name: String) : AnimeFilter.CheckBox(name)

    class GenreFilter : AnimeFilter.Group<Genre>(
        "Genres",
        listOf(
            Genre("Action"),
            Genre("Affaires"),
            Genre("Amitié"),
            Genre("Arts martiaux"),
            Genre("Aventure"),
            Genre("Comédie"),
            Genre("Contexte scolaire"),
            Genre("Crime"),
            Genre("Culinaire"),
            Genre("Documentaire"),
            Genre("Drame"),
            Genre("Famille"),
            Genre("Fantastique"),
            Genre("Guerre"),
            Genre("Historique"),
            Genre("Horreur"),
            Genre("Jeunesse"),
            Genre("Judiciaire"),
            Genre("Mature"),
            Genre("Medical"),
            Genre("Mélodrame"),
            Genre("Militaire"),
            Genre("Musique"),
            Genre("Mystère"),
            Genre("Politique"),
            Genre("Psychologique"),
            Genre("Romance"),
            Genre("SF"),
            Genre("Sitcom"),
            Genre("Sport"),
            Genre("Surnaturel"),
            Genre("Thriller"),
            Genre("Tokusatsu"),
            Genre("Vie quotidienne"),
            Genre("Wuxia"),
        ),
    ) {
        private val genreMap = mapOf(
            "Action" to "action",
            "Affaires" to "affaires",
            "Amitié" to "amitie",
            "Arts martiaux" to "arts-martiaux",
            "Aventure" to "aventure",
            "Comédie" to "comedie",
            "Contexte scolaire" to "contexte-scolaire",
            "Crime" to "crime",
            "Culinaire" to "culinaire",
            "Documentaire" to "documentaire",
            "Drame" to "drame",
            "Famille" to "famille",
            "Fantastique" to "fantastique",
            "Guerre" to "guerre",
            "Historique" to "historique",
            "Horreur" to "horreur",
            "Jeunesse" to "jeunesse",
            "Judiciaire" to "judiciaire",
            "Mature" to "mature",
            "Medical" to "medical",
            "Mélodrame" to "melodrame",
            "Militaire" to "militaire",
            "Musique" to "musique",
            "Mystère" to "mystere",
            "Politique" to "politique",
            "Psychologique" to "psychologique",
            "Romance" to "romance",
            "SF" to "sf",
            "Sitcom" to "sitcom",
            "Sport" to "sport",
            "Surnaturel" to "surnaturel",
            "Thriller" to "thriller",
            "Tokusatsu" to "tokusatsu",
            "Vie quotidienne" to "vie-quotidienne",
            "Wuxia" to "wuxia",
        )

        fun toQuery(): List<String> =
            state.filter { it.state }
                .mapNotNull { genreMap[it.name] }
    }

    /* ==============================
       FILTER LIST
       ============================== */

    fun getFilterList(): AnimeFilterList = AnimeFilterList(
        OrderByFilter(),
        TypeFilter(),
        LanguageFilter(),
        YearFilter(),
        StatusFilter(),
        GenreFilter(),
    )
}
