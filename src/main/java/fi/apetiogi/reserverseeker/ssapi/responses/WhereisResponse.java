package fi.apetiogi.reserverseeker.ssapi.responses;

import java.util.List;

public class WhereisResponse {
    public String name;

    public static class Record {
        public String name;
        public long ip;
        public int port;
        public String uuid;
        public Integer lastSeen;
    }

    public List<Record> data;
}
