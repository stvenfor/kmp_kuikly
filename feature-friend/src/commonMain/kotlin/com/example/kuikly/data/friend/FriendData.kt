package com.example.kuikly.data.friend

import com.example.kuikly.data.BaseMockRepository

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
) {
    // 首字母：取拼音首字符并归一为大写字母，缺失时归到 #。
    val initial: Char
        get() {
            val c = pinyin.firstOrNull() ?: return '#'
            return if (c in 'a'..'z') c - 32 else c
        }

    val initialLabel: String get() = initial.toString()

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
    private val groups = listOf(
        FriendGroup("g1", "我的好友"),
        FriendGroup("g2", "同事"),
        FriendGroup("g3", "家人"),
    )
    private val friends = listOf(
        Friend("f1", "张敏", "", "g1", true, "zhangmin", "138****0011"),
        Friend("f2", "李雷", "", "g2", false, "lilei", "139****0022"),
        Friend("f3", "韩梅梅", "", "g2", true, "hanmeimei", "137****0033"),
        Friend("f4", "妈妈", "", "g3", true, "mama", "136****0044"),
    )

    override fun seed(): List<Friend> = friends
    override fun idOf(item: Friend): String = item.id

    fun groups(): Result<List<FriendGroup>> = all { true }.map { groups }

    fun search(query: String): Result<List<Friend>> =
        all { query.isBlank() || it.name.contains(query, ignoreCase = true) }

    // 搜索：昵称或拼音命中（忽略大小写）。
    fun searchAll(query: String): Result<List<Friend>> =
        all { f ->
            val q = query.trim()
            q.isEmpty() || f.name.contains(q, ignoreCase = true) || f.pinyin.contains(q, ignoreCase = true)
        }

    // 按好友分组聚合：保持 groups 声明顺序。
    fun sectionsByGroup(): Result<List<FriendSection>> =
        all { true }.map { list ->
            groups.map { g ->
                FriendSection(
                    key = g.id,
                    title = g.name,
                    friends = list.filter { it.groupId == g.id }.sortedBy { it.pinyin },
                )
            }.filter { it.friends.isNotEmpty() }
        }

    // 按首字母聚合：A-Z 升序，非字母归到 # 并排在最后。
    fun sectionsByInitial(): Result<List<FriendSection>> =
        all { true }.map { list ->
            list
                .groupBy { it.initialLabel }
                .map { (letter, items) -> FriendSection(letter, letter, items.sortedBy { it.pinyin }) }
                .sortedWith(compareBy<FriendSection> { it.key == "#" }.thenBy { it.key })
        }

    // 聚合视图总人数，便于页面展示"共 N 位联系人"。
    fun totalCount(): Int = seed().size
}

object FriendStore {
    val repo: FriendRepository = FriendRepository()
}
