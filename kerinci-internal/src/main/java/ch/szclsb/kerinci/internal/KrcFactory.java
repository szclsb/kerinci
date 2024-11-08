package ch.szclsb.kerinci.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;

import static ch.szclsb.kerinci.internal.Utils.printAddress;

public class KrcFactory {
    private static final Logger logger = LoggerFactory.getLogger(KrcFactory.class);

    public static <T extends AbstractKrcHandle2> T createHandle(KrcDevice device, AbstractCreateInfo<T> createInfo) {
        try (var arena = Arena.ofConfined()) {
            var pCreateInfo = createInfo.allocateCreateInfo(arena::allocate);
            createInfo.writeCreateInfo(pCreateInfo);
            var pHandle = arena.allocate(createInfo.layout());
            if (!createInfo.create(device, pCreateInfo, pHandle)) {
                throw new RuntimeException(STR."Failed to create \{createInfo.gettClass().getSimpleName()}");
            }
            var vkHandle = pHandle.get(createInfo.layout(), 0);
            logger.debug(STR."Created \{createInfo.gettClass().getSimpleName()} at \{printAddress(vkHandle)}");
            Runnable destructor = () -> {
                createInfo.destroy(device, vkHandle);
                logger.debug(STR."Destroyed \{createInfo.gettClass().getSimpleName()} at \{printAddress(vkHandle)}");
            };
            return createInfo.getConstructor().apply(device, vkHandle, destructor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
