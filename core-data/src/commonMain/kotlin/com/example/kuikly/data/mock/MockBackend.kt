package com.example.kuikly.data.mock

enum class MockScenario {
    Success,
    Empty,
    Error,
    Unauthorized,
    Slow,
}

object MockBackend {
    var scenario: MockScenario = MockScenario.Success

    fun cycle(): MockScenario {
        scenario = when (scenario) {
            MockScenario.Success -> MockScenario.Empty
            MockScenario.Empty -> MockScenario.Error
            MockScenario.Error -> MockScenario.Unauthorized
            MockScenario.Unauthorized -> MockScenario.Slow
            MockScenario.Slow -> MockScenario.Success
        }
        return scenario
    }
}
