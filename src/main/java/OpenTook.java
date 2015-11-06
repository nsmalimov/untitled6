import com.opentok.OpenTok;
import com.opentok.exception.OpenTokException;

import com.opentok.MediaMode;
import com.opentok.ArchiveMode;
import com.opentok.Session;
import com.opentok.SessionProperties;
import com.opentok.TokenOptions;
import com.opentok.Role;
// inside a class or method...


public class OpenTook {
    public static String generateToken() throws OpenTokException
    {
        int apiKey = 45400602; // YOUR API KEY
        String apiSecret = "e832beaf7185469a0f6c42ba4f2358d8c0e78165";

        OpenTok opentok = new OpenTok(apiKey, apiSecret);

        // A session that attempts to stream media directly between clients:
        Session session = opentok.createSession();

        // Store this sessionId in the database for later use:
        String sessionId = session.getSessionId();

        String token = session.generateToken(new TokenOptions.Builder()
                //20 минут
                .expireTime((System.currentTimeMillis() / 1000L) + (20 * 60)) // in one week
                .build());

        return token;
    }

    public static void main(String args[]) throws OpenTokException
    {
        String token = generateToken();
    }
}
