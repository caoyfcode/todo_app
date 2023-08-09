package com.github.caoyfcode.todo.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.github.caoyfcode.todo.db.entity.Group
import com.github.caoyfcode.todo.db.entity.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Insert
    suspend fun insert(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)

    @Update
    suspend fun update(todo: Todo)

    @Query("SELECT * FROM todo_item")
    fun getAll(): Flow<List<Todo>>
}

@Dao
interface GroupDao {
    @Insert
    suspend fun insert(group: Group)

    @Query("DELETE FROM todo_group WHERE uid = :uid AND :uid NOT IN "
            + "(SELECT group_uid FROM todo_item WHERE group_uid = :uid)")
    suspend fun deleteIfEmpty(uid: Int): Int

//    @Delete
//    suspend fun delete(group: Group)
//
//    @Query("DELETE FROM todo_item WHERE group_uid = :groupUid")
//    suspend fun deleteTodoByGroupUid(groupUid: Int)
//
//    @Transaction
//    suspend fun deleteGroupWithTodo(group: Group) {
//        deleteTodoByGroupUid(group.uid)
//        delete(group)
//    }

    @Update
    suspend fun update(group: Group)

    @Query("SELECT * FROM todo_group")
    fun getAll(): Flow<List<Group>>
}
