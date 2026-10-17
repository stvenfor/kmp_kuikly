package com.example.kuikly.data.friend

import com.example.kuikly.data.BaseMockRepository

/** 申请状态（port of source ImFriendRequest.status）。 */
object FriendRequestStatus {
    const val PENDING = "pending"
    const val ACCEPTED = "accepted"
    const val REJECTED = "rejected"
}

/**
 * 好友申请（port of source ImFriendRequest）。
 *
 * [outgoing] 为 true 表示"我发出的申请"（源端仅有收到的列表，发出的用本地态持久化）。
 */
data class FriendRequest(
    val id: String,
    val fromUserId: String,
    val displayName: String,
    val avatarUrl: String = "",
    val phoneMasked: String = "",
    val status: String = FriendRequestStatus.PENDING,
    val createdAt: String = "",
    val outgoing: Boolean = false,
) {
    val isPending: Boolean get() = status == FriendRequestStatus.PENDING

    val subtitle: String
        get() = if (phoneMasked.isEmpty()) "请求加你为好友" else "$phoneMasked · 请求加你为好友"

    val statusLabel: String
        get() = when (status) {
            FriendRequestStatus.ACCEPTED -> "已同意"
            FriendRequestStatus.REJECTED -> "已拒绝"
            else -> "待处理"
        }
}

class FriendRequestRepository(
    private val friendRepo: FriendRepository = FriendStore.repo,
) : BaseMockRepository<FriendRequest>() {
    private val requests = mutableListOf(
        FriendRequest("r1", "u201", "孙悦", "", "133****0201", FriendRequestStatus.PENDING, "10-12 09:20"),
        FriendRequest("r2", "u202", "吴迪", "", "133****0202", FriendRequestStatus.PENDING, "10-13 18:40"),
    )
    private var seq = requests.size

    override fun seed(): List<FriendRequest> = requests.toList()
    override fun idOf(item: FriendRequest): String = item.id

    /** 收到的待处理申请（port of source listIncomingRequests）。 */
    fun incoming(): Result<List<FriendRequest>> = all { !it.outgoing && it.isPending }

    /** 我发出的、还在等待对方同意的申请。 */
    fun outgoingRequests(): Result<List<FriendRequest>> = all { it.outgoing && it.isPending }

    /** 已处理（同意或拒绝）的申请，用于"申请记录"。 */
    fun history(): Result<List<FriendRequest>> = all { !it.isPending }

    /**
     * 发送好友申请（port of source requestFriend）。
     *
     * 失败信息沿用源端后端英文串，由 [friendlyError] 统一转中文提示。
     */
    fun requestFriend(toUserId: String, displayName: String, phoneMasked: String = ""): Result<FriendRequest> {
        val target = toUserId.trim()
        if (target.isEmpty()) return Result.failure(IllegalArgumentException("user not found"))
        if (target == SELF_ID) return Result.failure(IllegalArgumentException("cannot friend yourself"))
        if (friendRepo.isFriend(target)) return Result.failure(IllegalStateException("already friends"))
        if (requests.any { it.outgoing && it.fromUserId == target && it.isPending }) {
            return Result.failure(IllegalStateException("already pending"))
        }
        seq += 1
        val request = FriendRequest(
            id = "r$seq",
            fromUserId = target,
            displayName = displayName.ifBlank { target },
            phoneMasked = phoneMasked,
            status = FriendRequestStatus.PENDING,
            createdAt = "刚刚",
            outgoing = true,
        )
        requests.add(request)
        return Result.success(request)
    }

    /**
     * 同意 / 拒绝申请（port of source respondFriend）。
     *
     * 同意后把对方写入好友表，使其满足单聊准入。
     */
    fun respond(requestId: String, accept: Boolean): Result<FriendRequest> {
        val index = requests.indexOfFirst { it.id == requestId }
        if (index < 0 || !requests[index].isPending) {
            return Result.failure(IllegalStateException("not the request recipient"))
        }
        val request = requests[index]
        val updated = request.copy(
            status = if (accept) FriendRequestStatus.ACCEPTED else FriendRequestStatus.REJECTED,
        )
        requests[index] = updated
        if (accept && !friendRepo.isFriend(request.fromUserId)) {
            friendRepo.addFriend(request.fromUserId, request.displayName, request.phoneMasked)
        }
        return Result.success(updated)
    }

    /** 单聊准入（port of source canPrivateChat）。 */
    fun canPrivateChat(peerUserId: String): Boolean = friendRepo.canPrivateChat(peerUserId)

    companion object {
        /** mock 当前登录用户（好友表里的 f1）。 */
        const val SELF_ID = "f1"

        /** port of source ImFriendApi.friendlyError：后端英文错误转可读中文提示。 */
        fun friendlyError(e: Throwable, fallback: String = "操作失败"): String {
            val raw = e.message ?: ""
            val lower = raw.lowercase()
            if (lower.contains("already pending")) return "已发送过申请，等待对方同意"
            if (lower.contains("already friends")) return "你们已经是好友了"
            if (lower.contains("cannot friend yourself")) return "不能添加自己"
            if (lower.contains("user not found")) return "用户不存在"
            if (lower.contains("not the request recipient")) return "无权处理该申请"
            if (raw.isBlank()) return fallback
            return raw
        }
    }
}

object FriendRequestStore {
    val repo: FriendRequestRepository = FriendRequestRepository()
}
