package studio.vy.TeleportSystem;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class PlayerHeadTexture {
    private final UUID playerUuid;

    public PlayerHeadTexture(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public String getTexture() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) return null;

        PlayerListEntry playerEntry = client.getNetworkHandler().getPlayerListEntry(playerUuid);
        if (playerEntry == null) return null;

        GameProfile profile = playerEntry.getProfile();
        Optional<Property> texturesProperty = profile.getProperties().get("textures").stream().findFirst();

        if (texturesProperty.isPresent()) {
            String textureValue = texturesProperty.get().value();
            String json = new String(Base64.getDecoder().decode(textureValue));
            return extractTextureURL(json);
        }
        return null;
    }

    private static String extractTextureURL(String json) {
        int index = json.indexOf("url\":\"") + 6;
        int endIndex = json.indexOf("\"", index);
        return json.substring(index, endIndex);
    }
}