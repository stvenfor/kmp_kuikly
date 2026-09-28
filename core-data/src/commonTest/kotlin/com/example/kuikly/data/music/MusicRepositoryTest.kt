package com.example.kuikly.data.music

import com.example.kuikly.data.mock.MockBackend
import com.example.kuikly.data.mock.MockScenario
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MusicRepositoryTest {
    private lateinit var music: FakeMusicRepository

    @BeforeTest
    fun setUp() {
        MockBackend.scenario = MockScenario.Success
        music = FakeMusicRepository()
        MusicPlaybackStore.clear()
    }

    @Test
    fun success_has_at_least_three_songs() {
        val songs = music.list().getOrThrow()
        assertTrue(songs.size >= 3)
        assertEquals("Ya Ali - DJMaza.Com", songs.first().title)
        assertTrue(songs.all { it.title.isNotBlank() && it.artist.isNotBlank() })
        assertEquals(songs.size, songs.map { it.id }.toSet().size)
    }

    @Test
    fun empty_scenario_returns_empty_list() {
        MockBackend.scenario = MockScenario.Empty
        assertTrue(music.list().getOrThrow().isEmpty())
    }

    @Test
    fun error_scenario_fails() {
        MockBackend.scenario = MockScenario.Error
        assertTrue(music.list().isFailure)
    }

    @Test
    fun unauthorized_scenario_fails() {
        MockBackend.scenario = MockScenario.Unauthorized
        assertTrue(music.list().isFailure)
    }

    @Test
    fun playback_store_smoke_play_toggle_clear() {
        val song = music.list().getOrThrow().first()

        assertFalse(MusicPlaybackStore.hasSession)
        MusicPlaybackStore.play(song)
        assertTrue(MusicPlaybackStore.hasSession)
        assertEquals(song, MusicPlaybackStore.currentSong)
        assertTrue(MusicPlaybackStore.playing)

        MusicPlaybackStore.toggle()
        assertFalse(MusicPlaybackStore.playing)
        MusicPlaybackStore.toggle()
        assertTrue(MusicPlaybackStore.playing)

        MusicPlaybackStore.clear()
        assertFalse(MusicPlaybackStore.hasSession)
        assertNull(MusicPlaybackStore.currentSong)
        assertFalse(MusicPlaybackStore.playing)
    }

    @Test
    fun playback_store_toggle_without_session_is_noop() {
        MusicPlaybackStore.toggle()
        assertFalse(MusicPlaybackStore.hasSession)
        assertFalse(MusicPlaybackStore.playing)
    }
}
