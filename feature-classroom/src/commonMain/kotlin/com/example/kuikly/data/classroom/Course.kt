package com.example.kuikly.data.classroom

data class Course(
    val id: String,
    val title: String,
    val teacher: String,
    val summary: String,
    /** Flutter `ClassInfo.inviteCode`（班级卡「邀请码：…」，`classroom_mock_data.dart`）。 */
    val inviteCode: String = "",
    /** Flutter `ClassInfo.memberCount`（班级卡「班级成员：…」）。 */
    val memberCount: Int = 0,
)
