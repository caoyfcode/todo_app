package com.github.caoyfcode.todo.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.caoyfcode.todo.entity.Group
import com.github.caoyfcode.todo.entity.Todo

class TodoViewModel: ViewModel() {
    private val _groups: MutableLiveData<List<Group>> by lazy {
        MutableLiveData(
            listOf(
                Group(0, "\uD83D\uDCBC", "工作"),
                Group(1, "\uD83D\uDCD6", "学习"),
                Group(2, "😊", "娱乐"),
                Group(3, "\uD83E\uDDFA", "杂务")
            )
        )
    }

    val groups: LiveData<List<Group>>
        get() = _groups

    private val _todos: MutableLiveData<List<Todo>> by lazy {
        MutableLiveData(
            listOf(
                Todo(0, 3, "打扫", "1.地板\n2.窗户"),
                Todo(1, 1, "读代码", "1.kotlin\n2.android"),
                Todo(2, 1, "学习视频", "打开bilibili学习"),
                Todo(3, 0, "领钱", "白日做梦"),
                Todo(4, 3, "发呆"),
                Todo(5, 2, "打游戏"),
                Todo(6, 0, "写代码"),
                Todo(7, 1, "读书", "", true),
            )
        )
    }

    val todos: LiveData<List<Todo>>
        get() = _todos

    fun toggleCheckedTodo(uid: Int) {
        _todos.value = _todos.value?.map {
            if (it.uid == uid) {
                it.copy(checked = !it.checked)
            } else {
                it
            }
        }
    }

    fun addTodo(todo: Todo) {
        var biggestUid = -1
        _todos.value?.map {
            if (it.uid > biggestUid) {
                biggestUid = it.uid
            }
        }
        _todos.value = _todos.value?.plus(
            todo.copy(
                uid = biggestUid + 1
            )
        )
    }

    fun deleteTodo(uid: Int) {
        _todos.value = _todos.value?.filter { it.uid != uid }
    }

    fun changeTodo(changed: Todo) {
        _todos.value = _todos.value?.map {
            if (it.uid == changed.uid) {
                changed
            } else {
                it
            }
        }
    }
}