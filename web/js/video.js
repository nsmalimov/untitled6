var initiator;
var pc;

var serverHostName = window.location.hostname;
//
var portName = window.location.port;
if (portName.length == 0) {
    portName = "80";
}

var myName = "";

var wasUsed = false;

var isVideoCall = 0;

var ws = null;

ws = new WebSocket("ws://" + serverHostName + ":" + portName + "/webrtc");

function initSocket()
{
    //alert(wasUsed);
    if (wasUsed){

        initialize();
        var sentJson = new Object();
        sentJson.command = "0";
        sentJson.name = $('#your_name').text().replace("Hello: ", "");

        $("#stopButton").prop('disabled', false);
        $("#newButton").prop('disabled', false);
        $("#startButton").prop('disabled', true);

        ws.send(JSON.stringify(sentJson));

        waitingWindowStart();
        return;
    }

    var sentJson = new Object();

    sentJson.command = "0";
    sentJson.name = $('#your_name').text().replace("Hello: ", "");

    $("#stopButton").prop('disabled', false);
    $("#newButton").prop('disabled', false);
    $("#startButton").prop('disabled', true);

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
        $('#interlocutor_name').text("You connected with: " + getData["nameInterlocutor"]);
        initiator = true;
        initialize();
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

        if (getCommand === "system"){
            var signal = JSON.parse(getJson["data"]);
            if (signal.sdp) {
                if (initiator) {
                    receiveAnswer(signal);
                    waitingWindowStop();
                } else {
                    $('#interlocutor_name').text("You connected with: " + getJson["interlocutorName"]);//interlocutorName

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
            var interlocutorNameChat = $('#interlocutor_name').text().replace("You connected with: ", "");
            upDateChatBoxGet(interlocutorNameChat, textMessages);
        }

        if (getCommand === "new_window")
        {
            pc.close();
            $('#remote_container').remove();

            $('#main_container').append("<div class='row' id='remote_container'><video id='remote' autoplay></video></div>");
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

    $('#main_container').append("<div class='row' id='remote_container'><video id='remote' autoplay></video></div>");

    $("#stopButton").prop('disabled', true);
    $("#newButton").prop('disabled', true);
    $("#startButton").prop('disabled', false);

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
    sentJson.name = $('#your_name').text().replace("Hello: ", "");

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
    var clientName = $('#your_name').text().replace("Hello: ", "");
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
