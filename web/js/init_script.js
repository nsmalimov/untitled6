var userName = "";

var serverHostName = window.location.hostname;

var serverProtocolName = window.location.protocol;

var portName = window.location.port;

if (portName.length == 0) {
    portName = "80";
}
var serverPath = serverProtocolName + "//" + serverHostName + ":" + portName;

function serverConnectFunc(serverUrl, jsonData) {
    $.ajax({
        url: serverUrl + "/autorization",
        type: 'POST',
        data: jsonData,

        dataType: 'json',
        async: true,
        //contentType: 'application/json',

        success: function (event) {
            //парсинг ответов сервера


            switch (event["answer"])
            {
                case "ok":
                   $('#myModal1').modal('hide');

                   userName = event["name"];


                    if (userName === "no")
                    {
                        $('#your_name').text($("#NameInput").val());

                        var nickname = $('#your_name').text();
                        $("#NewKeyInput").val(nickname);

                        userName = $("#NameInput").val();
                    }
                    else
                    {
                        $('#your_name').text(userName);

                        var nickname = $('#your_name').text();
                        $("#NewKeyInput").val(nickname);
                    }
                   break;

                case "cookies":
                    $('#myModal1').modal('show');

                    //rewrite
                    $("#NameInput").val("Руслан");
                    $("#KeyInput").val("9YQH-E8CI-N2XJ-2YV");

                    break;

                case "wrong":
                    alert("Key not correct or already used");
                    break;

                case "ip":
                    alert("Bad ip address");
                    break;

            }
        },
        error: function (xhr, status, error) {
            alert(error);
        }
    });
}

function createJsonRegistration() {
    var json_create = new Object();
    json_create.name = $("#NameInput").val();

    json_create.keyGen = $("#KeyInput").val();
    json_create.command = "1";

    return JSON.stringify(json_create);
}

function getURL(){
    return $.ajax({
        type: "GET",
        url: "http://jsonip.com?callback=?",
        cache: false,
        async: false
    }).responseText;
}

function getCoordinates(userIp){
    return $.ajax({
        type: "GET",
        url: "http://www.telize.com/geoip/" + userIp,
        cache: false,
        async: false
    }).responseText;
}

function createJsonAutorization() {

    var json_create = new Object();

    json_create.command = "0";

    var latitude_var = null;
    var longitude_var = null;
    var userIp = getURL();
    userIp = userIp.slice(2, userIp.length-1);
    userIp = JSON.parse(userIp)["ip"];

    var coordString = getCoordinates(userIp);

    var coordJson = JSON.parse(coordString);

    if (coordJson["isp"] === "Saint-Petersburg State University")
    {
        json_create.latitude_var = "0";
        json_create.longitude_var = "0";
    }
    else
    {
        json_create.latitude_var = coordJson["latitude"];
        json_create.longitude_var = coordJson["longitude"];
    }

    return JSON.stringify(json_create);
}

function autorizeFunc() {
    var jsonData = createJsonAutorization();
    serverConnectFunc(serverPath, jsonData);
}

function sentRegistrationData(){
    var jsonData = createJsonRegistration();
    serverConnectFunc(serverPath, jsonData);
}

//TODO
//сделать прозрачным экран ожидания
function waitingWindowStart()
{
    var loader = $("#element").introLoader({
        animation: {
            name: 'simpleLoader',
            options: {
                effect:'slideUp',
                ease: "easeInOutCirc",
                style: 'dark',
                stop: false
                //fixed: false
            }
        },

        spinJs: {
            lines: 13, // The number of lines to draw
            length: 20, // The length of each line
            width: 10, // The line thickness
            radius: 30, // The radius of the inner circle
            corners: 1, // Corner roundness (0..1)
            color: '#FFFFFF' // #rgb or #rrggbb or array of colors
        }
    });

    componentPropetrOff();

    $("#startButton").prop('disabled', true);
}

function waitingWindowStop()
{
    var loader = $('#element').data('introLoader');
    loader.stop();
}

function inviteFreind()
{
    $('#invite_friend').modal('show');
}


function myProfile()
{
    $('#my_profile').modal('show');
}

function componentPropetrOn()
{
    $("#stopButton").prop('disabled', false);
    $("#newButton").prop('disabled', false);
    $("#startButton").prop('disabled', true);

    $("#btn-chat").prop('disabled', false);

    $('#text_input').prop('disabled', false);
}

function componentPropetrOff()
{
    $("#stopButton").prop('disabled', true);
    $("#newButton").prop('disabled', true);
    $("#startButton").prop('disabled', false);

    $("#btn-chat").prop('disabled', true);

    $('#text_input').prop('disabled', true);

    $('#text_input').val("");

    $('#myModal2').modal('hide');
}

window.onload = function() {
    //Документ и все ресурсы загружены
    autorizeFunc();
    componentPropetrOff();

    $('#text_input').keydown(function (e){
        if(e.keyCode == 13){
            sentMessages();
        }
    })

};