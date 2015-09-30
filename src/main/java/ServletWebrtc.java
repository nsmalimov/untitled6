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

        for (int i = 0; i < BuildClass.SessionUser.connectedUsers.size(); i++) {
            String fistUser = BuildClass.SessionUser.connectedUsers.get(i)[0];
            String secondUser = BuildClass.SessionUser.connectedUsers.get(i)[1];

            if (fistUser.equals(session.getId()) || secondUser.equals(session.getId())) {
                BuildClass.SessionUser.connectedUsers.remove(i);
                break;
            }
        }

        BuildClass.SessionUser.sessions.remove(session);

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

                String interlocutorName = SessionUser.userSessionId.get(locutorSes.getId()).toString();
                System.out.println(interlocutorName);

                JSONObject jsonToReturn = new JSONObject();
                jsonToReturn.put("answer", "system");
                jsonToReturn.put("data", data);
                jsonToReturn.put("interlocutorName", interlocutorName);

                locutorSes.getBasicRemote().sendText(jsonToReturn.toString());


                System.out.println("ICE candidate get and sent");
                return;
            default:
                System.out.println("default");
                break;
        }

        for (Session session : BuildClass.SessionUser.sessions) {
            session.getBasicRemote().sendText(message);
        }
    }
}