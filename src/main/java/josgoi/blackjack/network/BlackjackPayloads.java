package josgoi.blackjack.network;

import josgoi.blackjack.game.BlackjackGame;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Todos los paquetes custom del mod, siguiendo el sistema de CustomPayload
 * de Fabric 1.20.5+ / 1.21.
 */
public class BlackjackPayloads {

    private static Identifier id(String path) {
        return Identifier.of("blackjack", path);
    }

    // ---------- C2S: el cliente le pide una accion al servidor ----------

    /** El jugador aposto "amount" emeralds y pide que se reparta. */
    public record BetPayload(int amount) implements CustomPayload {
        public static final Id<BetPayload> ID = new Id<>(id("bet"));
        public static final PacketCodec<RegistryByteBuf, BetPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.VAR_INT, BetPayload::amount, BetPayload::new);

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** El jugador pide carta. */
    public record HitPayload() implements CustomPayload {
        public static final Id<HitPayload> ID = new Id<>(id("hit"));
        public static final PacketCodec<RegistryByteBuf, HitPayload> CODEC =
                PacketCodec.unit(new HitPayload());

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    /** El jugador se planta. */
    public record StandPayload() implements CustomPayload {
        public static final Id<StandPayload> ID = new Id<>(id("stand"));
        public static final PacketCodec<RegistryByteBuf, StandPayload> CODEC =
                PacketCodec.unit(new StandPayload());

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    // ---------- S2C: el servidor le manda el estado actualizado al cliente ----------

    /**
     * Snapshot completo del estado de la mesa. Se manda cada vez que algo cambia
     * (repartir, pedir carta, plantarse, terminar ronda) para que el cliente
     * redibuje el GUI y las cartas en el mundo.
     *
     * Mandamos las cartas ya "serializadas" como strings simples (ej: "ACE_HEARTS")
     * para no tener que escribir un PacketCodec para el record Card directamente.
     */
    public record GameStatePayload(
            String phase,          // nombre del enum BlackjackGame.Phase
            String lastResult,     // nombre del enum BlackjackGame.Result
            int currentBet,
            int playerValue,
            int dealerValue,
            java.util.List<String> playerCards,
            java.util.List<String> dealerCards
    ) implements CustomPayload {
        public static final Id<GameStatePayload> ID = new Id<>(id("game_state"));
        public static final PacketCodec<RegistryByteBuf, GameStatePayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    PacketCodecs.STRING.encode(buf, value.phase());
                    PacketCodecs.STRING.encode(buf, value.lastResult());
                    PacketCodecs.VAR_INT.encode(buf, value.currentBet());
                    PacketCodecs.VAR_INT.encode(buf, value.playerValue());
                    PacketCodecs.VAR_INT.encode(buf, value.dealerValue());
                    PacketCodecs.STRING.collect(PacketCodecs.toList()).encode(buf, value.playerCards());
                    PacketCodecs.STRING.collect(PacketCodecs.toList()).encode(buf, value.dealerCards());
                },
                (buf) -> new GameStatePayload(
                        PacketCodecs.STRING.decode(buf),
                        PacketCodecs.STRING.decode(buf),
                        PacketCodecs.VAR_INT.decode(buf),
                        PacketCodecs.VAR_INT.decode(buf),
                        PacketCodecs.VAR_INT.decode(buf),
                        PacketCodecs.STRING.collect(PacketCodecs.toList()).decode(buf),
                        PacketCodecs.STRING.collect(PacketCodecs.toList()).decode(buf)
                )
        );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }

        /** Helper para construir el payload a partir de una BlackjackGame real. */
        public static GameStatePayload from(BlackjackGame game) {
            java.util.List<josgoi.blackjack.game.Card> dealerHand = game.dealerHand();
            boolean hideHoleCard = game.phase() == BlackjackGame.Phase.PLAYER_TURN && dealerHand.size() >= 2;

            java.util.List<String> dealerCardIds;
            int dealerValue;
            if (hideHoleCard) {
                // Solo mostramos la primera carta del dealer; la segunda queda
                // "boca abajo" hasta que el jugador se plante o se pase.
                josgoi.blackjack.game.Card visible = dealerHand.getFirst();
                dealerCardIds = java.util.List.of(visible.textureId(), "HIDDEN");
                dealerValue = BlackjackGame.handValue(java.util.List.of(visible));
            } else {
                dealerCardIds = dealerHand.stream().map(josgoi.blackjack.game.Card::textureId).toList();
                dealerValue = game.dealerValue();
            }

            return new GameStatePayload(
                    game.phase().name(),
                    game.lastResult().name(),
                    game.currentBet(),
                    game.playerValue(),
                    dealerValue,
                    game.playerHand().stream().map(josgoi.blackjack.game.Card::textureId).toList(),
                    dealerCardIds
            );
        }
    }
}
