var AccelerometerdataModel = require('../models/AccelerometerDataModel.js');

/**
 * AccelerometerDataController.js
 *
 * @description :: Server-side logic for managing AccelerometerDatas.
 */
module.exports = {

    /**
     * AccelerometerDataController.list()
     */
    list: function (req, res) {
        AccelerometerdataModel.find(function (err, AccelerometerDatas) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting AccelerometerData.',
                    error: err
                });
            }

            return res.json(AccelerometerDatas);
        });
    },

    /**
     * AccelerometerDataController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        AccelerometerdataModel.findOne({_id: id}, function (err, AccelerometerData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting AccelerometerData.',
                    error: err
                });
            }

            if (!AccelerometerData) {
                return res.status(404).json({
                    message: 'No such AccelerometerData'
                });
            }

            return res.json(AccelerometerData);
        });
    },

    /**
     * AccelerometerDataController.create()
     */
    create: function (req, res) {
        var AccelerometerData = new AccelerometerdataModel({
			Xaxis : req.body.Xaxis,
			Yaxis : req.body.Yaxis,
			Zaxis : req.body.Zaxis,
			acquisitionTime : req.body.acquisitionTime
        });

        AccelerometerData.save(function (err, AccelerometerData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating AccelerometerData',
                    error: err
                });
            }

            return res.status(201).json(AccelerometerData);
        });
    },

    /**
     * AccelerometerDataController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        AccelerometerdataModel.findOne({_id: id}, function (err, AccelerometerData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting AccelerometerData',
                    error: err
                });
            }

            if (!AccelerometerData) {
                return res.status(404).json({
                    message: 'No such AccelerometerData'
                });
            }

            AccelerometerData.Xaxis = req.body.Xaxis ? req.body.Xaxis : AccelerometerData.Xaxis;
			AccelerometerData.Yaxis = req.body.Yaxis ? req.body.Yaxis : AccelerometerData.Yaxis;
			AccelerometerData.Zaxis = req.body.Zaxis ? req.body.Zaxis : AccelerometerData.Zaxis;
			AccelerometerData.acquisitionTime = req.body.acquisitionTime ? req.body.acquisitionTime : AccelerometerData.acquisitionTime;
			
            AccelerometerData.save(function (err, AccelerometerData) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating AccelerometerData.',
                        error: err
                    });
                }

                return res.json(AccelerometerData);
            });
        });
    },

    /**
     * AccelerometerDataController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        AccelerometerdataModel.findByIdAndRemove(id, function (err, AccelerometerData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the AccelerometerData.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
