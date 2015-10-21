import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpServlet;
import javax.websocket.server.ServerEndpoint;
import java.text.ParseException;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONException;

public class MainServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        Cookie cookie = null;
//        Cookie[] cookies = null;
//        cookies = request.getCookies();

        //if (cookies != null) {
            response.setContentType("text/html");
            RequestDispatcher dispatcherChat = request.getRequestDispatcher("chat.html");
            if (dispatcherChat != null) {
                dispatcherChat.forward(request, response);
            }
        //}
        //куки небыли присланы
//        else {
//            response.setContentType("text/html");
//            RequestDispatcher dispatcherAuto = request.getRequestDispatcher("chat.html");
//            if (dispatcherAuto != null) {
//                dispatcherAuto.forward(request, response);
//            }
//        }
    }
}
