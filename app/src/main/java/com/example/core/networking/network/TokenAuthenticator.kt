package com.example.core.networking.network

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // This logic is called when the server returns a 401 Unauthorized
        
        // 1. Get refresh token
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        // 2. Synchronously refresh the token (using a separate Retrofit instance or OkHttp)
        val newAccessToken = refreshAccessToken(refreshToken) ?: return null

        // 3. Save new token
        tokenManager.saveAccessToken(newAccessToken)

        // 4. Retry the request with the new token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        // Implement your token refresh logic here
        // For example, make a synchronous call to your auth endpoint
        return "new_dummy_token" 
    }
}
