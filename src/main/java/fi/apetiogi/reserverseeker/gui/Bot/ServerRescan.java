package fi.apetiogi.reserverseeker.gui.Bot;

import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;
import static fi.apetiogi.reserverseeker.ReServerSeeker.userDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.apetiogi.reserverseeker.gui.Bot.MeteorToken.*;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
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
import net.minecraft.nbt.NbtCompound;

public class ServerRescan extends WindowScreen {
    public static NbtCompound savedSettings;

    public class Config {
        public String username;
        public String saveDirectory;
        public Boolean whitelistIntent = null;
        public Boolean crackedIntent = null;
        public boolean deleteOffline = true;
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

        public static WhitelistIntent fromBool(Boolean bool) {
            return bool == null ? Nothing : bool ? DeleteWhitelisted : Rename;
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

        public static CrackedIntent fromBool(Boolean bool) {
            return bool == null ? Nothing : bool ? DeleteNonCracked : Rename;
        }
    }

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();
    WContainer settingsContainer;
    MultiplayerScreen multiplayerScreen;
    WButton rescanButton;
    WLabel noteLabel;
    Config loadedConfig;
    
    //yes i will keep using this enum everywhere.
    private final Setting<WhitelistIntent> whitelistSetting = sg.add(new EnumSetting.Builder<WhitelistIntent>()
        .name("whitelist-intent")
        .description("What to do with scanned whitelisted servers")
        .defaultValue(WhitelistIntent.Nothing)
        .onChanged(bool -> { loadedConfig.whitelistIntent = bool.toBoolOrNull(); })
        .build()
    );

    private final Setting<CrackedIntent> crackedSetting = sg.add(new EnumSetting.Builder<CrackedIntent>()
        .name("cracked-intent")
        .description("What to do with scanned cracked servers")
        .defaultValue(CrackedIntent.Nothing)
        .onChanged(bool -> { loadedConfig.crackedIntent = bool.toBoolOrNull(); })
        .build()
    );

    private final Setting<Boolean> offlineSetting = sg.add(new BoolSetting.Builder()
        .name("delete-offline-servers")
        .description("Whether to delete timed out servers")
        .defaultValue(true)
        .onChanged(bool -> { loadedConfig.deleteOffline = bool; })
        .build()
    );


    public ServerRescan(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Server Rescan");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        loadConfig();
        onClosed(this::saveConfig);

        add(theme.label("Expect possible crashes / server list deletion"));
        settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings));
        
        noteLabel = add(theme.label(String.format("Note: This will use \"%s\" for joining", this.client.getSession().getUsername()))).widget();
        
        add(theme.button("Reset all")).expandX().widget().action = this::resetSettings;
        rescanButton = add(theme.button("Rescan Server List")).expandX().widget();
        rescanButton.action = () -> {
            StartRescan();
        };
    }

    private void StartRescan() {
        String indexPath = "Re-Scanner/index.js";
        File scriptFile = new File(userDir, indexPath);
        if (!scriptFile.exists()) {
            rescanButton.set("Script not found.");
            return;
        }

        Session session = this.client.getSession();
        MCAWrapper storedToken = new MCAWrapper();

        if (session.getAccountType() == Session.AccountType.MSA) {
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
                rescanButton.set("Failed to find logged microsoft account");
                return;
            }
        }

        else {
            noteLabel.set("Scanning without online account, disabling whitelist check");
            loadedConfig.username = session.getUsername();
            whitelistSetting.set(WhitelistIntent.Nothing);
        }

        rescanButton.set("Saving config");

        storedToken.writeFile(loadedConfig.username, userDir);
        saveConfig();

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
                        rescanButton.set(outputLine);
                    }

                    process.waitFor();
                    storedToken.clearFile(loadedConfig.username, userDir);
                }
                catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        catch (Exception e) {
            rescanButton.set("Error:" + e.getMessage());
            System.err.println(e.getMessage());
        }
    }

    private void loadConfig() {
        String configPath = "Re-Scanner/config.json";
        File configFile = new File(userDir, configPath);
        try {
            loadedConfig = gson.fromJson(new FileReader(configFile), Config.class);
            //also set the settings to the loaded values
            whitelistSetting.set(WhitelistIntent.fromBool(loadedConfig.whitelistIntent));
            crackedSetting.set(CrackedIntent.fromBool(loadedConfig.crackedIntent));
            offlineSetting.set(loadedConfig.deleteOffline);
        }
        catch (Exception e) {
            loadedConfig = new Config();
        }
    }

    private void saveConfig() {
        String configPath = "Re-Scanner/config.json";
        File configFile = new File(userDir, configPath);

        //set values with no settings here
        loadedConfig.saveDirectory = userDir;

        try (FileWriter writer = new FileWriter(configFile)) {
            Gson withNulls = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            withNulls.toJson(loadedConfig, writer);
        }
        catch (Exception e) {
            rescanButton.set("Failed to save config.");
            System.err.println(e.getMessage());
        }
    }

    public void resetSettings() {
        for (Setting<?> setting : sg) {
            setting.reset();
        }
        saveConfig();
        reload();
    }
}
