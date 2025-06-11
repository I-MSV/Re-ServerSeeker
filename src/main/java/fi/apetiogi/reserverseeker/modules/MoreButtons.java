package fi.apetiogi.reserverseeker.modules;

import fi.apetiogi.reserverseeker.ReServerSeeker;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;

public class MoreButtons extends Module {
    SettingGroup sgGeneral = settings.getDefaultGroup();

    public Setting<Boolean> enableCancelOverride = sgGeneral.add(new BoolSetting.Builder()
    .name("cancel-button-override")
    .description("Pressing cancel button while joining will force disconnect instead of returning to main menu.")
    .defaultValue(false)
    .build()
    );

    public Setting<Boolean> enableDeleteServerButton = sgGeneral.add(new BoolSetting.Builder()
    .name("delete-server-button")
    .description("Adds a button inside disconnect tab to delete current server from your list")
    .defaultValue(false)
    .build()
    );

    //Pressing cancel button while joining will force disconnect instead of returning to main menu.
    public MoreButtons() {
        super(ReServerSeeker.CATEGORY, "More buttons", 
        "Adds different buttons or changes existing functionalities");
    }
}
