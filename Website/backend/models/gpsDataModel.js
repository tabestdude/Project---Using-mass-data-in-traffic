var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var gpsDataSchema = new Schema({
	'latitude' : Number,
	'longitude' : Number,
	'speed' : Number,
	'acquisitionTime' : Date
});

module.exports = mongoose.model('gpsData', gpsDataSchema);
