package net.sigmarik.abilitymod.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.sigmarik.abilitymod.AbilityMod;

import java.util.*;

public class ServerState extends PersistentState {
    public static class PlayerData {
        Set<String> traits = new HashSet<>(Collections.emptySet());
        int addictionTimer = AbilityMod.ADDICTION_START_TIMER;
    }

    public HashMap<UUID, PlayerData> data = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put(AbilityMod.MOD_ID + ":player_data", new NbtCompound());

        NbtCompound storage = nbt.getCompound(AbilityMod.MOD_ID + ":player_data");

        for (UUID playerUUID : data.keySet()) {
            NbtCompound cell = new NbtCompound();

            NbtCompound traitList = new NbtCompound();
            for (String trait : data.get(playerUUID).traits) {
                traitList.putBoolean(trait, true);
            }
            cell.put("traits", traitList);

            cell.putInt("addiction_timer", data.get(playerUUID).addictionTimer);

            storage.put(String.valueOf(playerUUID), cell);
        }

        return nbt;
    }

    public static ServerState createFromNbt(NbtCompound nbt) {
        ServerState state = new ServerState();
        state.data.clear();

        NbtCompound playerNbtList = nbt.getCompound(AbilityMod.MOD_ID + ":player_data");

        for (String playerKey : playerNbtList.getKeys()) {
            NbtCompound playerNbt = playerNbtList.getCompound(playerKey);
            PlayerData playerData = state.registerPlayer(UUID.fromString(playerKey));

            playerData.traits.addAll(playerNbt.getCompound("traits").getKeys());
            playerData.addictionTimer = playerNbt.getInt("addiction_timer");
        }

        return state;
    }

    private PlayerData registerPlayer(UUID playerId) {
        if (data.containsKey(playerId)) return data.get(playerId);

        PlayerData playerData = new PlayerData();

        data.put(playerId, playerData);

        return data.get(playerId);
    }

    public Set<String> getTraitList(UUID playerId) {
        if (!data.containsKey(playerId)) registerPlayer(playerId);

        return data.get(playerId).traits;
    }

    public void addTrait(UUID playerId, String trait) {
        if (!data.containsKey(playerId)) registerPlayer(playerId);

        data.get(playerId).traits.add(trait);
    }

    public void removeTrait(UUID playerId, String trait) {
        if (!data.containsKey(playerId)) return;

        data.get(playerId).traits.remove(trait);
    }

    public static ServerState getTraitStates(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();

        return manager.getOrCreate(ServerState::createFromNbt, ServerState::new, AbilityMod.MOD_ID);
    }

    public static boolean hasTrait(PlayerEntity player, String trait) {
        if (player.getServer() == null) return false;

        ServerState state = getTraitStates(player.getServer());

        if (!state.data.containsKey(player.getUuid())) return false;
        return state.data.get(player.getUuid()).traits.contains(trait);
    }

    public static int getAddictionTimer(PlayerEntity player) {
        if (player.getServer() == null) return AbilityMod.ADDICTION_START_TIMER;

        ServerState state = getTraitStates(player.getServer());

        if (!state.data.containsKey(player.getUuid())) state.registerPlayer(player.getUuid());

        return state.data.get(player.getUuid()).addictionTimer;
    }

    public static void tickAddictionTimer(PlayerEntity player) {
        if (player.getServer() == null) return;

        ServerState state = getTraitStates(player.getServer());

        if (!state.data.containsKey(player.getUuid())) state.registerPlayer(player.getUuid());

        if (state.data.get(player.getUuid()).addictionTimer > 0)
            --state.data.get(player.getUuid()).addictionTimer;
    }

    public static void setAddictionTimer(PlayerEntity player, int newAddictionTimer) {
        if (player.getServer() == null) return;

        ServerState state = getTraitStates(player.getServer());

        if (!state.data.containsKey(player.getUuid())) state.registerPlayer(player.getUuid());

        state.data.get(player.getUuid()).addictionTimer = newAddictionTimer;
    }
}
