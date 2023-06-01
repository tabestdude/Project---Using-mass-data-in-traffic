var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var roadStateSchema = new Schema({
	'stateOfRoad' : Number, // 0 - good, 1 - semiGood, 2 - very bad, 3 - literal earthquake
	'latitude' : Number,
	'longitude' : Number,
	'acquisitionTime' : Date,
	'publisher' : {
		type: Schema.Types.ObjectId,
		ref: 'users'
	},
});

module.exports = mongoose.model('roadState', roadStateSchema);
