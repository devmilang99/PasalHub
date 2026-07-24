package com.psl.pasalhub.core.di

import com.psl.pasalhub.auth.forgotpassword.data.ForgotPasswordRepositoryImpl
import com.psl.pasalhub.auth.forgotpassword.domain.ForgotPasswordRepository
import com.psl.pasalhub.auth.login.data.LoginRepositoryImpl
import com.psl.pasalhub.auth.login.domain.LoginRepository
import com.psl.pasalhub.auth.register.data.RegisterRepositoryImpl
import com.psl.pasalhub.auth.register.domain.RegisterRepository
import com.psl.pasalhub.core.application.data.AppPreferencesRepositoryImpl
import com.psl.pasalhub.core.application.domain.AppPreferencesRepository
import com.psl.pasalhub.core.auth.data.SupabaseAuthRepositoryImpl
import com.psl.pasalhub.core.auth.domain.SupabaseAuthRepository
import com.psl.pasalhub.dashboard.cart.data.CartRepositoryImpl
import com.psl.pasalhub.dashboard.cart.domain.CartRepository
import com.psl.pasalhub.dashboard.home.data.HomeRepositoryImpl
import com.psl.pasalhub.dashboard.home.domain.HomeRepository
import com.psl.pasalhub.dashboard.order.data.OrderRepositoryImpl
import com.psl.pasalhub.dashboard.order.domain.OrderRepository
import com.psl.pasalhub.dashboard.profile.data.ProfileRepositoryImpl
import com.psl.pasalhub.dashboard.profile.domain.ProfileRepository
import com.psl.pasalhub.initial.data.InitialRepositoryImpl
import com.psl.pasalhub.initial.domain.InitialRepository
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

    @Binds
    @Singleton
    abstract fun bindSupabaseAuthRepository(impl: SupabaseAuthRepositoryImpl): SupabaseAuthRepository
}
