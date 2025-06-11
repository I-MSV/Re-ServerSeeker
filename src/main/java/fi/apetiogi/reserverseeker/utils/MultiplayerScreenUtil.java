package fi.apetiogi.reserverseeker.utils;

import java.util.Arrays;

import fi.apetiogi.reserverseeker.mixin.MultiplayerScreenAccessor;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

public class MultiplayerScreenUtil {

    public static void addInfoToServerList(MultiplayerScreen mps, ServerInfo info) {
        MultiplayerScreenAccessor mpsAccessor = (MultiplayerScreenAccessor) mps;
        mps.getServerList().add(info, false);
        mps.getServerList().saveFile();
        mpsAccessor.getServerListWidget().setServers(mps.getServerList());
    }
    public static void addInfoToServerList(MultiplayerScreen mps, ServerInfo info, boolean reload) {
        MultiplayerScreenAccessor mpsAccessor = (MultiplayerScreenAccessor) mps;
        mps.getServerList().add(info, false);
        if (reload) mpsAccessor.getServerListWidget().setServers(mps.getServerList());
    }

    public static void addNameIpToServerList(MultiplayerScreen mps, String name, String ip) {
        MultiplayerScreenAccessor mpsAccessor = (MultiplayerScreenAccessor) mps;
        ServerInfo info = new ServerInfo(name, ip, ServerInfo.ServerType.OTHER);
        mps.getServerList().add(info, false);
        mpsAccessor.getServerListWidget().setServers(mps.getServerList());
        mps.getServerList().saveFile();
    }
    public static void addNameIpToServerList(MultiplayerScreen mps, String name, String ip, boolean reload) {
        MultiplayerScreenAccessor mpsAccessor = (MultiplayerScreenAccessor) mps;
        ServerInfo info = new ServerInfo(name, ip, ServerInfo.ServerType.OTHER);
        mps.getServerList().add(info, false);
        if (reload) mpsAccessor.getServerListWidget().setServers(mps.getServerList());
    }

    public static void reloadServerList(MultiplayerScreen mps) {
        MultiplayerScreenAccessor mpsAccessor = (MultiplayerScreenAccessor) mps;
        mpsAccessor.getServerListWidget().setServers(mps.getServerList());
    }

    public static void reloadServerList(MultiplayerScreen mps, ServerList list) {
        MultiplayerScreenAccessor mpsAccessor = (MultiplayerScreenAccessor) mps;
        mpsAccessor.getServerListWidget().setServers(list);
    }

    public static void saveList(MultiplayerScreen mps) {
        mps.getServerList().saveFile();
    }
    
    public static String cleanIp(long ip, int port) {
        String cleanIp = ((ip >> 24) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                (ip & 0xFF);

        if (port != 25565) {
            cleanIp += ":" + port;
        }

        return cleanIp;
    }

    public static Long ipToLong(String stringIp) {
            return Arrays.stream(stringIp.split("\\."))
                    .mapToLong(Long::parseLong)
                    .reduce(0,
                            (a, b) -> (a << 8) + b);
    }
}
