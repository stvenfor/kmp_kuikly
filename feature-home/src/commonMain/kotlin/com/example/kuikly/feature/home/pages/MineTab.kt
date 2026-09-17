package com.example.kuikly.feature.home.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.LocalDesignScale
import com.example.kuikly.base.ProvideDesignScale
import com.example.kuikly.base.Utils
import com.example.kuikly.base.su
import com.example.kuikly.base.susp
import com.example.kuikly.data.auth.AuthSession
import com.example.kuikly.data.mine.FakeMineRepository
import com.example.kuikly.data.mine.MineProfile
import com.example.kuikly.data.mine.MineStore
import com.example.kuikly.navigation.LoginRedirect
import com.example.kuikly.navigation.MainTabLaunch
import com.example.kuikly.navigation.PageNames
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.HorizontalDivider
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

/**
 * 「我的」tab root. Flutter-density Mine parity (Phase-2 / P2-02, polish P2-V1b).
 *
 * Sections (mirroring Flutter `MinePage` / `MineHeaderWidget` etc.):
 *  - top icon bar (`我的` + info / calendar / settings / login-logout)
 *  - header card: 72dp avatar · displayName + RoleBadge · storeLine + chevron ·
 *    `电子名片` chip + maskedPhone（guest 文案对齐 Flutter：访客/未登录/— — —，
 *    无 `登录 / 注册` 按钮 —— 登录入口在右上角图标）
 *  - stats bar: guest 4 列（加入天数/员工数/店铺天数/累计客户，全 0，Flutter
 *    `guestStats`）；登录后沿用 repo 3 列（客户/订单/跟进）
 *  - 4-up 常用服务 (商城/HOT, 我的钱包, 我的课程, 我的订单)
 *  - 2×4 个人功能 grid（Flutter `mine_function_data.dart` catalog 8 项，
 *    aspectRatio 0.92）+ `长按拖动顺序` hint
 *  - 6-row menu list (商务合作 / 提醒事项 / 邀请好友 / 粉丝群 / 意见反馈 / 设置)
 *
 * Guest vs logged-in via [AuthSession.repo.isLoggedIn]. Settings / Login real
 * navigations preserved.
 *
 * All design units are `.su` / `.susp` (Flutter 375×812 scale). Status bar inset
 * stays unscaled (raw device px).
 */
@Composable
internal fun MineTab(statusBarHeight: Float, pageWidth: Float) {
    val repo = MineStore.repo
    val auth = AuthSession.repo
    val isLoggedIn = auth.isLoggedIn()
    val profile = repo.profile().getOrNull()
    val quickServices = repo.quickServices()
    val functions = repo.functions()
    val menu = repo.menu()

    val bridge = Utils.currentBridgeModule()
    val openLogin: () -> Unit = {
        LoginRedirect.setPending(PageNames.Main)
        MainTabLaunch.requestMe()
        bridge.openPage(PageNames.Login)
    }
    val openSettings: () -> Unit = {
        bridge.openPage(PageNames.Settings)
    }
    val showLater: (String) -> Unit = { label ->
        bridge.toast("「${label}」即将接入")
    }

    ProvideDesignScale(pageWidth) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MinePalette.background),
        ) {
            item {
                MineTopBar(
                    isLoggedIn = isLoggedIn,
                    statusBarHeight = statusBarHeight,
                    onLoginClick = openLogin,
                    onSettingsClick = openSettings,
                    showLater = showLater,
                )
            }
            item { MineHeaderCard(isLoggedIn = isLoggedIn, profile = profile) }
            item { Spacer(Modifier.height(16.su)) }
            item { MineStatsBar(isLoggedIn = isLoggedIn, profile = profile) }
            item { Spacer(Modifier.height(8.su)) }
            item { MineQuickServicesSection(items = quickServices, showLater = showLater) }
            item { MineFunctionGrid(showLater = showLater) }
            item { MineMenuList(items = menu, openSettings = openSettings, showLater = showLater) }
            item { Spacer(Modifier.height(24.su)) }
        }
    }
}

