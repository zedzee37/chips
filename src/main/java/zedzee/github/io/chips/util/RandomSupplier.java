package zedzee.github.io.chips.util;

import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

// idk about this one man
public class RandomSupplier<T> implements Supplier<T> {
    private final List<T> items;
    private Random random;

    public RandomSupplier(Random random, List<T> items) {
        this.items = items;
        this.random = random;
    }

    public void add(T item) {
       items.add(item);
    }

    @Nullable
    public T get() {
        if (items.isEmpty()) {
            return null;
        }

        int index = random.nextBetween(0, items.size() - 1);
        return items.get(index);
    }
}
