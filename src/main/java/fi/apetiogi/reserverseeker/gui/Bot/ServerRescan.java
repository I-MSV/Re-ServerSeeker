package fi.apetiogi.reserverseeker.gui.Bot;

import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.apetiogi.reserverseeker.gui.Bot.MeteorToken.*;
import fi.apetiogi.reserverseeker.gui.FindNewServersScreen.Cracked;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.accounts.types.MicrosoftAccount;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.session.Session;

public class ServerRescan extends WindowScreen {
    private static final String userDir = System.getProperty("user.dir");

    public class Config {
        public String username;
        public String saveDirectory;
        public Boolean removeWhitelisted = null;
        public Boolean removeNonCracked = null;
    }

    public enum WhitelistIntent {
        Nothing,
        DeleteWhitelisted,
        Rename;

        public Boolean toBoolOrNull() {
            return switch (this) {
                case Nothing -> null;
                case DeleteWhitelisted -> true;
                case Rename -> false;
            };
        }
    }

    public enum CrackedIntent {
        Nothing,
        DeleteNonCracked,
        Rename;

        public Boolean toBoolOrNull() {
            return switch (this) {
                case Nothing -> null;
                case DeleteNonCracked -> true;
                case Rename -> false;
            };
        }
    }

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();
    WContainer settingsContainer;
    MultiplayerScreen multiplayerScreen;
    WButton whitelistButton;
    Config loadedConfig;
    
    //yes i will keep using this enum everywhere.
    private final Setting<WhitelistIntent> whitelistSetting = sg.add(new EnumSetting.Builder<WhitelistIntent>()
        .name("whitelist-intent")
        .description("What to do with scanned whitelisted servers")
        .defaultValue(WhitelistIntent.Nothing)
        .onChanged(bool -> { loadedConfig.removeWhitelisted = bool.toBoolOrNull(); })
        .build()
    );

    private final Setting<CrackedIntent> crackedSetting = sg.add(new EnumSetting.Builder<CrackedIntent>()
        .name("cracked-intent")
        .description("What to do with scanned cracked servers")
        .defaultValue(CrackedIntent.Nothing)
        .onChanged(bool -> { loadedConfig.removeNonCracked = bool.toBoolOrNull(); })
        .build()
    );


    public ServerRescan(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Server Rescan");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        loadConfig();

        add(theme.label("Not well tested, expect possible crashes / server list deletion"));
        settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings));

        add(theme.label(String.format("Note: This will use \"%s\" for joining", this.client.getSession().getUsername())));
        
        whitelistButton = add(theme.button("Rescan Server List")).expandX().widget();
        whitelistButton.action = () -> {
            StartRescan();
        };
    }

    private void StartRescan() {
        String indexPath = "Re-ServerSeeker/scripts/index.js";
        File scriptFile = new File(userDir, indexPath);
        if (!scriptFile.exists()) {
            whitelistButton.set("Script not found.");
            return;
        }
        Session session = this.client.getSession();

        if (session.getAccountType() != Session.AccountType.MSA) {
            whitelistButton.set("Please login with a microsoft account.");
            return;
        }

        MCAWrapper storedToken = new MCAWrapper();
        storedToken.mca = new MCAWrapper.MCA();

        Boolean found = false;
        for (Account<?> acc : Accounts.get()) {
            String name = acc.getUsername();
            if (name == session.getUsername() && acc instanceof MicrosoftAccount msAcc) {
                storedToken.mca.access_token = MeteorToken.getAccessToken(msAcc);
                loadedConfig.username = name;
                found = true;
                break;
            }
        }

        if (!found) {
            whitelistButton.set("Failed to find logged microsoft account");
            return;
        }

        storedToken.writefile(loadedConfig.username, userDir);
        saveConfig();

        whitelistButton.set("Starting...");
        
        ProcessBuilder pb = new ProcessBuilder("node", "index.js");
        pb.directory(scriptFile.getParentFile());
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
            new Thread(() -> {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        final String outputLine = line;
                        whitelistButton.set(outputLine);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        catch (Exception e) {
            whitelistButton.set("Error:" + e.getMessage());
            System.err.println(e.getMessage());
        }
    }

    private void loadConfig() {
        String indexPath = "Re-ServerSeeker/scripts/index.js";
        String configPath = "Re-ServerSeeker/scripts/config.json";
        File scriptFile = new File(userDir, indexPath);
        if (!scriptFile.exists()) {
            whitelistButton.set("Failed to find config.");
            return;
        }

        File configFile = new File(userDir, configPath);
        try {
            loadedConfig = gson.fromJson(new FileReader(configFile), Config.class);
        }
        catch (Exception e) {
            loadedConfig = new Config();
        }
    }

    private void saveConfig() {
        String configPath = "Re-ServerSeeker/scripts/config.json";
        File configFile = new File(userDir, configPath);

        //set values with no settings here
        loadedConfig.saveDirectory = userDir;

        try (FileWriter writer = new FileWriter(configFile)) {
            Gson withNulls = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            withNulls.toJson(loadedConfig, writer);
        }
        catch (Exception e) {
            whitelistButton.set("Failed to save config.");
            System.err.println(e.getMessage());
        }
    }
}
