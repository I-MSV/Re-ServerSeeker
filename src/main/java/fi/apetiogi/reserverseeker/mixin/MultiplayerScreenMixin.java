package fi.apetiogi.reserverseeker.mixin;

import fi.apetiogi.reserverseeker.gui.GetInfoScreen;
import fi.apetiogi.reserverseeker.gui.ServerSeekerScreen;
import fi.apetiogi.reserverseeker.modules.KeepServerListScroll;
import fi.apetiogi.reserverseeker.utils.MultiplayerScreenUtil;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Shadow
    protected MultiplayerServerListWidget serverListWidget;

    @Unique
    private ButtonWidget getInfoButton;

    protected MultiplayerScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    private void onInit(CallbackInfo info) {

        if (Modules.get().get(KeepServerListScroll.class).isActive()) {
            serverListWidget.setScrollY(KeepServerListScroll.server_scroll);
        }

        MultiplayerScreenUtil.reloadServerList((MultiplayerScreen) (Object) this);
        // Add a button which sets the current screen to the ServerSeekerScreen
        this.addDrawableChild(
            new ButtonWidget.Builder(
                Text.literal("Re:ServerSeeker"),
                onPress -> {
                    if (this.client == null) return;
                    this.client.setScreen(new ServerSeekerScreen((MultiplayerScreen) (Object) this));
                }
            )
                .position(160, 3)
                .width(100)
                .build()
        );

        // Add a button to get the info of the selected server
        this.getInfoButton = this.addDrawableChild(
            new ButtonWidget.Builder(
                Text.literal("Get players"),
                onPress -> {
                    if (this.client == null) return;
                    MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
                    if (entry != null) {
                        if (this.client == null) return;
                        this.client.setScreen(new GetInfoScreen((MultiplayerScreen) (Object) this, entry));
                    }
                }
            )
                .position(160 + 100 + 5, 3)
                .width(80)
                .build()
        );
    }

    @Inject(method = "updateButtonActivationStates", at = @At("TAIL"))
    private void onUpdateButtonActivationStates(CallbackInfo info) {
        // Enable the button if a server is selected
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        this.getInfoButton.active = entry != null && !(entry instanceof MultiplayerServerListWidget.ScanningEntry);
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void onClose(CallbackInfo info) {
        KeepServerListScroll.server_scroll = serverListWidget.getScrollY();
    }
}