package fi.apetiogi.reserverseeker.gui;

import com.google.common.net.HostAndPort;
import com.google.gson.reflect.TypeToken;

import fi.apetiogi.reserverseeker.SmallHttp;
import fi.apetiogi.reserverseeker.ssapi.responses.ServerInfoResponse;
import fi.apetiogi.reserverseeker.utils.MultiplayerScreenUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import static fi.apetiogi.reserverseeker.ReServerSeeker.api_players;
import static fi.apetiogi.reserverseeker.ReServerSeeker.api_servers;
import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;
public class ServerInfoScreen extends WindowScreen {
    private final Long ipLong;
    private final int port;
    private final String serverIp;

    public ServerInfoScreen(Long ip, int port) {
        super(GuiThemes.get(), "Server Info: " + MultiplayerScreenUtil.cleanIp(ip, port));
        this.ipLong = ip;
        this.port = port;
        this.serverIp = MultiplayerScreenUtil.cleanIp(ip, port);
    }

    @Override
    public void initWidgets() {
        add(theme.label("Fetching server info..."));
        HostAndPort hap = HostAndPort.fromString(serverIp);
        //ip needs to be without dots for this API
        String ip = ipLong.toString();
        if (hap.hasPort()) {
            ip += "&port=" + hap.getPort();
        }
        String jsonResp = SmallHttp.get(api_servers + "&ip=" + ip);
        
        Type listType = new TypeToken<List<ServerInfoResponse>>() {}.getType();
        List<ServerInfoResponse> responseList;
        try {
            responseList = gson.fromJson(jsonResp, listType);
        }
        catch (Exception e) {
            clear();
            System.out.println(e.toString());
            add(theme.label(e.toString())).expandX();
            return;
        }

        if (responseList.isEmpty()) {
            clear();
            System.out.println(responseList.get(0));
            add(theme.label("Couldn't find a server. (2)")).expandX();
            return;
        }

        ServerInfoResponse resp = responseList.get(0);
        
        if (resp.isError()) {
            clear();
            add(theme.label(resp.error)).expandX();
            return;
        }

        Type playerType = new TypeToken<List<ServerInfoResponse.Player>>() {}.getType();
        String jsonPlayerString = SmallHttp.get(api_players + "&ip=" + ipLong + "&port=" + port);

        try {
            resp.playerInfo = gson.fromJson(jsonPlayerString, playerType);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        clear();

        Boolean cracked = resp.cracked;
        Boolean whitelisted = resp.whitelisted;
        String description = resp.description;
        int onlinePlayers = resp.players.online;
        int maxPlayers = resp.players.max;
        int protocol = resp.version.protocol;
        int lastSeen = resp.lastSeen;
        String version = resp.version.name;
        List<ServerInfoResponse.Player> players = resp.playerInfo;

        WTable dataTable = add(theme.table()).widget();
        WTable playersTable = add(theme.table()).expandX().widget();

        dataTable.add(theme.label("Cracked: "));
        dataTable.add(theme.label(cracked == null ? "Unknown" : cracked.toString()));
        dataTable.row();
        
        dataTable.add(theme.label("Whitelisted: "));
        dataTable.add(theme.label(whitelisted == null ? "Unknown" : cracked.toString()));
        dataTable.row();

        dataTable.add(theme.label("Description: "));
        if (description.length() > 100) description = description.substring(0, 100) + "...";
        description = description.replace("\n", "\\n");
        description = description.replace("§r", "");
        dataTable.add(theme.label(description));
        dataTable.row();

        dataTable.add(theme.label("Online Players (last scan): "));
        dataTable.add(theme.label(String.valueOf(onlinePlayers)));
        dataTable.row();

        dataTable.add(theme.label("Max Players: "));
        dataTable.add(theme.label(String.valueOf(maxPlayers)));
        dataTable.row();

        dataTable.add(theme.label("Last Seen: "));
        String lastSeenDate = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .format(Instant.ofEpochSecond(lastSeen).atZone(ZoneId.systemDefault()).toLocalDateTime());
        dataTable.add(theme.label(lastSeenDate));
        dataTable.row();

        dataTable.add(theme.label("Version: "));
        dataTable.add(theme.label(version + " (" + protocol + ")"));

        playersTable.add(theme.label(""));
        playersTable.row();
        playersTable.add(theme.label("Players:"));
        playersTable.row();


        playersTable.add(theme.label("Name ")).expandX();
        playersTable.add(theme.label("Last seen ")).expandX();
        playersTable.row();


        playersTable.add(theme.horizontalSeparator()).expandX();
        playersTable.row();

        for (ServerInfoResponse.Player player : players) {
            String name = player.name;
            long playerLastSeen = player.lastSession;
            String lastSeenFormatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(Instant.ofEpochSecond(playerLastSeen).atZone(ZoneId.systemDefault()).toLocalDateTime());

            playersTable.add(theme.label(name + " ")).expandX();
            playersTable.add(theme.label(lastSeenFormatted + " ")).expandX();
            playersTable.row();
        }

        WButton joinServerButton = add(theme.button("Join this Server")).expandX().widget();
        joinServerButton.action = ()
            -> ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(), new ServerAddress(hap.getHost(), hap.getPortOrDefault(25565)), new ServerInfo("a", hap.toString(), ServerInfo.ServerType.OTHER), false, null);
    }
}
