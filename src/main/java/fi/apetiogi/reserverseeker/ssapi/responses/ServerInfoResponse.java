package fi.apetiogi.reserverseeker.ssapi.responses;

import java.util.List;

public class ServerInfoResponse {
    public String error;

    public Boolean cracked;
    public Boolean whitelisted;
    public String description;
    public Integer lastSeen;
    
    public static class Version {
        public String name;
        public Integer protocol;
    }
    
    public static class Player {
        public String name;
        public String id;
        public Long lastSession;
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
    
    public boolean isError() {
        return error != null;
    }
}
