package fi.apetiogi.reserverseeker.modules;

import fi.apetiogi.reserverseeker.ReServerSeeker;
import meteordevelopment.meteorclient.systems.modules.Module;

public class DisableVersionCheck extends Module {

    public DisableVersionCheck() {
        super(ReServerSeeker.CATEGORY, "DisableVersionCheck", 
        "Always returns successful on version check (Useful when using Auto Detect in ViaFabric+)");
    }
}