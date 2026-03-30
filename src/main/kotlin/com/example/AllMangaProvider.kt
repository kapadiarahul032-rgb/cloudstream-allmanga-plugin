package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class AllMangaProvider : MainAPI() {
    override var name = "AllManga"
    override var mainUrl = "https://allmanga.to"
    override var lang = "en"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Manga)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl/").document
        val items = document.select(".manga-item").map { el ->
            newMangaSearchResponse(
                name = el.selectFirst(".title")?.text() ?: "",
                url = el.selectFirst("a")?.attr("href") ?: "",
                posterUrl = el.selectFirst("img")?.attr("src")
            )
        }
        return newHomePageResponse("Latest Updates", items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/search?q=$query").document
        return document.select(".search-result").map { el ->
            newMangaSearchResponse(
                name = el.selectFirst(".title")?.text() ?: "",
                url = el.selectFirst("a")?.attr("href") ?: "",
                posterUrl = el.selectFirst("img")?.attr("src")
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: ""
        val poster = document.selectFirst("img.cover")?.attr("src")
        val plot = document.selectFirst(".description")?.text()
        val tags = document.select(".tag").map { it.text() }

        val chapters = document.select(".chapter-item").map { el ->
            Chapter(
                name = el.selectFirst(".chapter-title")?.text() ?: "",
                data = el.selectFirst("a")?.attr("href") ?: ""
            )
        }

        return newMangaLoadResponse(title, url, TvType.Manga, chapters) {
            this.posterUrl = poster
            this.plot = plot
            this.tags = tags
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        val images = document.select("img.chapter-image").map { el ->
            el.attr("src")
        }

        images.forEachIndexed { index, imageUrl ->
            callback(
                ExtractorLink(
                    source = name,
                    name = "Page ${index + 1}",
                    url = imageUrl,
                    referer = mainUrl,
                    quality = Qualities.Unknown.value
                )
            )
        }
        return true
    }
}
