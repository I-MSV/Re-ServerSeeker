package fi.apetiogi.reserverseeker.modules;

import fi.apetiogi.reserverseeker.ReServerSeeker;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class CustomServerDescription extends Module {
    SettingGroup sgGeneral = settings.getDefaultGroup();

    public Setting<Boolean> includeOriginal = sgGeneral.add(new BoolSetting.Builder()
        .name("include-original-description")
        .description("Whether to also include the actual server description")
        .defaultValue(true)
        .build()
    );

    public Setting<Boolean> enableLastJoined = sgGeneral.add(new BoolSetting.Builder()
    .name("enable-last-joined-message")
    .description("Finds your last joined server and adds message to the description")
    .defaultValue(false)
    .build()
    );

    public Setting<SettingColor> customLastJoinedColor = sgGeneral.add(new ColorSetting.Builder()
        .name("message-color")
        .defaultValue(Color.MAGENTA)
        .visible(() -> enableLastJoined.get())
        .build()
    );

    public Setting<String> lastJoinedMessage = sgGeneral.add(new StringSetting.Builder()
        .name("last-joined-message")
        .defaultValue("^ Last joined server ^")
        .wide()
        .visible(() -> enableLastJoined.get())
        .build()
    );
    
    public Setting<Boolean> enableCustomMessage = sgGeneral.add(new BoolSetting.Builder()
    .name("enable-custom-message")
    .description("Adds your own message into every description")
    .defaultValue(false)
    .build()
    );

    public Setting<SettingColor> customMessageColor = sgGeneral.add(new ColorSetting.Builder()
        .name("custom-message-color")
        .defaultValue(Color.WHITE)
        .visible(() -> enableCustomMessage.get())
        .build()
    );

    public Setting<String> customDescriptionMessage = sgGeneral.add(new StringSetting.Builder()
        .name("custom-message")
        .description("Your custom description message")
        .defaultValue("My custom message!")
        .wide()
        .visible(() -> enableCustomMessage.get())
        .build()
    );

    public CustomServerDescription() {
        super(ReServerSeeker.CATEGORY, "CustomServerDescription", 
        "Change the way server descriptions look.");
    }
}
