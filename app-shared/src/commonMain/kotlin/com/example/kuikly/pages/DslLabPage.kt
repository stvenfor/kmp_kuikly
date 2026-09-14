package com.example.kuikly.pages

import com.example.kuikly.base.BasePager
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

@Page("DslLab")
internal class DslLabPage : BasePager() {
    override fun body(): ViewBuilder {
        return {
            attr {
                allCenter()
            }
            Text {
                attr {
                    text("Legacy DSL Track")
                    fontSize(22f)
                }
            }
            Text {
                attr {
                    text("Teaching contrast vs Compose Track")
                    fontSize(14f)
                    marginTop(12f)
                }
            }
        }
    }
}
