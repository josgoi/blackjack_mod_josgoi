package josgoi.blackjack.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Logica de una mesa de blackjack: 1 jugador vs la casa (dealer).
 * Esta clase no sabe nada de Minecraft: la BlockEntity la usa y
 * traduce los cambios de estado en paquetes de red / entidades visuales.
 */
public class BlackjackGame {

    public enum Phase {
        WAITING_FOR_BET,   // mesa libre, esperando que alguien apueste y pida repartir
        PLAYER_TURN,       // jugador puede pedir carta o plantarse
        DEALER_TURN,       // el dealer esta jugando su mano (automatico)
        ROUND_OVER         // se muestra el resultado hasta que alguien empiece otra ronda
    }

    public enum Result {
        NONE, PLAYER_BLACKJACK, PLAYER_WIN, DEALER_WIN, PUSH, PLAYER_BUST, DEALER_BUST
    }

    private final Deck deck;
    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> dealerHand = new ArrayList<>();

    private Phase phase = Phase.WAITING_FOR_BET;
    private Result lastResult = Result.NONE;
    private int currentBet = 0;

    public BlackjackGame() {
        this.deck = new Deck(new Random());
    }

    // ----- Flujo del juego -----

    /** Arranca una ronda nueva. bet ya se valido/desconto del inventario antes de llamar esto. */
    public void startRound(int bet) {
        deck.reset();
        playerHand.clear();
        dealerHand.clear();
        currentBet = bet;
        lastResult = Result.NONE;

        playerHand.add(deck.draw());
        dealerHand.add(deck.draw());
        playerHand.add(deck.draw());
        dealerHand.add(deck.draw()); // la segunda del dealer se muestra boca abajo en el cliente

        if (handValue(playerHand) == 21) {
            // Blackjack natural: se resuelve enseguida
            phase = Phase.DEALER_TURN;
            while (dealerHitStep()) { /* nada */ }
            finishDealerTurn();
        } else {
            phase = Phase.PLAYER_TURN;
        }
    }

    public void hit() {
        if (phase != Phase.PLAYER_TURN) return;
        playerHand.add(deck.draw());
        if (handValue(playerHand) > 21) {
            lastResult = Result.PLAYER_BUST;
            phase = Phase.ROUND_OVER;
        }
    }

    public void stand() {
        if (phase != Phase.PLAYER_TURN) return;
        phase = Phase.DEALER_TURN;
    }

    /**
     * Le hace pedir UNA carta al dealer si todavia no llego a 17.
     * Devuelve true si pidio carta (hay que seguir llamando esto), false si
     * ya se plantó y toca llamar a finishDealerTurn().
     */
    public boolean dealerHitStep() {
        if (handValue(dealerHand) < 17) {
            dealerHand.add(deck.draw());
            return true;
        }
        return false;
    }

    /** Cierra la ronda una vez que el dealer ya terminó de pedir cartas. */
    public void finishDealerTurn() {
        determineResult();
        phase = Phase.ROUND_OVER;
    }

    private void determineResult() {
        int player = handValue(playerHand);
        int dealer = handValue(dealerHand);

        if (player == 21 && playerHand.size() == 2) {
            lastResult = Result.PLAYER_BLACKJACK;
        } else if (dealer > 21) {
            lastResult = Result.DEALER_BUST;
        } else if (player > dealer) {
            lastResult = Result.PLAYER_WIN;
        } else if (player < dealer) {
            lastResult = Result.DEALER_WIN;
        } else {
            lastResult = Result.PUSH;
        }
    }

    /** Vuelve la mesa a un estado limpio para que se pueda apostar de nuevo. */
    public void resetToWaiting() {
        phase = Phase.WAITING_FOR_BET;
        lastResult = Result.NONE;
        currentBet = 0;
        playerHand.clear();
        dealerHand.clear();
    }

    /**
     * Devuelve cuantos emeralds hay que pagarle al jugador segun el resultado.
     * 0 = pierde la apuesta, currentBet = empate (se la devuelve), etc.
     * Blackjack natural paga 3:2.
     */
    public int payout() {
        return switch (lastResult) {
            case PLAYER_BLACKJACK -> (int) Math.round(currentBet * 2.5); // apuesta + 3:2 de ganancia
            case PLAYER_WIN, DEALER_BUST -> currentBet * 2;              // apuesta + ganancia 1:1
            case PUSH -> currentBet;                                     // se devuelve la apuesta
            case DEALER_WIN, PLAYER_BUST -> 0;                           // pierde todo
            case NONE -> 0;
        };
    }

    // ----- Valor de mano (con manejo de Ases 1 u 11) -----

    public static int handValue(List<Card> hand) {
        int total = 0;
        int aces = 0;
        for (Card card : hand) {
            total += card.rank().baseValue;
            if (card.isAce()) aces++;
        }
        // Si nos pasamos de 21 y tenemos ases contados como 11, los bajamos a 1 de a uno.
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    // ----- Getters para sincronizar estado -----

    public Phase phase() { return phase; }
    public Result lastResult() { return lastResult; }
    public int currentBet() { return currentBet; }
    public List<Card> playerHand() { return List.copyOf(playerHand); }
    public List<Card> dealerHand() { return List.copyOf(dealerHand); }
    public int playerValue() { return handValue(playerHand); }
    public int dealerValue() { return handValue(dealerHand); }
}
