var serverHostName = window.location.hostname;

var portName = window.location.port;
if (portName.length == 0) {
    portName = "80";
}
count_tocken = 0;

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

var publisher = null;
var subscriber = null;


var wasUsed = false;

function initSocket() {

    var pubOptions = {publishAudio:true, publishVideo:true, width: 400, height: 300};

    var sentJson1 = new Object();

    if (!wasUsed) {
        publisher = OT.initPublisher('local_container', pubOptions, function (error) {
            if (error) {
                alert("Проблемы с камерой. Собеседник вас не видит и врят ли захочет продолжить общение.");
                //sentJson1.video = "yes";
            } else {
                //sentJson1.video = "no";
            }
        });
        wasUsed = true;
    }


    if (!publisher.hasVideo)
    {
        sentJson1.video = "no";
    }
    else
    {
        sentJson1.video = "yes";
    }

    sentJson1.command = "0";

    sentJson1.ctrSum = $('#controlsum').text();
    sentJson1.ip = userIp;

    //проверка на возможность отправлять видео
    //sentJson1.video = "yes";

    sentJson1.name = $('#your_name').text();

    $("#stopButton").attr("disabled", false);

    ws.send(JSON.stringify(sentJson1));

    waitingWindowStart();
}

ws.onmessage = function (event) {

    var getJson = JSON.parse(event.data);
    var getCommand = getJson["answer"];


    log(getCommand);

    ///получить токен и начать трансляцию
    if (getCommand === "start")
    {
        componentPropetrOn();
        waitingWindowStop();

        var token = getJson["token"];

        $('#interlocutor_name').text(getJson["interlocutorName"]);

        session.connect(token, function (error) {
            if (error) {
                console.log(error.message);
            } else {
                session.publish(publisher);
            }
        });
    }

    //if (getCommand === "only_text")
    //{
    //    //if only text
    //    $('#interlocutor_name').text(getJson["interlocutorName"]);
    //
    //    alert("only text");
    //
    //    session.connect(token, function (error) {
    //        if (error) {
    //            console.log(error.message);
    //        } else {
    //            session.publish(publisher);
    //        }
    //    });
    //
    //    componentPropetrOn();
    //    waitingWindowStop();
    //}

    //// не верная контрольная сумма
    if (getCommand === "control") {
        alert("Возможно это ошибка. Но судя по всему, вы производите атаку на сервер подменой клиентского кода. Доступ закрыт. Сожалеем.");

        hangup();
        $("body").hide();
    }

    if (getCommand === "message") {
        var textMessages = getJson["message"];
        var interlocutorNameChat = $('#interlocutor_name').text();
        upDateChatBoxGet(interlocutorNameChat, textMessages);
    }

    if (getCommand === "new_window") {

        hangup();
        componentPropetrOff();
        $('#myModal2').modal('show');
    }

    //перейти в режим ожидания
    if (getCommand === "wait_window") {
        waitingWindowStart();
    }

    if (getJson["answer"] === "token") {
        //поставить ограничение на количество генерируемых ключей
        if (getJson["token"] == "") {
            $('#token_space').append("<p>" + "Ключей нет, обратитесь к администратору" + "</p>");
            $('#tokenButton').hide();
        }
        else {
            $('#token_space').append("<p>" + getJson["token"] + "</p>");
        }

        count_tocken = count_tocken + 1;

        if (count_tocken > 7) {
            $('#tokenButton').attr("disabled", true);
        }
    }

    if (getJson["answer"] === "changed") {
        $('#my_profile').modal('hide');
        $("#NameInput").val(getJson["NewName"]);
        alert("Name was changed");
    }

    if (getJson["answer"] === "changed_interlocutor_name") {
        $('#interlocutor_name').text(getJson["interlocutorName"]);
        log(getJson["interlocutorName"]);
    }
};

session.on({
    streamCreated: function (event) {
        var options = {width: 400, height: 300};
        subscriber = session.subscribe(event.stream, 'remote_container', options);
    }
});

function log() {
    $('#traceback').text(Array.prototype.join.call(arguments, ' '));
    console.log.apply(console, arguments);
}

function hangup() {

    session.unsubscribe(subscriber);
    $("#main_container").prepend("<div class='row' id='remote_container'></div>");

    componentPropetrOff();

    //call closeConnect server
    var sentJson = new Object();
    sentJson.command = "4";
    ws.send(JSON.stringify(sentJson));
}

function newInterlocutor() {
    initSocket();
}

function newInterlocutorButton() {
    initSocket();
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