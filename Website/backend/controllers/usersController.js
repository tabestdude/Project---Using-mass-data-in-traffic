var UsersModel = require('../models/usersModel.js');
var bcrypt = require('bcrypt');
var roadStateModel = require('../models/roadStateModel.js');

/**
 * usersController.js
 *
 * @description :: Server-side logic for managing userss.
 */
module.exports = {

    /**
     * usersController.list()
     */
    list: function (req, res) {
        UsersModel.find()
        .populate('roadStates')
        .exec(function (err, userss) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting users.',
                    error: err
                });
            }

            return res.json(userss);
        });
    },

    cleanupOldRoadStates: function(req, res){
        UsersModel.find()
        .populate('roadStates')
        .exec(function (err, userss) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting users.',
                    error: err
                });
            }
            var dateNow = new Date();
            var dateNowMinusOneMinute = new Date(dateNow.getTime() - 60*1000);
            var roadStatesArray = [];
            for(var i = 0; i < userss.length; i++){
                for(var j = 0; j < userss[i].roadStates.length; j++){
                    if(userss[i].roadStates[j].acquisitionTime < dateNowMinusOneMinute){
                        roadStatesArray.push(userss[i].roadStates[j]._id);
                        userss[i].roadStates.splice(j, 1);
                        j--;
                    }
                }
                userss[i].save();
            }
            roadStateModel.deleteMany({_id: {$in: roadStatesArray}}, function(err){
                if(err){
                    return res.status(500).json({
                        message: 'Error when deleting roadStates.',
                        error: err
                    });
                }
                return res.status(204).json(userss);
            });
        });
    },

    login: function(req, res, next){
        UsersModel.authenticate(req.body.username, req.body.password, function(err, user){
            if(err || !user){
                return res.status(401).json({ error: 'Invalid username or password' });
            }
            req.session.userId = user._id;
            return res.json(user);
        });
    },

    loginPhone: function(req, res, next){
        UsersModel.authenticate(req.body.username, req.body.password, function(err, user){
            if(err || !user){
                return res.status(401).json({ error: 'Invalid username or password' });
            }
            return res.json(user);
        });
    },

    logout: function(req, res, next){
        console.log("Session destroyed");
        if(req.session){
            req.session.destroy(function(err){
                if(err){
                    return next(err);
                } else{
                    return res.status(201).json({});
                }
            });
        }
    },

    /**
     * usersController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        UsersModel.findOne({_id: id}, function (err, users) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting users.',
                    error: err
                });
            }

            if (!users) {
                return res.status(404).json({
                    message: 'No such users'
                });
            }

            return res.json(users);
        });
    },

    showPersonal: function (req, res) {
        var id = req.session.userId;
        UsersModel.findOne({_id: id})
        .populate('roadStates')
        .exec(function (err, users) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting users.',
                    error: err
                });
            }

            if (!users) {
                return res.status(404).json({
                    message: 'No such users'
                });
            }

            return res.json(users);
        });
    },

    /**
     * usersController.create()
     */
    create: function (req, res) {
        var securePassword = "password123"
        bcrypt.hash(req.body.password, 10, function(err, hash){
            if(err){
                return res.status(500).json({
                    message: 'Hashing password failed',
                    error: err
                });
            }
            securePassword = hash;
            var users = new UsersModel({
                email : req.body.email,
                username : req.body.username,
                password : securePassword
            });

            users.save(function (err, user) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when creating users',
                        error: err
                    });
                }

                return res.status(201).json(user);
            });
        });
    },

    /**
     * usersController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        UsersModel.findOne({_id: id}, function (err, users) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting users',
                    error: err
                });
            }

            if (!users) {
                return res.status(404).json({
                    message: 'No such users'
                });
            }

            users.email = req.body.email ? req.body.email : users.email;
            users.username = req.body.username ? req.body.username : users.username;
			users.password = req.body.password ? req.body.password : users.password;
			
            users.save(function (err, users) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating users.',
                        error: err
                    });
                }

                return res.json(users);
            });
        });
    },

    /**
     * usersController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        UsersModel.findByIdAndRemove(id, function (err, users) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the users.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
