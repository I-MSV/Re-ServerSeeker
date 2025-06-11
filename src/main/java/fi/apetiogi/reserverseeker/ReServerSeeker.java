package fi.apetiogi.reserverseeker;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import fi.apetiogi.reserverseeker.country.Countries;
import fi.apetiogi.reserverseeker.country.Country;
import fi.apetiogi.reserverseeker.country.CountrySetting;
import fi.apetiogi.reserverseeker.hud.HistoricPlayersHud;
import fi.apetiogi.reserverseeker.modules.*;
import fi.apetiogi.reserverseeker.utils.ConnectionTracker;
import fi.apetiogi.reserverseeker.utils.HistoricPlayersUpdater;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;

import org.slf4j.Logger;

import java.util.Map;

public class ReServerSeeker extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Re:ServerSeeker", Items.SPYGLASS.getDefaultStack());
    public static final Map<String, Country> COUNTRY_MAP = new Object2ReferenceOpenHashMap<>();

    public static final String api_servers = "https://api.cornbread2100.com/servers?";
    public static final String api_players = "https://api.cornbread2100.com/playerHistory?";

    public static final Gson gson = new Gson();

    @Override
    public void onInitialize() {
        LOG.info("Loaded the Re:ServerSeeker addon!");

        // Load countries
        Countries.init();

        Modules.get().add( new CustomServerDescription() );
        Modules.get().add( new KeepServerListScroll() );
        Modules.get().add( new MoreButtons() );
        Modules.get().add( new BungeeSpoofModule() );
        Modules.get().add( new DisableVersionCheck() );
        
        Hud.get().register(HistoricPlayersHud.INFO);

        SettingsWidgetFactory.registerCustomFactory(CountrySetting.class, (theme) -> (table, setting) -> {
            CountrySetting.countrySettingW(table, (CountrySetting) setting, theme);
        });

        MeteorClient.EVENT_BUS.subscribe(new ConnectionTracker());
        MeteorClient.EVENT_BUS.subscribe(HistoricPlayersUpdater.class);
    }
    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "fi.apetiogi.reserverseeker";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("I-MSV", "Re-ServerSeeker");
    }

    @Override
    public String getWebsite() {
        return "NONE";
    }

    @Override
    public String getCommit() {
        // String commit = FabricLoader
        //     .getInstance()
        //     .getModContainer("serverseeker")
        //     .get().getMetadata()
        //     .getCustomValue("github:sha")
        //     .getAsString();
        // return commit.isEmpty() ? null : commit.trim();
        return null;
    }
}
