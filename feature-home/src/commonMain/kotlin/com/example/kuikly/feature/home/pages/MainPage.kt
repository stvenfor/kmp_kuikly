package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.base.Utils
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.auth.FakeAuthRepository
import com.example.kuikly.data.chat.ChatStore
import com.example.kuikly.data.chat.Conversation
import com.example.kuikly.data.mine.MineStore
import com.example.kuikly.data.music.MusicPlaybackStore
import com.example.kuikly.data.music.MusicStore
import com.example.kuikly.data.music.Song
import com.example.kuikly.navigation.MainTabLaunch
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Rect
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.graphics.StrokeJoin
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawScope
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawStyle
import com.tencent.kuikly.compose.ui.graphics.drawscope.Fill
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * Product Main shell: 首页 / 聊天 / 社区 / 我的.
 * 首页 = Flutter「首页」dashboard 复刻（见 [HomeTab]）；其余非 Home tab 仍为 Phase-1 形态。
 * 有音频会话时，Tab 栏上方显示 mock 迷你播放条（影响 Home 底部留白）。
 */
@Page(name = "Main", moduleId = "feature_home")
internal class MainPage : BaseComposePager() {

    // Mock 音频会话的 Compose 镜像：MusicList 页写入 MusicPlaybackStore，
    // 本页 pageDidAppear（从上层页面返回）时刷新，驱动迷你条显隐。
    private var musicSong by mutableStateOf<Song?>(null)
    private var musicPlaying by mutableStateOf(false)

    override fun pageDidAppear() {
        super.pageDidAppear()
        refreshMusicPlayback()
    }

    private fun refreshMusicPlayback() {
        musicSong = MusicPlaybackStore.currentSong
        musicPlaying = MusicPlaybackStore.playing
    }

    override fun willInit() {
        super.willInit()
        // Golden capture priming: deep-link pageData params pick the initial tab
        // (scripts/golden-*-capture.sh pass {"tab":"Me"}).
        val params = pageData.params
        when (params.optString("tab")) {
            "Me" -> MainTabLaunch.requestMe()
            "Chat" -> MainTabLaunch.requestChat()
            "Community" -> MainTabLaunch.requestCommunity()
        }
        // ponytail: golden priming — debug-only pageData mockLogin=1 performs one
        // FakeAuth OTP login so capture scripts can render the logged-in Me state.
        if (params.optString("mockLogin") == "1" && !AuthSession.repo.isLoggedIn()) {
            AuthSession.repo.loginWithOtp(
                FakeAuthRepository.MOCK_PHONE,
                FakeAuthRepository.MOCK_OTP,
            )
        }
        // ponytail: golden priming — mockPlay=1 starts a FakeMusic session so
        // capture can show the mini-player + Home inset without UI taps.
        if (params.optString("mockPlay") == "1" && !MusicPlaybackStore.hasSession) {
            MusicStore.repo.list().getOrNull()?.let { songs ->
                songs.firstOrNull()?.let { MusicPlaybackStore.play(it, songs) }
            }
        }
        refreshMusicPlayback()
        // ponytail: golden priming — homeTopTab / homeGreeting 复现 Flutter 参考图里
        // 视频 / Club tab 与问候语状态。Logged-in 名字取 MineStore.displayName
        // （FakeAuthRepository.loginWithOtp 在 mock OTP 登录后写入 `dev-{phone}`，
        // 与 Flutter `backendUser.username ?? email.split('@').first` 风格一致）；
        // guest 不变，沿用 `访客`。TOD 默认取 02b 捕获的「早上好」；
        // 捕获脚本可传 homeGreetingHour=14 锁「下午好」，对齐 Flutter
        // `hour < 12 ? '早上好' : hour < 18 ? '下午好' : '晚上好'`。
        val homeTopTab = params.optString("homeTopTab").toIntOrNull() ?: 0
        val greeting = params.optString("homeGreeting").ifEmpty {
            val name = if (AuthSession.repo.isLoggedIn()) {
                MineStore.repo.profile().getOrNull()?.displayName ?: "访客"
            } else {
                "访客"
            }
            val period = params.optString("homeGreetingHour").toIntOrNull()?.let { hour ->
                when {
                    hour < 12 -> "早上好"
                    hour < 18 -> "下午好"
                    else -> "晚上好"
                }
            } ?: "早上好"
            "$period，$name"
        }
        val statusBarHeight = statusBarInset()
        val bottomInset = bottomSafeInset()
        setContent {
            val initial = when (MainTabLaunch.consume()) {
                MainTabLaunch.Tab.Chat -> MainTab.Chat
                MainTabLaunch.Tab.Community -> MainTab.Community
                MainTabLaunch.Tab.Me -> MainTab.Me
                MainTabLaunch.Tab.Home, null -> MainTab.Home
            }
            var tab by remember { mutableStateOf(initial) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF3F5F8)),
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (tab) {
                        MainTab.Home -> HomeTab(
                            statusBarHeight = statusBarHeight,
                            pageWidth = pagerData.pageViewWidth,
                            miniPlayerVisible = musicSong != null,
                            greeting = greeting,
                            initialTopTab = homeTopTab,
                            onOpenCommunity = { tab = MainTab.Community },
                        )
                        MainTab.Chat -> ChatTab(statusBarHeight)
                        MainTab.Me -> MineTab(statusBarHeight = statusBarHeight, pageWidth = pagerData.pageViewWidth)
                        MainTab.Community -> CommunityTab(statusBarHeight)
                    }
                }
                if (musicSong != null) {
                    // 迷你条 = Flutter `MusicMiniPlayerBar`（P2-W2c 与 MusicList 页共用同一实现，
                    // 见 MusicListPage.kt）。
                    MusicMiniPlayerBar(
                        song = musicSong!!,
                        playing = musicPlaying,
                        onToggle = {
                            MusicPlaybackStore.toggle()
                            refreshMusicPlayback()
                        },
                        onClose = {
                            MusicPlaybackStore.clear()
                            refreshMusicPlayback()
                        },
                    )
                }
                MainBottomBar(
                    selected = tab,
                    onSelect = { tab = it },
                    bottomInset = bottomInset,
                )
            }
        }
    }
}

