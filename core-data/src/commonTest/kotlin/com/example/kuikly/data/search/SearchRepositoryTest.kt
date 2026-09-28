package com.example.kuikly.data.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchRepositoryTest {
    private val repo = FakeSearchRepository()

    @Test
    fun history_non_empty_with_chinese_labels() {
        val history = repo.history()
        assertTrue(history.isNotEmpty())
        assertTrue(history.all { it.isNotBlank() })
        assertEquals(listOf("极限文字一排两个显示", "极限文字超出九个字...", "宫崎骏宫漫作品", "龙猫", "闪光少女"), history)
    }

    @Test
    fun discovery_non_empty_with_chinese_labels() {
        val discovery = repo.discovery()
        assertTrue(discovery.isNotEmpty())
        assertTrue(discovery.all { it.isNotBlank() })
        assertEquals(6, discovery.size)
    }

    @Test
    fun filter_tags_non_empty() {
        val tags = repo.filterTags()
        assertTrue(tags.isNotEmpty())
        assertTrue(tags.all { it.isNotBlank() })
        assertEquals(5, tags.size)
    }

    @Test
    fun rank_tabs_match_source_labels() {
        val tabs = repo.rankTabs()
        assertEquals(
            listOf("热配榜", "诵读榜", "剧集榜", "记录榜", "合作榜"),
            tabs.map { it.label },
        )
        assertEquals(tabs.size, tabs.map { it.key }.toSet().size)
    }

    @Test
    fun rank_lists_non_empty_for_every_tab() {
        for (tab in SearchRankTab.entries) {
            val items = repo.rankListForTab(tab)
            assertTrue(items.isNotEmpty(), "rank list empty for ${tab.label}")
            assertEquals(items.size, items.map { it.id }.toSet().size)
            items.forEachIndexed { index, item ->
                assertEquals(index + 1, item.rank)
                assertTrue(item.title.isNotBlank())
                assertTrue(item.subtitle.isNotBlank())
                assertTrue(item.coverUrl.isNotBlank())
                assertEquals("${tab.key}_$index", item.id)
            }
        }
    }

    @Test
    fun hot_dubbing_rank_list_matches_source_first_items() {
        val items = repo.rankListForTab(SearchRankTab.HOT_DUBBING)
        assertEquals("穿条纹睡衣的男孩", items.first().title)
        assertEquals("疯狂动物城", items.last().title)
        assertEquals(8, items.size)
    }

    @Test
    fun rank_lists_are_immutable_snapshots() {
        val first = repo.rankListForTab(SearchRankTab.READING)
        val second = repo.rankListForTab(SearchRankTab.READING)
        assertEquals(first, second)
    }

    @Test
    fun search_placeholder_is_chinese() {
        assertTrue(FakeSearchRepository.SEARCH_PLACEHOLDER.isNotBlank())
        assertTrue(FakeSearchRepository.SEARCH_PLACEHOLDER.any { it.code > 0x4E00 })
    }
}
