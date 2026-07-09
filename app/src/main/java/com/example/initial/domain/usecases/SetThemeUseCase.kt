package com.example.initial.domain.usecases

import com.example.initial.domain.InitialRepository

class SetThemeUseCase(private val repository: InitialRepository) {
    suspend operator fun invoke(isDark: Boolean) {
        repository.setTheme(isDark)
    }
}
