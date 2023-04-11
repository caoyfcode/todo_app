package com.github.caoyfcode.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.caoyfcode.todo.ui.theme.TodoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Screen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TodoTheme {
        Screen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen() {
    val unchecked_messages = listOf(
        "Sleep",
        "Go to School",
        "Coding"
    )
    val checked_messages = listOf(
        "吃饭",
        "睡觉",
        "打游戏",
    )
    Scaffold(
        modifier = Modifier.padding(10.dp),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painter = painterResource(id = R.drawable.list), contentDescription = stringResource(
                            id = R.string.all_groups
                        ))
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painter = painterResource(id = R.drawable.search), contentDescription = stringResource(
                            id = R.string.search
                        ))
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painter = painterResource(id = R.drawable.add), contentDescription = stringResource(
                            id = R.string.add_todo
                        ))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (unchecked_messages.isEmpty()) {
                Column (
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(id = R.string.no_todo))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight()
                        .padding(vertical = 20.dp), // 前导 20dp
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp), // 没项相隔 20dp
                ) {
                    items(unchecked_messages) {
                        TodoItem(message = it, false)
                    }
                    item {
                        Divider(
                            color = Color.LightGray
                        )
                    }
                    items(checked_messages) {
                        TodoItem(message = it, true)
                    }
                }
            }
        }
    }
}

@Composable
fun TodoItem(message: String, checked: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(20),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.inverseSurface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp), // 前导 10dp
            horizontalArrangement = Arrangement.spacedBy(10.dp), // 中间相隔 10dp
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (checked) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(painter = painterResource(id = R.drawable.check), contentDescription = stringResource(
                        id = R.string.checked_todo
                    ))
                }
            } else {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(painter = painterResource(id = R.drawable.bookmark), contentDescription = stringResource(
                        id = R.string.unchecked_todo
                    ))
                }
            }
            Text(text = message)
        }
    }
}
