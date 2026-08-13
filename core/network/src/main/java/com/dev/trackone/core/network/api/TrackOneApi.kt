package com.dev.trackone.core.network.api

import com.dev.trackone.core.network.model.AccountUserDto
import com.dev.trackone.core.network.model.ApiEnvelope
import com.dev.trackone.core.network.model.CredentialItemDto
import com.dev.trackone.core.network.model.CredentialItemRequest
import com.dev.trackone.core.network.model.CredentialsBody
import com.dev.trackone.core.network.model.ExpenseItemDto
import com.dev.trackone.core.network.model.ExpenseItemRequest
import com.dev.trackone.core.network.model.ExpiryItemDto
import com.dev.trackone.core.network.model.ExpiryItemRequest
import com.dev.trackone.core.network.model.FavoriteRequest
import com.dev.trackone.core.network.model.SessionDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit binding for the TrackOne Cloudflare Worker API (see `docs/API.md`).
 *
 * Paths are relative and carry no origin — the origin is applied per-request by
 * [com.dev.trackone.core.network.interceptor.BaseUrlInterceptor] from the URL
 * stored in [com.dev.trackone.core.network.auth.ApiConfigStore], so the server
 * can be repointed at runtime without a rebuild.
 *
 * `DELETE` returns `204 No Content`, so those calls are typed as
 * `Response<Unit>` rather than an envelope.
 */
interface TrackOneApi {

    // ------------------------------------------------------------ auth

    @POST("api/v1/auth/signup")
    suspend fun signUp(@Body body: CredentialsBody): ApiEnvelope<SessionDto>

    @POST("api/v1/auth/signin")
    suspend fun signIn(@Body body: CredentialsBody): ApiEnvelope<SessionDto>

    @GET("api/v1/auth/me")
    suspend fun me(): ApiEnvelope<AccountUserDto>

    // ---------------------------------------------------------- expiry

    @GET("api/v1/expiry-items")
    suspend fun getExpiryItems(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("sort") sort: String = "expiryDate",
        @Query("order") order: String = "asc",
    ): ApiEnvelope<List<ExpiryItemDto>>

    @POST("api/v1/expiry-items")
    suspend fun createExpiryItem(@Body body: ExpiryItemRequest): ApiEnvelope<ExpiryItemDto>

    @PUT("api/v1/expiry-items/{id}")
    suspend fun updateExpiryItem(
        @Path("id") id: Long,
        @Body body: ExpiryItemRequest,
    ): ApiEnvelope<ExpiryItemDto>

    @DELETE("api/v1/expiry-items/{id}")
    suspend fun deleteExpiryItem(@Path("id") id: Long): Response<Unit>

    // --------------------------------------------------------- expense

    @GET("api/v1/expense-items")
    suspend fun getExpenseItems(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("sort") sort: String = "date",
        @Query("order") order: String = "desc",
    ): ApiEnvelope<List<ExpenseItemDto>>

    @POST("api/v1/expense-items")
    suspend fun createExpenseItem(@Body body: ExpenseItemRequest): ApiEnvelope<ExpenseItemDto>

    @PUT("api/v1/expense-items/{id}")
    suspend fun updateExpenseItem(
        @Path("id") id: Long,
        @Body body: ExpenseItemRequest,
    ): ApiEnvelope<ExpenseItemDto>

    @DELETE("api/v1/expense-items/{id}")
    suspend fun deleteExpenseItem(@Path("id") id: Long): Response<Unit>

    // ------------------------------------------------------ credential

    /**
     * List responses always mask the password as `********` — there is no
     * `reveal` switch. The real value comes from [getCredentialItem], one row at
     * a time.
     */
    @GET("api/v1/credential-items")
    suspend fun getCredentialItems(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("sort") sort: String = "title",
        @Query("order") order: String = "asc",
    ): ApiEnvelope<List<CredentialItemDto>>

    /** The only endpoint that returns an unmasked password. */
    @GET("api/v1/credential-items/{id}")
    suspend fun getCredentialItem(@Path("id") id: Long): ApiEnvelope<CredentialItemDto>

    @POST("api/v1/credential-items")
    suspend fun createCredentialItem(
        @Body body: CredentialItemRequest,
    ): ApiEnvelope<CredentialItemDto>

    @PUT("api/v1/credential-items/{id}")
    suspend fun updateCredentialItem(
        @Path("id") id: Long,
        @Body body: CredentialItemRequest,
    ): ApiEnvelope<CredentialItemDto>

    @PUT("api/v1/credential-items/{id}/favorite")
    suspend fun setCredentialFavorite(
        @Path("id") id: Long,
        @Body body: FavoriteRequest,
    ): ApiEnvelope<CredentialItemDto>

    @DELETE("api/v1/credential-items/{id}")
    suspend fun deleteCredentialItem(@Path("id") id: Long): Response<Unit>
}
