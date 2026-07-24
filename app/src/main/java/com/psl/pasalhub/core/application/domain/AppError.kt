package com.psl.pasalhub.core.application.domain

sealed class AppError {
    data class Network(val message: String = "No Internet Connection") : AppError()
    data class Database(val message: String = "Database Integrity Error") : AppError()
    data class Generic(val title: String, val message: String) : AppError()
}
