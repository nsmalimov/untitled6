import Databases.SQLiteClass;
import org.json.JSONObject;

import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;

public class AutorizationServlet extends HttpServlet {

    //TODO проверка по ip
    public static boolean checkIp(String ip) throws ClassNotFoundException, SQLException,  NamingException
    {
        //проверка ругулярными выражениями
        if (ip.equals("0"))
            return true;

        GoodIP gIpClass = new GoodIP();

        String[] goodIP = gIpClass.ipAddresses;

        String[] splitIP = ip.split(".");

        StringBuilder neIP = new StringBuilder();

        for (String c: splitIP)
                neIP.append(c);

        String newIP = neIP.toString();

        for (String c: goodIP)
        {
            if (newIP.equals(c))
                return true;
        }

        return (SQLiteClass.checkIP(ip));

    }

    public static boolean checkKeyGen(String name, String key, String ip) throws ClassNotFoundException, SQLException, NamingException {
        SQLiteClass.Conn();
        boolean answer = SQLiteClass.checkKeyGenDb(key);

        if (answer)
        {
            //запись в базу данных
            SQLiteClass.addUserDatabase(name, key, ip);
        }

        SQLiteClass.CloseDB();
        return answer;
    }

    public static String checkCookies(HttpServletRequest request) throws ClassNotFoundException, SQLException, NamingException{
        Cookie[] cookies = null;
        cookies = request.getCookies();

        String userName = "";

        if (cookies != null) {
            for(Cookie cookie : cookies){
                if("userKey".equals(cookie.getName())){

                    userName = SQLiteClass.getNameDb(cookie.getValue());

                    return userName;
                }
            }
            return userName;
        }
        return "";
    }

    public static String getUserKey(HttpServletRequest request) throws ClassNotFoundException, SQLException, NamingException{
        Cookie[] cookies = null;
        cookies = request.getCookies();

        String userName = "";

        if (cookies != null) {
            for(Cookie cookie : cookies){
                if("userKey".equals(cookie.getName())){

                    userName = SQLiteClass.getNameDb(cookie.getValue());

                    return cookie.getValue();
                }
            }
            return userName;
        }
        return "";
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //регистрация нового пользователя

        StringBuilder jb = new StringBuilder();
        String line = null;

        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            System.out.println(e);
        }


        try {
            JSONObject jsonObject = new JSONObject(jb.toString());

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            int command = jsonObject.getInt("command");

            //проверяем правильность куки


            switch (command) {
                case 0:  //авторизация

                    String userNameCookies = checkCookies(request);

                    String ip = jsonObject.getString("ip");

                    boolean checkIp = checkIp(ip);

                    //System.out.println(userNameCookies);

                    //ip верен cooki верны
                    if (checkIp && !userNameCookies.equals("")) {
                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "ok");
                        jsonToReturn.put("name", userNameCookies);
                        out.println(jsonToReturn.toString());
                    }

                    //ip верен cooki не верны или отсутствуют
                    if (checkIp && userNameCookies.equals(""))
                    {
                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "name");
                        out.println(jsonToReturn.toString());
                    }

                    //ip не правильный, но куки пришли
                    //нужно куки проверить
                    //динамический ip?
                    if (!checkIp && !userNameCookies.equals(""))
                    {
                        //добавить ip в базу данных
                        SQLiteClass.addUserIP(ip);

                        SQLiteClass.updateIP(getUserKey(request), ip);

                        //обновить ip?

                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "ok");
                        jsonToReturn.put("name", userNameCookies);
                        out.println(jsonToReturn.toString());
                    }

                    //отправить форму регистрации
                    if (!checkIp && userNameCookies.equals(""))
                    {
                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "ip");
                        out.println(jsonToReturn.toString());
                    }

                    break;

                case 2: //имя
                    String name = jsonObject.getString("name");

                    String userIp = jsonObject.getString("ip");

                    String uuid = UUID.randomUUID().toString();

                    SQLiteClass.addUser(name, uuid, userIp); //simple add user

                    JSONObject jsonToReturn = new JSONObject();
                    jsonToReturn.put("answer", "ok");
                    jsonToReturn.put("name", name);
                    out.println(jsonToReturn.toString());

                    Cookie userKeyCook = new Cookie("userKey", uuid);
                    userKeyCook.setMaxAge(60 * 60 * 24 * 5);
                    response.addCookie(userKeyCook);

                    break;

                case 1: //по имени и ключу

                    String userName = (String) jsonObject.get("name");
                    String keyGen = (String) jsonObject.get("keyGen");

                    String userIP = (String) jsonObject.get("ip");

                    boolean isOk = checkKeyGen(userName, keyGen, userIP);

                    //если всё нормально, то отправить куки
                    if (isOk) {
                        JSONObject jsonToReturn1 = new JSONObject();
                        jsonToReturn1.put("answer", "ok");
                        jsonToReturn1.put("name", userName);
                        out.println(jsonToReturn1.toString());

                        Cookie userKeyCook1 = new Cookie("userKey", keyGen);
                        userKeyCook1.setMaxAge(60 * 60 * 24 * 5);
                        response.addCookie(userKeyCook1);
                    } else {
                        //ошибка или не правильный ключ
                        JSONObject jsonToReturn1 = new JSONObject();
                        jsonToReturn1.put("answer", "wrong");
                        out.println(jsonToReturn1.toString());
                    }
                    break;
                default:
                    System.out.println("default switch");
                    break;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
