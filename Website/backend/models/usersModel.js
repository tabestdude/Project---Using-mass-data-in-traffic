var mongoose = require('mongoose');
var Schema   = mongoose.Schema;
var bcrypt = require('bcrypt');

var usersSchema = new Schema({
	'email' : String,
	'username' : String,
	'password' : String,
	'roadStates' : [{
		type: Schema.Types.ObjectId,
		ref: 'roadState'
	}],
	'archivedRoadStates' : {
		type: Schema.Types.ObjectId,
		ref: 'archivedRoadState'
	}
});

usersSchema.statics.authenticate = function(username, password, callback){
	User.findOne({username: username})
	.exec(function(err, user){
		if(err){
			return callback(err);
		} else if(!user) {
			var err = new Error("User not found.");
			err.status = 401;
			return callback(err);
		} 
		bcrypt.compare(password, user.password, function(err, result){
			if(result === true){
				return callback(null, user);
			} else{
				return callback();
			}
		});
		 
	});
}

var User = mongoose.model('users', usersSchema);
module.exports = User;
