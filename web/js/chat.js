function initSocket()
{
    ws.send("start");
}

var serverHostName = window.location.hostname;

var portName = window.location.port;
if (portName.length == 0) {
    portName = "80";
}

var ws = null;

ws = new WebSocket("ws://" + serverHostName + ":" + portName + "/chatwork");

function upDateChatBoxSent(name, message) {
    $(".chat").append('<li class="right clearfix"><span class="chat-img pull-left"></span><div class="chat-body clearfix"><div class="header"><strong class="primary-font">' + name + '</strong></div><p>' + message + '</p></div></li>');
    $('#btn-input').val('');
    var newmsg_top = parseInt($('.panel-body')[0].scrollHeight);
    $('.panel-body').scrollTop(newmsg_top - 100);
}

function upDateChatBoxGet(name, message) {
    $(".chat").append('<li class="left clearfix"><span class="chat-img pull-left"></span><div class="chat-body clearfix"><div class="header"><strong class="primary-font">' + name + '</strong></div><p>' + message + '</p></div></li>');
    var newmsg_top = parseInt($('.panel-body')[0].scrollHeight);
    $('.panel-body').scrollTop(newmsg_top - 100);
}

function createJson(text) {
    var json_create = new Object();
    json_create.command = text;
    return JSON.stringify(json_create);
}

function createJsonGetName() {
    var json_create = new Object();
    json_create.command = "2";
    return JSON.stringify(json_create);
}

function upDateChatBoxSent(name, message) {
    $(".chat").append('<li class="right clearfix"><span class="chat-img pull-left"></span><div class="chat-body clearfix"><div class="header"><strong class="primary-font">' + name + '</strong></div><p>' + message + '</p></div></li>');
    $('#btn-input').val('');
    var newmsg_top = parseInt($('.panel-body')[0].scrollHeight);
    $('.panel-body').scrollTop(newmsg_top - 100);
}

function upDateChatBoxGet(name, message) {
    $(".chat").append('<li class="left clearfix"><span class="chat-img pull-left"></span><div class="chat-body clearfix"><div class="header"><strong class="primary-font">' + name + '</strong></div><p>' + message + '</p></div></li>');
    var newmsg_top = parseInt($('.panel-body')[0].scrollHeight);
    $('.panel-body').scrollTop(newmsg_top - 100);
}

function readyFunc() {
    $("#startButton").prop('disabled', false);
    $("#callButton").prop('disabled', true);
    $("#hangupButton").prop('disabled', true);

    //$("#startButton").click();
    //имя пользователя другим post запросом

    var serverHostName = window.location.hostname;

    var serverProtocolName = window.location.protocol;

    var portName = window.location.port;

    if (portName.length == 0) {
        portName = "80";
    }
    var serverPath = serverProtocolName + "//" + serverHostName + ":" + portName;

    if (serverHostName != "localhost") {
        serverPath += "/roulette"
    }

    getNameServer(serverPath);

    //$("head").append('<script type="text/javascript" src="' + "js/main.js" + '"></script>');

    var interlocutorName = "";

    $('#btn-input').val('');
    $('#text_input').val('');

    var ws = new WebSocket("ws://" + serverHostName + ":" + portName + "/chatwork");

    ws.onopen = function (event) {
    };

    ws.onmessage = function (event) {

        var jsonGet = JSON.parse(event.data);

        var answer = jsonGet["answer"];

        if (answer == "not_free_users") {
            $("#main_container").css("visibility", "hidden");
            //$("#stop_chat").prop('disabled', true);
            //$("#start_chat").prop('disabled', false);
            //$("#find_new_interlocutor").prop('disabled', true);
            //alert("Free users not found");
        }

        if (answer == "connected") {
            interlocutorName = jsonGet["interlocutor"];
            $("#interlocutor_name").text("You connected with: " + interlocutorName);
            $("#main_container").css("visibility", "visible");
        }

        if (answer == "message") {
            var clientName = $('#your_name').text().replace("Hello ", "");
            upDateChatBoxGet(clientName, jsonGet["message"]);
        }

        if (answer == "disconnect") {
            $("#stop_chat").prop('disabled', true);
            $("#start_chat").prop('disabled', false);
            $("#find_new_interlocutor").prop('disabled', true);
        }
    };

    ws.onclose = function (event) {
    };

    var newmsg_top = parseInt($('.panel-body')[0].scrollHeight);
    $('.panel-body').scrollTop(newmsg_top - 100);

    $("#start_chat").prop('disabled', false);
    $("#stop_chat").prop('disabled', true);
    $("#find_new_interlocutor").prop('disabled', true);

    $('#startButton').click(function () {
        start();
    });

    $('#callButton').click(function () {
        call(ws);
    });

    $('#hangupButton').click(function () {
        hangup();
    });

    $('#start_chat').click(function () {
        var json_create = new Object();
        json_create.name = $('#your_name').text().replace("Hello ", "");
        json_create.command = "connect";
        var json = JSON.stringify(json_create);
        ws.send(json);

        $("#stop_chat").prop('disabled', false);
        $("#start_chat").prop('disabled', true);
        $("#find_new_interlocutor").prop('disabled', false);
    });

    $('#stop_chat').click(function () {

        var json_create = new Object();
        json_create.name = $('#your_name').text().replace("Hello ", "");
        json_create.command = "disconnect";
        var json = JSON.stringify(json_create);
        ws.send(json);

        $("#stop_chat").prop('disabled', true);
        $("#start_chat").prop('disabled', false);
        $("#find_new_interlocutor").prop('disabled', true);
    });

    $('#find_new_interlocutor').click(function () {
        $('#start_chat').click();
        var json_create = new Object();
        json_create.name = $('#your_name').text().replace("Hello ", "");
        json_create.command = "find_interlocutor";
        var json = JSON.stringify(json_create);
        ws.send(json);
    });

    $('#btn-chat').click(function () {
        var messageText = $('#text_input').val();
        var json_create = new Object();
        var clientName = $('#your_name').text().replace("Hello ", "");
        json_create.name = clientName;
        json_create.command = "sent_message";
        json_create.message = messageText;
        var json = JSON.stringify(json_create);
        ws.send(json);

        upDateChatBoxSent("You", messageText);
    });

}