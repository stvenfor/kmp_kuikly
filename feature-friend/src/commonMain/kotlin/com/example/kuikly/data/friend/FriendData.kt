package com.example.kuikly.data.friend

import com.example.kuikly.data.BaseMockRepository

/** 备注最大长度（源端无约束，移动端按输入框惯例收紧）。 */
const val FRIEND_REMARK_MAX = 20

/** 单个好友最多标签数。 */
const val FRIEND_TAG_MAX = 6

/** 好友分组（port of source FriendGroup）。 */
data class FriendGroup(val id: String, val name: String)

/** 好友（port of source Friend）。 */
data class Friend(
    val id: String,
    val name: String,
    val avatar: String,
    val groupId: String,
    val isOnline: Boolean,
    val pinyin: String = "",
    val phoneMasked: String = "",
    val bio: String = "",
    // 备注：为空时对外仍显示 name（port of source 备注为空回落昵称）。
    val remark: String = "",
    val tags: List<String> = emptyList(),
    // 黑名单：拉黑后不出现在联系人分组建里，只在黑名单页可见。
    val blocked: Boolean = false,
) {
    // 首字母：取拼音首字符并归一为大写字母，缺失时归到 #。
    val initial: Char
        get() {
            val c = pinyin.firstOrNull() ?: return '#'
            return if (c in 'a'..'z') c - 32 else c
        }

    val initialLabel: String get() = initial.toString()

    val subtitle: String get() = if (phoneMasked.isEmpty()) id else phoneMasked

    /** 对外显示名：备注优先（port of source displayName）。 */
    val displayName: String get() = if (remark.isBlank()) name else remark

    val tagLabel: String get() = if (tags.isEmpty()) "未设置标签" else tags.joinToString(" · ")
}

/** 搜索命中的用户（port of source ImFriendUser：可能是好友，也可能是陌生人）。 */
data class FriendCandidate(
    val id: String,
    val name: String,
    val pinyin: String = "",
    val phoneMasked: String = "",
    val desc: String = "",
    val isFriend: Boolean = false,
) {
    val subtitle: String get() = if (phoneMasked.isEmpty()) id else phoneMasked
}

/** 聚合后的联系人分组（按首字母或按好友分组）。 */
data class FriendSection(
    val key: String,
    val title: String,
    val friends: List<Friend>,
) {
    val count: Int get() = friends.size
    val onlineCount: Int get() = friends.count { it.isOnline }
}

class FriendRepository : BaseMockRepository<Friend>() {
    private val groups = mutableListOf(
        FriendGroup("g1", "我的好友"),
        FriendGroup("g2", "同事"),
        FriendGroup("g3", "家人"),
    )
    private val friends = mutableListOf(
        Friend("f1", "张敏", "", "g1", true, "zhangmin", "138****0011", "英语启蒙课主讲，擅长用儿歌带孩子开口说。"),
        Friend("f2", "李雷", "", "g2", false, "lilei", "139****0022", "产品同学，周末一起爬山遛娃。"),
        Friend("f3", "韩梅梅", "", "g2", true, "hanmeimei", "137****0033", "英语教研组，负责中高年级阅读。"),
        Friend("f4", "妈妈", "", "g3", true, "mama", "136****0044", "家里最忙的班主任，最爱提醒我喝水。"),
    )
    // 可被搜索到的非好友用户（port of source ImFriendApi.search 的远端用户池）。
    private val strangers = mutableListOf(
        FriendCandidate("u101", "周涛", "zhoutao", "135****0101", "同事推荐的健身搭档"),
        FriendCandidate("u102", "吴敏", "wumin", "135****0102", "同城书友会组织者"),
        FriendCandidate("u103", "郑凯", "zhengkai", "135****0103", "羽毛球群群主"),
        FriendCandidate("u104", "孙丽", "sunli", "135****0104", "前同事，做增长投放"),
    )
    private var groupSeq = groups.size

    override fun seed(): List<Friend> = friends.toList()
    override fun idOf(item: Friend): String = item.id

    /** 好友详情（自 demo-skeleton 并入的 FriendDetail 页用）。 */
    fun detail(id: String): Result<Friend> = byId(id)

    fun groups(): Result<List<FriendGroup>> = all { true }.map { groups.toList() }

    fun search(query: String): Result<List<Friend>> =
        all { query.isBlank() || it.name.contains(query, ignoreCase = true) }

