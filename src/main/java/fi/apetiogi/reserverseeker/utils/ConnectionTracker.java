package fi.apetiogi.reserverseeker.utils;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import meteordevelopment.meteorclient.events.world.ServerConnectBeginEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ConnectionTracker {
    public static Pair<ServerAddress, ServerInfo> lastServerConnection;

    @EventHandler
    private void onGameJoined(ServerConnectBeginEvent event) {
        lastServerConnection = new ObjectObjectImmutablePair<>(event.address, event.info);
    }
}
