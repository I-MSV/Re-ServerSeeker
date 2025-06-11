package fi.apetiogi.reserverseeker.ssapi.requests;

import static fi.apetiogi.reserverseeker.ReServerSeeker.gson;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

public class ServersRequest {
    private java.util.Map<String, String> params = new java.util.HashMap<>();

    public enum Software {
        Any,
        Bukkit,
        Spigot,
        Paper,
        Vanilla
    }
    private Software software;

    //unsupported by the API
    // public void setAsn(Integer asn) {
    //     this.asn = asn;
    // }

    public void setCountryCode(String cc) {
        params.put("country", cc.toUpperCase(Locale.ENGLISH));
    }

    public void setCracked(Boolean cracked) {
        if (cracked == null) {
            params.remove("cracked");
            return;
        }
        params.put("cracked", cracked.toString());
    }

    public void setWhitelist(Boolean whitelisted) {
    if (whitelisted == null) {
        params.remove("whitelisted");
        return;
        }
        params.put("whitelisted", whitelisted.toString());
    }

    public void sortByRecent(Boolean recent) {
        if (recent == null) {
            params.remove("sort=lastSeen&descending");
            return;
        }
        params.put("sort=lastSeen&descending", recent.toString());
    }

    public void setDescription(String description) {
        if (description.isBlank()) {
            params.remove("description");
            return;
        }
        //encode because this usually has a lot of spaces
        params.put("description", URLEncoder.encode(description, StandardCharsets.UTF_8));
    }

    public void setMaxPlayers(Integer exact) {
        if (exact == 0) {
            params.remove("playerLimit");
            return;
        }
        params.put("playerLimit", exact.toString());
    }

    public void setOnlinePlayers(Integer exact) {
        params.put("minPlayers", exact.toString());
    }

    public void setOnlinePlayers(Integer min, Integer max) {
        params.put("minPlayers", min.toString());
        params.put("maxPlayers", max.toString());
    }

    public void setProtocolVersion(Integer version) {
        params.put("protocol", version.toString());
    }

    //unsupported by the API
    // public void setSoftware(Software software) {
    //     this.software = software;
    // }

    public void setVanillaOnly(Boolean vanilla) {
        params.put("vanilla", vanilla.toString());
    }

    //unsupported by the API
    // public void setOnlyBungeeSpoofable(Boolean only) {
    //     this.only_bungeespoofable = only;
    // }

    public String get_params() {
        StringJoiner joiner = new StringJoiner("&");

        for (Map.Entry<String,String> entry : params.entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }

        return joiner.toString();
    }

    public String json() {
        return gson.toJson(this);
    }
}
