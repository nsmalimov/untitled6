var initiator;
var pc;

var serverHostName = window.location.hostname;
//
var portName = window.location.port;
if (portName.length == 0) {
    portName = "80";
}

var wasUsed = false;

var isVideoCall = 0;

var ws = null;

ws = new WebSocket("ws://" + serverHostName + ":" + portName + "/webrtc");

var count_tocken = 0;

function initSocket()
{
    //alert(wasUsed);
    if (wasUsed){

        initialize();
        var sentJson = new Object();
        sentJson.command = "0";
        sentJson.name = $('#your_name').text();

        //componentPropetrOn();
        $("#stopButton").prop('disabled', false);

        ws.send(JSON.stringify(sentJson));

        waitingWindowStart();
        return;
    }

    var sentJson = new Object();

    sentJson.command = "0";
    sentJson.name = $('#your_name').text();

    //componentPropetrOn();
    $("#stopButton").prop('disabled', false);

    ws.send(JSON.stringify(sentJson));
    waitingWindowStart();
}


var PeerConnection = window.mozRTCPeerConnection || window.webkitRTCPeerConnection;
var IceCandidate = window.mozRTCIceCandidate || window.RTCIceCandidate;
var SessionDescription = window.mozRTCSessionDescription || window.RTCSessionDescription;
navigator.getUserMedia = navigator.getUserMedia || navigator.mozGetUserMedia || navigator.webkitGetUserMedia;

function socketCallback(event) {

    var getData = JSON.parse(event.data);
    if (getData["answer"] == "owner") {
        initiator = false;
        initialize();
    }
    if (getData["answer"] == "guest") {
        $('#interlocutor_name').text(getData["nameInterlocutor"]);
        initiator = true;
        initialize();
    }

    if (getData["answer"] === "token")
    {
        //поставить ограничение на количество генерируемых ключей
        $('#token_space').append("<p>" + getData["token"] + "</p>");

        if (getData["token"] == "")
        {
            $('#token_space').append("<p>" + "Ключей нет, обратитесь к администратору" + "</p>");
            $('#tokenButton').hide();
        }

        count_tocken  = count_tocken + 1;

        if (count_tocken == 7)
        {
            $('#tokenButton').attr("disabled", true);
        }
    }

    if (getData["answer"] === "changed")
    {
        $('#my_profile').modal('hide');
        alert("Name was changed");
    }
}

ws.onmessage = socketCallback;

function initialize() {
    var constraints = {
        audio: false,
        video: true
    };
    navigator.getUserMedia(constraints, success, fail);
}

function success(stream) {
    pc = new PeerConnection(null);

    if (stream) {
        pc.addStream(stream);
        if (isVideoCall != 1) {
            $('#local').attachStream(stream);
            isVideoCall = 1;
        }
    }

    pc.onaddstream = function(event) {
        $('#remote').attachStream(event.stream);
        logStreaming(true);
    };

    pc.onicecandidate = function(event) {
        if (event.candidate) {

            var sentJson = new Object();
            sentJson.sentdata = JSON.stringify(event.candidate);
            sentJson.command = "1";
            ws.send(JSON.stringify(sentJson));
        }
    };

    ws.onmessage = function (event) {
        var getJson = JSON.parse(event.data);
        var getCommand = getJson["answer"];

        //console.log("значение", getCommand);

        //console.log("some info");
        //alert(getCommand);
        //alert(getCommand);


        if (getCommand === "system"){

            componentPropetrOn();

            var signal = JSON.parse(getJson["data"]);
            if (signal.sdp) {
                if (initiator) {
                    receiveAnswer(signal);
                    waitingWindowStop();
                } else {
                    $('#interlocutor_name').text(getJson["interlocutorName"]);

                    receiveOffer(signal);
                    waitingWindowStop();
                }
            } else if (signal.candidate) {

                pc.addIceCandidate(new IceCandidate(signal));
            }
        }

        if (getCommand === "message")
        {
            var textMessages = getJson["message"];
            var interlocutorNameChat = $('#interlocutor_name').text();
            upDateChatBoxGet(interlocutorNameChat, textMessages);
        }

        if (getCommand === "new_window")
        {
            pc.close();
            $('#remote_container').remove();

            $('#main_container').prepend("<div class='row' id='remote_container'><video id='remote' autoplay></video></div>");
            $('#myModal2').modal('show');

        }

        //перейти в режим ожидания
        if (getCommand === "wait_window")
        {
            success(stream);
            initiator = false;
        }

        //найден собеседник (ответить)
        if (getCommand === "new_interlocutor")
        {
            pc.close();
            success(stream);
            createOffer();
        }
    };

    if (initiator) {
        createOffer();
    } else {
        log('Waiting for guest connection...');
    }
}

function fail() {
    $('#traceback').text(Array.prototype.join.call(arguments, ' '));
    $('#traceback').attr('class', 'bg-danger');
    console.error.apply(console, arguments);
}

function createOffer() {
    log('Creating offer. Please wait.');
    pc.createOffer(function(offer) {
        log('Success offer');
        pc.setLocalDescription(offer, function() {
            log('Sending to remote...');
            var sentJson = new Object();
            sentJson.sentdata = JSON.stringify(offer);
            sentJson.command = "1";
            ws.send(JSON.stringify(sentJson));
        }, fail);
    }, fail);
}

function receiveOffer(offer) {
    log('Received offer.');
    pc.setRemoteDescription(new SessionDescription(offer), function() {
        log('Creating response');
        pc.createAnswer(function(answer) {
            log('Created response');
            pc.setLocalDescription(answer, function() {
                log('Sent response');
                var sentJson = new Object();
                sentJson.sentdata = JSON.stringify(answer);
                sentJson.command = "1";
                ws.send(JSON.stringify(sentJson));
            }, fail);
        }, fail);
    }, fail);
}

function receiveAnswer(answer) {
    log('received answer');
    pc.setRemoteDescription(new SessionDescription(answer));
}

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

    initiator = false;

    ws.send(JSON.stringify(sentJson));

}

jQuery.fn.attachStream = function(stream) {
    this.each(function() {
        this.src = URL.createObjectURL(stream);
        this.play();
    });
};

function upDateChatBoxSent(name, message) {
    $(".chat").append('<li class="right clearfix"><span class="chat-img pull-left"></span><div class="chat-body clearfix"><div class="header"><strong class="primary-font">' + name + '</strong></div><p>' + message + '</p></div></li>');
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
    $(".chat").append('<li class="left clearfix"><span class="chat-img pull-left"></span><div class="chat-body clearfix"><div class="header"><strong class="primary-font">' + name + '</strong></div><p>' + message + '</p></div></li>');
    var newmsg_top = parseInt($('.panel-body')[0].scrollHeight);
    $('.panel-body').scrollTop(newmsg_top - 100);
}

function generateToken() {
    var json_create = new Object();
    json_create.command = "5";
    var json = JSON.stringify(json_create);
    ws.send(json);
}

function changeNickName()
{
    var json_create = new Object();
    json_create.command = "6";
    json_create.new_name = $('#your_name').text();
    json_create.ip = userIp;

    var json = JSON.stringify(json_create);
    ws.send(json);
}