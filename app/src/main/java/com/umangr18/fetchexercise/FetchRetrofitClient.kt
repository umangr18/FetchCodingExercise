import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FetchRetrofitClient {
    private const val BASE_URL = "https://fetch-hiring.s3.amazonaws.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val fetchApiService: FetchApiService = retrofit.create(FetchApiService::class.java)
}
