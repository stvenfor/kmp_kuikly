package com.example.kuikly.pages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.kuikly.base.BaseComposePager
import com.tencent.kuikly.compose.animation.AnimatedVisibility
import com.tencent.kuikly.compose.animation.animateColorAsState
import com.tencent.kuikly.compose.animation.core.animateDpAsState
import com.tencent.kuikly.compose.animation.core.spring
import com.tencent.kuikly.compose.animation.core.tween
import com.tencent.kuikly.compose.animation.fadeIn
import com.tencent.kuikly.compose.animation.fadeOut
import com.tencent.kuikly.compose.animation.slideInVertically
import com.tencent.kuikly.compose.animation.slideOutVertically
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page

/** Compose 动画预览：AnimatedVisibility + animate*AsState（见 docs/Compose/animation-system.md） */
@Page("ComposeAnim")
internal class ComposeAnimPage : BaseComposePager() {
    override fun willInit() {
        super.willInit()
        setContent {
            var visible by remember { mutableStateOf(true) }
            var expanded by remember { mutableStateOf(false) }
            val boxColor by animateColorAsState(
                targetValue = if (expanded) Color(0xFF43A047) else Color(0xFFE53935),
                animationSpec = tween(durationMillis = 500),
            )
            val boxSize by animateDpAsState(
                targetValue = if (expanded) 160.dp else 88.dp,
                animationSpec = spring(),
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Compose Anim Lab", fontSize = 22.sp, color = Color.Black)
                Text("AnimatedVisibility / Color / Size", fontSize = 13.sp, color = Color.Gray)

                Button(onClick = { visible = !visible }) {
                    Text(if (visible) "隐藏卡片" else "显示卡片")
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    Box(
                        modifier = Modifier.size(120.dp).background(Color(0xFF1E88E5)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("可见动画", color = Color.White, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(onClick = { expanded = !expanded }) {
                    Text("切换颜色与尺寸")
                }

                Box(
                    modifier = Modifier.size(boxSize).background(boxColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (expanded) "展开" else "收起", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}
