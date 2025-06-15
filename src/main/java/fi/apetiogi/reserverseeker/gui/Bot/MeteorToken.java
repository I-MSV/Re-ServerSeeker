package fi.apetiogi.reserverseeker.gui.Bot;

import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import meteordevelopment.meteorclient.systems.accounts.types.MicrosoftAccount;

@SuppressWarnings("unused")
public class MeteorToken {
    public static class MCAWrapper {
        public MCA mca;

        public static class MCA {
            public String access_token;
            private int expires_in = 0;//required for auth
            private long obtainedOn = 9999999999999L;// this too...
        }

        public void writeFile(String username, String directory) {
            String configPath = "Re-ServerSeeker/scripts/" + getCacheFileName(username);
            File tokenFile = new File(directory, configPath);

            try (FileWriter writer = new FileWriter(tokenFile)) {
                gson.toJson(this, writer);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void clearFile(String username, String directory) {
            String configPath = "Re-ServerSeeker/scripts/" + getCacheFileName(username);
            File tokenFile = new File(directory, configPath);

            try {
                tokenFile.delete();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getAccessToken(MicrosoftAccount acc) {
        try {
            Field tokenField = acc.getClass().getDeclaredField("token");
            tokenField.setAccessible(true);
            return (String) tokenField.get(acc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String createHash(String input) {
        try {
            byte[] hashBytes = MessageDigest.getInstance("SHA-1").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.substring(0, 6);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCacheFileName(String username) {
        String hash = createHash(username);
        return hash + "_mca-cache.json";
    }
}
