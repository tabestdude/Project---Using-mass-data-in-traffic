console.log(process.env.PORT);
var createError = require('http-errors');
var express = require('express');
var usersController = require('./controllers/usersController.js');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

var mongoose = require('mongoose');
//var mongoDB = "mongodb://mongo-db:27017/MasiveDataProject";
var mongoDB = "mongodb://127.0.0.1/MasiveDataProject";
mongoose.connect(mongoDB);
mongoose.Promise = global.Promise;
var db = mongoose.connection;
db.on('error', console.error.bind(console, 'MongoDB connection error:'));

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/usersRoutes');
var roadStateRouter = require('./routes/roadStateRoutes');

var app = express();

var cors = require('cors');
//var allowedOrigins = ['http://localhost:3000', 'http://frontend:3000', 'http://backend:3080'];
var allowedOrigins = ['http://localhost:3000', 'http://localhost:3001'];
app.use(cors({
  credentials: true,
  origin: function(origin, callback){
    // Allow requests with no origin (mobile apps, curl)
    if(!origin) return callback(null, true);
    if(allowedOrigins.indexOf(origin)===-1){
      var msg = "The CORS policy does not allow access from the specified Origin.";
      return callback(new Error(msg), false);
    }
    return callback(null, true);
  }
}));

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');


app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

var session = require('express-session');
var MongoStore = require('connect-mongo');
app.use(session({
  secret: 'Lenko are Majer are best friends',
  resave: true,
  saveUninitialized: false,
  store: MongoStore.create({mongoUrl: mongoDB})
}));

//Shranimo sejne spremenljivke v locals
//Tako lahko do njih dostopamo v vseh view-ih (glej layout.hbs)
app.use(function (req, res, next) {
  res.locals.session = req.session;
  next();
});

// Clean up old data every 5 seconds
setInterval(() => {
  const mockReq = {};
  const mockRes = {
      status: function (statusCode) {
          this.statusCode = statusCode;
          return this;
      },
      json: function (data) {
          /*console.log("Response status code:", this.statusCode);
          console.log("Response data:", data);*/
          return this;
      }
  };
  usersController.cleanupOldRoadStates(mockReq, mockRes);
}, 5000);



app.use('/', indexRouter);
app.use('/users', usersRouter);
app.use('/roadState', roadStateRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

/*app.listen(3080, '0.0.0.0', function() {
  console.log('Node app is running on port', 3080);
});*/

module.exports = app;
