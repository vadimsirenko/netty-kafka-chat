(function () {

    var chat = {
        messageToSend: '',
        socket: null,
        init: function () {
            this.cacheDOM();
            this.bindEvents();
            this.loginUser();
        },
        cacheDOM: function () {
            this.$chatHistory = $('.chat-history');
            this.$button = $('button');
            this.$textarea = $('#message-to-send');
            this.$chatHistoryList = this.$chatHistory.find('ul');
        },
        bindEvents: function () {
            this.$button.on('click', this.addMessage.bind(this));
            this.$textarea.on('keyup', this.addMessageEnter.bind(this));
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
                    "room_id": "ae81251e-6e7e-11ee-b962-0242ac120002", "token": "dfgfdsgfdsgfdsgfdsgfdsg"
                }
                let configJSON = JSON.stringify(config);
                // Encode the String
                var encodedString = Base64.encode(configJSON);

                this.socket = new WebSocket("ws://localhost:8181/websocket/?request=" + encodedString);
                this.socket.addEventListener('message',this.receiveMessage.bind(this));
                this.socket.addEventListener('open',this.socketOpen.bind(this));
                this.socket.addEventListener('close',this.socketClose.bind(this));
            }
        },
        socketClose: function (event){
            console.log("websocket closed");
        },
        socketOpen: function (event){
            //document.getElementById("welcome").innerHTML = 'Добро пожаловать, <b>'+ login + '</b>. Вы находитсь в чате: <b>Главный чат</b>';
            console.log("websocket opened");
        },
        receiveMessage: function (event) {
            console.log(event.data);
            let data = JSON.parse(event.data);

            if(data.messageType == "MESSAGE"){
                this.processMessage(data.data.sender, data.data.recipient, data.data.message, data.data.ts);
            }
        },
        processMessage: function (sender, recipient, message, ts) {
            var templateResponse = Handlebars.compile($("#message-response-template").html());
            if(sender === this.login){
                templateResponse = Handlebars.compile($("#message-template").html());
            }
            var contextResponse = {
                login: sender,
                messageText: message,
                time: this.getFormatedTime(new Date(ts))
            };
            this.$chatHistoryList.append(templateResponse(contextResponse));
            this.scrollToBottom();
        },
        sendMessage: function () {
            this.scrollToBottom();
            if (this.messageToSend.trim() !== '') {

                if (!window.WebSocket || this.messageToSend.trim() === "") {
                    return;
                }
                if (this.socket.readyState === WebSocket.OPEN) {

                    let message = {
                        "messageType": "MESSAGE",
                        "operationType": "CREATE",
                        "recipient_id": null, //23,
                        "messageText": this.messageToSend,
                    };

                    let messageJSON = JSON.stringify(message);
                    this.socket.send(messageJSON);
/*
                    var template = Handlebars.compile($("#message-template").html());
                    var context = {
                        login: this.login,
                        messageOutput: this.messageToSend,
                        time: this.getCurrentTime()
                    };

                    this.$chatHistoryList.append(template(context));
                    this.scrollToBottom();
                    */
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

})();