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
    public void onOpen(Session session) throws IOException, EncodeException {

        sessions.add(session);

        if (freeUsersArray.size() == 0)
        {
            session.getBasicRemote().sendText("owner");
            freeUsersArray.add(session.getId());
        }
        else
        {
            session.getBasicRemote().sendText("guest");

            String waitingUsersId = freeUsersArray.get(0);
            String[] someArray = {session.getId(), waitingUsersId};
            connectedUsers.add(someArray.clone());

            freeUsersArray.remove(freeUsersArray.get(0));

        }

    }

    @OnClose
    public void onClose(Session session) {

        for (int i = 0; i < connectedUsers.size(); i++) {
            String fistUser = connectedUsers.get(i)[0];
            String secondUser = connectedUsers.get(i)[1];

            if (fistUser.equals(session.getId()) || secondUser.equals(session.getId())) {
                connectedUsers.remove(i);
                break;
            }
        }

        sessions.remove(session);

    }

    @OnMessage
    public void onMessage(String message, Session client)
            throws IOException, EncodeException {

        for (Session session : sessions) {
            session.getBasicRemote().sendText(message);
        }
    }
}