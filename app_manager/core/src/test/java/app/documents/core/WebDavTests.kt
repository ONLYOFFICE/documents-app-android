package app.documents.core

import android.content.Context
import android.net.Uri
import android.os.Environment
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.webdav.WebDavService
import app.documents.core.providers.WebDavFileProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import lib.toolkit.base.managers.utils.StringUtils
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import java.io.File

class WebDavFileProviderTest {

    @MockK
    private lateinit var webDavService: WebDavService

    @MockK
    private lateinit var context: Context

    @InjectMockKs
    private lateinit var webDavFileProvider: WebDavFileProvider

    @Before
    fun setUp() {
        mockkStatic(Environment::class)
        mockkStatic(Uri::class)
        every { Environment.getExternalStorageDirectory() } returns File("/")
        every { Uri.encode(any(), any()) } answers { firstArg() }
        every { Uri.encode(any()) } answers { firstArg() }

        MockKAnnotations.init(this)
//        webDavFileProvider = WebDavFileProvider(webDavService, context)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
        unmockkStatic(Environment::class)
        unmockkStatic(Uri::class)
    }

    @Test
    fun `rename should return updated CloudFile on successful rename`() {
        // Given
        val oldId = "http://example.com/dav/files/user/test.txt"
        val oldTitle = "test.txt"
        val fileExtension = ".txt"
        val newNameWithoutExt = "renamed"
        val version = 1

        val cloudFile = CloudFile().apply {
            id = oldId
            title = oldTitle
            fileExst = fileExtension
        }

        val encodedNewName = StringUtils.getEncodedString(newNameWithoutExt) + fileExtension
        val newId = "http://example.com/dav/files/user/" + encodedNewName

        val mockCall: Call<ResponseBody> = mockk()
        val mockResponse: Response<ResponseBody> = Response.success(mockk())

        every { webDavService.move(newId, oldId, "F") } returns mockCall
        every { mockCall.execute() } returns mockResponse

        // When
        val testObserver = webDavFileProvider.rename(cloudFile, newNameWithoutExt, version).test()

        // Then
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val resultItem = testObserver.values()[0] as CloudFile
        assertEquals(newId, resultItem.id)
        assertEquals(newNameWithoutExt + fileExtension, resultItem.title)

        verify(exactly = 1) { webDavService.move(newId, oldId, "F") }
    }

//    @Test
//    fun `rename should return updated CloudFolder on successful rename`() {
//        // Given
//        val oldId = "http://example.com/dav/files/user/oldFolder/"
//        val oldTitle = "oldFolder"
//        val newName = "newFolder"
//
//        val cloudFolder = CloudFolder().apply {
//            id = oldId
//            title = oldTitle
//        }
//
//        val newId = "http://example.com/dav/files/user/newFolder/"
//        val encodedCorrectPath = StringUtils.getEncodedString(newId)!!
//
//        val mockCall: Call<ResponseBody> = mockk()
//        val mockResponse: Response<ResponseBody> = Response.success(mockk())
//
//        every { webDavService.move(encodedCorrectPath, oldId, "F") } returns mockCall
//        every { mockCall.execute() } returns mockResponse
//
//        // When
//        val testObserver = webDavFileProvider.rename(cloudFolder, newName, null).test()
//
//        // Then
//        testObserver.assertNoErrors()
//        testObserver.assertComplete()
//
//        val result = testObserver.values()[0] as CloudFolder
//        assertEquals(newId, result.id)
//        assertEquals(newName, result.title)
//
//        verify(exactly = 1) { webDavService.move(encodedCorrectPath, oldId, "F") }
//    }
//
//    @Test
//    fun `rename should return error on failure`() {
//        // Given
//        val oldId = "http://example.com/dav/files/user/test.txt"
//        val oldTitle = "test.txt"
//        val fileExtension = ".txt"
//        val newNameWithoutExt = "renamed"
//        val version = 1
//
//        val cloudFile = CloudFile().apply {
//            id = oldId
//            title = oldTitle
//            fileExst = fileExtension
//        }
//
//        val encodedNewName = StringUtils.getEncodedString(newNameWithoutExt) + fileExtension
//        val newId = "http://example.com/dav/files/user/" + encodedNewName
//
//        val mockCall: Call<ResponseBody> = mockk()
//        val mockResponse: Response<ResponseBody> = Response.error(404, mockk(relaxed = true))
//
//        every { webDavService.move(newId, oldId, "F") } returns mockCall
//        every { mockCall.execute() } returns mockResponse
//
//        // When
//        val testObserver = webDavFileProvider.rename(cloudFile, newNameWithoutExt, version).test()
//
//        // Then
//        testObserver.assertError(HttpException::class.java)
//
//        verify(exactly = 1) { webDavService.move(newId, oldId, "F") }
//    }
//
//    @Test
//    fun `createFolder should return new CloudFolder on successful creation`() {
//        // Given
//        val folderId = "http://example.com/dav/files/user/"
//        val newFolderName = "newFolder"
//        val requestCreate = RequestCreate().apply { title = newFolderName }
//        val newFolderPath = folderId + newFolderName
//
//        val mockCall: Call<ResponseBody> = mockk()
//        val mockResponse: Response<ResponseBody> = Response.success(mockk())
//
//        every { webDavService.createFolder(newFolderPath) } returns mockCall
//        every { mockCall.execute() } returns mockResponse
//
//        // When
//        val testObserver = webDavFileProvider.createFolder(folderId, requestCreate).test()
//
//        // Then
//        testObserver.assertNoErrors()
//        testObserver.assertComplete()
//        testObserver.assertValueCount(1)
//
//        val result = testObserver.values()[0]
//        assertEquals(newFolderName, result.title)
//        assertEquals(newFolderPath, result.id)
//
//        verify(exactly = 1) { webDavService.createFolder(newFolderPath) }
//    }
//
//    @Test
//    fun `createFolder should return error on failure`() {
//        // Given
//        val folderId = "http://example.com/dav/files/user/"
//        val newFolderName = "newFolder"
//        val requestCreate = RequestCreate().apply { title = newFolderName }
//        val newFolderPath = folderId + newFolderName
//
//        val mockCall: Call<ResponseBody> = mockk()
//        val mockResponse: Response<ResponseBody> = Response.error(500, mockk(relaxed = true))
//
//        every { webDavService.createFolder(newFolderPath) } returns mockCall
//        every { mockCall.execute() } returns mockResponse
//
//        // When
//        val testObserver = webDavFileProvider.createFolder(folderId, requestCreate).test()
//
//        // Then
//        testObserver.assertError(HttpException::class.java)
//
//        verify(exactly = 1) { webDavService.createFolder(newFolderPath) }
//    }
}
