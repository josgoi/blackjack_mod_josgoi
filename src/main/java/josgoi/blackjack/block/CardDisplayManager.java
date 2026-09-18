package josgoi.blackjack.block;

import josgoi.blackjack.game.Card;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * PENDIENTE / DESACTIVADO POR AHORA.
 *
 * La idea original era mostrar las cartas fisicamente sobre la mesa usando
 * ItemDisplayEntity (las entidades de "display" nativas de Minecraft desde
 * 1.19.4). El problema: el metodo para asignarle el item a mostrar
 * (setItemStack en Yarn) es PRIVADO en la clase real del juego, no se puede
 * llamar directo desde afuera como hicimos con el resto del codigo.
 *
 * Para lograrlo de verdad hay 2 caminos, ninguno trivial:
 *   1) Construir un NbtCompound a mano con el formato exacto que usa
 *      Minecraft para /summon minecraft:item_display (incluye el item
 *      serializado con sus data components) y pasarselo a
 *      display.readNbt(nbt) - el metodo NBT si es publico.
 *   2) Usar Mixin para "abrir" el metodo privado (requiere el mixin skill/
 *      plugin de Fabric, mas setup).
 *
 * Por ahora el juego funciona perfecto solo con el GUI (que ya muestra las
 * manos clarito). Si despues queres que retome esto para tener las cartas
 * visibles en el mundo, decime y lo hacemos con el proyecto ya compilando y
 * corriendo delante, para poder probar la NBT exacta en el juego mismo en
 * vez de adivinarla a ciegas.
 */
public class CardDisplayManager {

    public void updateDisplay(ServerWorld world, BlockPos tablePos, List<Card> playerHand, List<Card> dealerHand) {
        // No-op por ahora - ver comentario de la clase.
    }

    public void clear(ServerWorld world) {
        // No-op por ahora - ver comentario de la clase.
    }
}
