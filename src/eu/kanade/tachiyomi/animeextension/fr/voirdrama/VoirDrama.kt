package eu.kanade.tachiyomi.animeextension.fr.voirdrama

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import eu.kanade.tachiyomi.lib.filemoonextractor.FilemoonExtractor
import eu.kanade.tachiyomi.lib.playlistutils.PlaylistUtils
import eu.kanade.tachiyomi.lib.streamtapeextractor.StreamTapeExtractor
import eu.kanade.tachiyomi.lib.voeextractor.VoeExtractor
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.util.asJsoup
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class VoirDrama : ParsedAnimeHttpSource(), ConfigurableAnimeSource {

    override val name = "VoirDrama"
    override val lang = "fr"
    override val supportsLatest = true

    private val preferences: SharedPreferences by lazy {
        Injekt.get<Application>().getSharedPreferences("source_$id", 0x0000)
    }

    override val baseUrl: String
        get() = preferences.getString(PREF_BASE_URL_KEY, DEFAULT_BASE_URL)!!.trimEnd('/')

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("Referer", baseUrl)

    companion object {
        private const val TAG = "VoirDrama"
        const val PREFIX_SEARCH = "slug:"

        private const val PREF_THUMBNAIL_QUALITY = "thumbnail_quality"
        private const val DEFAULT_THUMBNAIL_QUALITY = "193x278"

        private const val PREF_PREFERRED_PLAYER = "preferred_player"
        private const val DEFAULT_PREFERRED_PLAYER = "myTV"

        private val PLAYER_ENTRIES = arrayOf(
            "myTV (Vidmoly)",
            "MOON (Filemoon)",
            "VOE",
            "Stape (StreamTape)",
            "FHD1 (VK)",
        )
        private val PLAYER_VALUES = arrayOf(
            "myTV",
            "MOON",
            "VOE",
            "Stape",
            "FHD1",
        )

        private const val PREF_BASE_URL_KEY = "base_url"
        private const val DEFAULT_BASE_URL = "https://voirdrama.to"

        private const val PREF_PREFERRED_QUALITY = "preferred_quality"
        private const val DEFAULT_PREFERRED_QUALITY = "1080"

        private val QUALITY_ENTRIES = arrayOf(
            "1080p",
            "720p",
            "480p",
            "360p",
        )
        private val QUALITY_VALUES = arrayOf(
            "1080",
            "720",
            "480",
            "360",
        )
    }

    // ============================== Settings ===============================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        ListPreference(screen.context).apply {
            key = PREF_PREFERRED_PLAYER
            title = "Lecteur préféré"
            entries = PLAYER_ENTRIES
            entryValues = PLAYER_VALUES
            setDefaultValue(DEFAULT_PREFERRED_PLAYER)
            summary = "Le lecteur sélectionné sera affiché en premier dans la liste des sources vidéo.\n\nActuel: %s"
        }.also { screen.addPreference(it) }

        EditTextPreference(screen.context).apply {
            key = PREF_BASE_URL_KEY
            title = "URL du site"
            summary = "URL actuelle : ${preferences.getString(PREF_BASE_URL_KEY, DEFAULT_BASE_URL)}"
            dialogTitle = "Modifier l'URL du site"
            dialogMessage = "Entrez la nouvelle URL (ex: https://voirdrama.to)"
            setDefaultValue(DEFAULT_BASE_URL)

            setOnPreferenceChangeListener { _, newValue ->
                summary = "URL actuelle : $newValue"
                true
            }
        }.also { screen.addPreference(it) }

        ListPreference(screen.context).apply {
            key = PREF_PREFERRED_QUALITY
            title = "Qualité préférée"
            entries = QUALITY_ENTRIES
            entryValues = QUALITY_VALUES
            setDefaultValue(DEFAULT_PREFERRED_QUALITY)
            summary = "La qualité sélectionnée sera priorisée quand elle est disponible.\n\nActuel: %s"
        }.also { screen.addPreference(it) }

        ListPreference(screen.context).apply {
            key = PREF_THUMBNAIL_QUALITY
            title = "Qualité des thumbnails"
            entries = arrayOf(
                "110x150 (Très petite)",
                "125x180 (Petite)",
                "175x238 (Moyenne basse)",
                "193x278 (Par défaut)",
                "350x476 (Moyenne haute)",
                "460x630 (Grande)",
                "Originale",
            )
            entryValues = arrayOf(
                "110x150",
                "125x180",
                "175x238",
                "193x278",
                "350x476",
                "460x630",
                "original",
            )
            setDefaultValue(DEFAULT_THUMBNAIL_QUALITY)
            summary = "Choisissez la qualité des images d'aperçu. Des images de meilleure qualité consommeront plus de données.\n\nActuel: %s"
        }.also { screen.addPreference(it) }
    }

    private fun transformThumbnailUrl(url: String?): String? {
        if (url.isNullOrBlank()) return url

        val quality = preferences.getString(PREF_THUMBNAIL_QUALITY, DEFAULT_THUMBNAIL_QUALITY)!!

        // Si la qualité est celle par défaut, retourner l'URL telle quelle
        if (quality == DEFAULT_THUMBNAIL_QUALITY) return url

        // Pattern pour matcher les dimensions dans l'URL (ex: -193x278.jpg)
        val dimensionPattern = """-\d+x\d+\.jpg""".toRegex()

        return if (quality == "original") {
            // Supprimer les dimensions pour obtenir l'image originale
            url.replace(dimensionPattern, ".jpg")
        } else {
            // Remplacer les dimensions par celles choisies
            url.replace(dimensionPattern, "-$quality.jpg")
        }
    }

    // ============================== Popular ===============================

    override fun popularAnimeRequest(page: Int): Request =
        GET("$baseUrl/page/$page/?s&post_type=wp-manga&m_orderby=views", headers)

    override fun popularAnimeSelector(): String = "div.row.c-tabs-item__content"

    override fun popularAnimeFromElement(element: Element): SAnime = SAnime.create().apply {
        val anchor = element.selectFirst("div.post-title h3 a")!!
        setUrlWithoutDomain(anchor.attr("href"))
        title = anchor.text()
        thumbnail_url = element.selectFirst("div.tab-thumb img")?.let {
            it.attr("abs:src").ifEmpty { it.attr("abs:data-src") }
        }?.let { transformThumbnailUrl(it) }
    }

    override fun popularAnimeNextPageSelector(): String = "a.nextpostslink"

    // =============================== Latest ===============================

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/page/$page/?s&post_type=wp-manga&m_orderby=latest", headers)

    override fun latestUpdatesSelector(): String = popularAnimeSelector()

    override fun latestUpdatesFromElement(element: Element): SAnime = popularAnimeFromElement(element)

    override fun latestUpdatesNextPageSelector(): String = popularAnimeNextPageSelector()

    // =============================== Search ===============================

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        val url = "$baseUrl/page/$page/".toHttpUrl().newBuilder()

        // Handle slug search from deep links
        if (query.startsWith(PREFIX_SEARCH)) {
            val slug = query.removePrefix(PREFIX_SEARCH)
            return GET("$baseUrl/drama/$slug/", headers)
        }

        url.addQueryParameter("s", query)
        url.addQueryParameter("post_type", "wp-manga")

        // Check if TypeFilter is selected to avoid conflict with LanguageFilter
        var typeFilterUsed = false

        filters.forEach { filter ->
            when (filter) {
                is VoirDramaFilters.OrderByFilter -> {
                    filter.toQuery()?.let { url.addQueryParameter("m_orderby", it) }
                }
                is VoirDramaFilters.TypeFilter -> {
                    filter.toQuery()?.let {
                        // Replace spaces with + for "TV SHORT" -> "TV+SHORT"
                        url.addQueryParameter("type", it.replace(" ", "+"))
                        typeFilterUsed = true
                    }
                }
                is VoirDramaFilters.LanguageFilter -> {
                    filter.toQuery()?.let { language ->
                        // If no type filter, add empty type parameter
                        if (!typeFilterUsed) {
                            url.addQueryParameter("type", "")
                        }
                        url.addQueryParameter("language", language)
                    }
                }
                is VoirDramaFilters.YearFilter -> {
                    val year = filter.state.trim()
                    if (year.isNotEmpty()) {
                        url.addQueryParameter("release", year)
                    }
                }
                is VoirDramaFilters.StatusFilter -> {
                    filter.toQuery().forEach { url.addQueryParameter("status[]", it) }
                }
                is VoirDramaFilters.GenreFilter -> {
                    filter.toQuery().forEach { url.addQueryParameter("genre[]", it) }
                }
                else -> {}
            }
        }

        return GET(url.build(), headers)
    }

    override fun searchAnimeSelector(): String = popularAnimeSelector()

    override fun searchAnimeFromElement(element: Element): SAnime = popularAnimeFromElement(element)

    override fun searchAnimeNextPageSelector(): String = popularAnimeNextPageSelector()

    // ============================ Anime Details ============================

    override fun animeDetailsParse(document: Document): SAnime = SAnime.create().apply {
        title = document.selectFirst(".post-title h1")?.text().orEmpty()

        description = document.selectFirst("div.description-summary div.summary__content")?.text()
            ?: document.selectFirst("div.summary__content")?.text()
            ?: ""

        genre = document.select("div.genres-content a").joinToString { it.text() }

        author = document.select("div.author-content a").joinToString { it.text() }

        status = document.select("div.post-status div.post-content_item:contains(Statut) div.summary-content")
            .firstOrNull()?.text().let { statusText ->
                when {
                    statusText?.contains("En cours", ignoreCase = true) == true -> SAnime.ONGOING
                    statusText?.contains("Terminé", ignoreCase = true) == true -> SAnime.COMPLETED
                    else -> SAnime.UNKNOWN
                }
            }

        thumbnail_url = document.selectFirst("div.summary_image img")?.let {
            it.attr("abs:src").ifEmpty { it.attr("abs:data-src") }
        }?.let { transformThumbnailUrl(it) }
    }

    // ============================== Episodes ===============================

    override fun episodeListSelector(): String = "li.wp-manga-chapter"

    override fun episodeFromElement(element: Element): SEpisode = SEpisode.create().apply {
        val anchor = element.selectFirst("a")!!
        setUrlWithoutDomain(anchor.attr("href"))

        // Extract episode number from the anchor text
        // Format is usually like "Anime Name - 21 VF - 21" or "Solo Leveling 2 - 13 VOSTFR - 13"
        val rawName = anchor.text().trim()

        // Find the LAST number in the text (to avoid grabbing "2" from "Solo Leveling 2")
        val numberMatch = Regex("""\d+""").findAll(rawName).lastOrNull()
        episode_number = numberMatch?.value?.toFloatOrNull() ?: 0f

        // Format name as "Episode X"
        name = if (episode_number > 0) {
            "Episode ${episode_number.toInt()}"
        } else {
            rawName // Fallback to original name if no number found
        }

        // Try to get the date if available
        val dateText = element.selectFirst("span.chapter-release-date")?.text()
        date_upload = parseDate(dateText)
    }

    override fun episodeListParse(response: Response): List<SEpisode> {
        // Return episodes in descending order (latest first: 21, 20, 19... 1)
        return super.episodeListParse(response)
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L

        return try {
            when {
                // Handle relative dates: "2 days ago", "3 hours ago", etc.
                "ago" in dateStr -> {
                    val number = dateStr.filter { it.isDigit() }.toIntOrNull() ?: 0
                    val currentTime = System.currentTimeMillis()

                    when {
                        "second" in dateStr -> currentTime - (number * 1000L)
                        "minute" in dateStr -> currentTime - (number * 60 * 1000L)
                        "hour" in dateStr -> currentTime - (number * 60 * 60 * 1000L)
                        "day" in dateStr -> currentTime - (number * 24 * 60 * 60 * 1000L)
                        "week" in dateStr -> currentTime - (number * 7 * 24 * 60 * 60 * 1000L)
                        "month" in dateStr -> currentTime - (number * 30 * 24 * 60 * 60 * 1000L)
                        "year" in dateStr -> currentTime - (number * 365 * 24 * 60 * 60 * 1000L)
                        else -> 0L
                    }
                }
                // Handle absolute dates: "December 14, 2025"
                else -> {
                    val format = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.ENGLISH)
                    format.parse(dateStr.trim())?.time ?: 0L
                }
            }
        } catch (e: Exception) {
            0L
        }
    }

    // ============================== Videos =================================

    private val playlistUtils by lazy { PlaylistUtils(client) }

    private val sourcesRegex = Regex("sources: (.*?]),")
    private val fileRegex = Regex("""file:\s*'([^']+)'""")

    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        val videos = mutableListOf<Video>()

        Log.d(TAG, "videoListParse: URL - ${response.request.url}")

        // Initialize extractors
        val filemoonExtractor = FilemoonExtractor(client)
        val voeExtractor = VoeExtractor(client, headers)
        val streamtapeExtractor = StreamTapeExtractor(client)

        // Extract video sources from JavaScript object 'thisChapterSources'
        val scriptContent = document.select("script:containsData(thisChapterSources)").firstOrNull()?.data()

        Log.d(TAG, "videoListParse: scriptContent found = ${scriptContent != null}")
        if (scriptContent != null) {
            val iframeUrls = extractIframeUrls(scriptContent)
            Log.d(TAG, "videoListParse: Found ${iframeUrls.size} iframe URLs")

            iframeUrls.forEach { (playerName, iframeUrl) ->
                Log.d(TAG, "videoListParse: Processing player=$playerName, url=$iframeUrl")
                try {
                    val extractedVideos = when {
                        // LECTEUR myTV - Vidmoly (manual extraction)
                        iframeUrl.contains("vidmoly") -> {
                            extractVidmolyVideos(iframeUrl, playerName)
                        }
                        // LECTEUR MOON - Filemoon (f16px)
                        iframeUrl.contains("f16px") || iframeUrl.contains("filemoon") -> {
                            filemoonExtractor.videosFromUrl(iframeUrl, "$playerName: ", headers)
                        }
                        // LECTEUR VOE
                        iframeUrl.contains("voe.sx") || iframeUrl.contains("voe") -> {
                            voeExtractor.videosFromUrl(iframeUrl, "$playerName: ")
                        }
                        // LECTEUR Stape - StreamTape
                        iframeUrl.contains("streamtape") -> {
                            streamtapeExtractor.videosFromUrl(iframeUrl, "$playerName: ")
                        }
                        // LECTEUR FHD1 - Mail.ru
                        iframeUrl.contains("mail.ru") -> {
                            extractMailRuVideos(iframeUrl, playerName)
                        }
                        // VK
                        iframeUrl.contains("vk.com") || iframeUrl.contains("vkvideo") -> {
                            emptyList() // VK not supported for now
                        }
                        else -> {
                            Log.w(TAG, "videoListParse: No extractor for url=$iframeUrl")
                            emptyList()
                        }
                    }
                    Log.d(TAG, "videoListParse: Extracted ${extractedVideos.size} videos from $playerName")
                    videos.addAll(extractedVideos)
                } catch (e: Exception) {
                    Log.e(TAG, "videoListParse: Failed to extract from $playerName: ${e.message}", e)
                }
            }
        } else {
            Log.w(TAG, "videoListParse: No script with thisChapterSources found!")
        }

        Log.d(TAG, "videoListParse: Total videos found - ${videos.size}")
        return sortVideosByPreference(videos)
    }

    /**
     * Manual VidMoly extraction to avoid issues with the shared extractor
     * (which hardcodes vidmoly.to as Origin/Referer while URLs use vidmoly.biz)
     */
    private fun extractVidmolyVideos(url: String, playerName: String): List<Video> {
        val host = url.toHttpUrl().host
        val vidmolyHeaders = headers.newBuilder()
            .set("Origin", "https://$host")
            .set("Referer", "https://$host/")
            .set("Sec-Fetch-Dest", "iframe")
            .build()

        val page = client.newCall(GET(url, vidmolyHeaders)).execute().body.string()
        val sourcesBlock = sourcesRegex.find(page)?.groupValues?.get(1)
        if (sourcesBlock == null) {
            Log.w(TAG, "extractVidmolyVideos: No sources block found")
            return emptyList()
        }

        val m3u8Urls = fileRegex.findAll(sourcesBlock).map { it.groupValues[1] }.toList()
        Log.d(TAG, "extractVidmolyVideos: Found ${m3u8Urls.size} m3u8 URLs")

        val hlsHeaders = headers.newBuilder()
            .set("Origin", "https://$host")
            .set("Referer", "https://$host/")
            .build()

        return m3u8Urls.flatMap { m3u8Url ->
            playlistUtils.extractFromHls(
                m3u8Url,
                referer = "https://$host/",
                videoNameGen = { quality -> "$playerName: VidMoly - $quality" },
                masterHeaders = hlsHeaders,
                videoHeaders = hlsHeaders,
            )
        }
    }

    /**
     * Manual Mail.ru extraction via their meta API
     * The VkExtractor doesn't work for mail.ru embeds (video URLs loaded via JS)
     */
    private fun extractMailRuVideos(url: String, playerName: String): List<Video> {
        // Extract video ID from embed URL: https://my.mail.ru/video/embed/ID
        val videoId = url.substringAfterLast("/")
        Log.d(TAG, "extractMailRuVideos: videoId=$videoId")

        val metaUrl = "https://my.mail.ru/+/video/meta/$videoId"
        val metaHeaders = headers.newBuilder()
            .set("Referer", "https://my.mail.ru/")
            .build()

        val metaResponse = client.newCall(GET(metaUrl, metaHeaders)).execute().body.string()
        Log.d(TAG, "extractMailRuVideos: meta response length=${metaResponse.length}")

        val metaData = json.decodeFromString<MailRuMeta>(metaResponse)
        val videos = metaData.videos.map { video ->
            val videoUrl = if (video.url.startsWith("//")) "https:${video.url}" else video.url
            val videoHeaders = headers.newBuilder()
                .set("Referer", "https://my.mail.ru/")
                .build()
            Video(videoUrl, "$playerName: Mail.ru ${video.key}", videoUrl, videoHeaders)
        }
        Log.d(TAG, "extractMailRuVideos: Found ${videos.size} videos")
        return videos
    }

    @Serializable
    data class MailRuMeta(
        val videos: List<MailRuVideo> = emptyList(),
    )

    @Serializable
    data class MailRuVideo(
        val key: String,
        val url: String,
    )

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private fun sortVideosByPreference(videos: List<Video>): List<Video> {
        val preferredPlayer = preferences.getString(PREF_PREFERRED_PLAYER, DEFAULT_PREFERRED_PLAYER)!!
        val preferredQuality = preferences.getString(PREF_PREFERRED_QUALITY, DEFAULT_PREFERRED_QUALITY)!!

        return videos.sortedWith(
            compareByDescending<Video> { video ->
                // Priority 1: Preferred player + preferred quality
                video.quality.contains(preferredPlayer, ignoreCase = true) &&
                    video.quality.contains(preferredQuality, ignoreCase = true)
            }.thenByDescending { video ->
                // Priority 2: Preferred player (any quality)
                video.quality.contains(preferredPlayer, ignoreCase = true)
            }.thenByDescending { video ->
                // Priority 3: Preferred quality (any player)
                video.quality.contains(preferredQuality, ignoreCase = true)
            },
        )
    }

    /**
     * Extracts iframe URLs from the thisChapterSources JavaScript object
     * Format: var thisChapterSources = {"LECTEUR myTV":"<iframe src=\"url\" ...>", ...}
     */
    private fun extractIframeUrls(scriptContent: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()

        // Regex to match: "PLAYER_NAME":"<iframe src=\"URL\" ...>"
        // Accepte un préfixe Unicode optionnel (ex: "☰ ") devant "LECTEUR"
        val regex = """"([^"]*LECTEUR [^"]+)":"<iframe src=\\"([^"]+)\\\"""".toRegex()

        val matches = regex.findAll(scriptContent).toList()
        Log.d(TAG, "extractIframeUrls: Regex found ${matches.size} matches")

        matches.forEach { matchResult ->
            val playerName = matchResult.groupValues[1]
            val iframeUrl = matchResult.groupValues[2].replace("\\/", "/")
            Log.d(TAG, "extractIframeUrls: player=$playerName, url=$iframeUrl")
            results.add(Pair(playerName, iframeUrl))
        }

        if (results.isEmpty()) {
            Log.w(TAG, "extractIframeUrls: No matches! Raw script (first 1000 chars): ${scriptContent.take(1000)}")
        }

        return results
    }

    override fun videoListSelector(): String = throw UnsupportedOperationException("Not used")

    override fun videoFromElement(element: Element): Video = throw UnsupportedOperationException("Not used")

    override fun videoUrlParse(document: Document): String = throw UnsupportedOperationException("Not used")

    // =============================== Filters ===============================

    override fun getFilterList(): AnimeFilterList = VoirDramaFilters.getFilterList()
}
