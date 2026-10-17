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

/**
 * 作业统计（port of source HomeworkStatSummary）。
 * `typeCounts` 对齐源 `Map<HomeworkType, int>`：dubbing / sync / checkin 三类计数。
 */
data class HomeworkStats(
    val totalCount: Int,
    val avgCompletion: Int,
    val classCompletionRate: Double,
    val typeCounts: Map<String, Int> = emptyMap(),
)

/** 学生作业行（port of source StudentHomeworkRow）。 */
data class StudentHomeworkRow(
    val id: String,
    val name: String,
    val completed: Int,
    val pending: Int,
)

/** 学生档案（port of source StudentProfile）。 */
data class StudentProfile(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val homeworkCount: Int,
    /** 完成率，源为 double（如 74.0 / 100.0），Kotlin 必须写 0.0。 */
    val completionRate: Double,
)

/** 作业时间线项（port of source HomeworkTimelineItem）。type: dubbing | sync | checkin。 */
data class HomeworkTimelineItem(
    val id: String,
    val title: String,
    val className: String,
    val dateLabel: String,
    val type: String,
    val isCompleted: Boolean,
)

/** 学生作业子任务（port of source HomeworkTaskItem）。iconColor 用十六位 Int，避免数据层依赖 Compose Color。 */
data class HomeworkTaskItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val starReward: Int,
    val questionCount: Int,
    /** 十六位 ARGB；此 Kuikly Compose 的 `Color` 直接接受 Long 字面量（见 ClassroomPalette）。 */
    val iconColor: Long,
    val iconLabel: String,
)

/** 配音作业子项（port of source DubbingHomeworkItem）。 */
data class DubbingHomeworkItem(
    val id: String,
    val title: String,
    val score: Int,
    val canResubmit: Boolean,
)

/** 配音作业详情（port of source DubbingHomeworkDetail）。 */
data class DubbingHomeworkDetail(
    val id: String,
    val studentName: String,
    val title: String,
    val className: String,
    val deadline: String,
    val description: String,
    val items: List<DubbingHomeworkItem>,
)

/** 视频专辑分集（port of source VideoAlbumPart）。badge 为 null 表示无角标。 */
data class VideoAlbumPart(
    val id: String,
    val title: String,
    val isSelected: Boolean,
    val badge: String?,
)

class ClassroomRepository : BaseMockRepository<ClassInfo>() {
    private val classes = listOf(
        ClassInfo("c1", "英语启蒙 A 班", "JOIN001", 32),
        ClassInfo("c2", "趣味数学 B 班", "JOIN002", 28),
    )
    private val courseSeed = listOf(
        Course("course_001", "班级名称", "王老师", "本周配音作业：跟读 Unit 6 单词与句型，完成录音提交。", "11490rKkz", 6),
        Course("course_002", "三年级2班", "李老师", "同步课堂第 12 讲：两位数乘法，课后完成练习册 P34。", "88201aBxP", 32),
        Course("course_003", "三年级2班 · 每日打卡", "陈老师", "每日晨读打卡，坚持 21 天养成阅读习惯。", "33507mQwR", 28),
        Course("course_004", "兴趣班 · 少儿编程启蒙", "赵老师", "图形化编程入门：用积木块搭建第一个小游戏。", "66014tLpN", 12),
    )
    private val tasks = listOf(
        HomeworkTask("h1", "跟读第 3 单元", "配音作业 · 8 题", "英语启蒙 A 班", "dubbing", 50, 8, true),
        HomeworkTask("h2", "阅读理解", "闯关 · 5 题", "英语启蒙 A 班", "quiz", 30, 5, false),
        HomeworkTask("h3", "口算挑战", "每日一练", "趣味数学 B 班", "quiz", 20, 10, false),
    )

    // ---------- 作业统计 / 学生（新并入，对齐源 classroom_mock_data）----------

