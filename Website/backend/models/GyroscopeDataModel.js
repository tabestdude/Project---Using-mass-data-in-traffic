var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var GyroscopeDataSchema = new Schema({
	'Xaxis' : Number,
	'Yaxis' : Number,
	'Zaxis' : Number,
	'acquisitionTime' : Date
});

module.exports = mongoose.model('GyroscopeData', GyroscopeDataSchema);