// ─────────────────────────── palette (Flutter MineTheme parity) ───────────────────────────

private object MinePalette {
    val accent = Color(0xFF007AFF)
    val background = Color(0xFFF2F2F7)
    val surface = Color(0xFFFFFFFF)
    val fillSecondary = Color(0xFFE9E9EB)
    val labelPrimary = Color(0xFF000000)
    val labelSecondary = Color(0x993C3C43)
    val labelTertiary = Color(0x4D3C3C43)
    val separator = Color(0xFFC6C6C8)
    val destructive = Color(0xFFFF3B30)
    val badgeRed = Color(0xFFFF3B30)
    val iconBlue = Color(0xFF007AFF)
    val iconPurple = Color(0xFF5856D6)
    val iconOrange = Color(0xFFFF9500)
    val iconGreen = Color(0xFF34C759)
    val softBlue = Color(0x14007AFF)
    val softPurple = Color(0x145856D6)
    val softOrange = Color(0x14FF9500)
    val radiusMdDesign = 12
}

// ─────────────────────────── static source specs ───────────────────────────

private data class QuickServiceSpec(
    val key: String,
    val label: String,
    val iconColor: Color,
    val glyph: String,
    val badge: String?,
)

// Repo keys are "courses"/"orders" (FakeMineRepository); spec keys must match.
private val QUICK_SERVICES = listOf(
    QuickServiceSpec("mall", "商城", MinePalette.iconBlue, "🛍", "HOT"),
    QuickServiceSpec("wallet", "我的钱包", MinePalette.iconPurple, "💳", null),
    QuickServiceSpec("courses", "我的课程", MinePalette.iconOrange, "▶", null),
    QuickServiceSpec("orders", "我的订单", MinePalette.iconGreen, "🗎", null),
)

private data class FunctionSpec(
    val title: String,
    val subtitle: String,
    val accent: Color,
    val iconColor: Color,
    val glyph: String,
    val extraValue: String? = null,
)

/**
 * 视觉规格：Flutter `mine_function_data.dart` catalog 的 8 项镜像。
 * 数据层（core-data）不在本 ticket 文件锁内，故网格直接按 Flutter 默认顺序渲染
 * 这 8 项（P2-V1b 视觉对齐）；repo `functions()` 仍用于驱动非视觉逻辑/测试。
 */
private val FUNCTION_DISPLAY: Map<String, FunctionSpec> = mapOf(
    "sms" to FunctionSpec("短信模板", "一键发送 轻松快捷", MinePalette.softBlue, MinePalette.iconBlue, "✉"),
    "calculator" to FunctionSpec("购车计算器", "全款/贷款/保险全能算", MinePalette.softBlue, MinePalette.iconBlue, "🧮", "5830.00"),
    "used_car" to FunctionSpec("二手车", "置换/专卖/估价", MinePalette.softBlue, MinePalette.iconBlue, "🚗"),
    "short_video" to FunctionSpec("小视频", "用小视频秀车秀店", MinePalette.softPurple, MinePalette.iconPurple, "🎬"),
    "after_sales" to FunctionSpec("售后专区", "售后维修保养记录", MinePalette.softOrange, MinePalette.iconOrange, "🔧"),
    "qr_pay" to FunctionSpec("店铺收款码", "常见问题 功能介绍", MinePalette.softBlue, MinePalette.iconBlue, "▦"),
    "qa" to FunctionSpec("选买问答", "在线解答客户问题", MinePalette.softBlue, MinePalette.iconBlue, "💬"),
    "poster" to FunctionSpec("商家海报", "置换/专卖/估价", MinePalette.softOrange, MinePalette.iconOrange, "📊"),
)

/** Flutter `MineFunctionData.defaultOrderIds`. */
private val FUNCTION_ORDER = listOf(
    "sms", "calculator", "used_car", "short_video",
    "after_sales", "qr_pay", "qa", "poster",
)

private data class MenuSpec(
    val key: String,
    val label: String,
    val glyph: String,
    val showBadge: Boolean = false,
)

