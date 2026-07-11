package com.example.core.di

import com.example.auth.forgotpassword.data.ForgotPasswordRepositoryImpl
import com.example.auth.forgotpassword.domain.ForgotPasswordRepository
import com.example.auth.login.data.LoginRepositoryImpl
import com.example.auth.login.domain.LoginRepository
import com.example.auth.register.data.RegisterRepositoryImpl
import com.example.auth.register.domain.RegisterRepository
import com.example.core.application.data.AppPreferencesRepositoryImpl
import com.example.core.application.domain.AppPreferencesRepository
import com.example.dashboard.cart.data.CartRepositoryImpl
import com.example.dashboard.cart.domain.CartRepository
import com.example.dashboard.home.data.HomeRepositoryImpl
import com.example.dashboard.home.domain.HomeRepository
import com.example.dashboard.order.data.OrderRepositoryImpl
import com.example.dashboard.order.domain.OrderRepository
import com.example.dashboard.profile.data.ProfileRepositoryImpl
import com.example.dashboard.profile.domain.ProfileRepository
import com.example.initial.data.InitialRepositoryImpl
import com.example.initial.domain.InitialRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLoginRepository(impl: LoginRepositoryImpl): LoginRepository

    @Binds
    @Singleton
    abstract fun bindRegisterRepository(impl: RegisterRepositoryImpl): RegisterRepository

    @Binds
    @Singleton
    abstract fun bindForgotPasswordRepository(impl: ForgotPasswordRepositoryImpl): ForgotPasswordRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindInitialRepository(impl: InitialRepositoryImpl): InitialRepository

    @Binds
    @Singleton
    abstract fun bindAppPreferencesRepository(impl: AppPreferencesRepositoryImpl): AppPreferencesRepository
}
