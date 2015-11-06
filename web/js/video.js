var serverHostName = window.location.hostname;

var portName = window.location.port;
if (portName.length == 0) {
    portName = "80";
}

var wasUsed = true;

var isVideoCall = 0;

var ws = null;

if (serverHostName === 'videochatspbu.ru') {
    ws = new WebSocket("ws://" + "95.213.199.90" + ":" + "8080" + "/webrtc");
}
else {
    ws = new WebSocket("ws://" + serverHostName + ":" + portName + "/webrtc");
}

var apiKey = '45400602';
var sessionId = '2_MX40NTQwMDYwMn5-MTQ0NjgxMDEwMTUzOH52WVR6SmJ5Q29pRlljMG5MY2N3aG5VdVF-UH4';

var session = OT.initSession(apiKey, sessionId);

ws.onmessage = function (event) {
    var getData = JSON.parse(event.data);
    if (getData["answer"] === "token") {
        //поставить ограничение на количество генерируемых ключей
        if (getData["token"] == "") {
            $('#token_space').append("<p>" + "Ключей нет, обратитесь к администратору" + "</p>");
            $('#tokenButton').hide();
        }
        else {
            $('#token_space').append("<p>" + getData["token"] + "</p>");
        }
        count_tocken = count_tocken + 1;

        if (count_tocken > 7) {
            $('#tokenButton').attr("disabled", true);
        }
    }

    if (getData["answer"] === "changed") {
        $('#my_profile').modal('hide');

        $("#NameInput").val(getData["NewName"]);

        alert("Name was changed");
    }
};

function initSocket() {
    var sentJson1 = new Object();
    sentJson1.command = "0";

    sentJson1.ctrSum = $('#controlsum').text();
    sentJson1.ip = userIp;

    sentJson1.name = $('#your_name').text();

    //componentPropetrOn();
    $("#stopButton").attr("disabled", false);

    ws.send(JSON.stringify(sentJson1));

    waitingWindowStart();
}

ws.onmessage = function (event) {

    var getJson = JSON.parse(event.data);
    var getCommand = getJson["answer"];


    ///получить токен и начать трансляцию
    if (getCommand === "start")
    {
        var token = getJson["token"];

        session.on({
            streamCreated: function (event) {
                var options = {width: 400, height: 300};
                session.subscribe(event.stream, 'remote_container', options);
            }
        });

        //token = 'T1==cGFydG5lcl9pZD00NTQwMDYwMiZzaWc9MTI4Njg2NTg2ZmQwZjY4YWJmZDJkZTY1YjAxYjY3MjM4YjhhMmI0Mzpyb2xlPXB1Ymxpc2hlciZzZXNzaW9uX2lkPTJfTVg0ME5UUXdNRFl3TW41LU1UUTBOamd4TURFd01UVXpPSDUyV1ZSNlNtSjVRMjlwUmxsak1HNU1ZMk4zYUc1VmRWRi1VSDQmY3JlYXRlX3RpbWU9MTQ0NjgxMDEwOCZub25jZT0wLjQzNTc0MDc4MDA0OTY1NjImZXhwaXJlX3RpbWU9MTQ0OTQwMjA4NiZjb25uZWN0aW9uX2RhdGE9';
        session.connect(token, function (error) {
            if (error) {
                console.log(error.message);
            } else {
                session.publish('local_container', {width: 400, height: 300});
            }
        });

    }

    log(getCommand);

    //
    //// не верная контрольная сумма
    //if (getCommand === "control") {
    //    alert("Возможно это ошибка. Но судя по всему, вы производите атаку на сервер подменой клиентского кода. Доступ закрыт. Сожалеем.");
    //    pc.close();
    //    ws.close();
    //    $("body").hide();
    //}
    //
    //if (getCommand == "owner") {
    //
    //
    //    initiator = false;
    //
    //    if (initiator) {
    //        createOffer();
    //    } else {
    //        log('Waiting for guest connection...');
    //    }
    //    //initialize();
    //}
    //if (getCommand == "guest") {
    //    $('#interlocutor_name').text(getJson["nameInterlocutor"]);
    //    initiator = true;
    //
    //    if (initiator) {
    //        createOffer();
    //    } else {
    //        log('Waiting for guest connection...');
    //    }
    //    //initialize();
    //}
    //
    //if (getCommand === "system") {
    //
    //    componentPropetrOn();
    //
    //    var signal = JSON.parse(getJson["data"]);
    //    if (signal.sdp) {
    //        if (initiator) {
    //            receiveAnswer(signal);
    //            waitingWindowStop();
    //        } else {
    //            $('#interlocutor_name').text(getJson["interlocutorName"]);
    //
    //            receiveOffer(signal);
    //            waitingWindowStop();
    //        }
    //    } else if (signal.candidate) {
    //
    //        pc.addIceCandidate(new IceCandidate(signal));
    //    }
    //}
    //
    //if (getCommand === "message") {
    //    var textMessages = getJson["message"];
    //    var interlocutorNameChat = $('#interlocutor_name').text();
    //    upDateChatBoxGet(interlocutorNameChat, textMessages);
    //}
    //
    //if (getCommand === "new_window") {
    //    pc.close();
    //    $('#remote_container').remove();
    //
    //    $('#main_container').prepend("<div class='row' id='remote_container'><video id='remote' autoplay></video></div>");
    //
    //    componentPropetrOff();
    //    $('#myModal2').modal('show');
    //}
    //
    ////перейти в режим ожидания
    //if (getCommand === "wait_window") {
    //    success(stream);
    //    waitingWindowStart();
    //    initiator = false;
    //
    //}
    //
    ////найден собеседник (ответить)
    //if (getCommand === "new_interlocutor") {
    //    pc.close();
    //    success(stream);
    //    createOffer();
    //}
    //
    //if (getJson["answer"] === "token") {
    //    //поставить ограничение на количество генерируемых ключей
    //
    //
    //    if (getJson["token"] == "") {
    //        $('#token_space').append("<p>" + "Ключей нет, обратитесь к администратору" + "</p>");
    //        $('#tokenButton').hide();
    //    }
    //    else {
    //        $('#token_space').append("<p>" + getJson["token"] + "</p>");
    //    }
    //
    //    count_tocken = count_tocken + 1;
    //
    //    if (count_tocken > 7) {
    //        $('#tokenButton').attr("disabled", true);
    //    }
    //}
    //
    //if (getJson["answer"] === "changed") {
    //    $('#my_profile').modal('hide');
    //    $("#NameInput").val(getJson["NewName"]);
    //    alert("Name was changed");
    //}
    //
    //if (getJson["answer"] === "changed_interlocutor_name") {
    //    $('#interlocutor_name').text(getJson["interlocutorName"]);
    //    log(getJson["interlocutorName"]);
    //}
};

