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

@ServerEndpoint(value = "/webrtc")
public class ServletWebrtc extends HttpServlet {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
    private static final Map userSessionId = new HashMap<String, String>();

    private static final ArrayList<String> freeUsersArray = new ArrayList<String>();

    private static final ArrayList<String[]> connectedUsers = new ArrayList<String[]>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session client)
            throws IOException, EncodeException {

        System.out.println(message);

        //client.getBasicRemote().sendObject(message);
        for (Session session : sessions) {
            //JSONObject jo = new JSONObject();
            //jo.put("answer", "candidate");
            //jo.put("key", userSessionId.get(client.getId()));

            session.getBasicRemote().sendObject(message);
        }

        System.out.println(message);

        //JSONObject jsonObject = new JSONObject(message);

        //int type = jsonObject.getInt("type");

//        switch(type) {
//            //получение ICE key
//            case 0: //received_candidate
//                String keyWebrtc = jsonObject.getString("candidate");
//
//                userSessionId.put(client.getId(), keyWebrtc);
//
//                System.out.println();
//
//                for (Session session : sessions) {
//                    JSONObject jo = new JSONObject();
//                    jo.put("answer", "candidate");
//                    jo.put("key", userSessionId.get(client.getId()));
//
//                    session.getBasicRemote().sendText(jo.toString());
//                    //System.out.println(jo.toString());
//                    //break;
//                }
//                break;
//
//            //sent another ICE key
//            case 1: //received_offer
//
//                System.out.println(1);
//
//                for (Session session : sessions) {
//                    JSONObject jo = new JSONObject();
//                    jo.put("answer", "received_offer");
//                    jo.put("description", jsonObject.getString("description"));
//
//                    session.getBasicRemote().sendText(jo.toString());
//                    //System.out.println(jo.toString());
//
//                    //break;
//                }
//                break;
//
//
//            default:
//                System.out.println("default case");
//
//        }
    }
}
