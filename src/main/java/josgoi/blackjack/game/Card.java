package josgoi.blackjack.game;

/**
 * Representa una carta individual. No depende de nada de Minecraft,
 * asi que es facil de testear por separado.
 */
public record Card(Suit suit, Rank rank) {

    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    public enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8),
        NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10), ACE(11);

        public final int baseValue;

        Rank(int baseValue) {
            this.baseValue = baseValue;
        }
    }

    public boolean isAce() {
        return rank == Rank.ACE;
    }

    /**
     * Id corto usado para elegir la textura del item que representa
     * esta carta en el mundo, ej: "ace_of_spades", "ten_of_hearts".
     */
    public String textureId() {
        return rank.name().toLowerCase() + "_of_" + suit.name().toLowerCase();
    }

    @Override
    public String toString() {
        return rank.name() + " of " + suit.name();
    }
}