private enum class MainTab(val label: String) {
    Home("首页"),
    Chat("聊天"),
    Community("社区"),
    Me("我的"),
}

/**
 * Flutter `AppTheme` 底栏令牌 + `IosTabBar` 几何
 * （`commons/ui/lib/widgets/ios_tab_bar.dart`、`commons/ui/lib/theme/app_theme.dart`）。
 *
 * **刻度**：Flutter `IosTabBar` 全用**裸逻辑 px**（49 / 44 / 28 / 22 / 10），
 * 不走 `flutter_screenutil`。故此处一律裸 `dp` / `sp`，**不要** `.su()`——
 * 加 su 会按 548.6/375≈1.463 放大（Pixel_7_Pro 实测 dpr 2.625：
 * 选中 pill 高 73px = 27.8 逻辑 px ≈ Flutter 的 28，宽度 114px ≈ 44）。
 */
private object TabBarTokens {
    val accent = Color(0xFF007AFF)
    val labelSecondary = Color(0x993C3C43)
    val separator = Color(0xFFC6C6C8)
    val background = Color(0xF2FFFFFF)

    const val HAIRLINE = 0.5f
    const val BAR_HEIGHT = 49f
    const val PILL_WIDTH = 44f
    const val PILL_HEIGHT = 28f
    const val PILL_CORNER = 14f
    const val ICON_SIZE = 22f
    const val LABEL_GAP = 2f
    const val LABEL_SIZE = 10f

    /** Flutter `TextStyle(height: 1.1)` × 10。 */
    const val LABEL_LINE_HEIGHT = 11f
}