    private val studentSeed = listOf(
        StudentHomeworkRow("student_001", "老谭酸菜", 12, 0),
        StudentHomeworkRow("student_002", "乌克丽丽", 12, 0),
        StudentHomeworkRow("student_003", "酸菜", 10, 2),
        StudentHomeworkRow("student_004", "张三李四", 8, 4),
    )
    private val studentProfileSeed = mapOf(
        "student_001" to StudentProfile("student_001", "老谭酸菜", "🐧", 10, 100.0),
        "student_002" to StudentProfile("student_002", "乌克丽丽", "🐧", 10, 100.0),
        "student_003" to StudentProfile("student_003", "酸菜", "🎒", 10, 74.0),
    )
    private val defaultStudentId = "student_001"
    private val timelineSeed = listOf(
        HomeworkTimelineItem("hw_001", "周一（7月10日）配音作业", "三年级2班", "2026 05-21", "dubbing", true),
        HomeworkTimelineItem("hw_002", "周二（7月11日）同步作业", "三年级2班", "2026 05-20", "sync", true),
        HomeworkTimelineItem("hw_003", "周三打卡作业", "三年级2班", "2026 05-19", "checkin", false),
        HomeworkTimelineItem("hw_004", "周四（7月13日）配音作业", "三年级2班", "2026 05-18", "dubbing", false),
    )
    private val studentTaskSeed = listOf(
        HomeworkTaskItem("task_001", "单词学习", "共5道题", 3, 5, 0xFF1890FF, "En"),
        HomeworkTaskItem("task_002", "单词拼写", "共22道题", 3, 22, 0xFF52C41A, "Aa"),
    )
    private val dubbingSeed = DubbingHomeworkDetail(
        "hw_dub_001", "酸菜", "周二（7月11日）配音作业", "三年二班", "7月15日23:59",
        "本次作业核心考验大家的发音能力，重点练习a的发音",
        listOf(
            DubbingHomeworkItem("dub_001", "蜘蛛侠之英雄归来", 100, false),
            DubbingHomeworkItem("dub_002", "侏罗纪世界2：失落王国", 0, true),
            DubbingHomeworkItem("dub_003", "哈利波特与魔法石", 85, false),
        ),
    )
    private val videoPartSeed = listOf(
        VideoAlbumPart("part_1", "Part 1 制服牛油果小怪兽", true, "试听"),
        VideoAlbumPart("part_2", "Part 2 想到制服牛油果...", false, "付费"),
        VideoAlbumPart("part_3", "Part 3 顺利制服...", false, null),
    )

    override fun seed(): List<ClassInfo> = classes
    override fun idOf(item: ClassInfo): String = item.id

    fun tasks(): Result<List<HomeworkTask>> = all { true }.map { tasks }

    fun completeTask(id: String): Result<HomeworkTask> {
        val t = tasks.find { it.id == id } ?: return Result.failure(NoSuchElementException("task=$id"))
        return Result.success(t.copy(isCompleted = true))
    }

    /**
     * 作业统计：对齐源 `statSummary`（作业数 12 / 人均完成 10 / 班级完成率 85%）。
     * 复用 `all` 的 4 态：Error → failure；Empty/Success/Slow → 同一份汇总（详情类数据本就恒定）。
     */
    fun stats(): Result<HomeworkStats> = all { true }.map {
        HomeworkStats(
            totalCount = 12,
            avgCompletion = 10,
            classCompletionRate = 85.0,
            typeCounts = mapOf("dubbing" to 5, "sync" to 3, "checkin" to 1),
        )
    }

    // ---------- 班级（Course，自 demo-skeleton 并入）----------

    /** 班级列表；Empty 场景返回空表，Error 场景返回失败。 */
    fun courses(): Result<List<Course>> = all { true }.map { courseSeed }

    /** 班级详情；找不到或错误场景返回失败。 */
    fun courseDetail(id: String): Result<Course> {
        val course = courseSeed.firstOrNull { it.id == id }
            ?: return Result.failure(NoSuchElementException("course=$id"))
        return all { true }.map { course }
    }

    // ---------- 作业统计子页数据（新并入）----------

    /** 学生作业列表（port of source `statSummary` 下的 students）。 */
    fun students(): Result<List<StudentHomeworkRow>> = all { true }.map { studentSeed }

    /** 学生档案；按 id 查找，缺失回退默认学生。 */
    fun studentProfile(id: String): Result<StudentProfile> {
        val p = studentProfileSeed[id] ?: studentProfileSeed.getValue(defaultStudentId)
        return all { true }.map { p }
    }

    /** 某学生作业时间线（port of source `timelineForStudent`）。 */
    fun timeline(studentId: String): Result<List<HomeworkTimelineItem>> = all { true }.map { timelineSeed }

    /** 学生作业子任务列表（port of source `studentHomeworkTasks`）。 */
    fun studentTasks(): Result<List<HomeworkTaskItem>> = all { true }.map { studentTaskSeed }

    /** 配音作业详情（port of source `dubbingHomework`）。 */
    fun dubbingDetail(): Result<DubbingHomeworkDetail> = all { true }.map { dubbingSeed }

    /** 视频专辑分集（port of source `videoAlbumParts`）。 */
    fun videoParts(): Result<List<VideoAlbumPart>> = all { true }.map { videoPartSeed }
}

object ClassroomStore {
    val repo: ClassroomRepository = ClassroomRepository()
}
