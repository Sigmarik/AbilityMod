package net.sigmarik.abilitymod.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.sigmarik.abilitymod.AbilityMod;

import java.util.HashMap;
import java.util.UUID;

public class ServerState extends PersistentState {
    public HashMap<UUID, NbtCompound> traits = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put(AbilityMod.MOD_ID + ":player_traits", new NbtCompound());

        NbtCompound storage = nbt.getCompound(AbilityMod.MOD_ID + ":player_traits");

        for (UUID playerUUID : traits.keySet()) {
            storage.put(String.valueOf(playerUUID), traits.get(playerUUID));
        }

        return nbt;
    }

    public static ServerState createFromNbt(NbtCompound nbt) {
        ServerState states = new ServerState();
        states.traits.clear();

        NbtCompound player_data = nbt.getCompound(AbilityMod.MOD_ID + ":player_traits");

        for (String key : player_data.getKeys()) {
            states.traits.put(UUID.fromString(key), player_data.getCompound(key));
        }

        return states;
    }

    public NbtCompound getTraitList(UUID playerId) {
        if (!traits.containsKey(playerId)) return new NbtCompound();

        return traits.get(playerId);
    }

    public void addTrait(UUID playerId, String trait) {
        if (!traits.containsKey(playerId)) {
            traits.put(playerId, new NbtCompound());
        }

        traits.get(playerId).putBoolean(trait, true);
    }

    public void removeTrait(UUID playerId, String trait) {
        if (!traits.containsKey(playerId)) return;

        if (traits.get(playerId).contains(trait))
            traits.get(playerId).remove(trait);
    }

    public void setSilenced(UUID playerId, String trait, boolean silenced) {
        if (!traits.containsKey(playerId)) return;

        if (traits.get(playerId).contains(trait)) {
            traits.get(playerId).putBoolean(trait, silenced);
        }
    }

    public static ServerState getTraitStates(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();

        return manager.getOrCreate(ServerState::createFromNbt, ServerState::new, AbilityMod.MOD_ID);
    }

    public static boolean hasTrait(PlayerEntity player, String trait) {
        if (player.getServer() == null) return false;

        ServerState states = getTraitStates(player.getServer());

        if (!states.traits.containsKey(player.getUuid())) return false;
        NbtCompound playerTraitsNbt = states.traits.get(player.getUuid());
        if (!playerTraitsNbt.contains(trait)) return false;
        return playerTraitsNbt.getBoolean(trait);
    }
}
