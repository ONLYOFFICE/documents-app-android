package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.login.StorageLoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

sealed interface StorageLoginRepository {

    suspend fun signIn(code: String): Flow<Result<*>>

    suspend fun refreshToken(): Flow<Result<*>>
}

internal abstract class StorageLoginRepositoryImpl<U, D : StorageLoginDataSource<U>>(
    private val accountRepository: AccountRepository,
    private val storageLoginDataSource: D
) : StorageLoginRepository {

    abstract fun mapToCloudAccount(user: U): CloudAccount

    override suspend fun signIn(code: String): Flow<Result<*>> {
        return flow {
            val response = storageLoginDataSource.signIn(code)
            accountRepository.addAccount(
                cloudAccount = mapToCloudAccount(storageLoginDataSource.getUserInfo(response.accessToken)),
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
            emit(null)
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun refreshToken(): Flow<Result<*>> {
        return flow {
            val response = storageLoginDataSource.refreshToken(requireNotNull(accountRepository.getRefreshToken()))
            accountRepository.updateAccount(
                token = response.accessToken,
                refreshToken = response.refreshToken
            )
            emit(null)
        }.flowOn(Dispatchers.IO)
            .asResult()
    }
}
