var express = require('express');
var router = express.Router();
var roadStateController = require('../controllers/roadStateController.js');

/*
 * GET
 */
router.get('/', roadStateController.list);

/*
 * GET
 */
router.get('/:id', roadStateController.show);
router.get('/publisher/:id', roadStateController.listByPublisher);

/*
 * POST
 */
router.post('/', roadStateController.create);

/*
 * PUT
 */
router.put('/:id', roadStateController.update);

/*
 * DELETE
 */
router.delete('/:id', roadStateController.remove);

module.exports = router;
