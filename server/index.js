var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);

var players = [];
var playerIDs = [];
var map = {};
var currentPlayer;
var gameCommenced = false;

server.listen(9021, function(){
    console.log("Server is now running...");
    createMap();
});

function shuffle(array) {
    var counter = array.length;

    // While there are elements in the array
    while (counter > 0) {
        // Pick a random index
        var index = Math.floor(Math.random() * counter);

        // Decrease counter by 1
        counter--;

        // And swap the last element with it
        var temp = array[counter];
        array[counter] = array[index];
        array[index] = temp;
    }

    return array;
}

function createMap() {
    var TileType = {
        WOOL : 0,
        WOOD : 1,
        WHEAT : 2,
        ORE : 3,
        STONE : 4,
        DESERT : 5,
        WATER : 6
    };

    var tileTypes = [];

    for (var i = 0; i < 4; i++) {
        tileTypes.push(TileType.WOOL);
        tileTypes.push(TileType.WOOD);
        tileTypes.push(TileType.WHEAT);
    }

    for (i = 0; i < 3; i++) {
        tileTypes.push(TileType.ORE);
        tileTypes.push(TileType.STONE);
    }

    tileTypes.push(TileType.DESERT);

    tileTypes = shuffle(tileTypes);

    var tileNumbers = [0, 2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12];

    var desertPosInTypes, desertPosInNumbers;

    for (i = 0; i < tileTypes.length; i++) {
        if (tileTypes[i] == TileType.DESERT) {
            desertPosInTypes = i;
        }
    }

    tileNumbers = shuffle(tileNumbers);

    for (i = 0; i < tileNumbers.length; i++) {
        if (tileNumbers[i] == 0) {
            desertPosInNumbers = i;
        }
    }

    tileNumbers[desertPosInNumbers] = tileNumbers[desertPosInTypes];
    tileNumbers[desertPosInTypes] = 0;

    map = {
        0: {
            1: {tileType: tileTypes[0], tileNumber: tileNumbers[0]},
            2: {tileType: tileTypes[1], tileNumber: tileNumbers[1]},
            3: {tileType: tileTypes[2], tileNumber: tileNumbers[2]}
        },
        1: {
            0: {tileType: tileTypes[3], tileNumber: tileNumbers[3]},
            1: {tileType: tileTypes[4], tileNumber: tileNumbers[4]},
            2: {tileType: tileTypes[5], tileNumber: tileNumbers[5]},
            3: {tileType: tileTypes[6], tileNumber: tileNumbers[6]}
        },
        2: {
            0: {tileType: tileTypes[7], tileNumber: tileNumbers[7]},
            1: {tileType: tileTypes[8], tileNumber: tileNumbers[8]},
            2: {tileType: tileTypes[9], tileNumber: tileNumbers[9]},
            3: {tileType: tileTypes[10], tileNumber: tileNumbers[10]},
            4: {tileType: tileTypes[11], tileNumber: tileNumbers[11]}
        },
        3: {
            0: {tileType: tileTypes[12], tileNumber: tileNumbers[12]},
            1: {tileType: tileTypes[13], tileNumber: tileNumbers[13]},
            2: {tileType: tileTypes[14], tileNumber: tileNumbers[14]},
            3: {tileType: tileTypes[15], tileNumber: tileNumbers[15]}
        },
        4: {
            1: {tileType: tileTypes[16], tileNumber: tileNumbers[16]},
            2: {tileType: tileTypes[17], tileNumber: tileNumbers[17]},
            3: {tileType: tileTypes[18], tileNumber: tileNumbers[18]}
        }
    };
}

io.on('connection', function (socket) {
    console.log("connect");
    socket.emit('socketID', {id: socket.id});
    socket.broadcast.emit('newPlayer', {id: socket.id});
    socket.emit('mapData', {mapData: map});

    players.push(socket);
    playerIDs.push(socket.id);
    io.sockets.emit("playerList", {playerList: playerIDs});
    if (players.length == 2) {
        io.sockets.emit("gameInfo", "2 players in game, the game has begun.");
        gameCommenced = true;
        currentPlayer = players[0];
    }
    
    socket.on('disconnect', function () {
        var index = players.indexOf(socket);
        players.splice(index, 1);
        playerIDs.splice(index, 1);
        socket.broadcast.emit('playerLeft', {id: socket.id});
        socket.broadcast.emit("playerList", {playerList: playerIDs});
        console.log("disconnect");
    });

    socket.on('placeVillageRequest', function(x, y) {
        if (gameCommenced) {
            if (socket == currentPlayer) {
                console.log("It is currently this player's turn; placing village.");
                io.sockets.emit("placeVillage", x, y, socket);
            } else {
                console.log("It's not currently this player's turn; not placing village.")
            }
        } else {
            console.log("Game has not commenced.");
        }
    })
});