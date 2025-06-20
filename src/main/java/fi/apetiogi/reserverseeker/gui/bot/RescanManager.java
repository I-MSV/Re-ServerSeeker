package fi.apetiogi.reserverseeker.gui.bot;

import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import fi.apetiogi.reserverseeker.gui.bot.ServerRescan.Config;
import fi.apetiogi.reserverseeker.utils.MultiplayerScreenUtil;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.types.MicrosoftAccount;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

@SuppressWarnings("unused")
public class RescanManager {
    public static Process ongoingProcess = null;
    public static WLabel processLabel = null;
    public static WButton processButton = null;
    public static String currentLine = "";

    public static class MCAWrapper {
        public MCA mca;

        public static class MCA {
            public String access_token;
            private int expires_in = 0;//required for auth
            private long obtainedOn = 9999999999999L;// this too...
        }

        public void writeFile(String username, String directory) {
            String configPath = "Re-Scanner/" + getCacheFileName(username);
            File tokenFile = new File(directory, configPath);

            try (FileWriter writer = new FileWriter(tokenFile)) {
                gson.toJson(this, writer);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void clearFile(String username, String directory) {
            String configPath = "Re-Scanner/" + getCacheFileName(username);
            File tokenFile = new File(directory, configPath);

            if (!tokenFile.exists()) return;

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

    public static void StartProcess(File scriptFile, MCAWrapper storedToken, Config config, MultiplayerScreen mpScreen) {
        String lastButtonText = processButton.getText();
        processButton.set("Cancel");

        ProcessBuilder pb = new ProcessBuilder("node", "index.js");
        pb.directory(scriptFile.getParentFile());
        try {
            ongoingProcess = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ongoingProcess.getInputStream()));

            new Thread(() -> {
                try {
                    while ((currentLine = reader.readLine()) != null) {
                        processLabel.set(currentLine);
                    }

                    ongoingProcess.waitFor();
                    ongoingProcess = null;
                    MultiplayerScreenUtil.reloadServerList(mpScreen);
                    processButton.set(lastButtonText);
                    storedToken.clearFile(config.username, config.saveDirectory);
                }
                catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    storedToken.clearFile(config.username, config.saveDirectory);
                }
            }).start();
        }
        catch (Exception e) {
            processLabel.set("Error:" + e.getMessage());
            storedToken.clearFile(config.username, config.saveDirectory);
            System.err.println(e.getMessage());
        }
    }

    public static void UpdateText(WLabel label, WButton button) {
        processLabel = label;
        processButton = button;
        if (ongoingProcess != null) {
            processButton.set("Cancel");
            processLabel.set(currentLine);
        }
    }
}
