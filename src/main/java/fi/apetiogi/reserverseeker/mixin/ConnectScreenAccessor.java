package fi.apetiogi.reserverseeker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;

@Mixin(ConnectScreen.class)
public interface ConnectScreenAccessor {
    @Accessor("connectingCancelled")
    void setConnectingCancelled(boolean value);
}