private val MENU_ITEMS = listOf(
    MenuSpec("cooperation", "商务合作", "👥"),
    MenuSpec("reminder", "提醒事项", "🔔"),
    MenuSpec("invite", "邀请好友", "👤"),
    MenuSpec("fan_group", "粉丝群", "💬", showBadge = true),
    MenuSpec("feedback", "意见反馈", "🏷"),
    MenuSpec("settings", "设置", "⚙"),
)

private fun specForMenu(key: String): MenuSpec =
    MENU_ITEMS.firstOrNull { it.key == key }
        ?: MenuSpec(key, key, "•", showBadge = false)

// ─────────────────────────── top icon bar ───────────────────────────

@Composable
private fun MineTopBar(
    isLoggedIn: Boolean,
    statusBarHeight: Float,
    onLoginClick: () -> Unit,
    onSettingsClick: () -> Unit,
    showLater: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.su,
                end = 8.su,
                // Flutter `AppSafeInsets.top(context) + 8`：8 为裸字面量（不吃 screenutil）。
                top = (statusBarHeight + 8).dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "我的",
            fontSize = 32.susp,
            fontWeight = FontWeight.Bold,
            color = MinePalette.labelPrimary,
            modifier = Modifier.weight(1f),
        )
        TopIcon("ℹ") { showLater("信息") }
        TopIcon("📅") { showLater("签到日历") }
        TopIcon("⚙", onClick = onSettingsClick)
        TopIcon(
            glyph = if (isLoggedIn) "↪" else "👤+",
            onClick = if (isLoggedIn) {
                { showLater("退出登录") }
            } else onLoginClick,
        )
    }
}

@Composable
private fun TopIcon(glyph: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.su)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 22.susp, color = MinePalette.accent)
    }
}

// ─────────────────────────── header card ───────────────────────────

