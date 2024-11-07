package ch.szclsb.kerinci.internal.extent;

public record KrcExtent3D(
        int width,
        int height,
        int depth
) {
    public static KrcExtent3D cap(KrcExtent3D current, KrcExtent3D min, KrcExtent3D max) {
        return new KrcExtent3D(
                Integer.max(min.width, Integer.min(max.width, current.width)),
                Integer.max(min.height, Integer.min(max.height, current.height)),
                Integer.max(min.depth, Integer.min(max.depth, current.depth))
        );
    }
}
