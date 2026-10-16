package com.example.kuikly.data.classroom

import com.example.kuikly.data.BaseMockRepository

/** 班级（port of source ClassInfo）。 */
data class ClassInfo(
    val id: String,
    val name: String,
    val inviteCode: String,
    val memberCount: Int,
)

/** 作业任务（port of source HomeworkTaskItem / HomeworkTimelineItem）。 */
data class HomeworkTask(
    val id: String,
    val title: String,
    val subtitle: String,
    val className: String,
    val type: String, // reading | dubbing | quiz
    val starReward: Int,
    val questionCount: Int,
    val isCompleted: Boolean,
)

/** 作业统计（port of source HomeworkStatSummary）。 */
data class HomeworkStats(
    val totalCount: Int,
    val avgCompletion: Int,
    val classCompletionRate: Double,
)

class ClassroomRepository : BaseMockRepository<ClassInfo>() {
    private val classes = listOf(
        ClassInfo("c1", "英语启蒙 A 班", "JOIN001", 32),
        ClassInfo("c2", "趣味数学 B 班", "JOIN002", 28),
    )
    private val tasks = listOf(
        HomeworkTask("h1", "跟读第 3 单元", "配音作业 · 8 题", "英语启蒙 A 班", "dubbing", 50, 8, true),
        HomeworkTask("h2", "阅读理解", "闯关 · 5 题", "英语启蒙 A 班", "quiz", 30, 5, false),
        HomeworkTask("h3", "口算挑战", "每日一练", "趣味数学 B 班", "quiz", 20, 10, false),
    )

    override fun seed(): List<ClassInfo> = classes
    override fun idOf(item: ClassInfo): String = item.id

    fun tasks(): Result<List<HomeworkTask>> = all { true }.map { tasks }

    fun completeTask(id: String): Result<HomeworkTask> {
        val t = tasks.find { it.id == id } ?: return Result.failure(NoSuchElementException("task=$id"))
        return Result.success(t.copy(isCompleted = true))
    }

    fun stats(): Result<HomeworkStats> {
        val total = tasks.size
        val done = tasks.count { it.isCompleted }
        val rate = if (total == 0) 0.0 else done.toDouble() / total
        return Result.success(HomeworkStats(total, if (total == 0) 0 else done * 100 / total, rate))
    }
}

object ClassroomStore {
    val repo: ClassroomRepository = ClassroomRepository()
}
