var GpsdataModel = require('../models/gpsDataModel.js');

/**
 * gpsDataController.js
 *
 * @description :: Server-side logic for managing gpsDatas.
 */
module.exports = {

    /**
     * gpsDataController.list()
     */
    list: function (req, res) {
        GpsdataModel.find(function (err, gpsDatas) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting gpsData.',
                    error: err
                });
            }

            return res.json(gpsDatas);
        });
    },

    /**
     * gpsDataController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        GpsdataModel.findOne({_id: id}, function (err, gpsData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting gpsData.',
                    error: err
                });
            }

            if (!gpsData) {
                return res.status(404).json({
                    message: 'No such gpsData'
                });
            }

            return res.json(gpsData);
        });
    },

    /**
     * gpsDataController.create()
     */
    create: function (req, res) {
        var gpsData = new GpsdataModel({
			latitude : req.body.latitude,
			longitude : req.body.longitude,
			speed : req.body.speed
        });

        gpsData.save(function (err, gpsData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating gpsData',
                    error: err
                });
            }

            return res.status(201).json(gpsData);
        });
    },

    /**
     * gpsDataController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        GpsdataModel.findOne({_id: id}, function (err, gpsData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting gpsData',
                    error: err
                });
            }

            if (!gpsData) {
                return res.status(404).json({
                    message: 'No such gpsData'
                });
            }

            gpsData.latitude = req.body.latitude ? req.body.latitude : gpsData.latitude;
			gpsData.longitude = req.body.longitude ? req.body.longitude : gpsData.longitude;
			gpsData.speed = req.body.speed ? req.body.speed : gpsData.speed;
			
            gpsData.save(function (err, gpsData) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating gpsData.',
                        error: err
                    });
                }

                return res.json(gpsData);
            });
        });
    },

    /**
     * gpsDataController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        GpsdataModel.findByIdAndRemove(id, function (err, gpsData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the gpsData.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
