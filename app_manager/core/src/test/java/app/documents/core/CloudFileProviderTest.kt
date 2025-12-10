package app.documents.core

import android.content.Context
import app.documents.core.account.AccountRepository
import app.documents.core.manager.ManagerRepository
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.request.RequestFavorites
import app.documents.core.network.room.RoomService
import app.documents.core.providers.CloudFileProvider
import app.documents.core.utils.FirebaseTool
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class CloudFileProviderTest {

    @MockK(relaxed = true)
    private lateinit var context: Context
    @MockK
    private lateinit var managerService: ManagerService
    @MockK(relaxed = true)
    private lateinit var roomService: RoomService
    @MockK(relaxed = true)
    private lateinit var managerRepository: ManagerRepository
    @MockK(relaxed = true)
    private lateinit var accountRepository: AccountRepository
    @MockK(relaxed = true)
    private lateinit var firebaseTool: FirebaseTool

    @InjectMockKs
    private lateinit var cloudFileProvider: CloudFileProvider

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

//        or alternatively init with
//        accountRepository = mockk(relaxed = true)

        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    @Test
    fun `addToFavorites with isAdd true should call add service and return success`() {
        // Arrange
        val request = RequestFavorites(fileIds = listOf("file1", "file2"))
        val mockResponse = BaseResponse().apply { status = "0" }
        val successResponse = Response.success(mockResponse)

//        val expectedResponse = BaseResponse().apply { status = "1" }

        every { managerService.addToFavorites(request) } returns Observable.just(successResponse)

        // Act
        val testObserver = cloudFileProvider.addToFavorites(request, isAdd = true).test()

        // Assert
        testObserver.assertNoErrors()
        testObserver.assertComplete()
//        testObserver.assertValue { response -> response.status == "1" }
        testObserver.assertValue(mockResponse)

        verify(exactly = 1) { managerService.addToFavorites(request) }
        verify(exactly = 0) { managerService.deleteFromFavorites(any()) }
    }

    @Test
    fun `addToFavorites with isAdd false should call delete service and return success`() {
        // Arrange
        val request = RequestFavorites(fileIds = listOf("file1"))
        val mockResponse = BaseResponse()
        val successResponse = Response.success(mockResponse)

        every { managerService.deleteFromFavorites(request) } returns Observable.just(successResponse)

        // Act
        val testObserver = cloudFileProvider.addToFavorites(request, isAdd = false).test()

        // Assert
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValue(mockResponse)

        verify(exactly = 1) { managerService.deleteFromFavorites(request) }
        verify(exactly = 0) { managerService.addToFavorites(any()) }
    }

    @Test
    fun `addToFavorites with isAdd true and network error should propagate error`() {
        // Arrange
        val request = RequestFavorites(fileIds = listOf("file1"))
        val error = Throwable("Network error")

        every { managerService.addToFavorites(request) } returns Observable.error(error)

        // Act
        val testObserver = cloudFileProvider.addToFavorites(request, isAdd = true).test()

        // Assert
        testObserver.assertError(error)
        testObserver.assertNotComplete()
    }
}