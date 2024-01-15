var UsersModel = require('../models/usersModel.js');
var bcrypt = require('bcrypt');
var roadStateModel = require('../models/roadStateModel.js');
const archivedRoadStateModel = require('../models/archivedRoadStateModel.js');

function getDifferenceVector(fileData) {
    let differenceVector = [];
    differenceVector.push(parseInt(fileData[0]));
    for(let i = 1; i < fileData.length; i++) {
        differenceVector.push(parseInt(fileData[i]) - parseInt(fileData[i-1]));
    }
    return differenceVector;
}

function compress(fileData) {
    let differenceVector = getDifferenceVector(fileData);

    let binaryString = differenceVector[0].toString(2).padStart(8, '0');

    let repeatCounter = 1;
    let j = 0;
    for(let i = 1; i < differenceVector.length; i++) {
        if (parseInt(differenceVector[i]) == 0) {
            repeatCounter = 1;
            j = i + 1;
            while(j < i+8 && j < differenceVector.length && differenceVector[j] == 0) {
                repeatCounter++;
                j++;
            }
            i += repeatCounter - 1;
            binaryString += "01";
            binaryString += (repeatCounter-1).toString(2).padStart(3, '0');
        }
        else if (parseInt(differenceVector[i]) >= -2 && parseInt(differenceVector[i]) <= 2) {
            binaryString += "0000";
            binaryString += (parseInt(differenceVector[i]) < 0 ? parseInt(differenceVector[i]) + 2 : parseInt(differenceVector[i]) + 1).toString(2).padStart(2, '0');
        }
        else if (parseInt(differenceVector[i]) >= -6 && parseInt(differenceVector[i]) <= 6) {
            binaryString += "0001";
            binaryString += (parseInt(differenceVector[i]) < 0 ? parseInt(differenceVector[i]) + 6 : parseInt(differenceVector[i]) + 1).toString(2).padStart(3, '0');
        }
        else if (parseInt(differenceVector[i]) >= -14 && parseInt(differenceVector[i]) <= 14) {
            binaryString += "0010";
            binaryString += (parseInt(differenceVector[i]) < 0 ? parseInt(differenceVector[i]) + 14 : parseInt(differenceVector[i]) + 1).toString(2).padStart(4, '0');
        }
        else if (parseInt(differenceVector[i]) >= -30 && parseInt(differenceVector[i]) <= 30) {
            binaryString += "0011";
            binaryString += (parseInt(differenceVector[i]) < 0 ? parseInt(differenceVector[i]) + 30 : parseInt(differenceVector[i]) + 1).toString(2).padStart(5, '0');
        }
        else {
            binaryString += "10";
            let absoluteValue = Math.abs(parseInt(differenceVector[i]));
            binaryString += (parseInt(differenceVector[i]) < 0 ? "1" : "0");
            binaryString += absoluteValue.toString(2).padStart(8, '0');
        }
    }
    binaryString += "11";

    return binaryString;
}

function getValuesFromDifferenceVector(differenceVector) {
    let decompressedData = [];
    decompressedData.push(differenceVector[0]);
    for (let i = 1; i < differenceVector.length; i++) {
        decompressedData.push(decompressedData[i-1] + parseInt(differenceVector[i]));
    }
    return decompressedData;
}

