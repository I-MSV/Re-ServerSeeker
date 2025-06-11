package fi.apetiogi.reserverseeker.ssapi.requests;

import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;

public class ServerInfoRequest {
    private String ip;
    private Integer port;

    public void setIpPort(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public String json() {
        return gson.toJson(this);
    }
}
