package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.music.DownloadStatus
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
import com.tencent.kuikly.compose.foundation.lazy.itemsIndexed
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

private val MUSIC_COLLECTION_TABS = listOf("收藏", "下载", "播放队列")

/**
 * 音乐收藏中心（mock）：收藏 / 下载态 / 播放队列管理三个页签。
 *
 * 源 Flutter music 模块只有列表 + 播放页，没有这三块能力；
 * 按任务要求以 mock 数据层补齐，交互与 Music/NowPlaying 两页保持一致。
 */
@Page("MusicCollection")
internal class MusicCollectionPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var tabIndex by remember { mutableStateOf(0) }
            var favorites by remember { mutableStateOf<List<Track>>(emptyList()) }
            var library by remember { mutableStateOf<List<Track>>(emptyList()) }
            var queue by remember { mutableStateOf<List<Track>>(emptyList()) }
            var currentId by remember { mutableStateOf<String?>(null) }
            var downloadSnapshot by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
            var booted by remember { mutableStateOf(false) }

            fun refresh() {
                favorites = MusicStore.repo.favoriteTracks()
                library = MusicStore.repo.all().getOrDefault(emptyList())
                queue = MusicStore.repo.queueTracks()
                currentId = MusicStore.repo.playbackState().second
                downloadSnapshot = MusicStore.repo.downloadSnapshot()
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
                refresh()
            }

            Column(modifier = Modifier.fillMaxSize().background(KuiklyTheme.canvasSoft2)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(KuiklyTheme.canvas)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹ 返回",
                        fontSize = 16.sp,
                        color = KuiklyTheme.link,
                        modifier = Modifier.clickable { Utils.currentBridgeModule().closePage() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("我的音乐", fontSize = 18.sp, color = KuiklyTheme.foreground, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MUSIC_COLLECTION_TABS.forEachIndexed { index, label ->
                        MusicCollectionTab(
                            label = label,
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                }

                when (tabIndex) {
                    0 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        if (favorites.isEmpty()) {
                            item {
                                MusicCollectionEmpty(
                                    title = "还没有收藏歌曲",
                                    subtitle = "在列表或搜索页点击 ♥ 收藏",
                                )
                            }
                        } else {
                            items(favorites, key = { it.id }) { track ->
                                MusicFavoriteRow(
                                    track = track,
                                    onPlay = { openPlayer(track) },
                                    onRemove = {
                                        MusicStore.repo.toggleFavorite(track.id)
                                        refresh()
                                        Utils.currentBridgeModule().toast("已取消收藏 ${track.title}")
                                    },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }

                    1 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "下载管理（mock，无定时器，手动推进）",
                                    fontSize = 12.sp,
                                    color = KuiklyTheme.muted,
                                )
                                Spacer(Modifier.width(8.dp))
                                MusicTextButton(text = "全部 +25%") {
                                    MusicStore.repo.advanceDownloads(25)
                                    refresh()
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        if (library.isEmpty()) {
                            item {
                                MusicCollectionEmpty(title = "曲库为空", subtitle = "切换 Mock 场景后重试")
                            }
                        } else {
                            items(library, key = { it.id }) { track ->
                                MusicDownloadRow(
                                    track = track,
                                    status = MusicStore.repo.downloadStatus(track.id),
                                    progress = downloadSnapshot[track.id] ?: 0,
                                    onStart = {
                                        MusicStore.repo.startDownload(track.id)
                                            .onSuccess { refresh() }
                                            .onFailure {
                                                Utils.currentBridgeModule().toast("下载启动失败")
                                            }
                                    },
                                    onAdvance = {
                                        MusicStore.repo.advanceDownload(track.id, 25)
                                        refresh()
                                    },
                                    onFinish = {
                                        MusicStore.repo.advanceDownload(track.id, 100)
                                        refresh()
                                    },
                                    onRemove = {
                                        MusicStore.repo.removeDownload(track.id)
                                        refresh()
                                        Utils.currentBridgeModule().toast("已删除下载")
                                    },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }

                    else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "共 ${queue.size} 首 · 播放顺序即队列顺序",
                                    fontSize = 12.sp,
                                    color = KuiklyTheme.muted,
                                )
                                Spacer(Modifier.width(8.dp))
                                MusicTextButton(text = "重置队列") {
                                    MusicStore.repo.resetQueue()
                                    refresh()
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        if (queue.isEmpty()) {
                            item {
                                MusicCollectionEmpty(title = "播放队列为空", subtitle = "点击重置队列恢复曲库全序")
                            }
                        } else {
                            itemsIndexed(queue, key = { _, track -> track.id }) { index, track ->
                                MusicQueueRow(
                                    index = index,
                                    track = track,
                                    playing = currentId == track.id,
                                    onPlay = { openPlayer(track) },
                                    onMoveUp = {
                                        MusicStore.repo.moveInQueue(index, index - 1)
                                        refresh()
                                    },
                                    onMoveDown = {
                                        MusicStore.repo.moveInQueue(index, index + 1)
                                        refresh()
                                    },
                                    onRemove = {
                                        MusicStore.repo.dequeue(track.id)
                                        refresh()
                                        Utils.currentBridgeModule().toast("已移出队列")
                                    },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        // 「增」：曲库中尚未入队的曲目可手动追加到队尾。
                        val notInQueue = library.filter { t -> queue.none { it.id == t.id } }
                        if (notInQueue.isNotEmpty()) {
                            item {
                                Text(
                                    "未入队曲目 · ${notInQueue.size}",
                                    fontSize = 13.sp,
                                    color = KuiklyTheme.muted,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                            items(notInQueue, key = { "add-" + it.id }) { track ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MusicCoverBlock(title = track.title, size = 36)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            track.title,
                                            fontSize = 13.sp,
                                            color = KuiklyTheme.ink,
                                            maxLines = 1,
                                        )
                                        Text(track.artist, fontSize = 11.sp, color = KuiklyTheme.muted)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    MusicTextButton(text = "+ 入队") {
                                        MusicStore.repo.enqueue(track.id)
                                        refresh()
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MusicCollectionTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KuiklyTheme.primary else KuiklyTheme.canvas,
                RoundedCornerShape(KuiklyTheme.radiusPill),
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = if (selected) KuiklyTheme.onPrimary else KuiklyTheme.body,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun MusicCollectionEmpty(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, fontSize = 15.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 12.sp, color = KuiklyTheme.muted)
    }
}

@Composable
private fun MusicFavoriteRow(track: Track, onPlay: () -> Unit, onRemove: () -> Unit) {
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
                Text("${track.artist} · ${track.album}", fontSize = 12.sp, color = KuiklyTheme.muted)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicTextButton(text = "播放", onClick = onPlay)
            Spacer(Modifier.width(8.dp))
            MusicTextButton(text = "♡ 取消收藏", onClick = onRemove)
        }
    }
}

@Composable
private fun MusicDownloadRow(
    track: Track,
    status: DownloadStatus,
    progress: Int,
    onStart: () -> Unit,
    onAdvance: () -> Unit,
    onFinish: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(KuiklyTheme.canvas, RoundedCornerShape(KuiklyTheme.radiusMd))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicCoverBlock(title = track.title, size = 40)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    track.title,
                    fontSize = 14.sp,
                    color = KuiklyTheme.foreground,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text("${track.artist} · ${track.durationLabel}", fontSize = 11.sp, color = KuiklyTheme.muted)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(
                        when (status) {
                            DownloadStatus.downloaded -> KuiklyTheme.success
                            DownloadStatus.downloading -> KuiklyTheme.warningSoft
                            DownloadStatus.idle -> KuiklyTheme.canvasSoft2
                        },
                        RoundedCornerShape(KuiklyTheme.radiusSm),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (status) {
                        DownloadStatus.downloaded -> "已下载"
                        DownloadStatus.downloading -> "下载中 $progress%"
                        DownloadStatus.idle -> "未下载"
                    },
                    fontSize = 12.sp,
                    color = when (status) {
                        DownloadStatus.downloaded -> KuiklyTheme.onPrimary
                        DownloadStatus.downloading -> KuiklyTheme.warningDeep
                        DownloadStatus.idle -> KuiklyTheme.ink
                    },
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.width(8.dp))
            when (status) {
                DownloadStatus.idle -> MusicTextButton(text = "下载", onClick = onStart)
                DownloadStatus.downloading -> {
                    MusicTextButton(text = "+25%", onClick = onAdvance)
                    Spacer(Modifier.width(6.dp))
                    MusicTextButton(text = "完成", onClick = onFinish)
                }
                DownloadStatus.downloaded -> MusicTextButton(text = "删除", onClick = onRemove)
            }
        }
    }
}

@Composable
private fun MusicQueueRow(
    index: Int,
    track: Track,
    playing: Boolean,
    onPlay: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                if (playing) KuiklyTheme.linkSoft else KuiklyTheme.canvas,
                RoundedCornerShape(KuiklyTheme.radiusMd),
            )
            .clickable { onPlay() }
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
                    .background(KuiklyTheme.canvasSoft2, RoundedCornerShape(KuiklyTheme.radiusPill)),
                contentAlignment = Alignment.Center,
            ) {
                Text("${index + 1}", fontSize = 11.sp, color = KuiklyTheme.muted, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            MusicCoverBlock(title = track.title, size = 40)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${track.title}${if (playing) "  ▶" else ""}",
                    fontSize = 14.sp,
                    color = if (playing) KuiklyTheme.linkDeep else KuiklyTheme.foreground,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text("${track.artist} · ${track.durationLabel}", fontSize = 11.sp, color = KuiklyTheme.muted)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MusicTextButton(text = "↑ 上移", onClick = onMoveUp)
            Spacer(Modifier.width(6.dp))
            MusicTextButton(text = "↓ 下移", onClick = onMoveDown)
            Spacer(Modifier.width(6.dp))
            MusicTextButton(text = "移除", onClick = onRemove)
        }
    }
}
