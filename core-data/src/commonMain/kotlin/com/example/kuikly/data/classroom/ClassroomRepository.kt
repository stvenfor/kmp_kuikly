package com.example.kuikly.data.classroom

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario

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

interface ClassroomRepository {
    fun list(): Result<List<Course>>
    fun detail(id: String): Result<Course>
}

/**
 * Mock classroom class list, mirroring the Flutter `ClassroomMockData.classes` seed
 * (班级名 / 邀请码 / 班级成员；Kuikly 保留 Phase-1 的 teacher/summary 供详情页使用).
 */
class FakeClassroomRepository : ClassroomRepository {
    private val seed = listOf(
        Course(
            id = "course_001",
            title = "班级名称",
            teacher = "王老师",
            summary = "本周配音作业：跟读 Unit 6 单词与句型，完成录音提交。",
            inviteCode = "11490rKkz",
            memberCount = 6,
        ),
        Course(
            id = "course_002",
            title = "三年级2班",
            teacher = "李老师",
            summary = "同步课堂第 12 讲：两位数乘法，课后完成练习册 P34。",
            inviteCode = "88201aBxP",
            memberCount = 32,
        ),
        Course(
            id = "course_003",
            title = "三年级2班 · 每日打卡",
            teacher = "陈老师",
            summary = "每日晨读打卡，坚持 21 天养成阅读习惯。",
            inviteCode = "33507mQwR",
            memberCount = 28,
        ),
        Course(
            id = "course_004",
            title = "兴趣班 · 少儿编程启蒙",
            teacher = "赵老师",
            summary = "图形化编程入门：用积木块搭建第一个小游戏。",
            inviteCode = "66014tLpN",
            memberCount = 12,
        ),
    )

    override fun list(): Result<List<Course>> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow -> Result.success(seed)
        MockScenario.Empty -> Result.success(emptyList())
        MockScenario.Error -> Result.failure(IllegalStateException("mock classroom error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }

    override fun detail(id: String): Result<Course> = when (MockBackend.scenario) {
        MockScenario.Success, MockScenario.Slow ->
            seed.firstOrNull { it.id == id }
                ?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("course not found: $id"))
        MockScenario.Empty -> Result.failure(IllegalStateException("course not found: $id"))
        MockScenario.Error -> Result.failure(IllegalStateException("mock classroom error"))
        MockScenario.Unauthorized -> Result.failure(IllegalStateException("unauthorized"))
    }
}

object ClassroomStore {
    val repo: ClassroomRepository = FakeClassroomRepository()
}
