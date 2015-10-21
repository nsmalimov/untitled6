package BuildClass;

import Databases.SQLiteClass;
import org.json.JSONObject;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import javax.naming.NamingException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;


public class SessionUser {

    public static Map<String, Session> sessions = new HashMap<String, Session>();

    public static List<String> freeUsersArray = new ArrayList<String>();

    public static Map<String, String> map1 = new HashMap<String, String>();
    public static Map<String, String> map2 = new HashMap<String, String>();

    public static Map<String, String> userSessionId = new HashMap<String, String>();

    public static Map<String, String> waitUsers = new HashMap<String, String>();

    public static void printParams()
    {
        System.out.println("print on");

        for (Map.Entry<String,Session> entry : sessions.entrySet()) {

            System.out.println("Sessions " + "Key : " + entry.getKey() + " Value : " + entry.getValue());
        }

        for (String s: freeUsersArray)
            System.out.println("freeUsersArray: " + s);

        for (Map.Entry<String, String> entry : map1.entrySet()) {

            System.out.println("Sessions " + "Key : " + entry.getKey() + " Value : " + entry.getValue());
        }

        for (Map.Entry<String, String> entry : map2.entrySet()) {

            System.out.println("Sessions " + "Key : " + entry.getKey() + " Value : " + entry.getValue());
        }


        for (Map.Entry<String, String> entry : userSessionId.entrySet()) {

            System.out.println("userSessionId " + "Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
    }

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

            map1.put(session.getId(), waitingUsersId);
            map2.put(waitingUsersId, session.getId());

            JSONObject jsonToReturn = new JSONObject();

            jsonToReturn.put("answer", "guest");
            System.out.println("guest " + session.getId());

            jsonToReturn.put("nameInterlocutor", userSessionId.get(waitingUsersId));

            session.getBasicRemote().sendText(jsonToReturn.toString());

            freeUsersArray.remove(freeUsersArray.get(0));
        }
    }

    public static Session getInterlocutorSession(Session client) {
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

    public static String getInterlocutorName(Session client) {
        String needSent = "";

        if (map1.containsKey(client.getId()))
        {
            needSent = map1.get(client.getId());
        }

        if (map2.containsKey(client.getId()))
        {
            needSent = map2.get(client.getId());
        }

        return needSent;
    }


    public static int connectTwo(Session client) throws IOException, EncodeException {

        if (freeUsersArray.size() == 0) {
            freeUsersArray.add(client.getId());
            waitUsers.put(client.getId(), "000");
            return 0;
        }

        String waitingUsersId = freeUsersArray.get(0);

        // можно ускорить через map
        map1.put(client.getId(), waitingUsersId);
        map2.put(waitingUsersId, client.getId());

        freeUsersArray.remove(freeUsersArray.get(0));
        freeUsersArray.remove(waitingUsersId);

        return 1;
    }

    public static void closeConnect(Session session) throws IOException, EncodeException {

        String interlocutorName = getInterlocutorName(session);

        Session interlocutorSes = getInterlocutorSession(session);

        boolean checker = false;

        if (map1.containsKey(session.getId()))
        {
            map1.remove(session.getId());
            checker = true;
        }

        if (map2.containsKey(session.getId()))
        {
            map2.remove(session.getId());
            checker = true;
        }

        if (map1.containsKey(interlocutorName))
        {
            map1.remove(interlocutorName);
            checker = true;
        }

        if (map2.containsKey(interlocutorName))
        {
            map2.remove(interlocutorName);
            checker = true;
        }

        if (sessions.containsKey(session.getId()))
        {
            sessions.remove(session.getId());
        }

        if (userSessionId.containsKey(session.getId()))
        {
            userSessionId.remove(session.getId());
        }


        try {
            freeUsersArray.remove(session.getId());
        }
        catch (Throwable e)
        {
            System.out.println(e);
        }

        //сказать что собеседник вышел => открыть окно подтверждения
        JSONObject jsonToReturn = new JSONObject();
        jsonToReturn.put("answer", "new_window");


        //not in free users
        if (checker) {
            interlocutorSes.getBasicRemote().sendText(jsonToReturn.toString());
        }

    }
}
