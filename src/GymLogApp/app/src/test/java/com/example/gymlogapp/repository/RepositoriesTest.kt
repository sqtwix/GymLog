package com.example.gymlogapp.repository

import com.example.gymlogapp.api.ApiClient
import com.example.gymlogapp.model.CreateTrainRequest
import com.example.gymlogapp.model.RegisterRequest
import com.example.gymlogapp.utils.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class RepositoriesTest {
    private val clients = mutableListOf<HttpClient>()
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @After
    fun closeClients() {
        clients.forEach { it.close() }
    }

    private fun client(engine: MockEngine): HttpClient =
        ApiClient.create(engine, "http://localhost/").also { clients += it }

    @Test
    fun loginSendsCredentialsAndSavesTokenAfterDecodingResponse() = runBlocking {
        val store = InMemoryTokenStore("old-token")
        val client = client(MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/api/User/login", request.url.encodedPath)
            assertNull(request.headers[HttpHeaders.Authorization])
            val body = request.body as TextContent
            assertEquals(ContentType.Application.Json, body.contentType.withoutParameters())
            val json = Json.parseToJsonElement(body.text).jsonObject
            assertEquals("test@example.com", json["email"]!!.jsonPrimitive.content)
            assertEquals("password123", json["password"]!!.jsonPrimitive.content)
            respond(
                """{"token":"new-token","user":{"id":7,"username":"Test","email":"test@example.com"},"extra":true}""",
                HttpStatusCode.OK,
                jsonHeaders
            )
        })

        val result = AuthRepository(client, store).login("test@example.com", "password123")

        assertEquals("new-token", store.value)
        assertEquals(7, result.user?.id)
    }

    @Test
    fun loginAcceptsTokenWithoutOptionalUserProfile() = runBlocking {
        val store = InMemoryTokenStore()
        val client = client(MockEngine {
            respond("""{"token":"access-token"}""", HttpStatusCode.OK, jsonHeaders)
        })

        val response = AuthRepository(client, store).login("test@example.com", "password123")

        assertEquals("access-token", store.value)
        assertNull(response.user)
    }

    @Test
    fun rejectedLoginDoesNotReplaceExistingToken() = runBlocking {
        val store = InMemoryTokenStore("existing-token")
        val client = client(MockEngine {
            respond("Unauthorized", HttpStatusCode.Unauthorized)
        })

        try {
            AuthRepository(client, store).login("test@example.com", "wrong-password")
            fail("HTTP 401 must not be treated as a successful login")
        } catch (error: ClientRequestException) {
            assertEquals(HttpStatusCode.Unauthorized, error.response.status)
        }
        assertEquals("existing-token", store.value)
    }

    @Test
    fun blankTokenIsNotSaved() = runBlocking {
        val store = InMemoryTokenStore("existing-token")
        val client = client(MockEngine {
            respond("""{"token":" "}""", HttpStatusCode.OK, jsonHeaders)
        })

        try {
            AuthRepository(client, store).login("test@example.com", "password123")
            fail("A blank token must not replace the session")
        } catch (_: IllegalStateException) {
            assertEquals("existing-token", store.value)
        }
    }

    @Test
    fun registrationSendsAllFieldsAndAcceptsEmptySuccessResponse() = runBlocking {
        val store = InMemoryTokenStore("existing-token")
        val client = client(MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/api/User/register", request.url.encodedPath)
            assertNull(request.headers[HttpHeaders.Authorization])
            val json = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
            assertEquals("Test", json["name"]!!.jsonPrimitive.content)
            assertEquals("test@example.com", json["email"]!!.jsonPrimitive.content)
            assertEquals("password123", json["password"]!!.jsonPrimitive.content)
            assertEquals("Other", json["gender"]!!.jsonPrimitive.content)
            assertEquals("2000-01-02", json["birthDay"]!!.jsonPrimitive.content)
            respond("", HttpStatusCode.NoContent)
        })

        AuthRepository(client, store).register(
            RegisterRequest("Test", "test@example.com", "password123", "Other", "2000-01-02")
        )

        assertEquals("existing-token", store.value)
    }

    @Test
    fun workoutsDecodeNullableAndMissingOptionalFields() = runBlocking {
        val client = client(MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/api/trains", request.url.encodedPath)
            assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
            respond(
                """[{"type":"Strength","description":null,"date":"2026-11-08","duration":45,"unused":true},{"type":"Run","date":"2026-11-09","duration":30}]""",
                HttpStatusCode.OK,
                jsonHeaders
            )
        })

        val workouts = WorkoutRepository(client, InMemoryTokenStore("access-token")).getWorkouts()

        assertEquals(2, workouts.size)
        assertEquals("Strength", workouts[0].type)
        assertEquals(45, workouts[0].duration)
        assertNull(workouts[0].description)
        assertNull(workouts[1].description)
        assertEquals(0, workouts[1].userId)
    }

    @Test
    fun sameClientUsesLatestTokenAndStopsSendingItAfterLogout() = runBlocking {
        val store = InMemoryTokenStore("first-token")
        val headers = mutableListOf<String?>()
        val client = client(MockEngine { request ->
            headers += request.headers[HttpHeaders.Authorization]
            respond("[]", HttpStatusCode.OK, jsonHeaders)
        })
        val workouts = WorkoutRepository(client, store)

        workouts.getWorkouts()
        store.saveToken("second-token")
        workouts.getWorkouts()
        AuthRepository(client, store).logout()
        workouts.getWorkouts()

        assertEquals(listOf("Bearer first-token", "Bearer second-token", null), headers)
        assertNull(store.value)
    }

    @Test
    fun createSendsWorkoutAndDoesNotRequireAResponseBody() = runBlocking {
        val client = client(MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/api/trains/train", request.url.encodedPath)
            assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
            val json = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
            assertEquals("Strength", json["type"]!!.jsonPrimitive.content)
            assertEquals("Leg day", json["description"]!!.jsonPrimitive.content)
            assertEquals("2026-11-08", json["date"]!!.jsonPrimitive.content)
            assertEquals("45", json["duration"]!!.jsonPrimitive.content)
            assertEquals("0", json["userId"]!!.jsonPrimitive.content)
            respond("", HttpStatusCode.Created)
        })

        WorkoutRepository(client, InMemoryTokenStore("access-token")).createWorkout(
            CreateTrainRequest("Strength", "Leg day", "2026-11-08", 45, 0)
        )
    }

    @Test
    fun deleteSendsDateAndAcceptsNoContent() = runBlocking {
        val client = client(MockEngine { request ->
            assertEquals(HttpMethod.Delete, request.method)
            assertEquals("/api/trains/delete/2026-11-08", request.url.encodedPath)
            assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
            respond("", HttpStatusCode.NoContent)
        })

        WorkoutRepository(client, InMemoryTokenStore("access-token")).deleteWorkout("2026-11-08")
    }

    @Test
    fun failedDeleteIsNotReportedAsSuccess() = runBlocking {
        val client = client(MockEngine {
            respond("Server error", HttpStatusCode.InternalServerError)
        })

        try {
            WorkoutRepository(client, InMemoryTokenStore()).deleteWorkout("2026-11-08")
            fail("HTTP 500 must propagate to the screen error handler")
        } catch (error: ServerResponseException) {
            assertEquals(HttpStatusCode.InternalServerError, error.response.status)
        }
    }

    @Test
    fun cancelledLoginPropagatesCancellationAndDoesNotSaveToken() = runBlocking {
        val store = InMemoryTokenStore()
        val client = client(MockEngine { throw CancellationException("Request cancelled") })

        try {
            AuthRepository(client, store).login("test@example.com", "password123")
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertNull(store.value)
        }
    }

    private class InMemoryTokenStore(var value: String? = null) : TokenStore {
        override fun saveToken(token: String) { value = token }
        override fun getToken(): String? = value
        override fun clearToken() { value = null }
    }
}
