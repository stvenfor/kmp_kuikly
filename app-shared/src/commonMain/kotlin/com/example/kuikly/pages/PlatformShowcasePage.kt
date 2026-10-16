package com.example.kuikly.pages

import androidx.compose.runtime.Composable
import com.example.kuikly.base.BaseComposePager
import com.example.kuikly.platform.bluetooth.BluetoothService
import com.example.kuikly.platform.chart.ChartCatalog
import com.example.kuikly.platform.dokit.DokitService
import com.example.kuikly.platform.dokitbootstrap.DokitBootstrapService
import com.example.kuikly.platform.faceverify.FaceVerifyService
import com.example.kuikly.platform.im.ImService
import com.example.kuikly.platform.linking.LinkingService
import com.example.kuikly.platform.loginsharepay.LoginSharePayService
import com.example.kuikly.platform.push.PushService
import com.example.kuikly.platform.realtime.RealtimeService
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/**
 * 平台能力总览页（对应源项目 components 下各移动端原生能力模块）。
 *
 * 全部能力均为 Mock 实现；真实 SDK 接入时替换各 Service 的 [Any] nativeCall 即可，
 * 页面与调用方无需改动。
 */
@Page("PlatformShowcase")
internal class PlatformShowcasePage : BaseComposePager() {

    /** 能力条目：(显示名, 可用性探针, 说明) */
    private val capabilities: List<Triple<String, () -> Boolean, String>> = listOf(
        Triple("IM 即时通讯", { ImService.isAvailable() }, "对应 wys_chat / rongcloud_im"),
        Triple("消息推送", { PushService.isAvailable() }, "对应 wys_push"),
        Triple("实时通信", { RealtimeService.isAvailable() }, "对应 realtime"),
        Triple("深度链接", { LinkingService.isAvailable() }, "对应 linking"),
        Triple("人脸核身", { FaceVerifyService.isAvailable() }, "对应 wys_face_verify"),
        Triple("蓝牙", { BluetoothService.isAvailable() }, "对应 bluetooth"),
        Triple("登录/分享/支付", { LoginSharePayService.isAvailable() }, "对应 wys_login_share_pay"),
        Triple("Dokit 调试", { DokitService.isAvailable() }, "对应 dokit"),
        Triple("Dokit 引导", { DokitBootstrapService.isAvailable() }, "对应 dokit_bootstrap"),
    )

    override fun willInit() {
        super.willInit()
        val items = capabilities
        setContent {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("平台能力总览", fontSize = 22.sp, color = Color(0xFF171717))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "全部为 Mock 实现 · 当前场景=${ChartCatalog.scenarioLabel()}",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                )
                Spacer(Modifier.height(16.dp))

                Text("图表能力 (wys_chart)", fontSize = 16.sp, color = Color(0xFF171717))
                Spacer(Modifier.height(8.dp))
                val charts = ChartCatalog.load().getOrDefault(emptyList())
                if (charts.isEmpty()) {
                    Text("当前场景无图表数据", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                } else {
                    charts.forEach { spec -> CapabilityRow(spec.name, spec.desc, true) }
                }

                Spacer(Modifier.height(20.dp))
                Text("原生能力", fontSize = 16.sp, color = Color(0xFF171717))
                Spacer(Modifier.height(8.dp))
                items.forEach { item -> CapabilityRow(item.first, item.third, item.second()) }
            }
        }
    }
}

@Composable
private fun CapabilityRow(name: String, desc: String, available: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(Color(0xFFF7F7F8), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text("$name  ·  ${if (available) "可用" else "降级"}", fontSize = 15.sp, color = Color(0xFF171717))
        Spacer(Modifier.height(2.dp))
        Text(desc, fontSize = 11.sp, color = Color(0xFF6B7280))
    }
}