@Composable
private fun MineHeaderCard(isLoggedIn: Boolean, profile: MineProfile?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su)
            .background(MinePalette.surface, RoundedCornerShape(MinePalette.radiusMdDesign.su))
            .border(0.5.su, MinePalette.separator, RoundedCornerShape(MinePalette.radiusMdDesign.su))
            .padding(16.su),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Avatar placeholder: 72dp circle, surface 2dp ring, fillSecondary bg.
            // Flutter guest placeholder is person_fill icon (36, labelTertiary).
            Box(
                modifier = Modifier
                    .size(72.su)
                    .background(MinePalette.fillSecondary, CircleShape)
                    .border(2.su, MinePalette.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (!isLoggedIn) {
                        "👤"
                    } else {
                        profile?.displayName?.take(1) ?: "?"
                    },
                    fontSize = if (!isLoggedIn) 36.susp else 24.susp,
                    fontWeight = FontWeight.SemiBold,
                    color = MinePalette.labelTertiary,
                )
            }
            Spacer(Modifier.width(16.su))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Flutter guest: displayName '访客' + roleBadge '未登录'.
                    Text(
                        text = if (!isLoggedIn) {
                            "访客"
                        } else {
                            profile?.displayName ?: FakeMineRepository.GUEST_DISPLAY_NAME
                        },
                        fontSize = 20.susp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinePalette.labelPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    val role = profile?.roleBadge
                    if (!isLoggedIn) {
                        Spacer(Modifier.width(8.su))
                        RoleBadge("未登录")
                    } else if (role != null) {
                        Spacer(Modifier.width(8.su))
                        RoleBadge(role)
                    }
                }
                Spacer(Modifier.height(8.su))
                // Flutter store row always renders with chevron_down (guest copy
                // is '登录后查看门店信息').
                val storeText = if (isLoggedIn) {
                    profile?.storeLine ?: "— — —"
                } else {
                    "登录后查看门店信息"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        Utils.currentBridgeModule().toast("「切换门店」即将接入")
                    },
                ) {
                    Text(
                        storeText,
                        fontSize = 13.susp,
                        color = MinePalette.labelSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(4.su))
                    Text("▾", fontSize = 14.susp, color = MinePalette.labelSecondary)
                }
                Spacer(Modifier.height(12.su))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MetaChip("💳", "电子名片") {
                        Utils.currentBridgeModule().toast("「电子名片」即将接入")
                    }
                    // Flutter guest maskedPhone is '— — —' (always rendered).
                    val phone = profile?.maskedPhone ?: "— — —"
                    Spacer(Modifier.width(16.su))
                    Text(phone, fontSize = 13.susp, color = MinePalette.labelSecondary)
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(label: String) {
    Row(
        modifier = Modifier
            .background(MinePalette.accent, RoundedCornerShape(6.su))
            .padding(horizontal = 8.su, vertical = 4.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("✓", fontSize = 12.susp, color = Color.White)
        Spacer(Modifier.width(4.su))
        Text(label, fontSize = 11.susp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun MetaChip(glyph: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(MinePalette.accent.copy(alpha = 0.08f), RoundedCornerShape(20.su))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.su, vertical = 6.su),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(glyph, fontSize = 14.susp, color = MinePalette.accent)
        Spacer(Modifier.width(4.su))
        Text(label, fontSize = 13.susp, color = MinePalette.accent)
    }
}

// ─────────────────────────── stats bar ───────────────────────────

@Composable
private fun MineStatsBar(isLoggedIn: Boolean, profile: MineProfile?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su)
            .background(MinePalette.surface, RoundedCornerShape(MinePalette.radiusMdDesign.su))
            .border(0.5.su, MinePalette.separator, RoundedCornerShape(MinePalette.radiusMdDesign.su))
            .padding(vertical = 20.su, horizontal = 8.su),
    ) {
        if (!isLoggedIn) {
            // Flutter guestStats: 4 columns, all "0" (加入天数/员工数/店铺天数/累计客户).
            StatBlock("0", "加入天数", Modifier.weight(1f))
            StatBlock("0", "员工数", Modifier.weight(1f))
            StatBlock("0", "店铺天数", Modifier.weight(1f))
            StatBlock("0", "累计客户", Modifier.weight(1f))
        } else {
            val stats = profile?.stats
            StatBlock(stats?.customerCount?.toString() ?: "0", "客户", Modifier.weight(1f))
            StatBlock(stats?.orderCount?.toString() ?: "0", "订单", Modifier.weight(1f))
            StatBlock(stats?.followUpCount?.toString() ?: "0", "跟进", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.susp, fontWeight = FontWeight.Bold, color = MinePalette.labelPrimary)
        Spacer(Modifier.height(6.su))
        Text(label, fontSize = 13.susp, color = MinePalette.labelSecondary)
    }
}

// ─────────────────────────── quick services ───────────────────────────

@Composable
private fun MineQuickServicesSection(
    items: List<com.example.kuikly.data.mine.MineQuickService>,
    showLater: (String) -> Unit,
) {
    // Repositories supply 4 keys; we map to Flutter visual specs by key with fallback.
    val specs = items.map { item ->
        QUICK_SERVICES.firstOrNull { it.key == item.key }
            ?: QuickServiceSpec(item.key, item.label, MinePalette.iconBlue, "•", null)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su),
    ) {
        Text(
            "常用服务",
            fontSize = 17.susp,
            fontWeight = FontWeight.SemiBold,
            color = MinePalette.labelPrimary,
        )
        Spacer(Modifier.height(12.su))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MinePalette.surface, RoundedCornerShape(MinePalette.radiusMdDesign.su))
                .border(0.5.su, MinePalette.separator, RoundedCornerShape(MinePalette.radiusMdDesign.su))
                .padding(vertical = 20.su, horizontal = 8.su),
        ) {
            specs.forEach { svc ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showLater(svc.label) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(modifier = Modifier.size(44.su), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(44.su)
                                .background(svc.iconColor.copy(alpha = 0.12f), RoundedCornerShape(12.su)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(svc.glyph, fontSize = 22.susp, color = svc.iconColor)
                        }
                        val badge = svc.badge
                        if (badge != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.su, y = (-6).su)
                                    .background(MinePalette.badgeRed, RoundedCornerShape(6.su))
                                    .padding(horizontal = 5.su, vertical = 2.su),
                            ) {
                                Text(
                                    badge,
                                    fontSize = 9.susp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.su))
                    Text(
                        svc.label,
                        fontSize = 12.susp,
                        color = MinePalette.labelPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ─────────────────────────── function grid ───────────────────────────

@Composable
private fun MineFunctionGrid(showLater: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.su),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.su, end = 16.su, bottom = 12.su),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "个人功能",
                fontSize = 17.susp,
                fontWeight = FontWeight.SemiBold,
                color = MinePalette.labelPrimary,
            )
            Spacer(Modifier.weight(1f))
            Text("长按拖动顺序", fontSize = 13.susp, color = MinePalette.labelSecondary)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.su),
        ) {
            val specs = FUNCTION_ORDER.map { FUNCTION_DISPLAY.getValue(it) }
            specs.chunked(2).forEachIndexed { rowIdx, rowSpecs ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.su),
                ) {
                    rowSpecs.forEach { spec ->
                        FunctionCard(
                            spec = spec,
                            onTap = { showLater(spec.title) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowSpecs.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                // Flutter grid mainAxisSpacing 12 — only between rows.
                if (rowIdx < (specs.size + 1) / 2 - 1) {
                    Spacer(Modifier.height(12.su))
                }
            }
        }
    }
}

