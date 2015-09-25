var startButton = $('#startButton');
var callButton = $('#callButton');
var hangupButton = $('#hangupButton');

var startTime;
var localVideo = $('#localVideo');
//alert(localVideo);
var remoteVideo = $('#remoteVideo');
//alert(remoteVideo);

//var localVideo = null;
//var remoteVideo = null;

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

function gotStream(stream) {
    trace('Received local stream');
    // Call the polyfill wrapper to attach the media stream to this element.
    attachMediaStream(document.getElementById('localVideo'), stream);
    localStream = stream;
    $("#callButton").prop('disabled', false);
}

function start() {
    trace('Requesting local stream');
    $("#startButton").prop('disabled', true);
    navigator.mediaDevices.getUserMedia({
        audio: true,
        video: true
    })
        .then(gotStream)
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
            ws.send(JSON.stringify({
                type : 0,
                data : {
                    data : {
                        candidate : JSON.stringify(e.candidate)
                    }
                }
            }));
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
            ws.send(JSON.stringify({
                type : 0,
                data : {
                    candidate : JSON.stringify(e.candidate)
                }
            }));
        }
    };

    pc2.onaddstream = gotRemoteStream;

    pc1.addStream(localStream);

    pc1.createOffer(onCreateOfferSuccess, onCreateSessionDescriptionError,
        offerOptions);
}

function onCreateSessionDescriptionError(error) {
    trace('Failed to create session description: ' + error.toString());
}

function onCreateOfferSuccess(desc) {
    trace('Offer from pc1\n' + desc.sdp);
    trace('pc1 setLocalDescription start');
    pc1.setLocalDescription(desc, function() {
        onSetLocalSuccess(pc1);
    }, onSetSessionDescriptionError);
    trace('pc2 setRemoteDescription start');
    pc2.setRemoteDescription(desc, function() {
        onSetRemoteSuccess(pc2);
    }, onSetSessionDescriptionError);
    trace('pc2 createAnswer start');
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

function gotRemoteStream(e) {
    // Call the polyfill wrapper to attach the media stream to this element.
    attachMediaStream(document.getElementById('remoteVideo'), e.stream);
    trace('pc2 received remote stream');
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

