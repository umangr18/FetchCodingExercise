import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FetchViewModel : ViewModel() {

    private val _screenState = MutableStateFlow<FetchScreenContract.UiState<FetchScreenContract.FetchScreenState>>(FetchScreenContract.UiState.ContentLoading)
    val screenState: StateFlow<FetchScreenContract.UiState<FetchScreenContract.FetchScreenState>> get() = _screenState

    init {
        fetchItems()
    }

    private fun fetchItems() {
        viewModelScope.launch {
            FetchRetrofitClient.fetchApiService.getListItems().enqueue(object : Callback<List<ListItem>> {
                override fun onResponse(call: Call<List<ListItem>>, response: Response<List<ListItem>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { data ->
                            val filteredData = data.filter { !it.name.isNullOrBlank() }
                            val sortedData = filteredData.sortedWith(compareBy(
                                { it.listId },
                                { it.name?.removePrefix(PREFIX)?.toIntOrNull() ?: 0 }
                            ))
                            val state = FetchScreenContract.FetchScreenState(uiData = sortedData)
                            _screenState.value = FetchScreenContract.UiState.Success(state)
                        }
                    } else {
                        Log.e("ItemViewModel", "Failed to load data")
                        _screenState.value = FetchScreenContract.UiState.Error()
                    }
                }

                override fun onFailure(call: Call<List<ListItem>>, t: Throwable) {
                    Log.e("ItemViewModel", "Error: ${t.message}")
                    _screenState.value = FetchScreenContract.UiState.Error()
                }
            })
        }
    }

    companion object {
        const val PREFIX = "Item "
    }
}


interface FetchScreenContract {
    data class FetchScreenState(val uiData: List<ListItem>) {
        companion object {
            fun default() = FetchScreenState(emptyList())
        }
    }

    sealed interface UiState<out T> {
        data object ContentLoading : UiState<Nothing>
        data class Success<T>(val data: T) : UiState<T>
        data class Error<T>(val error: T? = null) : UiState<T>
    }
}
