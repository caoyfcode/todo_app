package com.github.caoyfcode.todo.entity

data class Todo(
    val uid: Int,
    val groupUid: Int,
    val subject: String,
    val content: String = "",
    val checked: Boolean = false,
)

data class Group(
    val uid: Int,
    val icon: String,
    val name: String,
)