    // 搜索：昵称或拼音命中（忽略大小写）。
    fun searchAll(query: String): Result<List<Friend>> =
        all { f ->
            val q = query.trim()
            q.isEmpty() || f.name.contains(q, ignoreCase = true) || f.pinyin.contains(q, ignoreCase = true)
        }

    // 聚合视图总人数，便于页面展示"共 N 位联系人"。
    fun totalCount(): Int = seed().size

    // 联系人口径：拉黑的人不进分组/首字母聚合。
    private fun contactable(): List<Friend> = friends.filter { !it.blocked }

    // 按好友分组聚合：保持 groups 声明顺序。
    fun sectionsByGroup(): Result<List<FriendSection>> =
        all { true }.map { list ->
            val visible = list.filter { !it.blocked }
            groups.map { g ->
                FriendSection(
                    key = g.id,
                    title = g.name,
                    friends = visible.filter { it.groupId == g.id }.sortedBy { it.pinyin },
                )
            }.filter { it.friends.isNotEmpty() }
        }

    // 按首字母聚合：A-Z 升序，非字母归到 # 并排在最后。
    fun sectionsByInitial(): Result<List<FriendSection>> =
        all { true }.map { list ->
            list.filter { !it.blocked }
                .groupBy { it.initialLabel }
                .map { (letter, items) -> FriendSection(letter, letter, items.sortedBy { it.pinyin }) }
                .sortedWith(compareBy<FriendSection> { it.key == "#" }.thenBy { it.key })
        }

    /**
     * 用户搜索：昵称 / 备注 / 拼音 / 用户 ID / 手机号命中，
     * 好友在前、陌生人在后（port of source ImFriendApi.search）。
     */
    fun searchUsers(query: String): Result<List<FriendCandidate>> =
        all { true }.map { list ->
            val q = query.trim()
            if (q.isEmpty()) {
                emptyList()
            } else {
                val friendHits = list
                    .filter { !it.blocked && hit(it.name, it.remark, it.pinyin, it.id, it.phoneMasked, q) }
                    .map { it.toCandidate() }
                val known = list.map { it.id }.toSet()
                val strangerHits = strangers.filter { !known.contains(it.id) && hit(it.name, "", it.pinyin, it.id, it.phoneMasked, q) }
                friendHits + strangerHits
            }
        }

    private fun hit(
        name: String,
        remark: String,
        pinyin: String,
        id: String,
        phoneMasked: String,
        q: String,
    ): Boolean =
        name.contains(q, ignoreCase = true) ||
            remark.contains(q, ignoreCase = true) ||
            pinyin.contains(q, ignoreCase = true) ||
            id.contains(q, ignoreCase = true) ||
            phoneMasked.contains(q)

    private fun Friend.toCandidate(): FriendCandidate = FriendCandidate(
        id = id,
        name = displayName,
        pinyin = pinyin,
        phoneMasked = phoneMasked,
        desc = bio,
        isFriend = true,
    )

    /** 是否已是好友（申请前校验用）。 */
    fun isFriend(id: String): Boolean = friends.any { it.id == id }

    fun friendIds(): Set<String> = friends.map { it.id }.toSet()

    /** 同意申请后把对方写入好友表（port of source respondFriend accept=true 后的可单聊状态）。 */
    fun addFriend(id: String, name: String, phoneMasked: String = "", groupId: String = ""): Result<Friend> {
        val target = id.trim()
        if (target.isEmpty()) return Result.failure(IllegalArgumentException("user not found"))
        if (friends.any { it.id == target }) return Result.failure(IllegalStateException("already friends"))
        val friend = Friend(
            id = target,
            name = name.ifBlank { target },
            avatar = "",
            groupId = if (groupId.isBlank()) groups.first().id else groupId,
            isOnline = false,
            pinyin = "",
            phoneMasked = phoneMasked,
            bio = "",
        )
        friends.add(friend)
        return Result.success(friend)
    }

    /** 单聊准入：必须已是好友且未拉黑（port of source ImFriendApi.canPrivateChat）。 */
    fun canPrivateChat(peerUserId: String): Boolean {
        val friend = friends.firstOrNull { it.id == peerUserId } ?: return false
        return !friend.blocked
    }

    // --- 分组管理 ---

