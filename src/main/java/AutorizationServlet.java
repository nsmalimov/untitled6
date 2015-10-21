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
    public static boolean checkIp(String latitude, String longitude) {
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
        Cookie[] cookies = null;
        cookies = request.getCookies();

        String userName = "";

        if (cookies != null) {
            for(Cookie cookie : cookies){
                if("userKey".equals(cookie.getName())){
                    //проверка что такой ключ есть в базе
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


        try {
            JSONObject jsonObject = new JSONObject(jb.toString());

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            int command = jsonObject.getInt("command");

            switch (command) {
                case 0:  //авторизация

                    String latitude = jsonObject.getString("latitude_var"); //широта
                    String longitude = jsonObject.getString("longitude_var"); //долгота

                    System.out.println(latitude + " " + longitude);

                    boolean checkIp = checkIp(latitude, longitude);

                    //пришли ли куки
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

                    break;

                case 1: //регистрация нового пользователя
                    //сюда же приходят те, кто удалил куки

                    String name = (String) jsonObject.get("name");
                    String keyGen = (String) jsonObject.get("keyGen");

                    boolean isOk = checkKeyGen(name, keyGen);

                    //если всё нормально, то отправить куки
                    if (isOk) {
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
