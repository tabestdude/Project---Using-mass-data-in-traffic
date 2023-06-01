var RoadstateModel = require('../models/roadStateModel.js');

/**
 * roadStateController.js
 *
 * @description :: Server-side logic for managing roadStates.
 */

function calculateStandardDeviation(data) {
    const mean = calculateMean(data);
    const differencesSquared = data.map(x => Math.pow(x - mean, 2));
    const sumOfDifferencesSquared = differencesSquared.reduce((a, b) => a + b, 0);
    const variance = sumOfDifferencesSquared / data.length;
    const standardDeviation = Math.sqrt(variance);
    return standardDeviation;
}

function calculateMean(data) {
    const sum = data.reduce((a, b) => a + b, 0);
    const mean = sum / data.length;
    return mean;
}
  

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

    getTwoNewest: function(req, res){
        RoadstateModel.find()
        .sort({acquisitionTime: -1})
        .limit(2)
        .populate('publisher')
        .exec(function(err, roadState){
            if(err){
                return res.status(500).json({
                    message: 'Error when getting users.',
                    error: err
                });
            }
            return res.json(roadState);
        });
    },

    /**
     * roadStateController.create()
     */
    create: function (req, res) {
        const { accX, accY, accZ, longitude, latitude, ownerId } = req.body;

        const accXStd = calculateStandardDeviation(accX);
        const accYStd = calculateStandardDeviation(accY);
        const accZStd = calculateStandardDeviation(accZ);

        const stdMean = (accXStd + accYStd + accZStd) / 3;

        // Define the thresholds
        const lowThreshold = 2; // Adjust as needed
        const mediumThreshold = 3.5; // Adjust as needed
        const highThreshold = 7; // Adjust as needed
        
        // Determine if the road was bumpy
        const stateOfRoadCalculated = stdMean < lowThreshold ? 0 : stdMean < mediumThreshold ? 1 : stdMean < highThreshold ? 2 : 3;

        var roadState = new RoadstateModel({
			stateOfRoad : stateOfRoadCalculated,
			latitude : latitude,
			longitude : longitude,
			acquisitionTime : Date.now(),
            publisher: ownerId
        });

        roadState.save(function (err, roadState) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating roadState',
                    error: err
                });
            }

            return res.json({ status: 'success', message: 'Data received successfully' });
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