function log() {
    $('#traceback').text(Array.prototype.join.call(arguments, ' '));
    console.log.apply(console, arguments);
}

function hangup() {
    pc.close();

    waitingWindowStop();

    $('#remote_container').remove();

    $('#main_container').prepend("<div class='row' id='remote_container'><video id='remote' autoplay></video></div>");

    componentPropetrOff();

    //call closeConnect server
    var sentJson = new Object();
    sentJson.command = "4";
    ws.send(JSON.stringify(sentJson));

    initiator = true;

    wasUsed = true;
}

function newInterlocutor() {
    var sentJson = new Object();
    sentJson.command = "2";
    sentJson.name = $('#your_name').text();

    $('#myModal2').modal('hide');

    waitingWindowStart();

    initiator = true;
    wasUsed = false;

    ws.send(JSON.stringify(sentJson));
}


function newInterlocutorButton() {

    var sentJson = new Object();
    sentJson.command = "7";
    ws.send(JSON.stringify(sentJson));

    initiator = false;
    wasUsed = true;

    pc.close();

    $('#remote_container').remove();

    $('#main_container').prepend("<div class='row' id='remote_container'><video id='remote' autoplay></video></div>");

    componentPropetrOff();
}


function upDateChatBoxSent(name, message) {

    var newMes = message.replace(/\r?\n/g, '<br />');

    $(".chat").append('<li class="right clearfix"><span class="chat-img pull-left"></span><div class="chat-body clearfix"><div class="header"><strong class="primary-font">' + name + '</strong></div><p>' + newMes + '</p></div></li>');
    $('#text_input').val('');
    var newmsg_top = parseInt($('.panel-body')[0].scrollHeight);
    $('.panel-body').scrollTop(newmsg_top - 100);
}

function sentMessages() {
    var messageText = $('#text_input').val();
    var json_create = new Object();
    var clientName = $('#your_name').text();
    json_create.command = "3";
    json_create.message = messageText;
    var json = JSON.stringify(json_create);
    ws.send(json);

    upDateChatBoxSent("You", messageText);
}

function upDateChatBoxGet(name, message) {
    var newMes = message.replace(/\r?\n/g, '<br />');
    $(".chat").append('<li class="left clearfix"><span class="chat-img pull-left"></span><div class="chat-body clearfix"><div class="header"><strong class="primary-font">' + name + '</strong></div><p>' + newMes + '</p></div></li>');
    var newmsg_top = parseInt($('.panel-body')[0].scrollHeight);
    $('.panel-body').scrollTop(newmsg_top - 100);
}

function generateToken() {
    var json_create = new Object();
    json_create.command = "5";
    var json = JSON.stringify(json_create);
    ws.send(json);
}

function changeNickName() {
    var json_create = new Object();
    json_create.command = "6";
    json_create.new_name = $('#NewKeyInput').val();
    json_create.ip = userIp;
    json_create.last_name = $('#your_name').text();

    var json = JSON.stringify(json_create);
    ws.send(json);
}