    /** 新建分组：同名拒绝，空名拒绝。 */
    fun createGroup(name: String): Result<FriendGroup> {
        val value = name.trim()
        if (value.isEmpty()) return Result.failure(IllegalArgumentException("分组名不能为空"))
        if (groups.any { it.name == value }) return Result.failure(IllegalArgumentException("已存在同名分组"))
        groupSeq += 1
        val group = FriendGroup("g$groupSeq", value)
        groups.add(group)
        return Result.success(group)
    }

    /** 重命名分组。 */
    fun renameGroup(id: String, name: String): Result<FriendGroup> {
        val value = name.trim()
        if (value.isEmpty()) return Result.failure(IllegalArgumentException("分组名不能为空"))
        if (groups.any { it.id != id && it.name == value }) return Result.failure(IllegalArgumentException("已存在同名分组"))
        val index = groups.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(NoSuchElementException("分组不存在"))
        val updated = FriendGroup(id, value)
        groups[index] = updated
        return Result.success(updated)
    }

    /** 删除分组：至少保留一个，组内好友回落到首个剩余分组，返回被迁移人数。 */
    fun deleteGroup(id: String): Result<Int> {
        if (groups.size <= 1) return Result.failure(IllegalStateException("至少保留一个分组"))
        val index = groups.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(NoSuchElementException("分组不存在"))
        val fallback = groups.first { it.id != id }.id
        var moved = 0
        friends.indices.forEach { i ->
            if (friends[i].groupId == id) {
                friends[i] = friends[i].copy(groupId = fallback)
                moved += 1
            }
        }
        groups.removeAt(index)
        return Result.success(moved)
    }

    /** 删除分组的预演：返回将被迁移的人数与目标分组名，供二次确认展示。 */
    fun deleteGroupImpact(id: String): Pair<Int, String> {
        val fallback = groups.firstOrNull { it.id != id } ?: return Pair(0, "")
        return Pair(friends.count { it.groupId == id }, fallback.name)
    }

    /** 移动好友到指定分组。 */
    fun moveFriend(friendId: String, groupId: String): Result<Friend> {
        if (groups.none { it.id == groupId }) return Result.failure(NoSuchElementException("分组不存在"))
        val index = friends.indexOfFirst { it.id == friendId }
        if (index < 0) return Result.failure(NoSuchElementException("好友不存在"))
        val updated = friends[index].copy(groupId = groupId)
        friends[index] = updated
        return Result.success(updated)
    }

    fun memberCount(groupId: String): Int = contactable().count { it.groupId == groupId }

    // --- 好友属性 ---

    /** 修改备注：超长拒绝，允许清空（清空后回落昵称）。 */
    fun setRemark(friendId: String, remark: String): Result<Friend> {
        val value = remark.trim()
        if (value.length > FRIEND_REMARK_MAX) {
            return Result.failure(IllegalArgumentException("备注最多 ${FRIEND_REMARK_MAX} 个字"))
        }
        return update(friendId) { it.copy(remark = value) }
    }

    /** 设置标签：去空白、去重、限制条数。 */
    fun setTags(friendId: String, tags: List<String>): Result<Friend> {
        val clean = tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (clean.size > FRIEND_TAG_MAX) {
            return Result.failure(IllegalArgumentException("最多 ${FRIEND_TAG_MAX} 个标签"))
        }
        return update(friendId) { it.copy(tags = clean) }
    }

    /** 加入 / 移出黑名单。 */
    fun setBlocked(friendId: String, blocked: Boolean): Result<Friend> =
        update(friendId) { it.copy(blocked = blocked) }

    fun blockedFriends(): Result<List<Friend>> = all { it.blocked }

    /** 删除好友。 */
    fun removeFriend(friendId: String): Result<Boolean> {
        val index = friends.indexOfFirst { it.id == friendId }
        if (index < 0) return Result.failure(NoSuchElementException("好友不存在"))
        friends.removeAt(index)
        return Result.success(true)
    }

    private fun update(friendId: String, transform: (Friend) -> Friend): Result<Friend> {
        val index = friends.indexOfFirst { it.id == friendId }
        if (index < 0) return Result.failure(NoSuchElementException("好友不存在"))
        val updated = transform(friends[index])
        friends[index] = updated
        return Result.success(updated)
    }
}

object FriendStore {
    val repo: FriendRepository = FriendRepository()
}
