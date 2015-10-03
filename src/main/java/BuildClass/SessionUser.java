package BuildClass;

import org.json.JSONObject;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;


public class SessionUser {

    public static Map<String, Session> sessions = new HashMap<String, Session>();

    public static List<String> freeUsersArray = new ArrayList<String>();

    public static Map<String, String> map1 = new HashMap<String, String>();
    public static Map<String, String> map2 = new HashMap<String, String>();

    public static Map<String, String> userSessionId = new HashMap<String, String>();

//    public static Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<String, Session>());
//
//    public static List<String> freeUsersArray = Collections.synchronizedList(new ArrayList<String>());
//
//    public static Map<String, String> map1 = Collections.synchronizedMap(new HashMap<String, String>());
//    public static Map<String, String> map2 = Collections.synchronizedMap(new HashMap<String, String>());
//
//    public static Map<String, String> userSessionId = Collections.synchronizedMap(new HashMap<String, String>());

    public static void printParams()
    {
        sessions.forEach((k,v)->System.out.println("Sessions " + "Key : " + k + " Value : " + v));

        for (String s: freeUsersArray)
            System.out.println("freeUsersArray:" + s);

        map1.forEach((k,v)->System.out.println("Sessions " + "Key : " + k + " Value : " + v));
        map2.forEach((k,v)->System.out.println("Sessions " + "Key : " + k + " Value : " + v));

        userSessionId.forEach((k,v)->System.out.println("userSessionId " + "Key : " + k + " Value : " + v));

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

        //Session ses = sessions.get(needSent);

        return needSent;
    }


    public static int connectTwo(Session client) throws IOException, EncodeException {
     // 0 - if ok
     //   1 - else

        JSONObject jsonToReturn = new JSONObject();
        jsonToReturn.put("answer", "new_window");

        client.getBasicRemote().sendText(jsonToReturn.toString());

        if (freeUsersArray.size() == 0)
            return 0;

        String waitingUsersId = freeUsersArray.get(0);

        // можно ускорить через map
        map1.put(client.getId(), waitingUsersId);
        map2.put(waitingUsersId, client.getId());

        freeUsersArray.remove(freeUsersArray.get(0));
        freeUsersArray.remove(waitingUsersId);

        return 1;


    }

    public static void newInterlocutor(Session session, String name) throws IOException, EncodeException
    {
        closeConnect(session);

        addFreeUser(session, name);
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


        interlocutorSes.getBasicRemote().sendText(jsonToReturn.toString());

        //if (checker) {
        //    freeUsersArray.add(interlocutor.getId());

            //установить новое соединение
        //    int answer = connectTwo(interlocutor);
        //}
    }
}
