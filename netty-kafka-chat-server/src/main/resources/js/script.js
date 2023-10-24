(function () {

    String.prototype.interpolate = function(params) {
        const names = Object.keys(params);
        const vals = Object.values(params);
        return new Function(...names, `return \`${this}\`;`)(...vals);
    }

    const logoffTemplate = 'Пользователь ${text} покинул чат';
    const logonTemplate = 'Пользователь ${text} вошел в чат';
    let chat = {
        messageToSend: '',
        socket: null,
        roomId: null,
        senderId: null,
        nickName: null,
        messagesLength: 0,
        init: function () {
            this.cacheDOM();
            this.bindEvents();
            this.loginUser();
        },
        uuidv4: function() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
                return v.toString(16);
            });
        },
        cacheDOM: function () {
            this.$chatHistory = $('.chat-history');
            this.$roomSet = $('.room-set');
            this.$button = $('button');
            this.$textarea = $('#message-to-send');
            this.$chatTitle = $('.chat-with');
            this.$chatCountTitle = $('.chat-num-messages');
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
            let roomId = $(event.target).attr('data-id')
            if(roomId==undefined)
                return;
            this.$roomSetList.find('.chat-item').css({"font-weight": "normal","color":"white"});
            $(event.target).css({"font-weight": "bold","color":"#E38968"});
            this.$chatTitle.text($(event.target).text());
            console.log("roomId=" + roomId);
            this.goToRoom(roomId);
        },
        scrollToBottom: function () {
            this.$chatHistory.scrollTop(this.$chatHistory[0].scrollHeight);
        },
        getFormatedTime: function (date) {
            return date.toLocaleString();
        },
        loginUser: function () {
            let username = window.prompt("Enter your username:", "");
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
                    "login": userLogin,
                    "roomId": "057a7522-df71-4406-9559-844e7ce7cf4c",
                    "token": "dfgfdsgfdsgfdsgfdsgfdsg"
                }
                let configJSON = JSON.stringify(config);
                // Encode the String
                let encodedString = Base64.encode(configJSON);

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
            let data = JSON.parse(event.data);

            if (data.messageType === "MESSAGE") {
                console.log(data);
                this.processMessage(data);
            }
            if (data.messageType === "ROOM_LIST") {
                console.log(data);
                this.processRoomList(data.operationType, data.rooms);
            }
            if (data.messageType === "CLIENT") {
                console.log(data);
                this.processClientProfile(data.id, data.nickName);
            }
            if (data.messageType === "INFO") {
                console.log(data);
                this.processInfo(data);
            }
            if (data.messageType === "MESSAGE_LIST" && data.roomId === this.roomId) {
                this.processMessageList(data.operationType, data.messages);
            }
        },
        processClientProfile: function (id, nickName) {
            this.senderId = id;
            this.nickName = nickName;
        },
        processMessage: function (message) {
            this.messagesLength = this.messagesLength + 1;
            this.updateRoomStatistic();
            this.$chatHistoryList.append(this.renderedMessage(message));
            this.scrollToBottom();
        },
        processInfo: function (info) {
            this.$chatHistoryList.append(this.renderedInfoMessage(info));
            this.scrollToBottom();
        },
        renderedMessage(message){
            let templateResponse = Handlebars.compile($("#message-response-template").html());
            if (message.senderId === this.senderId) {
                templateResponse = Handlebars.compile($("#message-template").html());
            }
            let contextResponse = {
                login: message.sender,
                messageText: message.messageText,
                time: this.getFormatedTime(new Date(message.ts))
            };
            return templateResponse(contextResponse);
        },
        renderedInfoMessage(info){
            infoTemplate = null;
            if(info.operationType =="LOGON"){
                infoTemplate = logonTemplate;
            }
            if(info.operationType =="LOGOFF"){
                infoTemplate = logoffTemplate;
            }
            let templateResponse = Handlebars.compile($("#message-info-template").html());
            let contextResponse = {
                messageText: infoTemplate.interpolate({text: info.messageText}),
                time: this.getFormatedTime(new Date(info.ts))
            };
            return templateResponse(contextResponse);
        },
        processRoomList: function (operationType, rooms) {
            let templateRoomList = Handlebars.compile($("#room-set-item-template").html());
            this.$roomSetList.empty();
            this.$roomSetList.append( templateRoomList({objects:rooms}) );
        },
        processMessageList: function (operationType, messages) {
            this.$chatHistoryList.empty();
            this.messagesLength = messages.length;
            this.updateRoomStatistic();
            for(let i=0;i<messages.length;i++)
            {
                this.$chatHistoryList.append(this.renderedMessage(messages[i]));
            }
            this.$chatHistory.scrollTop(this.$chatHistory[0].scrollHeight);
        },
        updateRoomStatistic: function () {
            this.$chatCountTitle.text("уже " + this.messagesLength + " сообщений");
        },
        sendMessage: function () {
            this.scrollToBottom();
            if (this.messageToSend.trim() !== '' && this.roomId != null) {

                if (!window.WebSocket || this.messageToSend.trim() === "") {
                    return;
                }
                if (this.socket.readyState === WebSocket.OPEN) {

                    let message = {
                        "id": this.uuidv4(),
                        "messageType": "MESSAGE",
                        "operationType": "CREATE",
                        "ts": new Date().getTime(),
                        "messageText": this.messageToSend,
                        "roomId": this.roomId,
                        "senderId": this.senderId,
                        "sender": this.nickName
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
            if (roomId != null && this.roomId !== roomId) {
                this.roomId = roomId;
                if (!window.WebSocket) {
                    return;
                }
                if (this.socket.readyState === WebSocket.OPEN) {

                    let request = {
                        "messageType": "MESSAGE_LIST",
                        "operationType": "RECEIVE",
                        "senderId": this.senderId,
                        "ts": new Date().getTime(),
                        "roomId": roomId
                    };
                    let requestJSON = JSON.stringify(request);
                    this.socket.send(requestJSON);
                } else {
                    alert("The socket is not open.");
                }
            }
        }
    };

    chat.init();

    let searchFilter = {
        options: {valueNames: ['name']},
        init: function () {
            let userList = new List('people-list', this.options);
            let noItems = $('<li id="no-items-found">No items found</li>');

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

    let searchRoomFilter = {
        options: {valueNames: ['name']},
        init: function () {
            let chatList = new List('chat-list', this.options);
            let noItems = $('<li id="no-items-found">No items found</li>');

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