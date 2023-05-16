var RoadstateModel = require('../models/roadStateModel.js');

/**
 * roadStateController.js
 *
 * @description :: Server-side logic for managing roadStates.
 */
module.exports = {

    /**
     * roadStateController.list()
     */
    list: function (req, res) {
        RoadstateModel.find(function (err, roadStates) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting roadState.',
                    error: err
                });
            }

            return res.json(roadStates);
        });
    },

    /**
     * roadStateController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        RoadstateModel.findOne({_id: id}, function (err, roadState) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting roadState.',
                    error: err
                });
            }

            if (!roadState) {
                return res.status(404).json({
                    message: 'No such roadState'
                });
            }

            return res.json(roadState);
        });
    },

    /**
     * roadStateController.create()
     */
    create: function (req, res) {
        var roadState = new RoadstateModel({
			gpsData : req.body.gpsData,
			accelerometerData : req.body.accelerometerData,
			gyroscopeData : req.body.gyroscopeData,
			stateOfRoad : req.body.stateOfRoad,
			recommendedDriving : req.body.recommendedDriving
        });

        roadState.save(function (err, roadState) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating roadState',
                    error: err
                });
            }

            return res.status(201).json(roadState);
        });
    },

    /**
     * roadStateController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        RoadstateModel.findOne({_id: id}, function (err, roadState) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting roadState',
                    error: err
                });
            }

            if (!roadState) {
                return res.status(404).json({
                    message: 'No such roadState'
                });
            }

            roadState.gpsData = req.body.gpsData ? req.body.gpsData : roadState.gpsData;
			roadState.accelerometerData = req.body.accelerometerData ? req.body.accelerometerData : roadState.accelerometerData;
			roadState.gyroscopeData = req.body.gyroscopeData ? req.body.gyroscopeData : roadState.gyroscopeData;
			roadState.stateOfRoad = req.body.stateOfRoad ? req.body.stateOfRoad : roadState.stateOfRoad;
			roadState.recommendedDriving = req.body.recommendedDriving ? req.body.recommendedDriving : roadState.recommendedDriving;
			
            roadState.save(function (err, roadState) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating roadState.',
                        error: err
                    });
                }

                return res.json(roadState);
            });
        });
    },

    /**
     * roadStateController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        RoadstateModel.findByIdAndRemove(id, function (err, roadState) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the roadState.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
