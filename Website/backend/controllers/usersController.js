var UsersModel = require('../models/usersModel.js');

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
        UsersModel.find(function (err, userss) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting users.',
                    error: err
                });
            }

            return res.json(userss);
        });
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
        });

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
