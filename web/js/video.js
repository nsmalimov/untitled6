var initiator;
var pc;

//var userName = $('#your_name').text().replace("Hello: ");
var serverHostName = window.location.hostname;
//
var portName = window.location.port;
if (portName.length == 0) {
    portName = "80";
}

var ws = null;

ws = new WebSocket("ws://" + serverHostName + ":" + portName + "/webrtc");

function initSocket()
{
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
    //alert(getData["nameInterlocutor"]);
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
// add handler
ws.onmessage = socketCallback;

function initialize() {
    var constraints = {
        audio: true,
        video: true
    };
    navigator.getUserMedia(constraints, success, fail);
}

function success(stream) {
    pc = new PeerConnection(null);

    if (stream) {
        pc.addStream(stream);
        $('#local').attachStream(stream);
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

        if (getCommand === "stop_connect")
        {
            newInterlocutor();
        }

        if (getCommand === "new_interloc")
        {
            //initialize();
            pc.close();

            //waitingWindowStart();

            success(stream);
            //pc = new PeerConnection(null);
            createOffer();
            initiator = false;
            //alert("111");
        }
    };

    if (initiator) {
        createOffer();
    } else {
        log('Waiting for guest connection...');
    }
    logStreaming(false);
}

function fail() {
    $('#traceback').text(Array.prototype.join.call(arguments, ' '));
    $('#traceback').attr('class', 'bg-danger');
    console.error.apply(console, arguments);
    //TODO
    //ошибка
    //alert("fail");
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

function logStreaming(streaming) {
    $('#streaming').text(streaming ? '[streaming]' : '[..]');
}

function hangup() {
    pc.close();

    ws.close();

    //$('#remote').src = URL.createObjectURL(null);

    //alert("111");

    $("#stopButton").prop('disabled', true);
    $("#newButton").prop('disabled', true);
    $("#startButton").prop('disabled', false);
}

function newInterlocutor() {
    var sentJson = new Object();
    sentJson.command = "2";
    sentJson.name = $('#your_name').text().replace("Hello: ", "");



    ws.send(JSON.stringify(sentJson));

    waitingWindowStart();

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


