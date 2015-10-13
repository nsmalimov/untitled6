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

public class AutorizationServlet extends HttpServlet {

    //TODO проверка по ip
    public static boolean checkIp(String ip) {
        return true;
    }

    public static boolean checkKeyGen(String name, String key) throws ClassNotFoundException, SQLException, NamingException {
        SQLiteClass.Conn();
        boolean answer = SQLiteClass.checkKeyGenDb(key);

        if (answer)
        {
            //запись в базу данных
            SQLiteClass.addUserDatabase(name, key);
        }

        SQLiteClass.CloseDB();
        return answer;
    }

    public static String checkCookies(HttpServletRequest request) throws ClassNotFoundException, SQLException, NamingException{
        //Cookie cookie = null;
        Cookie[] cookies = null;
        cookies = request.getCookies();

        String userName = "";

        if (cookies != null) {

            //Cookie[] cookies = request.getCookies();
            //String keyGenGetUser = null;
            for(Cookie cookie : cookies){
                if("userKey".equals(cookie.getName())){
                    userName = SQLiteClass.getNameDb(cookie.getValue());
                    break;
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


        //System.out.println(jb.toString());

        try {
            JSONObject jsonObject = new JSONObject(jb.toString());

            Iterator it = jsonObject.keys();

            //response.setHeader("Content-Type", "text/plain");
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            int command = jsonObject.getInt("command");

            switch (command) {
                case 0:  //авторизация

                    String ip = (String) jsonObject.get("ip");


                    boolean checkIp = checkIp(ip);
                    String checkCookies = checkCookies(request); //userName

                    if (checkIp && !checkCookies.equals("")) {
                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "ok");

                        //имя из куки
                        jsonToReturn.put("name", checkCookies);
                        out.println(jsonToReturn.toString());
                    }

                    if (!checkIp) {
                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "ip");
                        out.println(jsonToReturn.toString());
                    }

                    if (checkCookies.equals("")) {
                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "cookies");
                        out.println(jsonToReturn.toString());
                    }
                    //System.out.println(jb.toString());

                    break;

                case 1: //регистрация

                    String name = (String) jsonObject.get("name");
                    String keyGen = (String) jsonObject.get("keyGen");

                    boolean checkUser = false;

                    //проверка ключа и запись юзера в базу данных
                    String username = SQLiteClass.getNameDb(keyGen);

                    //проверка есть такой пользователь в базе
                    if (username.equals("")) {
                        checkUser = checkKeyGen(name, keyGen);
                    }
                    else {
                        checkUser = true;
                        name = username;
                    }

                    //если всё нормально, то отправить куки
                    if (checkUser) {
                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "ok");
                        jsonToReturn.put("name", name);
                        out.println(jsonToReturn.toString());

                        Cookie userKeyCook = new Cookie("userKey", keyGen);
                        userKeyCook.setMaxAge(60 * 60 * 24 * 5);
                        response.addCookie(userKeyCook);
                    } else {
                        //ошибка или не правильный ключ
                        JSONObject jsonToReturn = new JSONObject();
                        jsonToReturn.put("answer", "wrong");
                        out.println(jsonToReturn.toString());
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
