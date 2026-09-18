package josgoi.blackjack.registry;

import josgoi.blackjack.game.Card;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Un item "coleccionable" por cada una de las 52 cartas, mas uno extra
 * para el dorso (la carta oculta del dealer). No son items que el jugador
 * vaya a tener en el inventario para usar - son solo para poder dibujar su
 * icono en el GUI (y en el futuro, en el mundo).
 */
public class ModItems {

    /** id de textura ("ace_of_spades", "HIDDEN", etc) -> Item registrado. */
    private static final Map<String, Item> CARD_ITEMS = new HashMap<>();

    public static void register() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                String textureId = rank.name().toLowerCase() + "_of_" + suit.name().toLowerCase();
                registerCardItem(textureId);
            }
        }
        registerCardItem("back"); // dorso de la carta oculta
    }

    private static void registerCardItem(String textureId) {
        Identifier id = Identifier.of("blackjack", "card_" + textureId);
        Item item = Registry.register(Registries.ITEM, id, new Item(new Item.Settings()));
        CARD_ITEMS.put(textureId, item);
    }

    /**
     * Busca el item que representa una carta a partir del id que viaja en
     * el paquete de red (ej: "ace_of_spades"). Si llega "HIDDEN" (la carta
     * tapada del dealer), devuelve el item del dorso.
     */
    public static Item getCardItem(String networkTextureId) {
        if ("HIDDEN".equals(networkTextureId)) {
            return CARD_ITEMS.get("back");
        }
        return CARD_ITEMS.get(networkTextureId);
    }
}
