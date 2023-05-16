var express = require('express');
var router = express.Router();
var AccelerometerDataController = require('../controllers/AccelerometerDataController.js');

/*
 * GET
 */
router.get('/', AccelerometerDataController.list);

/*
 * GET
 */
router.get('/:id', AccelerometerDataController.show);

/*
 * POST
 */
router.post('/', AccelerometerDataController.create);

/*
 * PUT
 */
router.put('/:id', AccelerometerDataController.update);

/*
 * DELETE
 */
router.delete('/:id', AccelerometerDataController.remove);

module.exports = router;
