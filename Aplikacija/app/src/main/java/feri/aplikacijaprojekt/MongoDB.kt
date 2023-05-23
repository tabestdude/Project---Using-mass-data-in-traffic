package feri.aplikacijaprojekt

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId

object MongoDB : MongoRepository {
    private val app = App.create("application-0-ofilw")
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        Log.d("MongoDB", "Configuring the realm.")
        configureTheRealm()
    }

    override fun configureTheRealm() {
        if (user != null) {
            Log.d("MongoDB", "User is not null.")
            val config = SyncConfiguration.Builder(
                user,
                setOf(Data::class)
            )
                .initialSubscriptions { sub ->
                    Log.d("MongoDB", "Adding initial subscription.")
                    add(query = sub.query<Data>(query = "owner_id == \\$0", user.id))
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        } else {
            Log.d("MongoDB", "User is null.")
        }
    }

    override fun getData(): Flow<List<Data>> {
        Log.d("MongoDB", "Getting data.")
        return realm.query<Data>().asFlow().map { it.list }
    }

    override fun filterData(name: String): Flow<List<Data>> {
        Log.d("MongoDB", "Filtering data.")
        return realm.query<Data>(query = "name CONTAINS[c] \\$0", name)
            .asFlow().map { it.list }
    }

    override suspend fun insertData(data: Data) {
        Log.d("MongoDB", "Inserting data.")
        if (user != null) {
            realm.write {
                try {
                    copyToRealm(data.apply { ownerId = user.id })
                } catch (e: Exception) {
                    Log.d("MongoRepository", e.message.toString())
                }
            }
        } else {
            Log.d("MongoDB", "User is null.")
        }
    }

    override suspend fun updateData(data: Data) {
        Log.d("MongoDB", "Updating data.")
        realm.write {
            val queriedData =
                query<Data>(query = "_id == \\$0", data.id)
                    .first()
                    .find()
            if (queriedData != null) {
                queriedData.globalGiroX = data.globalGiroX
            } else {
                Log.d("MongoRepository", "Queried Data does not exist.")
            }
        }
    }

    override suspend fun deleteData(id: ObjectId) {
        Log.d("MongoDB", "Deleting data.")
        realm.write {
            try {
                val data = query<Data>(query = "_id == \\$0", id)
                    .first()
                    .find()
                data?.let { delete(it) }
            } catch (e: Exception) {
                Log.d("MongoRepository", "${e.message}")
            }
        }
    }
}