package fi.apetiogi.reserverseeker.modules;

import fi.apetiogi.reserverseeker.ReServerSeeker;
import meteordevelopment.meteorclient.systems.modules.Module;

public class KeepServerListScroll extends Module {
    // SettingGroup sgGeneral = settings.getDefaultGroup();

    // public Setting<String> spoofedAddress = sgGeneral.add(new StringSetting.Builder()
    //     .name("custom-message")
    //     .description("")
    //     .defaultValue("")
    //     .build()
    // );

    public static double server_scroll;

    public KeepServerListScroll() {
        super(ReServerSeeker.CATEGORY, "KeepServerScroll", 
        "Will try to keep your server list scroll position during your game session");
    }
}