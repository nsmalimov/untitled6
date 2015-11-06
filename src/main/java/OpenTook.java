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

        TokenOptions tokenOpts = new TokenOptions.Builder()
                .expireTime((System.currentTimeMillis() / 1000L) + (1 * 60)) // in one week
                .build();

        String token = opentok.generateToken("2_MX40NTQwMDYwMn5-MTQ0NjgxMDEwMTUzOH52WVR6Sm" +
                "J5Q29pRlljMG5MY2N3aG5VdVF-UH4", tokenOpts);

        return token;
    }
}
