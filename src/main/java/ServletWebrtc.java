import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;


import java.util.*;
import java.util.concurrent.ConcurrentMap;
import static java.util.Collections.emptySet;
import java.io.StringReader;

import BuildClass.SessionUser;

@ServerEndpoint(value = "/webrtc")
public class ServletWebrtc extends HttpServlet {

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {

    }

    @OnClose
    public void onClose(Session session) {
        BuildClass.SessionUser.closeConnect(session);
        System.out.println("close connect");
    }

    @OnMessage
    public void onMessage(String message, Session client)
            throws IOException, EncodeException {



        JSONObject jsonObject = new JSONObject(message);

        int command = Integer.parseInt(jsonObject.getString("command"));
        //0 - start

        switch (command)
        {
            case 0:
                //start chat
                SessionUser.addFreeUser(client, jsonObject.getString("name"));
                System.out.println("user connect");
                return;
            //break;

            case 1:
                //recived ICE candidate
                String data = jsonObject.getString("sentdata");


                //System.out.println(data);
                Session locutorSes = SessionUser.getInterlocutor(client);

                String interlocutorName = SessionUser.userSessionId.get(locutorSes.getId());


                JSONObject jsonToReturn1 = new JSONObject();
                jsonToReturn1.put("answer", "system");
                jsonToReturn1.put("data", data);
                jsonToReturn1.put("interlocutorName", interlocutorName);

                locutorSes.getBasicRemote().sendText(jsonToReturn1.toString());


                System.out.println("ICE candidate get and sent");
                return;

            case 2:
                //new interlocutor
                SessionUser.newInterlocutor(client);

                JSONObject jsonToReturn2 = new JSONObject();
                jsonToReturn2.put("answer", "stopped");

                client.getBasicRemote().sendText(jsonToReturn2.toString());

                System.out.println("new interlocutor");
                return;

            default:
                System.out.println("default");
                break;
        }
    }
}