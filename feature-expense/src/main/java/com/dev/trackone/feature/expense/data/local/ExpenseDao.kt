package com.dev.trackone.feature.expense.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for expense items.
 */
@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExpenseEntity): Long

    @Update
    suspend fun update(entity: ExpenseEntity)

    @Delete
    suspend fun delete(entity: ExpenseEntity)

    @Query("DELETE FROM expense_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM expense_items")
    suspend fun getAllOnce(): List<ExpenseEntity>

    @Query("DELETE FROM expense_items")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExpenseEntity>)

    @Query("SELECT * FROM expense_items ORDER BY date DESC")
    fun getAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense_items WHERE id = :id")
    fun getById(id: Long): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expense_items WHERE date BETWEEN :startTime AND :endTime ORDER BY date DESC")
    fun getByDateRange(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense_items WHERE isIncome = :isIncome ORDER BY date DESC")
    fun getByType(isIncome: Boolean): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense_items WHERE category = :category ORDER BY date DESC")
    fun getByCategory(category: String): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expense_items 
        WHERE title LIKE '%' || :query || '%' 
        OR category LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun search(query: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM expense_items WHERE isIncome = 1 AND date BETWEEN :startTime AND :endTime")
    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM expense_items WHERE isIncome = 0 AND date BETWEEN :startTime AND :endTime")
    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double?>
}
