var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
const net = require('net'); 

app.get('/', function(req, res){
  res.sendfile('Dashboard.html');
});


var port = 7777; //The same port that the server is listening on
var host = 'localhost';
const client = new net.Socket();
client.connect(port, host);

io.sockets.on('connection', function (socket) {
    console.log('a user connected');

    socket.on('disconnect', function () {
        console.log('user disconnected');
        
    });

    client.on("data", data => {
      var json = data.toString();
      console.log("Received data : " + json);
      var jsonArr = json.split("|");
      // console.log(jsonArr);

      for(var i = 0; i < jsonArr.length - 1 ; i++) {
        var packet
        try {
          packet = JSON.parse(jsonArr[i]);
          var x = packet.time * 1000,
          y = packet.totalSize / (1024);
          socket.emit('chart_data', {
            x: x,
            y: y
          });
        } catch(err) {
          console.log(err.message);
          continue;
        }
        // console.info("emitted: [" + x + "," + y + "]");
      }
    })
});

http.listen(3000, function(){
  console.log('listening on *:3000');
});