function decompress(fileData) {
    let differenceVector = [];
    let firstValue = 0;
    for (let i = 0; i < 8; i++) {
        firstValue += fileData[8 - i - 1] * Math.pow(2, i);
    }
    differenceVector.push(firstValue);
    for (let i = 8; i < fileData.length; i++) {
        if (fileData[i] == 1){
            // is 11 - exit
            if (fileData[i+1] == 1)
                break;
            // is 10 - absolute coding
            else {
                let absoluteValue = 0;
                for (let j = 0; j < 8; j++) {
                    absoluteValue += fileData[(i + 10) - j] * Math.pow(2, j);
                }
                if (fileData[i+2]){
                    absoluteValue = -absoluteValue;
                }
                differenceVector.push(absoluteValue);
                i += 10;
                
            }
        }
        else {
            // is 01 - repeats
            if (fileData[i+1] == 1){
                let numberOfRepeats = 0;
                for (let j = 0; j < 3; j++) {
                    numberOfRepeats += fileData[(i + 4) - j] * Math.pow(2, j);
                }
                // +1 because 1 repeat is 000
                numberOfRepeats += 1;
                
                for (let j = 0; j < numberOfRepeats; j++){
                    differenceVector.push(0);
                }
                i += 4;
            }
            // is 00 - differences
            else {
                let value = 0;
                // 11 - difference coded with 5 bits
                if (fileData[i+2] == 1 && fileData[i+3] == 1){
                    for (let j = 0; j < 5; j++) {
                        value += fileData[(i + 8) - j] * Math.pow(2, j);
                    }
                    if (value < 16)
                        value -= 30;
                    else
                        value -= 1;
                    differenceVector.push(value);
                    i += 8;
                }
                // 10 - difference coded with 4 bits
                else if (fileData[i+2] == 1 && !fileData[i+3] == 1){
                    for (let j = 0; j < 4; j++) {
                        value += fileData[(i + 7) - j] * Math.pow(2, j);
                    }
                    if (value < 8)
                        value -= 14;
                    else
                        value -= 1;
                    differenceVector.push(value);
                    i += 7;
                }
                // 01 - difference coded with 3 bits
                else if (!fileData[i+2] == 1 && fileData[i+3] == 1){
                    for (let j = 0; j < 3; j++) {
                        value += fileData[(i + 6) - j] * Math.pow(2, j);
                    }
                    if (value < 4)
                        value -= 6;
                    else
                        value -= 1;
                    differenceVector.push(value);
                    i += 6;
                }
                // 00 - difference coded with 2 bits
                else {
                    for (let j = 0; j < 2; j++) {
                        value += fileData[(i + 5) - j] * Math.pow(2, j);
                    }
                    if (value < 2)
                        value -= 2;
                    else
                        value -= 1;
                    differenceVector.push(value);
                    i += 5;
                }
            }
        }
    }

    return getValuesFromDifferenceVector(differenceVector);
}

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
        .populate('archivedRoadStates')
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
        console.log("Cleaning up and arhiving old road states");
        UsersModel.find()
        .populate('roadStates')
        .populate('archivedRoadStates')
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
            var userRoadStatesArray = [];
            for(var i = 0; i < userss.length; i++){
                for(var j = 0; j < userss[i].roadStates.length; j++){
                    if(userss[i].roadStates[j].acquisitionTime < dateNowMinusOneMinute){
                        roadStatesArray.push(userss[i].roadStates[j]._id);  
                        userRoadStatesArray.push(userss[i].roadStates[j]);
                        userss[i].roadStates.splice(j, 1);
                        j--;
                    }
                }
                if (userRoadStatesArray.length > 0) {
                    var archivedRoadStates;
                    var archivedRoadStatesLatitude;
                    var archivedRoadStatesLongitude;
                    var archivedRoadStatesAcquisitionTime;
                    if(userss[i].archivedRoadStates != null, userss[i].archivedRoadStates != undefined && userss[i].archivedRoadStates.roadStatesArr != "") {
                        let binaryString = userss[i].archivedRoadStates.roadStatesArr.toString('binary');
                        archivedRoadStates = decompress(binaryString);
                        archivedRoadStatesLatitude = userss[i].archivedRoadStates.latitudeArr;
                        archivedRoadStatesLongitude = userss[i].archivedRoadStates.longitudeArr;
                        archivedRoadStatesAcquisitionTime = userss[i].archivedRoadStates.acquisitionTime;
                    } else {
                        archivedRoadStates = [];
                        archivedRoadStatesLatitude = [];
                        archivedRoadStatesLongitude = [];
                        archivedRoadStatesAcquisitionTime = [];
                    }

                    for(var j = 0; j < userRoadStatesArray.length; j++){
                        archivedRoadStates.push(userRoadStatesArray[j].stateOfRoad);
                        archivedRoadStatesLatitude.push(userRoadStatesArray[j].latitude);
                        archivedRoadStatesLongitude.push(userRoadStatesArray[j].longitude);
                        archivedRoadStatesAcquisitionTime.push(userRoadStatesArray[j].acquisitionTime);
                    }
                    if (userss[i].archivedRoadStates?._id !== undefined) {
                        let binaryString = compress(archivedRoadStates);
                        archivedRoadStateModel.findOneAndUpdate({_id: userss[i].archivedRoadStates._id}, {
                            roadStatesArr : Buffer.from(binaryString, 'binary'),
                            latitudeArr : archivedRoadStatesLatitude,
                            longitudeArr : archivedRoadStatesLongitude,
                            acquisitionTime : archivedRoadStatesAcquisitionTime,
                        }, function(err, doc){
                            if (err) {
                                console.log("Something wrong when updating data!");
                            }
                        });
                    } else {
                        let binaryString = compress(archivedRoadStates);
                        var archivedRoadState = new archivedRoadStateModel({
                            roadStatesArr : Buffer.from(binaryString, 'binary'),
                            latitudeArr : archivedRoadStatesLatitude,
                            longitudeArr : archivedRoadStatesLongitude,
                            acquisitionTime : archivedRoadStatesAcquisitionTime,
                        });
                        archivedRoadState.save();
                        userss[i].archivedRoadStates = archivedRoadState._id;
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
                console.log("Deleted and arhived old road states");
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

    showArchived: function (req, res) {
        var id = req.session.userId;
        
        UsersModel.findOne({_id: id})
        .populate('archivedRoadStates')
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
            var archivedRoadStates;
            var archivedRoadStatesLatitude;
            var archivedRoadStatesLongitude;
            var archivedRoadStatesAcquisitionTime;
            if(users.archivedRoadStates != null, users.archivedRoadStates != undefined && users.archivedRoadStates.roadStatesArr != "") {
                let binaryString = users.archivedRoadStates.roadStatesArr.toString('binary');
                archivedRoadStates = decompress(binaryString);
                archivedRoadStatesLatitude = users.archivedRoadStates.latitudeArr;
                archivedRoadStatesLongitude = users.archivedRoadStates.longitudeArr;
                archivedRoadStatesAcquisitionTime = users.archivedRoadStates.acquisitionTime;
            } else {
                archivedRoadStates = [];
                archivedRoadStatesLatitude = [];
                archivedRoadStatesLongitude = [];
                archivedRoadStatesAcquisitionTime = [];
            }

            return res.json({
                roadStates: archivedRoadStates,
                latitude: archivedRoadStatesLatitude,
                longitude: archivedRoadStatesLongitude,
                acquisitionTime: archivedRoadStatesAcquisitionTime
            });
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
