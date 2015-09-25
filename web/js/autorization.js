function login(serverUrl, jsonData) {
    $.ajax({
        url: serverUrl + "/chat",
        type: 'POST',
        data: jsonData,

        dataType: 'json',
        async: true,
        //contentType: 'application/json',

        success: function (data) {
            if (data['answer'] == "ok") {
                window.location.href = serverUrl + "/chat";
            }
            else {
                alert("incorrect key");
            }
        },
        error: function (xhr, status, error) {
            alert("error");
        }
    });
}

function createJson() {
    //var randomKey = Math.floor(Math.random() * (1000000 - 100000 + 1)) + 100000;
    var json_create = new Object();
    json_create.name = $("#NameInput").val();
    json_create.keyGen = $("#KeyInput").val();
    json_create.command = "1";
    //json_create.randomKey = randomKey.toString();
    return JSON.stringify(json_create);
}

$(document).ready(
    function () {

        var serverHostName = window.location.hostname;

        var serverProtocolName = window.location.protocol;

        var portName = window.location.port;

        if (portName.length == 0){portName = "80"; }
        var serverPath = serverProtocolName + "//" + serverHostName + ":" + portName;

        if (serverHostName != "localhost")
        {
            serverPath += "/roulette"
        }

        $("#NameInput").val("Руслан");
        $("#KeyInput").val("9YQH-E8CI-N2XJ-2YV");

        $('#button_sent').click(function () {
            var jsonData = createJson();
            login(serverPath, jsonData);
        });
    }
);

