package fi.apetiogi.reserverseeker.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.apetiogi.reserverseeker.modules.MoreButtons;
import fi.apetiogi.reserverseeker.utils.ConnectionTracker;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Shadow
    @Final
    @Nullable
    private DirectionalLayoutWidget grid;
    ServerInfo targetServer = null;
    @Unique
    private ButtonWidget deleteButton;


    private ServerList currentList;

    protected DisconnectedScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V", shift = At.Shift.BEFORE))
    private void addButtons(CallbackInfo info) {
        MoreButtons module = Modules.get().get(MoreButtons.class);
        if (!module.isActive() || !module.enableDeleteServerButton.get()) return;

        //load current server list
        //this isnt the best way of doing things but its fine...
        currentList = new ServerList(client);
        currentList.loadFile();
        
        //only add the button if a server exists in the list
        String lastConnectedAddress = ConnectionTracker.lastServerConnection.value().address;
        for (int i = 0; i < currentList.size(); i++) {
            ServerInfo server = currentList.get(i);
            if (server.address.equals(lastConnectedAddress)) {
                targetServer = server;
                break;
            }
        }
        
        if (targetServer == null) return;
        if (grid == null) return;

        //add a button which deletes last attempted server
        this.deleteButton = grid.add(
            new ButtonWidget.Builder(
                Text.literal("Delete server"),
                onPress -> {
                    if (this.client == null) return;
                    currentList.remove(targetServer);
                    currentList.saveFile();

                    this.client.setScreen(new MultiplayerScreen(null));
                }
            )
            .build()
        );
    }
}
