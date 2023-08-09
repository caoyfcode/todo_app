package com.github.caoyfcode.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.caoyfcode.todo.db.AppDatabase
import com.github.caoyfcode.todo.ui.theme.TodoTheme
import com.github.caoyfcode.todo.ui.Screen
import com.github.caoyfcode.todo.viewmodel.TodoViewModel

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
                    Screen(viewModel(
                        factory = object: ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return TodoViewModel(AppDatabase.getDatabase(applicationContext)) as T
                                }
                                throw IllegalArgumentException("Unknown ViewModel class")
                            }
                        }
                    ))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TodoTheme {
        Screen(viewModel())
    }
}