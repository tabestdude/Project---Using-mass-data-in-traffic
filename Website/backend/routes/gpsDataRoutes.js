var express = require('express');
var router = express.Router();
var gpsDataController = require('../controllers/gpsDataController.js');

/*
 * GET
 */
router.get('/', gpsDataController.list);

/*
 * GET
 */
router.get('/:id', gpsDataController.show);

/*
 * POST
 */
router.post('/', gpsDataController.create);

/*
 * PUT
 */
router.put('/:id', gpsDataController.update);

/*
 * DELETE
 */
router.delete('/:id', gpsDataController.remove);

module.exports = router;
