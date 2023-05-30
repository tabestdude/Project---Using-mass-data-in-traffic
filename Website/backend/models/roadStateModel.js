var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var roadStateSchema = new Schema({
	'gpsData' : [{
	 	type: Schema.Types.ObjectId,
	 	ref: 'gps'
	}],
	'accelerometerData' : [{
	 	type: Schema.Types.ObjectId,
	 	ref: 'accelerometer'
	}],
	'gyroscopeData' : [{
	 	type: Schema.Types.ObjectId,
	 	ref: 'gyroscope'
	}],
	'stateOfRoad' : Number,
	'recommendedDriving' : Boolean
});

module.exports = mongoose.model('roadState', roadStateSchema);
