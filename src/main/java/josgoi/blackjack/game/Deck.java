package josgoi.blackjack.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class Deck {

    private final Deque<Card> cards = new ArrayDeque<>();
    private final Random random;

    public Deck(Random random) {
        this.random = random;
        reset();
    }

    /** Rearma las 52 cartas y las mezcla. Se llama al empezar cada mano nueva. */
    public void reset() {
        cards.clear();
        List<Card> fresh = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                fresh.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(fresh, random);
        cards.addAll(fresh);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            // No deberia pasar con una sola mesa, pero por las dudas rearmamos.
            reset();
        }
        return cards.poll();
    }

    public int remaining() {
        return cards.size();
    }
}
