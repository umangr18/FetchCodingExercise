import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FetchScreen(viewModel: FetchViewModel = viewModel()) {
    val screenState by viewModel.screenState.collectAsState()

    FetchScreenContent(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(),
        state = screenState,
        loadingContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        },
        successContent = { uiData, scrollState ->
            FetchScreenSuccessContent(uiData, scrollState)
        },
        errorState = {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

@Composable
fun FetchScreenContent(
    modifier: Modifier,
    state: FetchScreenContract.UiState<FetchScreenContract.FetchScreenState>,
    loadingContent: @Composable () -> Unit,
    successContent: @Composable (List<ListItem>, lazyListState: ScrollState) -> Unit,
    errorState: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        when (state) {
            is FetchScreenContract.UiState.ContentLoading -> {
                loadingContent()
            }

            is FetchScreenContract.UiState.Success -> {
                successContent(state.data.uiData, scrollState)
            }

            is FetchScreenContract.UiState.Error -> {
                errorState()
            }

            else -> { /* should never reach here */ }
        }
    }
}

@Composable
fun FetchScreenSuccessContent(items: List<ListItem>, scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .padding(vertical = 64.dp, horizontal = 32.dp)
            .verticalScroll(scrollState)
            .animateContentSize(),
    ) {
        Text(
            text = "Fetch Coding Exercise",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        items.groupBy { it.listId }.forEach {
            Divider(
                modifier = Modifier.padding(16.dp),
                thickness = 1.dp
            )

            CollapsibleList(it.value, it.key)
        }
    }
}

@Composable
fun CollapsibleList(items: List<ListItem>, listId: Int) {
    var showItems by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (showItems) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "Icon Animation"
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { showItems = !showItems }
            )
        ) {
            Text(
                text = "List ID: $listId",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Arrow",
                tint = Color.Black,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(48.dp)
                    .graphicsLayer { rotationZ = rotationAngle }
            )
        }

        if (showItems) {
            Column {
                items.forEachIndexed { index, item ->
                    CollapsibleListItem(
                        item = item,
                        showDivider = index != items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
fun CollapsibleListItem(item: ListItem, showDivider: Boolean) {
    Text(
        text = "${item.name}",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    )

    if (showDivider) {
        Divider(
            modifier = Modifier.padding(16.dp),
            thickness = 1.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MaterialTheme {
        FetchScreenSuccessContent(
            items = listOf(
                ListItem(2, 1, "Item 2"),
                ListItem(1, 2, "Item 1"),
                ListItem(3, 3, "Item 3")
            ),
            scrollState = rememberScrollState()
        )
    }
}
