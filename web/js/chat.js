var startButton = $('#startButton');
var callButton = $('#callButton');
var hangupButton = $('#hangupButton');

var localVideo = $('#localVideo');
var remoteVideo = $('#remoteVideo');

var ws = new WebSocket("ws://" + "localhost:8080" + "/webrtc");

var localStream;
var pc1;
var pc2;
var offerOptions = {
    offerToReceiveAudio: true,
    offerToReceiveVideo: true
};

var configuration = {
    "iceServers" : [ {
        "url" : "stun:stun.l.google.com:19302"
    } ]
};

function getName(pc) {
    return (pc === pc1) ? 'pc1' : 'pc2';
}

function getOtherPc(pc) {
    return (pc === pc1) ? pc2 : pc1;
}

function start() {
    $("#startButton").prop('disabled', true);

    navigator.mediaDevices.getUserMedia({
        audio: true,
        video: true
    })
        .then(function(stream){
            attachMediaStream(document.getElementById('localVideo'), stream);
            localStream = stream;
            $("#callButton").prop('disabled', false);
        })
        .catch(function(e) {
            alert('getUserMedia() error: ' + e.name);
        });
}

function call(ws) {

    var localVideo = document.getElementById('localVideo');
    var remoteVideo = document.getElementById('remoteVideo');

    $("#callButton").prop('disabled', true);
    $("#hangupButton").prop('disabled', false);

    pc1 = new RTCPeerConnection(configuration);

    pc1.onicecandidate = function(e) {
        if (e.candidate) {
            getOtherPc(pc1).addIceCandidate(new RTCIceCandidate(e.candidate),
                function() {
                    onAddIceCandidateSuccess(pc1);
                },
                function(err) {
                    onAddIceCandidateError(pc1, err);
                }
            );
            var jsonToSent = new Object();
            jsonToSent.type = 0;
            jsonToSent.candidate = JSON.stringify(e.candidate);
            ws.send(JSON.stringify(jsonToSent));
            alert(e.candidate.candidate);
        }
    };

    pc2 = new RTCPeerConnection(configuration);

    pc2.onicecandidate = function(e) {
        if (e.candidate) {
            getOtherPc(pc2).addIceCandidate(new RTCIceCandidate(e.candidate),
                function() {
                    onAddIceCandidateSuccess(pc2);
                },
                function(err) {
                    onAddIceCandidateError(pc2, err);
                }
            );
            var jsonToSent = new Object();
            jsonToSent.type = 0;
            jsonToSent.candidate = JSON.stringify(e.candidate);
            ws.send(JSON.stringify(jsonToSent));
        }
    };


    pc2.onaddstream = function(e) {
        attachMediaStream(document.getElementById('remoteVideo'), e.stream);
    };

    pc1.addStream(localStream);

    pc1.createOffer(onCreateOfferSuccess, onCreateSessionDescriptionError,
        offerOptions);
}

function start_connect() {


}

function onCreateSessionDescriptionError(error) {
    trace('Failed to create session description: ' + error.toString());
}

function onCreateOfferSuccess(desc) {
    pc1.setLocalDescription(desc, function() {
        onSetLocalSuccess(pc1);
    }, onSetSessionDescriptionError);

    pc2.setRemoteDescription(desc, function() {
        onSetRemoteSuccess(pc2);
    }, onSetSessionDescriptionError);

    pc2.createAnswer(onCreateAnswerSuccess, onCreateSessionDescriptionError);
}

function onSetLocalSuccess(pc) {
    trace(getName(pc) + ' setLocalDescription complete');
}

function onSetRemoteSuccess(pc) {
    trace(getName(pc) + ' setRemoteDescription complete');
}

function onSetSessionDescriptionError(error) {
    trace('Failed to set session description: ' + error.toString());
}

function onCreateAnswerSuccess(desc) {
    trace('Answer from pc2:\n' + desc.sdp);
    trace('pc2 setLocalDescription start');
    pc2.setLocalDescription(desc, function() {
        onSetLocalSuccess(pc2);
    }, onSetSessionDescriptionError);
    trace('pc1 setRemoteDescription start');
    pc1.setRemoteDescription(desc, function() {
        onSetRemoteSuccess(pc1);
    }, onSetSessionDescriptionError);
}

function onAddIceCandidateSuccess(pc) {
    trace(getName(pc) + ' addIceCandidate success');
}

function onAddIceCandidateError(pc, error) {
    trace(getName(pc) + ' failed to add ICE Candidate: ' + error.toString());
}

function hangup() {
    trace('Ending call');
    pc1.close();
    pc2.close();
    pc1 = null;
    pc2 = null;

    $("#hangupButton").prop('disabled', true);
    $("#callButton").prop('disabled', false);
}

$(document).ready(
    function () {
        $("#startButton").prop('disabled', false);
        $("#callButton").prop('disabled', true);
        $("#hangupButton").prop('disabled', true);


        start();
        ws.onopen = function (event) {};

        ws.onmessage = function (event) {
            var msg = JSON.parse(message.data);
        };

        ws.onclose = function (event) {};



        $('#startButton').click(function () {
            start();
        });

        $('#callButton').click(function () {
            call(ws);
        });

        $('#hangupButton').click(function () {
            hangup();
        });

    }
);

