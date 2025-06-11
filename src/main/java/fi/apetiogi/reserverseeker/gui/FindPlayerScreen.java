package fi.apetiogi.reserverseeker.gui;

import com.google.common.net.HostAndPort;
import fi.apetiogi.reserverseeker.SmallHttp;
import fi.apetiogi.reserverseeker.ssapi.responses.WhereisResponse;
import fi.apetiogi.reserverseeker.utils.MultiplayerScreenUtil;
import fi.apetiogi.reserverseeker.utils.Utils;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import static fi.apetiogi.reserverseeker.ReServerSeeker.api_servers;
import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FindPlayerScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    public enum NameOrUUID {
        Name,
        UUID
    }

    private final Settings settings = new Settings();
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<NameOrUUID> nameOrUUID = sg.add(new EnumSetting.Builder<NameOrUUID>()
        .name("name-or-uuid")
        .description("Whether to search by name or UUID.")
        .defaultValue(NameOrUUID.Name)
        .build()
    );

    private final Setting<String> name = sg.add(new StringSetting.Builder()
        .name("name")
        .description("The name to search for.")
        .defaultValue("")
        .visible(() -> nameOrUUID.get() == NameOrUUID.Name)
        .build()
    );

    private final Setting<String> uuid = sg.add(new StringSetting.Builder()
        .name("UUID")
        .description("The UUID to search for.")
        .defaultValue("")
        .visible(() -> nameOrUUID.get() == NameOrUUID.UUID)
        .build()
    );

    WContainer settingsContainer;

    public FindPlayerScreen(MultiplayerScreen multiplayerScreen) {
        super(GuiThemes.get(), "Find Players");
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        WContainer settingsContainer = add(theme.verticalList()).widget();
        settingsContainer.add(theme.settings(settings)).expandX();

        this.settingsContainer = settingsContainer;

        add(theme.button("Find Player")).expandX().widget().action = () -> {

            String final_url = api_servers + "&playerHistory=";
            String uuid_url = "https://sessionserver.mojang.com/session/minecraft/profile/";
            String player_name = "None";

            switch (nameOrUUID.get()) {
                case Name:
                player_name = name.get();
                final_url += URLEncoder.encode(name.get(), StandardCharsets.UTF_8);
                break;

                case UUID:
                uuid_url += uuid.get().replace("-", "");
                WhereisResponse uuidResponse = gson.fromJson(SmallHttp.get(uuid_url), WhereisResponse.class);
                
                player_name = uuidResponse.name;
                final_url += URLEncoder.encode(uuidResponse.name, StandardCharsets.UTF_8);
                break;
            }

            //adds server search limit and also sorts by most recent
            final_url += "&limit=128&sort=lastSeen&descending=true";

            String jsonResponse = SmallHttp.get(final_url);

            Type listType = new TypeToken<List<WhereisResponse.Record>>() {}.getType();
            List<WhereisResponse.Record> responseList = gson.fromJson(jsonResponse, listType);

            WhereisResponse resp = new WhereisResponse();
            resp.name = player_name;
            resp.data = responseList;

            if (resp.data.isEmpty()) {
                clear();
                add(theme.label("Not found")).expandX();
                return;
            }

            clear();

            add(theme.label("Found " + resp.data.size() + " servers:"));
            WTable table = add(theme.table()).widget();
            WButton addAllButton = table.add(theme.button("Add all")).expandX().widget();
            addAllButton.action = () -> addAllServers(resp.data);

            table.row();
            table.add(theme.label("Server IP"));
            table.add(theme.label("Player name"));
            table.add(theme.label("Last seen"));

            table.row();
            table.add(theme.horizontalSeparator()).expandX();
            table.row();


            for (WhereisResponse.Record server : resp.data) {
                String serverIP = MultiplayerScreenUtil.cleanIp(server.ip, server.port);
                String playerName = resp.name;
                server.name = resp.name;
                long playerLastSeen = server.lastSeen; // Unix timestamp

                // Format last seen to human-readable
                String playerLastSeenFormatted = Utils.get_days_hours_minutes(playerLastSeen);
                int minWidth = (int)(mc.getWindow().getWidth() * 0.2);
                table.add(theme.label(serverIP)).minWidth(minWidth);
                table.add(theme.label(playerName)).minWidth(minWidth);
                table.add(theme.label(playerLastSeenFormatted)).minWidth(minWidth);

                WButton addServerButton = theme.button("Add Server");
                addServerButton.action = () -> {
                    ServerInfo info = new ServerInfo("ServerSeeker " + serverIP + " (Player: " + playerName + ")", serverIP, ServerInfo.ServerType.OTHER);
                    MultiplayerScreenUtil.addInfoToServerList(multiplayerScreen, info);
                    addServerButton.visible = false;
                };

                HostAndPort hap = HostAndPort.fromString(serverIP);
                WButton joinServerButton = theme.button("Join Server");
                joinServerButton.action = () -> {
                    ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(), new ServerAddress(hap.getHost(), hap.getPortOrDefault(25565)), new ServerInfo("a", hap.toString(), ServerInfo.ServerType.OTHER), false, null);
                };

                WButton serverInfoButton = theme.button("Server Info");
                serverInfoButton.action = () -> this.client.setScreen(new ServerInfoScreen(server.ip, server.port));

                table.add(addServerButton);
                table.add(joinServerButton);
                table.add(serverInfoButton);
                table.row();
            }
        };
    }

    private void addAllServers(List<WhereisResponse.Record> records) {
        for (WhereisResponse.Record record : records) {
            String serverIP = record.ip + ":" + record.port;
            String playerName = record.name;
            ServerInfo info = new ServerInfo("ServerSeeker " + serverIP + " (Player: " + playerName + ")", serverIP, ServerInfo.ServerType.OTHER);
            MultiplayerScreenUtil.addInfoToServerList(multiplayerScreen, info, false);
        }
        MultiplayerScreenUtil.saveList(multiplayerScreen);
        if (client == null) return;
        client.setScreen(this.multiplayerScreen);
    }

    @Override
    public void tick() {
        super.tick();
        settings.tick(settingsContainer, theme);
    }
}
