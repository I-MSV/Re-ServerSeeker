package fi.apetiogi.reserverseeker.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.apetiogi.reserverseeker.modules.CustomServerDescription;
import fi.apetiogi.reserverseeker.modules.DisableVersionCheck;
import fi.apetiogi.reserverseeker.utils.ConnectionTracker;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.Status;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

@Mixin(net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget.ServerEntry.class)
public abstract class ServerEntryMixin {
    @Shadow
    private MinecraftClient client;
    @Shadow
    private ServerInfo server;

    //lambda from render method
    @Inject(method = "method_55816", at = @At("TAIL"))
    private void disableVersionCheck(CallbackInfo info) {

        if (Modules.get().get(DisableVersionCheck.class).isActive()) {
            server.setStatus(Status.SUCCESSFUL);
        }
    }
    
    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), index = 11)
    private List<OrderedText> customDescription(List<OrderedText> originalList, DrawContext context, int index, int y, int x, int entryWidth) {

        CustomServerDescription module = Modules.get().get(CustomServerDescription.class);
        if (!module.isActive()) return originalList;

        List<OrderedText> modifiedList = new ArrayList<>();
        Text styledText;

        if (module.enableLastJoined.get() && ConnectionTracker.lastServerConnection != null 
        && this.server.address.equals(ConnectionTracker.lastServerConnection.value().address)) {
            styledText = Text.literal(module.lastJoinedMessage.get()
            .replace("\\n", "\n"))
            .setStyle(module.customLastJoinedColor.get().toStyle());
            modifiedList.addAll(this.client.textRenderer.wrapLines(styledText, entryWidth - 32 - 2));
        }

        if (module.enableCustomMessage.get()) {
            styledText = Text.literal(module.customDescriptionMessage.get()
            .replace("\\n", "\n"))
            .setStyle(module.customMessageColor.get().toStyle());
            modifiedList.addAll(this.client.textRenderer.wrapLines(styledText, entryWidth - 32 - 2));
        }

        if (module.includeOriginal.get()) {
            modifiedList.addAll(originalList);
        }

        return modifiedList;
    }
}
