package fi.apetiogi.reserverseeker.gui;

import fi.apetiogi.reserverseeker.SmallHttp;
import fi.apetiogi.reserverseeker.ssapi.requests.ServerInfoRequest;
import fi.apetiogi.reserverseeker.ssapi.responses.ServerInfoResponse;
import fi.apetiogi.reserverseeker.utils.MultiplayerScreenUtil;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import static fi.apetiogi.reserverseeker.ReServerSeeker.api_players;
import static fi.apetiogi.reserverseeker.ReServerSeeker.api_servers;
import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GetInfoScreen extends WindowScreen {
    MultiplayerServerListWidget.Entry entry;

    public GetInfoScreen(MultiplayerScreen multiplayerScreen, MultiplayerServerListWidget.Entry entry) {
        super(GuiThemes.get(), "Get players");
        this.parent = multiplayerScreen;
        this.entry = entry;
    }

    @Override
    public void initWidgets() {
        if (entry == null) {
            add(theme.label("No server selected"));
            return;
        }

        // Get info about the server
        if (!(entry instanceof MultiplayerServerListWidget.ServerEntry)) {
            add(theme.label("No server selected"));
            return;
        }
        ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry) entry).getServer();
        String address = serverInfo.address;

        // Check if the server matches the regex for ip(:port)
        if (!address.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?::[0-9]{1,5})?$")) {
            add(theme.label("You can only get player info for servers with an IP address"));
            return;
        }
        String ip = address.split(":")[0];
        int port = address.split(":").length > 1 ? Integer.parseInt(address.split(":")[1]) : 25565;

        Long ipLong = MultiplayerScreenUtil.ipToLong(ip);

        String jsonResp = SmallHttp.get(api_servers + "&ip=" + ipLong + "&port=" + port);
        
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
            add(theme.label("Couldn't find the server.")).expandX();
            return;
        }

        ServerInfoResponse resp = responseList.get(0);

        // Set error message if there is one
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
        List<ServerInfoResponse.Player> players = resp.playerInfo;
        if (players.isEmpty()) {
            clear();
            add(theme.label("No records of players found.")).expandX();
            return;
        }
        /* "players": [ // An array of when which players were seen on the server. Limited to 1000
            {
              "last_seen": 1683790506, // The last time the player was seen on the server (unix timestamp)
              "name": "DAMcraft", // The name of the player
              "uuid": "68af4d98-24a2-41b6-96bc-a9c2ef9b397b" // The uuid of the player
            }, ...
          ] */
        boolean cracked = false;
        if (resp.cracked != null) {
            cracked = resp.cracked;

            if (!cracked) {
            add(theme.label("Attention: The server is NOT cracked!")).expandX();
            add(theme.label("")).expandX();
            }
            else {
            add(theme.label("Attention: Cracked status UNKNOWN.")).expandX();
            add(theme.label("")).expandX();
            }
        }
        
        String playersLabel = players.size() == 1 ? " player:" : " players:";
        add(theme.label("Found " + players.size() + playersLabel));

        WTable table = add(theme.table()).widget();

        table.add(theme.label("Name "));
        table.add(theme.label("Last seen "));
        table.add(theme.label("Login (cracked)"));
        table.row();

        table.add(theme.horizontalSeparator()).expandX();
        table.row();

        for (ServerInfoResponse.Player player : players) {
            String name = player.name;
            long lastSeen = player.lastSession;
            String lastSeenFormatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(Instant.ofEpochSecond(lastSeen).atZone(ZoneId.systemDefault()).toLocalDateTime());

            table.add(theme.label(name + " "));
            table.add(theme.label(lastSeenFormatted + " "));

            if (mc.getSession().getUsername().equals(name)) {
                table.add(theme.label("Logged in")).expandCellX();
            } else {

                WButton loginButton = table.add(theme.button("Login")).widget();
                // Check if the user is currently logged in
                if (mc.getSession().getUsername().equals(name)) {
                    loginButton.visible = false;
                }

                // Log in the user
                loginButton.action = () -> {
                    loginButton.visible = false;
                    if (this.client == null) return;
                    // Check if the account already exists
                    boolean exists = false;
                    for (Account<?> account : Accounts.get()) {
                        if (account instanceof CrackedAccount && account.getUsername().equals(name)) {
                            account.login();
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        CrackedAccount account = new CrackedAccount(name);
                        account.login();
                        Accounts.get().add(account);
                    }
                    close();
                };
            }
            table.row();
        }
    }
}