@Composable
private fun MainBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    bottomInset: Float,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TabBarTokens.background),
    ) {
        // Flutter `BoxDecoration(border: Border(top: 0.5 / separator@0.6))`。
        // ponytail: 无 BackdropFilter blur(20) 等价物 —— `tabBarBackground` 半透明白已够
        // （Flutter ref 实测底栏像素即纯白 255）。
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TabBarTokens.HAIRLINE.dp)
                .background(TabBarTokens.separator.copy(alpha = 0.6f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TabBarTokens.BAR_HEIGHT.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val selectedTab = tab == selected
                val tint = if (selectedTab) TabBarTokens.accent else TabBarTokens.labelSecondary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(TabBarTokens.PILL_WIDTH.dp, TabBarTokens.PILL_HEIGHT.dp)
                            .background(
                                if (selectedTab) {
                                    TabBarTokens.accent.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                },
                                RoundedCornerShape(TabBarTokens.PILL_CORNER.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        TabBarIcon(tab = tab, tint = tint, filled = selectedTab)
                    }
                    Spacer(Modifier.height(TabBarTokens.LABEL_GAP.dp))
                    Text(
                        tab.label,
                        fontSize = TabBarTokens.LABEL_SIZE.sp,
                        lineHeight = TabBarTokens.LABEL_LINE_HEIGHT.sp,
                        fontWeight = if (selectedTab) FontWeight.SemiBold else FontWeight.Normal,
                        color = tint,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.height(bottomInset.dp))
    }
}

/**
 * Tab 图标：Flutter `CupertinoIcons.house / chat_bubble / person_2 / person` 的 Canvas 线稿近似
 * （选中的 `*_fill` 用实心）。
 *
 * ponytail: Kuikly 无 icon font/矢量图资源，用 `Path` + `arc` 画几何体；
 * 与 Cupertino 原图非像素级一致，但语义/尺寸/着色（`#007AFF` vs `#993C3C43`）对齐。
 */
@Composable
private fun TabBarIcon(tab: MainTab, tint: Color, filled: Boolean) {
    Canvas(modifier = Modifier.size(TabBarTokens.ICON_SIZE.dp)) {
        val w = size.width
        val h = size.height
        val style: DrawStyle = if (filled) {
            Fill
        } else {
            Stroke(width = h * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        }
        when (tab) {
            MainTab.Home -> drawPath(homeIconPath(w, h), tint, style = style)
            MainTab.Chat -> drawPath(chatIconPath(w, h), tint, style = style)
            MainTab.Community -> drawCommunityIcon(w, h, tint, style)
            MainTab.Me -> drawPersonIcon(w, h, tint, style)
        }
    }
}

/** 房子：五边形（屋顶 + 屋身），Fill 即 Cupertino `house_fill` 的实心剪影。 */
private fun homeIconPath(w: Float, h: Float): Path = Path().apply {
    moveTo(w * 0.50f, h * 0.05f)
    lineTo(w * 0.95f, h * 0.42f)
    lineTo(w * 0.95f, h * 0.95f)
    lineTo(w * 0.05f, h * 0.95f)
    lineTo(w * 0.05f, h * 0.42f)
    close()
}

/** 对话气泡：椭圆 + 左下小尾巴（Fill 时两段子路径一起填实）。 */
private fun chatIconPath(w: Float, h: Float): Path = Path().apply {
    addOval(Rect(w * 0.05f, h * 0.12f, w * 0.95f, h * 0.80f))
    moveTo(w * 0.30f, h * 0.66f)
    lineTo(w * 0.26f, h * 0.95f)
    lineTo(w * 0.52f, h * 0.74f)
}

/** 单人：头 + 肩（半椭圆；Fill 时 `drawPath` 隐式闭合，Stroke 时保持开弧）。 */
private fun DrawScope.drawPersonIcon(w: Float, h: Float, tint: Color, style: DrawStyle) {
    drawCircle(tint, radius = w * 0.20f, center = Offset(w * 0.50f, h * 0.27f), style = style)
    drawPath(
        Path().apply { addArc(Rect(w * 0.15f, h * 0.55f, w * 0.85f, h * 0.97f), 180f, 180f) },
        tint,
        style = style,
    )
}

/** 双人（社区）：左前 + 右后各一组头/肩。 */
private fun DrawScope.drawCommunityIcon(w: Float, h: Float, tint: Color, style: DrawStyle) {
    drawCircle(tint, radius = w * 0.15f, center = Offset(w * 0.70f, h * 0.27f), style = style)
    drawPath(
        Path().apply { addArc(Rect(w * 0.45f, h * 0.52f, w * 0.97f, h * 0.88f), 180f, 180f) },
        tint,
        style = style,
    )
    drawCircle(tint, radius = w * 0.17f, center = Offset(w * 0.34f, h * 0.30f), style = style)
    drawPath(
        Path().apply { addArc(Rect(w * 0.04f, h * 0.56f, w * 0.66f, h * 0.96f), 180f, 180f) },
        tint,
        style = style,
    )
}

/**
 * 迷你播放条实测高度（Flutter `MusicMiniPlayerBar`：2 进度 + 8×2 内距 + 44 内容 = 62）。
 *
 * 注：Flutter 另有一个**留白常量** `musicMiniPlayerBarHeight = 72`（列表/FAB 避让用），
 * 见 [MUSIC_MINI_BAR_INSET]。本常量只描述条的渲染高度（Home 底部留白引用）。
 */
internal const val MUSIC_MINI_BAR_HEIGHT = 62

/**
 * `ChatTheme` 令牌镜像（`my_ai_project/features/chat/lib/chat/theme/chat_theme.dart`）。
 *
 * **刻度**：Flutter Chat 模块全用**裸逻辑 px**（`chat_page` / `conversation_list_item` /
 * `chat_theme` 均无 `.w/.h/.sp`），不走 `flutter_screenutil`。故此处一律裸 `dp`/`sp`，
 * **不要** `.su()`——判断法与 P2-03 `TabBarTokens` KDoc 相同（若加 su 会 ×1.463 破坏 parity）。
 */
internal object ChatPalette {
    val accent = Color(0xFF007AFF)
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val fillSecondary = Color(0xFFE9E9EB)
    val labelPrimary = Color(0xFF000000)
    val labelSecondary = Color(0x993C3C43)
    val labelTertiary = Color(0x4D3C3C43)
    val separator = Color(0xFFC6C6C8)
    val online = Color(0xFF34C759)
    val unreadBadge = Color(0xFFFF3B30)

    const val RADIUS_MD = 12f
}

/**
 * 「聊天」tab — Flutter `ChatPage` 复刻（Phase-2 / P2-W2b）。
 * 真源：`features/chat/lib/chat/view/chat_page.dart` + `widgets/conversation_list_item.dart`。
 */
@Composable
private fun ChatTab(statusBarHeight: Float) {
    val result = remember { ChatStore.repo.conversations() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatPalette.background),
    ) {
        // Flutter `Padding(EdgeInsets.fromLTRB(16, AppSafeInsets.top + 8, 8, 8))`。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 8.dp,
                    top = (statusBarHeight + 8f).dp,
                    bottom = 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Flutter `ChatTheme.largeTitle`（32 / w700 / 黑）。
            Text(
                "消息",
                modifier = Modifier.weight(1f),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ChatPalette.labelPrimary,
            )
            // Flutter `IconButton(CupertinoIcons.search | square_pencil, color: accent)`
            // （Material IconButton 默认 48×48 / 图标 24，用 22sp 字形 + 13dp padding 近似）。
            Text(
                "⌕",
                fontSize = 22.sp,
                color = ChatPalette.accent,
                modifier = Modifier
                    .clickable { Utils.currentBridgeModule().toast("「搜索」即将接入") }
                    .padding(13.dp),
            )
            Text(
                "✎",
                fontSize = 22.sp,
                color = ChatPalette.accent,
                modifier = Modifier
                    .clickable { Utils.currentBridgeModule().toast("「发起聊天」即将接入") }
                    .padding(13.dp),
            )
        }
        result
            .onSuccess { conversations ->
                if (conversations.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("暂无消息", fontSize = 13.sp, color = ChatPalette.labelSecondary)
                    }
                } else {
                    // Flutter `ListView(padding: EdgeInsets.fromLTRB(16, 8, 16, 24))`
                    // + `ChatTheme.groupedCardDecoration`（白底 r12 卡 + 组内 0.5 分隔线）。
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ChatPalette.surface, RoundedCornerShape(ChatPalette.RADIUS_MD.dp)),
                            ) {
                                conversations.forEachIndexed { index, conversation ->
                                    ConversationRow(conversation = conversation)
                                    if (index < conversations.lastIndex) {
                                        // Flutter `ChatTheme.groupedDivider()`（0.5 / indent 72）。
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 72.dp)
                                                .height(0.5.dp)
                                                .background(ChatPalette.separator),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .onFailure {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        it.message ?: "加载失败",
                        fontSize = 13.sp,
                        color = ChatPalette.labelSecondary,
                    )
                }
            }
    }
}

