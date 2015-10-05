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

        //BuildClass.SessionUser.printParams();
    }

    @OnMessage
    public void onMessage(String message, Session client)
            throws IOException, EncodeException {

        JSONObject jsonObject = new JSONObject(message);

        int command = Integer.parseInt(jsonObject.getString("command"));

        System.out.println(command);
        switch (command)
        {
            case 0:
                //start chat
                SessionUser.addFreeUser(client, jsonObject.getString("name"));

                System.out.println("connect");
                BuildClass.SessionUser.printParams();
                //System.out.println("user connect");
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

                //System.out.println("ICE candidate get and sent");
                break;

            case 2:
                //new interlocutor

                //присылает запрос на нового собеседника
                //соединение с тем кто ожидает

                //соеденить с пользователем
                int answer = SessionUser.connectTwo(client);

                if (answer == 0)
                {
                    //в режим ожидания
                    JSONObject jsonToReturn2 = new JSONObject();
                    jsonToReturn2.put("answer", "wait_window");
                    client.getBasicRemote().sendText(jsonToReturn2.toString());
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


//                JSONObject jsonToReturn2 = new JSONObject();
//                jsonToReturn2.put("answer", "stop_connect");
//
//                String name = jsonObject.getString("name");
//
//                Session locutorSes4 = SessionUser.getInterlocutor(client);
//
//                //бывшему собеседнику отправить остановить соединение
//
//
//
//                //соеденить с новым
//                SessionUser.newInterlocutor(client, name);
//
//                Session locutorSes3 = SessionUser.getInterlocutor(client);
//
//                //сказать новому что он инициатор и createoffer
//
//                JSONObject jsonToReturn5 = new JSONObject();
//                jsonToReturn5.put("answer", "new_interloc");
//
//                //String name = jsonObject.getString("name");
//
//                client.getBasicRemote().sendText(jsonToReturn5.toString());
//
//                //System.out.println(locutorSes3.getId());
//
//
//                //client.getBasicRemote().sendText(jsonToReturn2.toString());
//
//                //System.out.println("new interlocutor");
//
//                locutorSes4.getBasicRemote().sendText(jsonToReturn2.toString());
                break;

            case 3:
                //get and set messages

                String messages = jsonObject.getString("message");

                //System.out.println(messages);

                JSONObject jsonToReturn3 = new JSONObject();
                jsonToReturn3.put("answer", "message");
                jsonToReturn3.put("message", messages);

                Session locutorSes2 = SessionUser.getInterlocutorSession(client);

                locutorSes2.getBasicRemote().sendText(jsonToReturn3.toString());

                break;

            case 4:
                BuildClass.SessionUser.closeConnect(client);
                break;
            default:
                System.out.println("default");
                break;
        }
    }
}