@Composable
private fun FunctionCard(spec: FunctionSpec, onTap: () -> Unit, modifier: Modifier) {
    // Flutter grid childAspectRatio 0.92：cell 宽 (375-32-12)/2=165.5 → 高 ≈180 设计单位。
    Column(
        modifier = modifier
            .height(180.su)
            .background(MinePalette.surface, RoundedCornerShape(MinePalette.radiusMdDesign.su))
            .border(0.5.su, MinePalette.separator, RoundedCornerShape(MinePalette.radiusMdDesign.su))
            .clickable(onClick = onTap)
            .padding(16.su),
    ) {
        Box(
            modifier = Modifier
                .size(44.su)
                .background(spec.accent, RoundedCornerShape(12.su)),
            contentAlignment = Alignment.Center,
        ) {
            Text(spec.glyph, fontSize = 24.susp, color = spec.iconColor)
        }
        Spacer(Modifier.weight(1f))
        Text(
            spec.title,
            fontSize = 16.susp,
            fontWeight = FontWeight.SemiBold,
            color = MinePalette.labelPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.su))
        Text(
            spec.subtitle,
            fontSize = 12.susp,
            color = MinePalette.labelSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val extra = spec.extraValue
        if (extra != null) {
            Spacer(Modifier.height(8.su))
            Text(extra, fontSize = 16.susp, fontWeight = FontWeight.Bold, color = spec.iconColor)
        }
    }
}

// ─────────────────────────── menu list ───────────────────────────

@Composable
private fun MineMenuList(
    items: List<com.example.kuikly.data.mine.MineMenuItem>,
    openSettings: () -> Unit,
    showLater: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.su, end = 16.su, top = 16.su),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MinePalette.surface, RoundedCornerShape(MinePalette.radiusMdDesign.su))
                .border(0.5.su, MinePalette.separator, RoundedCornerShape(MinePalette.radiusMdDesign.su)),
        ) {
            items.forEachIndexed { idx, item ->
                val spec = specForMenu(item.key)
                val onTap: () -> Unit = if (item.key == "settings") openSettings else {
                    { showLater(item.label) }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTap)
                        .padding(horizontal = 16.su, vertical = 14.su),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(spec.glyph, fontSize = 22.susp, color = MinePalette.accent)
                    Spacer(Modifier.width(14.su))
                    Text(
                        item.label,
                        fontSize = 15.susp,
                        color = MinePalette.labelPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (spec.showBadge) {
                        Box(
                            modifier = Modifier
                                .size(8.su)
                                .background(MinePalette.badgeRed, CircleShape),
                        )
                        Spacer(Modifier.width(8.su))
                    }
                    Text("›", fontSize = 16.susp, color = MinePalette.labelTertiary)
                }
                if (idx < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.su),
                        color = MinePalette.separator,
                    )
                }
            }
        }
    }
}