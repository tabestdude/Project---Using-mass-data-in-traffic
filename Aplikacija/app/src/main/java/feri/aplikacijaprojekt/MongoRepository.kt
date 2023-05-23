package feri.aplikacijaprojekt

import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

interface MongoRepository {
    fun configureTheRealm()
    fun getData(): Flow<List<Data>>
    fun filterData(name: String): Flow<List<Data>>
    suspend fun insertData(person: Data)
    suspend fun updateData(person: Data)
    suspend fun deleteData(id: ObjectId)
}
