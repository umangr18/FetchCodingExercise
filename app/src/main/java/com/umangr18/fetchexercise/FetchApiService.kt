import retrofit2.Call
import retrofit2.http.GET

interface FetchApiService {
    @GET("hiring.json")
    fun getListItems(): Call<List<ListItem>>
}