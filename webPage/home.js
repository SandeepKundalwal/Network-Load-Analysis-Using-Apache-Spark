const express = require('express');
const socketIO = require('socket.io');
let app = express();
var server = http.createServer(app);
let io = socketIO(server);
var __dirname = "/Users/mahimagupta/Desktop/Semester/BigData_and_MapReduce/FrontEnd/webPage/views/home.html";
app.user(express.static(__dirname));

io.on('connection', (socket) =>{
    console.log("Connected");
    socket.broadcast.emit('newMessage', "Welcome");
    socket.on('disconnect', ()=>{
        console.log("Connection closed");
    })
})