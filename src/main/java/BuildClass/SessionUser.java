package BuildClass;

import org.json.JSONObject;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;


public class SessionUser {

    public static Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<String, Session>());

    public static List<String> freeUsersArray = Collections.synchronizedList(new ArrayList<String>());

    public static Map<String, String> map1 = Collections.synchronizedMap(new HashMap<String, String>());
    public static Map<String, String> map2 = Collections.synchronizedMap(new HashMap<String, String>());

    public static Map<String, String> userSessionId = Collections.synchronizedMap(new HashMap<String, String>());

    public static void addFreeUser(Session session, String name) throws IOException, EncodeException
    {
        sessions.put(session.getId(), session);
        userSessionId.put(session.getId(), name);

        if (freeUsersArray.size() == 0)
        {
            freeUsersArray.add(session.getId());

            JSONObject jsonToReturn = new JSONObject();
            jsonToReturn.put("answer", "owner");
            session.getBasicRemote().sendText(jsonToReturn.toString());

        }
        else
        {
            String waitingUsersId = freeUsersArray.get(0);

            // можно ускорить через map
            map1.put(session.getId(), waitingUsersId);
            map2.put(waitingUsersId, session.getId());

            JSONObject jsonToReturn = new JSONObject();
            jsonToReturn.put("answer", "guest");
            jsonToReturn.put("nameInterlocutor", userSessionId.get(waitingUsersId));

            session.getBasicRemote().sendText(jsonToReturn.toString());

            freeUsersArray.remove(freeUsersArray.get(0));
        }
    }

    public static Session getInterlocutor(Session client) {
        String needSent = "";

        if (map1.containsKey(client.getId()))
        {
            needSent = map1.get(client.getId());
        }

        if (map2.containsKey(client.getId()))
        {
            needSent = map2.get(client.getId());
        }

        Session ses = sessions.get(needSent);

        return ses;
    }

    public static void newInterlocutor(Session session)
    {
        closeConnect(session);
    }

    public static void closeConnect(Session session)
    {
        boolean check = false;
        if (map1.containsKey(session.getId()))
        {
            map2.remove(map1.get(session.getId()));
            map1.remove(session.getId());

            freeUsersArray.add(session.getId());

            check = true;
        }

        if (!check)
        {
            freeUsersArray.remove(session.getId());
        }

        sessions.remove(session.getId());
        userSessionId.remove(session.getId());
    }
}
