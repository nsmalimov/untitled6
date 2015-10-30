package Databases;

import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.*;

//TODO
//add finally

public class SQLiteClass {
    public static Connection conn;
    public static Statement stat;
    public static ResultSet rs;

    public static void Conn() throws ClassNotFoundException, SQLException, NamingException {
        Class.forName("org.sqlite.JDBC");

        //полный путь к базе данных
        conn = DriverManager.getConnection("jdbc:sqlite:/Users/Nurislam/Downloads/untitled6/ChatDatabase");
    }

    public static boolean checkKeyGenDb(String keyGen) throws ClassNotFoundException, SQLException {
        stat = conn.createStatement();

        //если найдено значение неиспользованное
        ResultSet rs = stat.executeQuery("select id from keyGens where keyGen = '" + keyGen + "'" +
                "and marker != " + "'registrated'");
        while (rs.next()) {
            rs.close();
            stat.close();
            return true;
        }

        rs.close();
        stat.close();
        return false;
    }

    //добавить пользователя в базу данных
    public static void addUserDatabase(String userName, String keyGen) throws ClassNotFoundException, SQLException, NamingException {
        stat = conn.createStatement();

        int n = stat.executeUpdate("UPDATE keyGens SET marker = 'registrated' WHERE keyGen = '" + keyGen + "'");

        stat.close();

        Conn();
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO freeUsers (name,  userKeyGen) VALUES ( ?, ?)");
            statement.setString(1, userName);
            statement.setString(2, keyGen);

            statement.execute();
            statement.close();
        } catch (Exception e) {
            //nothing
        }
        finally {
            CloseDB();
        }
    }

    //получить имя по ключу
    public static String getNameDb(String keyGen) throws ClassNotFoundException, SQLException, NamingException
    {
        Conn();

        stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select name from freeUsers where userKeyGen = '" + keyGen + "'");

        while (rs.next()) {
            String answer = rs.getString("name");
            rs.close();
            stat.close();
            CloseDB();
            return answer;
        }

        rs.close();
        stat.close();
        CloseDB();

        return "";
    }

    public static void addUserIP(String ip) throws ClassNotFoundException, SQLException, NamingException {
        Conn();
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO usersIP (ip) VALUES (?)");
            statement.setString(1, ip);

            statement.execute();
            statement.close();
        } catch (Exception e) {
            //nothing
        }
        finally {
            CloseDB();
        }
    }

    public static boolean checkIP(String ip) throws ClassNotFoundException, SQLException,  NamingException
    {
        stat = conn.createStatement();

        //если найдено значение неиспользованное
        ResultSet rs = stat.executeQuery("select id from free where usersIP = '" + ip + "'");
        while (rs.next()) {
            rs.close();
            stat.close();
            return true;
        }

        rs.close();
        stat.close();
        return false;
    }


    public static String generateKeygen() throws ClassNotFoundException, SQLException,  NamingException
    {
        Conn();

        stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select keyGen from keyGens where marker = " + "'not_used'");

        while (rs.next()) {
            String answer = rs.getString("keyGen");


            int n = stat.executeUpdate("UPDATE keyGens SET marker = 'sent' WHERE keyGen =" + "'" + answer + "'");

            rs.close();
            stat.close();
            CloseDB();
            return answer;
        }

        rs.close();
        stat.close();
        CloseDB();

        return "";
    }

    public static void CloseDB() throws ClassNotFoundException, SQLException {
        //conn.commit();
        conn.close();
    }

    public static void addUser(String userName, String keyGen) throws ClassNotFoundException, SQLException, NamingException {
        Conn();
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO freeUsers (name,  userKeyGen) VALUES ( ?, ?)");
            statement.setString(1, userName);
            statement.setString(2, keyGen);

            statement.execute();
            statement.close();
        } catch (Exception e) {
            //nothing
        }
        finally {
            CloseDB();
        }
    }

    //TODO
    //извлечение и генерация ключей
}