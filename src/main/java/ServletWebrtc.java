import Databases.SQLiteClass;

import javax.servlet.http.HttpServlet;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import com.opentok.OpenTok;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.json.JSONObject;

import javax.json.Json;
import javax.json.JsonObject;

import java.io.IOException;
import java.sql.SQLException;
import javax.naming.NamingException;


import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.emptySet;

import java.io.StringReader;

import BuildClass.SessionUser;

@ServerEndpoint(value = "/webrtc")
public class ServletWebrtc {

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {

    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException, SQLException,
            NamingException, ClassNotFoundException {

        String sessionName = SessionUser.userSessions.get(session.getId());

        OpenTook.Conn();

        OpenTook.updateSession(sessionName, 0);
        OpenTook.CloseDB();

        BuildClass.SessionUser.closeConnect(session);

        //TODO
        //удаление из базы используемый номер комнаты
    }

    @OnMessage
    public void onMessage(String message, Session client)
            throws IOException, EncodeException, IOException, EncodeException, SQLException,
            NamingException, ClassNotFoundException {

        JSONObject jsonObject = new JSONObject(message);

        System.out.println(message);

        int command = Integer.parseInt(jsonObject.getString("command"));

        switch (command) {
            case 0:
                //добавить и соеденить
                //TODO проверка на контрольную сумму
                String controlSum = jsonObject.getString("ctrSum");
                String ipNew = jsonObject.getString("ip");

                String isVideo = jsonObject.getString("video");

                boolean ctrSumAnswer = ControlSum.checkControlSum(controlSum, ipNew);

                if (ctrSumAnswer) {
                    SessionUser.addFreeUser(client, jsonObject.getString("name"), isVideo);
                } else {
                    JSONObject jsonToReturn0 = new JSONObject();
                    jsonToReturn0.put("answer", "control");
                    client.getBasicRemote().sendText(jsonToReturn0.toString());
                }

                if (SessionUser.getInterlocutorName(client).equals("")) {
                    break;
                }

                Session locutorSes1 = SessionUser.getInterlocutorSession(client);
                String interlocutorName1 = SessionUser.userSessionId.get(locutorSes1.getId());

                try {
                    String tokens = OpenTook.generateToken();

                    String[] tok = tokens.split(",");

                    String token1 = tok[0];
                    String token2 = tok[1];

                    String sessionName = tok[2];

                    System.out.println(sessionName);

                    System.out.println(token1);
                    System.out.println(token2);

                    JSONObject jsonToReturn1 = new JSONObject();
                    jsonToReturn1.put("answer", "start");
                    jsonToReturn1.put("token", token1);
                    jsonToReturn1.put("session_name", sessionName);
                    jsonToReturn1.put("interlocutorName", interlocutorName1);

                    client.getBasicRemote().sendText(jsonToReturn1.toString());

                    JSONObject jsonToReturn2 = new JSONObject();
                    jsonToReturn2.put("answer", "start");
                    jsonToReturn2.put("token", token2);
                    jsonToReturn2.put("session_name", sessionName);
                    jsonToReturn2.put("interlocutorName", interlocutorName1);

                    if (SessionUser.hasVideoId.containsKey(client.getId()))
                    {
                        JSONObject jsonToReturn12 = new JSONObject();
                        jsonToReturn12.put("answer", "only_text");

                        locutorSes1.getBasicRemote().sendText(jsonToReturn12.toString());
                        SessionUser.hasVideoId.remove(client.getId());
                    }

                    if (SessionUser.hasVideoId.containsKey(locutorSes1.getId()))
                    {
                        JSONObject jsonToReturn12 = new JSONObject();
                        jsonToReturn12.put("answer", "only_text");

                        client.getBasicRemote().sendText(jsonToReturn12.toString());
                        SessionUser.hasVideoId.remove(locutorSes1.getId());
                    }

                    locutorSes1.getBasicRemote().sendText(jsonToReturn2.toString());

                    SessionUser.userSessions.put(client.getId(), sessionName);
                    SessionUser.userSessions.put(locutorSes1.getId(), sessionName);

                } catch (Exception e) {
                    System.out.println(e);
                }


                break;

            case 2: //new interlocutor

                //System.out.println(2);
                int answer = SessionUser.connectTwo(client);

                //System.out.println("answer " + answer);

                if (answer == 0) {
                    //в режим ожидания
                    JSONObject jsonToReturn2 = new JSONObject();
                    jsonToReturn2.put("answer", "wait_window");
                    client.getBasicRemote().sendText(jsonToReturn2.toString());
                    //System.out.println("wait_command");
                } else {
                    //начать чат
                    JSONObject jsonToReturn2 = new JSONObject();
                    jsonToReturn2.put("answer", "new_interlocutor");

                    String interlocutorName = SessionUser.getInterlocutorName(client);

                    jsonToReturn2.put("interlocutorName", interlocutorName);
                    client.getBasicRemote().sendText(jsonToReturn2.toString());
                }

                //BuildClass.SessionUser.printParams();

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
                //полностью удалить пользователя

                String sessionToRemove = jsonObject.getString("session");
                OpenTook.Conn();
                OpenTook.updateSession(sessionToRemove, 0);
                OpenTook.CloseDB();

                System.out.println("update " + sessionToRemove);

                BuildClass.SessionUser.closeConnect(client);

                break;

            case 5:
                //сгенерировать ключ и пометить как переданный

                SQLiteClass.Conn();
                String genKeygen = "null";

                try {
                    genKeygen = SQLiteClass.generateKeygen();
                } catch (Exception e) {
                    //System.out.println(e);
                } finally {
                    SQLiteClass.CloseDB();
                }


                JSONObject jsonToReturn5 = new JSONObject();
                jsonToReturn5.put("answer", "token");
                jsonToReturn5.put("token", genKeygen);

                client.getBasicRemote().sendText(jsonToReturn5.toString());

                break;

            //изменить имя на лету
            case 6: //change name
                String ip = jsonObject.getString("ip");
                String newName = jsonObject.getString("new_name");

                String lastName = jsonObject.getString("last_name");

                //System.out.println(newName);

                SQLiteClass.Conn();

                try {
                    SQLiteClass.updateName(lastName, newName, ip);
                } catch (Exception e) {
                    //System.out.println(e);
                } finally {
                    SQLiteClass.CloseDB();
                }

                JSONObject jsonToReturn6 = new JSONObject();
                jsonToReturn6.put("answer", "changed");
                jsonToReturn6.put("NewName", newName);

                SessionUser.userSessionId.put(client.getId(), newName);

                client.getBasicRemote().sendText(jsonToReturn6.toString());

                Session locutorSes3 = SessionUser.getInterlocutorSession(client);

                if (locutorSes3 != null) {

                    JSONObject jsonToReturn7 = new JSONObject();
                    jsonToReturn7.put("answer", "changed_interlocutor_name");
                    jsonToReturn7.put("interlocutorName", newName);

                    locutorSes3.getBasicRemote().sendText(jsonToReturn7.toString());
                }

                //System.out.println(jsonToReturn6.toString());

                break;

            case 7: //новый пользователь
                JSONObject jsonToReturn8 = new JSONObject();
                jsonToReturn8.put("answer", "new_window");

                Session locutorSes8 = SessionUser.getInterlocutorSession(client);

                locutorSes8.getBasicRemote().sendText(jsonToReturn8.toString());

                SessionUser.simpleClose(client);

                JSONObject jsonToReturn9 = new JSONObject();
                jsonToReturn9.put("answer", "wait_window");

                client.getBasicRemote().sendText(jsonToReturn9.toString());

                break;

            default:
                //System.out.println("default");
                break;
        }
    }
}
