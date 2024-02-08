var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var archivedRoadState = new Schema({
	'roadStatesArr' : Buffer,
	'latitudeArr' : [Number],
	'longitudeArr' : [Number],
    'acquisitionTime' : [Date]
});

module.exports = mongoose.model('archivedRoadState', archivedRoadState);
