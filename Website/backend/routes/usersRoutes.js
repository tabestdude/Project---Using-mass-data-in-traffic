var express = require('express');
var router = express.Router();
var usersController = require('../controllers/usersController.js');

/*
 * GET
 */
router.get('/', usersController.list);

/*
 * GET
 */
router.get('/:id', usersController.show);
//router.get('/logout', usersController.logout); // it's not working
router.get('/logout/user', usersController.logout);
/*
 * POST
 */
router.post('/', usersController.create);
router.post('/login', usersController.login);
router.post('/login/phone', usersController.loginPhone);

/*
 * PUT
 */
router.put('/:id', usersController.update);

/*
 * DELETE
 */
router.delete('/:id', usersController.remove);

module.exports = router;
