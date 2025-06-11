package fi.apetiogi.reserverseeker.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.apetiogi.reserverseeker.modules.MoreButtons;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {
    @Unique
    private ButtonWidget deleteButton;

    @Unique
    private static ServerInfo targetServer = null;
    
    protected ConnectScreenMixin() {
        super(null);
    }

    @Inject(method = "connect", at = @At("TAIL")) 
    private static void onConnect(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, boolean quickPlay, @Nullable CookieStorage cookieStorage,
    CallbackInfo callbackInfo) {
        targetServer = info;
    }



    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        MoreButtons module = Modules.get().get(MoreButtons.class);
        if (!module.isActive() || !module.enableCancelOverride.get()) return;

        ConnectScreenAccessor access = (ConnectScreenAccessor)(Object)this;

        // Assume connect screen only has one button
        ButtonWidget cancelButton = (ButtonWidget)this.children().get(0);
        ButtonWidget overrideButton = new ButtonWidget.Builder(
            Text.literal("Force disconnect"),
            onPress -> {
                access.setConnectingCancelled(true);
                Text title_text = Text.literal("Force disconnected");
                // String disconnected_from = String.format("Disconnected from: %s", targetServer.name);
                // String ip_address = String.format("Ip: %s", targetServer.address);
                // String server_status = "Server status: offline";
                // if (targetServer.players != null) {
                //     server_status = String.format("Server status: %d/%d", targetServer.players.online(), targetServer.players.max());
                // }

                // Text reason_text = Text.literal(String.format("%s\n%s\n%s", disconnected_from, ip_address, server_status));
                Text reason_text = Text.literal("");
                
                // dont rly like how it looks, and it doesnt provide you with any actual information
                this.client.setScreen(new DisconnectedScreen(new MultiplayerScreen(null), title_text, reason_text));
            }
        )
        .position(cancelButton.getX(), cancelButton.getY())
        .size(cancelButton.getWidth(), cancelButton.getHeight())
        .build();

        this.remove(cancelButton);
        this.addDrawableChild(overrideButton);
    }
}
