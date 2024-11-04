package ch.szclsb.kerinci.internal.extent;

public record KrcExtent2D(
        int width,
        int height
) {
    public static KrcExtent2D cap(KrcExtent2D current, KrcExtent2D min, KrcExtent2D max) {
        return new KrcExtent2D(
                Integer.max(min.width, Integer.min(max.width, current.width)),
                Integer.max(min.height, Integer.min(max.height, current.height))
        );
    }
}
