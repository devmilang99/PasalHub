package com.example.initial.domain.usecases

import com.example.initial.domain.InitialRepository

class CompleteOnboardingUseCase(private val repository: InitialRepository) {
    suspend operator fun invoke() {
        repository.completeOnboarding()
    }
}
