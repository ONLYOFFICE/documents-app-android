package app.documents.core.login

import app.documents.core.network.common.Result
import kotlinx.coroutines.flow.Flow

sealed interface StorageLoginRepository {

    suspend fun signIn(code: String): Flow<Result<*>>

    suspend fun refreshToken(): Flow<Result<*>>
}