package com.example.kuikly.data.ai

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AiChatSessionTest {

    private fun drain(session: AiChatSession) {
        var guard = 0
        while (session.isStreaming && guard < 1000) {
            session.pump()
            guard++
        }
    }

    @Test
    fun `send appends deltas and stores conversationId from meta`() {
        MockBackend.scenario = MockScenario.Success
        val session = AiChatSession()
        session.start()

        assertTrue(session.send("二手车入口在哪里？"))
        drain(session)

        assertNotNull(session.conversationId)
        assertEquals(AiStreamStatus.Done, session.status)
        val assistant = session.messages.last()
        assertTrue(assistant.text.contains("二手车"))
        assertFalse(assistant.isStreaming)
    }

    @Test
    fun `single in-flight rejects second send`() {
        MockBackend.scenario = MockScenario.Success
        val session = AiChatSession()
        session.start()

        assertTrue(session.send("怎么登录账号？"))
        assertTrue(session.isStreaming)
        assertFalse(session.send("数据分析怎么看？"))

        // welcome + user + assistant，第二条提问被拒
        assertEquals(3, session.messages.size)
    }

    @Test
    fun `stop keeps partial text and ends streaming`() {
        MockBackend.scenario = MockScenario.Slow
        val session = AiChatSession()
        session.start()
        session.send("讲讲转化率")
        session.pump()
        session.pump()
        val partial = session.messages.last().text

        assertTrue(session.stop())
        assertEquals(partial, session.messages.last().text)
        assertTrue(partial.isNotEmpty())
        assertFalse(session.isStreaming)
        assertEquals(AiStreamStatus.Done, session.status)

        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `error sets bubble error and retry resumes`() {
        MockBackend.scenario = MockScenario.Error
        val session = AiChatSession()
        session.start()
        session.send("数据分析怎么看？")
        drain(session)

        assertEquals(AiStreamStatus.Error, session.status)
        val failed = session.messages.last()
        assertNotNull(failed.errorMessage)

        MockBackend.scenario = MockScenario.Success
        assertTrue(session.retry())
        drain(session)

        assertEquals(AiStreamStatus.Done, session.status)
        assertTrue(session.messages.last().errorMessage == null)
        assertNotNull(session.conversationId)
    }

    @Test
    fun `blank prompt is rejected`() {
        MockBackend.scenario = MockScenario.Success
        val session = AiChatSession()
        session.start()
        assertFalse(session.send("   "))
        assertEquals(1, session.messages.size)
    }

    @Test
    fun `multi-turn context is carried into reply`() {
        MockBackend.scenario = MockScenario.Success
        val session = AiChatSession()
        session.start()
        session.send("怎么登录账号？")
        drain(session)
        session.send("还有别的入口吗")
        drain(session)

        val last = session.messages.last()
        assertTrue(last.text.contains("已结合前 1 轮"))
    }

    @Test
    fun `conversation history create switch and delete`() {
        MockBackend.scenario = MockScenario.Success
        val session = AiChatSession()
        val repo = AiConversationRepository()
        val created = repo.create("新对话")

        repo.replaceMessages(created.id, listOf(AiMessage("m1", AiRole.user, "hi")))
        assertEquals(1, repo.byId(created.id).getOrThrow().messages.size)

        assertTrue(repo.remove(created.id))
        assertTrue(repo.byId(created.id).isFailure)
        assertFalse(session.openConversation(created.id))
    }

    @Test
    fun `sse error code mapping`() {
        val repo = AiStreamRepository()
        assertEquals("登录已失效，请重新登录", repo.mapSseError(401))
        assertEquals("登录已失效，请重新登录", repo.mapSseError(403))
        assertEquals("请求过于频繁，请稍后再试", repo.mapSseError(429))
        assertEquals("服务暂不可用，请稍后再试", repo.mapSseError(503))
    }

    @Test
    fun `empty scenario yields no reply text`() {
        MockBackend.scenario = MockScenario.Empty
        val session = AiChatSession()
        session.start()
        session.send("二手车入口在哪里？")
        drain(session)

        assertEquals("", session.messages.last().text)
        assertEquals(AiStreamStatus.Done, session.status)

        MockBackend.scenario = MockScenario.Success
    }

    @Test
    fun `model selection is sent in request and echoed by meta`() {
        MockBackend.scenario = MockScenario.Success
        val session = AiChatSession()
        session.start()
        session.modelId = "gpt-4o-mini"
        session.send("怎么登录账号？")
        drain(session)

        assertEquals("gpt-4o-mini", session.serverModel)
        assertTrue(AiModels.all.any { it.id == session.modelId })
    }
}
