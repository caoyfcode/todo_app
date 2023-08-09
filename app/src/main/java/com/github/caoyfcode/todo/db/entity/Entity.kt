package com.github.caoyfcode.todo.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "todo_item")
data class Todo(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "group_uid") val groupUid: Int,
    val subject: String,
    val content: String = "",
    val checked: Boolean = false,
    @ColumnInfo(name = "create_time") val createTime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "check_time") val checkTime: LocalDateTime = LocalDateTime.now()
)

@Entity(tableName = "todo_group")
data class Group(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val icon: String,
    val name: String,
)