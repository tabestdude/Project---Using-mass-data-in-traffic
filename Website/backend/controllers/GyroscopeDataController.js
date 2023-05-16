var GyroscopedataModel = require('../models/GyroscopeDataModel.js');

/**
 * GyroscopeDataController.js
 *
 * @description :: Server-side logic for managing GyroscopeDatas.
 */
module.exports = {

    /**
     * GyroscopeDataController.list()
     */
    list: function (req, res) {
        GyroscopedataModel.find(function (err, GyroscopeDatas) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting GyroscopeData.',
                    error: err
                });
            }

            return res.json(GyroscopeDatas);
        });
    },

    /**
     * GyroscopeDataController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        GyroscopedataModel.findOne({_id: id}, function (err, GyroscopeData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting GyroscopeData.',
                    error: err
                });
            }

            if (!GyroscopeData) {
                return res.status(404).json({
                    message: 'No such GyroscopeData'
                });
            }

            return res.json(GyroscopeData);
        });
    },

    /**
     * GyroscopeDataController.create()
     */
    create: function (req, res) {
        var GyroscopeData = new GyroscopedataModel({
			Xaxis : req.body.Xaxis,
			Yaxis : req.body.Yaxis,
			Zaxis : req.body.Zaxis,
			acquisitionTime : req.body.acquisitionTime
        });

        GyroscopeData.save(function (err, GyroscopeData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating GyroscopeData',
                    error: err
                });
            }

            return res.status(201).json(GyroscopeData);
        });
    },

    /**
     * GyroscopeDataController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        GyroscopedataModel.findOne({_id: id}, function (err, GyroscopeData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting GyroscopeData',
                    error: err
                });
            }

            if (!GyroscopeData) {
                return res.status(404).json({
                    message: 'No such GyroscopeData'
                });
            }

            GyroscopeData.Xaxis = req.body.Xaxis ? req.body.Xaxis : GyroscopeData.Xaxis;
			GyroscopeData.Yaxis = req.body.Yaxis ? req.body.Yaxis : GyroscopeData.Yaxis;
			GyroscopeData.Zaxis = req.body.Zaxis ? req.body.Zaxis : GyroscopeData.Zaxis;
			GyroscopeData.acquisitionTime = req.body.acquisitionTime ? req.body.acquisitionTime : GyroscopeData.acquisitionTime;
			
            GyroscopeData.save(function (err, GyroscopeData) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating GyroscopeData.',
                        error: err
                    });
                }

                return res.json(GyroscopeData);
            });
        });
    },

    /**
     * GyroscopeDataController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        GyroscopedataModel.findByIdAndRemove(id, function (err, GyroscopeData) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the GyroscopeData.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
