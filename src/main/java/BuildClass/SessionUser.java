package BuildClass;

import org.json.JSONObject;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

/**
 * Created by Nurislam on 30.09.15.
 */
public class SessionUser {
    public static Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

    public static ArrayList<String> freeUsersArray = new ArrayList<String>();

    public static ArrayList<String[]> connectedUsers = new ArrayList<String[]>();

    public static Map userSessionId = new HashMap<String, String>();

    public static void addFreeUser(Session session, String name) throws IOException, EncodeException
    {
        sessions.add(session);
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

            //TODO
            // можно ускорить через map
            String[] someArray = {session.getId(), waitingUsersId};


            JSONObject jsonToReturn = new JSONObject();
            jsonToReturn.put("answer", "guest");
            jsonToReturn.put("nameInterlocutor", userSessionId.get(waitingUsersId));

            session.getBasicRemote().sendText(jsonToReturn.toString());

            connectedUsers.add(someArray.clone());

            freeUsersArray.remove(freeUsersArray.get(0));

        }
    }

    public static Session getInterlocutor(Session client) {
        String needSent = "";

        //TODO
        //упростить
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

        for (Session ses: sessions)
            if (ses.getId().equals(needSent))
            {
                return ses;
            }

        return null;
    }
}
