(function () {

    var chat = {
        messageToSend: '',
        socket: null,
        roomId: null,
        senderId: null,
        init: function () {
            this.cacheDOM();
            this.bindEvents();
            this.loginUser();
        },
        cacheDOM: function () {
            this.$chatHistory = $('.chat-history');
            this.$roomSet = $('.room-set');
            this.$button = $('button');
            this.$textarea = $('#message-to-send');
            this.$chatHistoryList = this.$chatHistory.find('ul');
            this.$roomSetList = this.$roomSet.find('ul');
        },
        bindEvents: function () {
            this.$button.on('click', this.addMessage.bind(this));
            this.$textarea.on('keyup', this.addMessageEnter.bind(this));
            this.$roomSetList.on("click","li",this.chatClick.bind(this));
        },
        addMessage: function () {
            this.messageToSend = this.$textarea.val()
            this.sendMessage();
        },
        addMessageEnter: function (event) {
            // enter was pressed
            if (event.keyCode === 13) {
                this.addMessage();
            }
        },
        chatClick: function (event) {
            roomId = $(event.target).attr('data-id');
            this.goToRoom(roomId);
        },
        scrollToBottom: function () {
            this.$chatHistory.scrollTop(this.$chatHistory[0].scrollHeight);
        },
        getCurrentTime: function () {
            return this.getFormatedTime(new Date());
        },
        getFormatedTime: function (date) {
            return date.toLocaleTimeString('ru-RU', {hour12: false}).replace(/([\d]+:[\d]{2})(:[\d]{2})(.*)/, "$1$3");
        },
        loginUser: function () {
            var username = window.prompt("Enter your username:", "");
            if (username.toString().length > 2) {
                this.login = username;
                this.connectToChatServer(username);
            } else {
                alert("Your username must be at least two characters.");
                loginUser();
            }
        },
        connectToChatServer: function (userLogin) {
            if (!window.WebSocket) {
                window.WebSocket = window.MozWebSocket;
            }
            if (window.WebSocket) {

                let config = {
                    "name": userLogin,
                    "token": "dfgfdsgfdsgfdsgfdsgfdsg"
                }
                let configJSON = JSON.stringify(config);
                // Encode the String
                var encodedString = Base64.encode(configJSON);

                this.socket = new WebSocket("ws://localhost:8181/websocket/?request=" + encodedString);
                this.socket.addEventListener('message', this.receiveMessage.bind(this));
                this.socket.addEventListener('open', this.socketOpen.bind(this));
                this.socket.addEventListener('close', this.socketClose.bind(this));
            }
        },
        socketClose: function (event) {
            console.log("websocket closed");
        },
        socketOpen: function (event) {
            //document.getElementById("welcome").innerHTML = 'Добро пожаловать, <b>'+ login + '</b>. Вы находитсь в чате: <b>Главный чат</b>';
            console.log("websocket opened");
        },
        receiveMessage: function (event) {
            console.log(event.data);
            let data = JSON.parse(event.data);

            if (data.messageType === "MESSAGE") {
                this.processMessage(data);
            }
            if (data.messageType === "ROOM_LIST") {
                this.processRoomList(data.operationType, data.rooms);
            }
            if (data.messageType === "CLIENT") {
                this.processClientProfile(data.id, data.name);
            }
            if (data.messageType === "MESSAGE_LIST" && data.roomId === this.roomId) {
                this.processMessageList(data.operationType, data.messages);
            }
        },
        processClientProfile: function (id, name) {
            this.senderId = id;
        },
        processMessage: function (message) {
            //this.renderMessage(message);
/*
            var templateResponse = Handlebars.compile($("#message-response-template").html());
            if (sender === this.login) {
                templateResponse = Handlebars.compile($("#message-template").html());
            }
            var contextResponse = {
                login: sender,
                messageText: message,
                time: this.getFormatedTime(new Date(ts))
            };
            this.$chatHistoryList.append(templateResponse(contextResponse));

*/
            this.$chatHistoryList.append(this.renderedMessage(message));
            this.scrollToBottom();
        },
        renderedMessage(message){
            var templateResponse = Handlebars.compile($("#message-response-template").html());
            //if (message.sender === this.login) {
            if (message.senderId === this.senderId) {
                templateResponse = Handlebars.compile($("#message-template").html());
            }
            var contextResponse = {
                login: message.senderId,
                messageText: message.messageText,
                time: this.getFormatedTime(new Date(message.ts))
            };
            return templateResponse(contextResponse);
        },
        processRoomList: function (operationType, rooms) {
            var templateRoomList = Handlebars.compile($("#room-set-item-template").html());
            this.$roomSetList.empty();
            this.$roomSetList.append( templateRoomList({objects:rooms}) );

            //this.goToRoom(rooms[0].id);
        },
        processMessageList: function (operationType, messages) {
            var templateRoomList = Handlebars.compile($("#room-set-item-template").html());
            this.$chatHistoryList.empty();
            for(var i=0;i<messages.length;i++)
            {
                this.$chatHistoryList.append(this.renderedMessage(messages[i]));
            }
            this.$chatHistory.scrollTop(this.$chatHistory[0].scrollHeight);
        },
        sendMessage: function () {
            this.scrollToBottom();
            if (this.messageToSend.trim() !== '' && this.roomId != null) {

                if (!window.WebSocket || this.messageToSend.trim() === "") {
                    return;
                }
                if (this.socket.readyState === WebSocket.OPEN) {

                    let message = {
                        "messageType": "MESSAGE",
                        "operationType": "CREATE",
                        "recipient_id": null, //23,
                        "ts": new Date().getTime(),
                        "messageText": this.messageToSend,
                        "roomId": "ae81251e-6e7e-11ee-b962-0242ac120002",
                        "senderId": this.senderId
                    };
                    let messageJSON = JSON.stringify(message);
                    this.socket.send(messageJSON);
                    this.$textarea.val('');

                } else {
                    alert("The socket is not open.");
                }
            }
        },
        goToRoom: function (roomId) {
            this.scrollToBottom();
            this.roomId = roomId;
            if (this.roomId != null) {
                if (!window.WebSocket) {
                    return;
                }
                if (this.socket.readyState === WebSocket.OPEN) {

                    let request = {
                        "messageType": "MESSAGE_LIST",
                        "operationType": "RECEIVE",
                        "ts": new Date().getTime(),
                        "roomId": roomId
                    };
                    let requestJSON = JSON.stringify(request);
                    this.socket.send(requestJSON);
                    this.$textarea.val('');

                } else {
                    alert("The socket is not open.");
                }
            }
        }
    };

    chat.init();

    var searchFilter = {
        options: {valueNames: ['name']},
        init: function () {
            var userList = new List('people-list', this.options);
            var noItems = $('<li id="no-items-found">No items found</li>');

            userList.on('updated', function (list) {
                if (list.matchingItems.length === 0) {
                    $(list.list).append(noItems);
                } else {
                    noItems.detach();
                }
            });
        }
    };

    searchFilter.init();

    var searchRoomFilter = {
        options: {valueNames: ['name']},
        init: function () {
            var chatList = new List('chat-list', this.options);
            var noItems = $('<li id="no-items-found">No items found</li>');

            chatList.on('updated', function (list) {
                if (list.matchingItems.length === 0) {
                    $(list.list).append(noItems);
                } else {
                    noItems.detach();
                }
            });
        }
    };
    searchRoomFilter.init();

})();