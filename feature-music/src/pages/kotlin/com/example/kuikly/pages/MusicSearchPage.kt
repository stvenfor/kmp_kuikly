package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.music.MusicStore
import com.example.kuikly.data.music.Track
import com.example.kuikly.design.KuiklyTheme
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 音乐搜索页（mock 过滤）。
 *
 * 源 Flutter music 模块没有搜索能力，按任务要求以 mock 补齐：
 * 标题/歌手/专辑模糊匹配（`MusicRepository.search`）。Kuikly 无文本输入框，
 * 沿用 CommunitySearchPage 的候选关键词 chips 交互。
 */
@Page("MusicSearch")
internal class MusicSearchPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var query by remember { mutableStateOf("") }
            var results by remember { mutableStateOf<List<Track>>(emptyList()) }
            var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
            var error by remember { mutableStateOf<String?>(null) }
            var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
            var booted by remember { mutableStateOf(false) }

            fun runSearch(keyword: String) {
                query = keyword
                if (keyword.isBlank()) {
                    results = emptyList()
                    error = null
                    return
                }
                MusicStore.repo.search(keyword)
                    .onSuccess { list ->
                        results = list
                        error = null
                    }
                    .onFailure { e ->
                        results = emptyList()
                        error = e.message ?: "搜索失败"
                    }
            }

            fun refreshFavorites() {
                favoriteIds = MusicStore.repo.favoriteTracks().map { it.id }.toSet()
            }

            fun openPlayer(track: Track) {
                MusicStore.repo.play(track.id)
                Utils.currentBridgeModule().openPage(
                    url = "NowPlaying",
                    userData = JSONObject().apply { put("id", track.id) },
                )
            }

            if (!booted) {
                booted = true
                suggestions = MusicStore.repo.searchSuggestions()
                refreshFavorites()
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .background(KuiklyTheme.canvasSoft2)
                    .padding(horizontal = 14.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "‹ 返回",
                            fontSize = 15.sp,
                            color = KuiklyTheme.link,
                            modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("搜索", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                            Text("搜索歌曲、歌手、专辑", fontSize = 11.sp, color = KuiklyTheme.muted)
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusLg))
                            .padding(12.dp),
                    ) {
                        Text(
                            if (query.isBlank()) "点击候选关键词开始搜索" else "当前关键词：$query",
                            fontSize = 14.sp,
                            color = if (query.isBlank()) KuiklyTheme.muted else KuiklyTheme.ink,
                        )
                        Spacer(Modifier.height(10.dp))
                        // 无 FlowRow：候选词按每行 3 个手动分行。
                        suggestions.chunked(3).forEach { rowKeywords ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                rowKeywords.forEach { kw ->
                                    MusicKeywordChip(
                                        label = kw,
                                        selected = query == kw,
                                        onClick = { runSearch(if (query == kw) "" else kw) },
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                when {
                    error != null -> item {
                        MusicErrorState(message = error ?: "搜索失败", onRetry = { runSearch(query) })
                    }
                    query.isBlank() -> item {
                        MusicSearchPrompt()
                    }
                    results.isEmpty() -> item {
                        MusicSearchEmpty(onClear = { runSearch("") })
                    }
                    else -> {
                        item {
                            Text(
                                "歌曲 · ${results.size}",
                                fontSize = 13.sp,
                                color = KuiklyTheme.muted,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        items(results, key = { it.id }) { track ->
                            MusicSearchResultRow(
                                track = track,
                                favorite = favoriteIds.contains(track.id),
                                onToggleFavorite = {
                                    MusicStore.repo.toggleFavorite(track.id)
                                        .onSuccess { fav ->
                                            refreshFavorites()
                                            Utils.currentBridgeModule()
                                                .toast(if (fav) "已收藏 ${track.title}" else "已取消收藏")
                                        }
                                        .onFailure {
                                            Utils.currentBridgeModule().toast("操作失败")
                                        }
                                },
                                onPlay = { openPlayer(track) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun MusicKeywordChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.link else KuiklyTheme.canvasSoft2,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.ink,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun MusicSearchResultRow(
    track: Track,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPlay: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .clickable { onPlay() }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicCoverBlock(title = track.title, size = 48)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    track.title,
                    fontSize = 15.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${track.artist} · ${track.album} · ${track.durationLabel}",
                    fontSize = 12.sp,
                    color = KuiklyTheme.muted,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicTextButton(text = "播放", onClick = onPlay)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (favorite) KuiklyTheme.highlightMagenta else KuiklyTheme.canvasSoft2,
                        RoundedCornerShape(KuiklyTheme.radiusSm),
                    )
                    .clickable { onToggleFavorite() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (favorite) "♥ 已收藏" else "♡ 收藏",
                    fontSize = 12.sp,
                    color = if (favorite) KuiklyTheme.onPrimary else KuiklyTheme.ink,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun MusicSearchPrompt() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("输入关键词或点击上方候选词", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("匹配歌曲名、歌手与专辑", fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun MusicSearchEmpty(onClear: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("暂无搜索结果", fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("换个关键词试试", fontSize = 12.sp, color = KuiklyTheme.muted)
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .background(KuiklyTheme.primary, RoundedCornerShape(KuiklyTheme.radiusMd))
                .clickable { onClear() }
                .padding(horizontal = 22.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("清空关键词", fontSize = 13.sp, color = KuiklyTheme.onPrimary)
        }
    }
}
