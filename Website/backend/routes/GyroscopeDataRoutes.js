var express = require('express');
var router = express.Router();
var GyroscopeDataController = require('../controllers/GyroscopeDataController.js');

/*
 * GET
 */
router.get('/', GyroscopeDataController.list);

/*
 * GET
 */
router.get('/:id', GyroscopeDataController.show);

/*
 * POST
 */
router.post('/', GyroscopeDataController.create);

/*
 * PUT
 */
router.put('/:id', GyroscopeDataController.update);

/*
 * DELETE
 */
router.delete('/:id', GyroscopeDataController.remove);

module.exports = router;
