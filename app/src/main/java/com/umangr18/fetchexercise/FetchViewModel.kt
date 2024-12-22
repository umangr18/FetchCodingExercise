import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FetchViewModel(
    private val fetchApiService: FetchApiService = FetchRetrofitClient.fetchApiService
) : ViewModel() {

    private val _screenState = MutableStateFlow<FetchScreenContract.UiState<FetchScreenContract.FetchScreenState>>(FetchScreenContract.UiState.ContentLoading)
    val screenState: StateFlow<FetchScreenContract.UiState<FetchScreenContract.FetchScreenState>> get() = _screenState

    init {
        fetchItems()
    }

    fun fetchItems() {
        viewModelScope.launch {
            try {
                _screenState.value = FetchScreenContract.UiState.ContentLoading
                val result = fetchApiService.getListItems()
                val filteredData = result.filter { !it.name.isNullOrBlank() }
                val sortedData = filteredData.sortedWith(compareBy(
                    { it.listId },
                    { it.name?.removePrefix(PREFIX)?.toIntOrNull() ?: 0 }
                ))
                val state = FetchScreenContract.FetchScreenState(uiData = sortedData)
                _screenState.value = FetchScreenContract.UiState.Success(state)
            } catch (e: Exception) {
                _screenState.value = FetchScreenContract.UiState.Error()
            }
        }
    }

    companion object {
        const val PREFIX = "Item "
    }
}


interface FetchScreenContract {
    data class FetchScreenState(val uiData: List<ListItem>)

    sealed interface UiState<out T> {
        data object ContentLoading : UiState<Nothing>
        data class Success<T>(val data: T) : UiState<T>
        data class Error<T>(val error: T? = null) : UiState<T>
    }
}
