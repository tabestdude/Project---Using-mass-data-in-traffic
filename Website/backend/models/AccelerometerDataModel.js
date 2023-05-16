var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var AccelerometerDataSchema = new Schema({
	'Xaxis' : Number,
	'Yaxis' : Number,
	'Zaxis' : Number,
	'acquisitionTime' : Date
});

module.exports = mongoose.model('AccelerometerData', AccelerometerDataSchema);
