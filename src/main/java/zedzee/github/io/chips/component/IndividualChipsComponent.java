package zedzee.github.io.chips.component;

import com.mojang.serialization.Codec;

public class IndividualChipsComponent {
    public static Codec<IndividualChipsComponent> CODEC = Codec.unit(IndividualChipsComponent::new);
}