/** Flutter `ConversationListItem`：52 头像 + 名字/时间 + 摘要/未读红胶囊。 */
@Composable
private fun ConversationRow(conversation: Conversation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatPalette.surface)
            .clickable {
                Utils.currentBridgeModule().openPage(
                    PageNames.ChatDetail,
                    userData = JSONObject().apply { put("id", conversation.id) },
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Flutter `CacheImageUtils.circle(52)` 的未加载占位（灰底圆 + 首字母，同仓库惯例）。
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(ChatPalette.fillSecondary, RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = conversation.title.take(1),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChatPalette.labelPrimary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // 名字（headline 17 / w600）+ 时间（caption 13）。
            Row {
                Text(
                    conversation.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChatPalette.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    conversation.timeLabel,
                    fontSize = 13.sp,
                    color = ChatPalette.labelSecondary,
                )
            }
            Spacer(Modifier.height(4.dp))
            // 摘要（subhead 15）+ 未读红胶囊（r10 / h7v2 / 12 w600 / 99+）。
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    conversation.lastMessage,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    color = ChatPalette.labelSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (conversation.unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(ChatPalette.unreadBadge, RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderTab(title: String, statusBarHeight: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = (statusBarHeight + 24f).dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(12.dp))
        Text(
            "后续竖切 · 本页为占位，非业务 1:1",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
        )
    }
}
