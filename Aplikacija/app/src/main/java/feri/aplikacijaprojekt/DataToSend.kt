package feri.aplikacijaprojekt

import org.mongodb.kbson.ObjectId

class DataToSend(
    val globalGiroX: Double,
    val globalGiroY: Double,
    val globalGiroZ: Double,
    val globalAccX: Double,
    val globalAccY: Double,
    val globalAccZ: Double,
    val globalLongitude: Double,
    val globalLatitude: Double,
    val ownerId: Nothing?
) {
    val id: ObjectId = ObjectId()
}