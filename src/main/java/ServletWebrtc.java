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
    public void onClose(Session session) throws IOException, EncodeException{
        //Session interlocutor = BuildClass.SessionUser.getInterlocutor(session);


        //JSONObject jsonToReturn1 = new JSONObject();
        //jsonToReturn1.put("answer", "stop_connect");

        //interlocutor.getBasicRemote().sendText(jsonToReturn1.toString());

        BuildClass.SessionUser.closeConnect(session);

        System.out.println("close connect");

        BuildClass.SessionUser.printParams();
        //BuildClass.SessionUser.printParams();



        //BuildClass.SessionUser.printParams();
    }

    @OnMessage
    public void onMessage(String message, Session client)
            throws IOException, EncodeException {

        JSONObject jsonObject = new JSONObject(message);

        int command = Integer.parseInt(jsonObject.getString("command"));

        //System.out.println(jsonObject.getString("name"));

        switch (command)
        {
            case 0:
                //start chat
                SessionUser.addFreeUser(client, jsonObject.getString("name"));

                System.out.println("connect");
                BuildClass.SessionUser.printParams();
                break;

            case 1:
                //recived ICE candidate
                String data = jsonObject.getString("sentdata");

                Session locutorSes1 = SessionUser.getInterlocutorSession(client);

                String interlocutorName1 = SessionUser.userSessionId.get(locutorSes1.getId());

                JSONObject jsonToReturn1 = new JSONObject();
                jsonToReturn1.put("answer", "system");
                jsonToReturn1.put("data", data);
                jsonToReturn1.put("interlocutorName", interlocutorName1);

                locutorSes1.getBasicRemote().sendText(jsonToReturn1.toString());

                break;

            case 2: //new interlocutor

                System.out.println(2);
                int answer = SessionUser.connectTwo(client);

                //System.out.println("answer " + answer);

                if (answer == 0)
                {
                    //в режим ожидания
                    JSONObject jsonToReturn2 = new JSONObject();
                    jsonToReturn2.put("answer", "wait_window");
                    client.getBasicRemote().sendText(jsonToReturn2.toString());
                    System.out.println("wait_command");
                }
                else
                {
                    //начать чат
                    JSONObject jsonToReturn2 = new JSONObject();
                    jsonToReturn2.put("answer", "new_interlocutor");

                    String interlocutorName = SessionUser.getInterlocutorName(client);

                    jsonToReturn2.put("interlocutorName", interlocutorName);
                    client.getBasicRemote().sendText(jsonToReturn2.toString());
                }

                BuildClass.SessionUser.printParams();

                break;

            case 3: //get and set messages

                String messages = jsonObject.getString("message");

                JSONObject jsonToReturn3 = new JSONObject();
                jsonToReturn3.put("answer", "message");
                jsonToReturn3.put("message", messages);

                Session locutorSes2 = SessionUser.getInterlocutorSession(client);

                locutorSes2.getBasicRemote().sendText(jsonToReturn3.toString());

                break;

            case 4:
                System.out.println(4);
                //полностью удалить пользователя

                BuildClass.SessionUser.closeConnect(client);
                BuildClass.SessionUser.printParams();

                break;
            default:
                System.out.println("default");
                break;
        }
    }
}