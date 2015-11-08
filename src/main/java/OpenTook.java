import Databases.SQLiteClass;
import com.opentok.OpenTok;
import com.opentok.exception.OpenTokException;

import com.opentok.MediaMode;
import com.opentok.ArchiveMode;
import com.opentok.Session;
import com.opentok.SessionProperties;
import com.opentok.TokenOptions;
import com.opentok.Role;

import javax.naming.NamingException;
import java.sql.*;
// inside a class or method...


public class OpenTook {

    public static Connection conn;
    public static Statement stat;
    public static ResultSet rs;

    public static void Conn() throws ClassNotFoundException, SQLException, NamingException {
        Class.forName("org.postgresql.Driver");

        //полный путь к базе данных

        conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1/videochat", "postgres", "ve;br");

    }

    public static void CloseDB() throws ClassNotFoundException, SQLException {
        conn.close();
    }

    public static String getFreeSession() throws ClassNotFoundException, SQLException, NamingException {

        stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select session from rooms where much = 0");

        while (rs.next()) {
            String answer = rs.getString("session");
            rs.close();
            stat.close();

            updateSession(answer, 2);

            return answer;
        }

        rs.close();
        stat.close();

        return "";
    }

    public static void updateSession(String sessionName, int much) throws ClassNotFoundException, SQLException, NamingException {

        stat = conn.createStatement();

        int n = stat.executeUpdate("UPDATE rooms SET much = " + Integer.toString(much)
                + "where session = '" + sessionName + "'");

        stat.close();
    }

    public static void updateMinusOne(String sessionName) throws ClassNotFoundException, SQLException, NamingException {

        stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select much from rooms where session = '" + sessionName + "'");

        while (rs.next()) {
            int answer = rs.getInt("much");

            if (answer == 0)
            {
                rs.close();
                stat.close();
                return;
            }

            answer -= 1;

            int n = stat.executeUpdate("UPDATE rooms SET much = " + Integer.toString(answer)
                    + "where session = '" + sessionName + "'");
            break;
        }

        rs.close();
        stat.close();
    }

    public static void updateAllNull() throws ClassNotFoundException, SQLException, NamingException {

        stat = conn.createStatement();

        int n = stat.executeUpdate("UPDATE rooms SET much = " + Integer.toString(0));

        stat.close();
    }

    public static String generateToken() throws OpenTokException,ClassNotFoundException, SQLException, NamingException
    {
        int apiKey = 45400602; // YOUR API KEY
        String apiSecret = "e832beaf7185469a0f6c42ba4f2358d8c0e78165";

        OpenTok opentok = new OpenTok(apiKey, apiSecret);

        //Session session = opentok.createSession();

        TokenOptions tokenOpts = new TokenOptions.Builder()
                .expireTime((System.currentTimeMillis() / 1000L) + (20 * 60)) // in one week
                .build();

        Conn();
        String sessionId = getFreeSession();
        CloseDB();

        String token1 = opentok.generateToken(sessionId, tokenOpts);

        String token2 = opentok.generateToken(sessionId, tokenOpts);

        return token1 + "," + token2 + "," + sessionId;
    }

    public static void main(String args[]) throws ClassNotFoundException, SQLException, NamingException
    {

        Conn();

        //String sessionName = getFreeSession();

        updateAllNull();
        //updateSession(sessionName, 2);

        //System.out.println(sessionName);

        CloseDB();
    }
}
