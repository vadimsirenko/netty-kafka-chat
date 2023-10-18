var login;
function Username() {
    var username = window.prompt("Enter your username:", "");

    if (username.toString().length > 2) {
        login = username;
    }
    else {
        alert("Your username must be at least two characters.");
        Username();
    }
}

var socket;
Username();
if (!window.WebSocket) {
    window.WebSocket = window.MozWebSocket;
}
if (window.WebSocket) {

    let config = { "name": login,
        "room_id":78, "token": "dfgfdsgfdsgfdsgfdsgfdsg"
    }
    let configJSON = JSON.stringify(config);
    // Encode the String
    var encodedString = Base64.encode(configJSON);

    //socket = new WebSocket("ws://localhost:8181/websocket/?request=eyJpZCI6MzQsICJuYW1lIjoidmFkaW0iLCAiY2hhdF9pZCI6NzgsICJ0b2tlbiI6ICJkZmdmZHNnZmRzZ2Zkc2dmZHNnZmRzZyJ9");
    socket = new WebSocket("ws://localhost:8181/websocket/?request="+encodedString);
    socket.onmessage = function (event) {
        console.log(event.data);
        let data = JSON.parse(event.data);
        date = new Date(data.data.ts);
        if(data.data.recipient === "") {
            document.getElementById("chatbox").innerText += "\n" + data.data.sender + ": " + data.data.message + " " + date.toLocaleString('ru-RU');
        } else if(data.data.sender === login || data.data.recipient === login){
            document.getElementById("chatbox").innerText += "\n" + data.data.sender + " для " + data.data.recipient + ": " + data.data.message + " " + date.toLocaleString('ru-RU');
        }
    };
    socket.onopen = function (event) {
        document.getElementById("welcome").innerHTML = 'Добро пожаловать, <b>'+ login + '</b>. Вы находитсь в чате: <b>Главный чат</b>';
        console.log("websocket opened");
    };
    socket.onclose = function (event) {
        console.log("websocket closed");
    };
}

function send(messageText) {
    if (!window.WebSocket || document.getElementById("messageText").value === "") {
        return;
    }
    if (socket.readyState === WebSocket.OPEN) {

        let message = {
            "messageType": "MESSAGE",
            "operationType":"CREATE",
            "recipient_id": document.getElementById("recipient").value,
            "messageText": messageText,
        };

        let messageJSON = JSON.stringify(message);
        //console.log(messageJSON);

        socket.send(messageJSON);
        document.getElementById("messageText").value = "";
    } else {
        alert("The socket is not open.");
    }
}