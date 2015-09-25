import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chatwork")
public class ChatServlet extends HttpServlet {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    private static final ArrayList<String> freeUsersArray = new ArrayList<String>();

    private static final ArrayList<String[]> connectedUsers = new ArrayList<String[]>();

    private static final Map userSessionId = new HashMap<String, String>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        //freeUsersArray.add(session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);

        freeUsersArray.remove(session.getId());

        for (int i = 0; i < connectedUsers.size(); i++) {
            String fistUser = connectedUsers.get(i)[0];
            String secondUser = connectedUsers.get(i)[1];

            if (fistUser.equals(session.getId()) || secondUser.equals(session.getId())) {
                connectedUsers.remove(i);
                break;
            }
        }
    }

    public static String getInterlocutor(Session client) {
        String needSent = "";

        for (int i = 0; i < connectedUsers.size(); i++) {

            String firstUser = connectedUsers.get(i)[0];
            String secondUser = connectedUsers.get(i)[1];

            if (firstUser.equals(client.getId())) {
                needSent = secondUser;
                break;
            }

            if (secondUser.equals(client.getId())) {
                needSent = firstUser;
                break;
            }
        }
        return needSent;
    }

    @OnMessage
    public void onMessage(String message, Session client)
            throws IOException, EncodeException {

        JSONObject jsonObject = new JSONObject(message);
        String command = jsonObject.getString("command");

        String userName = jsonObject.getString("name");
        userSessionId.put(client.getId(), userName);

        if (command.equals("connect")) {

            freeUsersArray.add(client.getId());

            //если свободных юзеров нет
            if (freeUsersArray.size() == 1)
            {
                client.getBasicRemote().sendText("{\"answer\":" + "\"" + "not_free_users" + "\"" + "}");
                return;
            }

            //добавили в конец списка
            String waitingUsersId = freeUsersArray.get(0);

            String[] someArray = {client.getId(), waitingUsersId};

            client.getBasicRemote().sendText("{\"answer\":" + "\"" + "connected" + "\"" + ","
                    + "\"interlocutor\":" + "\"" + userSessionId.get(client.getId()) + "\"" + "}");

            for (Session session : sessions) {
                if (session.getId().equals(waitingUsersId)) {
                    session.getBasicRemote().sendText("{\"answer\":" + "\"" + "connected" + "\"" + ","
                            + "\"interlocutor\":" + "\"" + userSessionId.get(waitingUsersId) + "\"" + "}");
                    break;
                }
            }

            freeUsersArray.remove(freeUsersArray.get(0));

            freeUsersArray.remove(client.getId());

            connectedUsers.add(someArray.clone());

        }

        if (command.equals("disconnect")) {
            String needSent = getInterlocutor(client);
            freeUsersArray.add(needSent);

            for (int i = 0; i < connectedUsers.size(); i++) {
                String fistUser = connectedUsers.get(i)[0];
                String secondUser = connectedUsers.get(i)[1];

                if (fistUser.equals(client.getId()) || secondUser.equals(client.getId())) {
                    connectedUsers.remove(i);
                    break;
                }
            }

            for (Session session : sessions) {
                if (session.getId().equals(needSent)) {
                    session.getBasicRemote().sendText("{\"answer\":" + "\"" + "disconnect" + "\"" + "}");
                    break;
                }
            }

            System.out.println("disconnect");
        }

        if (command.equals("find_interlocutor")) {
            String needSent = getInterlocutor(client);
            for (Session session : sessions) {
                if (session.getId().equals(needSent)) {
                    session.getBasicRemote().sendText("{\"answer\":" + "\"" + "disconnect" + "\"" + "}");
                    break;
                }
            }
            System.out.println("find_interlocutor");
        }

        if (command.equals("sent_message")) {
            String needSent = getInterlocutor(client);

            for (Session session : sessions) {
                if (session.getId().equals(needSent)) {
                    session.getBasicRemote().sendText("{\"answer\":" + "\"" + "message" + "\"" + ","
                            + "\"message\":" + "\"" + jsonObject.getString("message") + "\"" + "}");
                    break;
                }
            }
        }
    }
}