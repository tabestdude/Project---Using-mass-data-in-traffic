package feri.aplikacijaprojekt

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

open class Data(
    globalGiroX: Double,
    globalGiroY: Double,
    globalGiroZ: Double,
    globalAccX: Double,
    globalAccY: Double,
    globalAccZ: Double,
    globalLongitude: Double,
    globalLatitude: Double,
    ownerId: Nothing?
) : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var ownerId: String = ""
    var globalGiroX: Double = 0.0
    var globalGiroY: Double = 0.0
    var globalGiroZ: Double = 0.0
    var globalAccX: Double = 0.0
    var globalAccY: Double = 0.0
    var globalAccZ: Double = 0.0
    var globalLongitude: Double = 0.0
    var globalLatitude: Double = 0.0
}