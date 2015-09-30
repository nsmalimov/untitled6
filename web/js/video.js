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

$("#startButton").prop('disabled', true);
$("#newButton").prop('disabled', false);
$("#stopButton").prop('disabled', false);

function initSocket()
{
    var sentJson = new Object();
    sentJson.command = "0";
    sentJson.name = $('#your_name').text().replace("Hello: ", "");

    $("#hangupButton").prop('disabled', true);
    $("#newButton").prop('disabled', true);
    $("#startButton").prop('disabled', true);

    ws.send(JSON.stringify(sentJson));
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
            //alert("111");
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
                } else {
                    $('#interlocutor_name').text("You connected with: " + getJson["interlocutorName"]);//interlocutorName
                    receiveOffer(signal);
                }
            } else if (signal.candidate) {

                pc.addIceCandidate(new IceCandidate(signal));
            }
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
    alert("fail");
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
    //trace('Ending call');
    pc.close();

    $("#stopButton").prop('disabled', true);
    $("#newButton").prop('disabled', false);
}

function newInterlocutor() {

}

jQuery.fn.attachStream = function(stream) {
    this.each(function() {
        this.src = URL.createObjectURL(stream);
        this.play();
    });
};



