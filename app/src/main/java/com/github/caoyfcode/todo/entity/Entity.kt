package com.github.caoyfcode.todo.entity

import java.time.LocalDateTime

data class Todo(
    val uid: Int,
    val groupUid: Int,
    val subject: String,
    val content: String = "",
    val checked: Boolean = false,
    val createTime: LocalDateTime = LocalDateTime.now(),
    val checkTime: LocalDateTime = LocalDateTime.now()
)

data class Group(
    val uid: Int,
    val icon: String,
    val name: String,
)