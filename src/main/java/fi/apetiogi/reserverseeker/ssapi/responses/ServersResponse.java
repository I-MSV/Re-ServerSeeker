package fi.apetiogi.reserverseeker.ssapi.responses;

import java.util.List;

import fi.apetiogi.reserverseeker.ssapi.responses.ServerInfoResponse.Player;

public class ServersResponse {
    public String error;

    public static class Server {
        public Long ip;
        public int port;
        public Boolean whitelisted;
        public Boolean cracked;
        public String description;
        public Long lastSeen;

        public static class Version {
            public String name;
            public Integer protocol;
        }

        public static class Players {
            public Integer max;
            public Integer online;
            public Boolean hasPlayerSample;
            public List<Player> players;
        }
    
        public Version version;
        public Players players;
        public List<Player> playerInfo;
    }

    public List<Server> data;

    public boolean isError() {
        return error != null;
    }
}
