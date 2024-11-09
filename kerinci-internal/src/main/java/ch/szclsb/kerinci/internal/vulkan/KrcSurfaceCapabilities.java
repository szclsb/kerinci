package ch.szclsb.kerinci.internal.vulkan;

import ch.szclsb.kerinci.api.VkSurfaceCapabilitiesKHR;
import ch.szclsb.kerinci.internal.Allocator;

import java.lang.foreign.MemorySegment;

public class KrcSurfaceCapabilities extends AbstractStruct {
    private int minImageCount;
    private int maxImageCount;
    private KrcExtent2D currentExtent = new KrcExtent2D();
    private KrcExtent2D minImageExtent = new KrcExtent2D();
    private KrcExtent2D maxImageExtent = new KrcExtent2D();
    private int maxImageArrayLayers;
    private int supportedTransforms; // todo
    private int currentTransform; // todo
    private int supportedCompositeAlpha; // todo
    private int supportedUsageFlags; // todo

    public KrcSurfaceCapabilities() {
    }

    public int getMinImageCount() {
        return minImageCount;
    }

    public int getMaxImageCount() {
        return maxImageCount;
    }

    public KrcExtent2D getCurrentExtent() {
        return currentExtent;
    }

    public KrcExtent2D getMinImageExtent() {
        return minImageExtent;
    }

    public KrcExtent2D getMaxImageExtent() {
        return maxImageExtent;
    }

    public int getMaxImageArrayLayers() {
        return maxImageArrayLayers;
    }

    public int getSupportedTransforms() {
        return supportedTransforms;
    }

    public int getCurrentTransform() {
        return currentTransform;
    }

    public int getSupportedCompositeAlpha() {
        return supportedCompositeAlpha;
    }

    public int getSupportedUsageFlags() {
        return supportedUsageFlags;
    }

    @Override
    protected void write(MemorySegment pStruct, Allocator additional) {
        VkSurfaceCapabilitiesKHR.minImageCount$set(pStruct, minImageCount);
        VkSurfaceCapabilitiesKHR.maxImageCount$set(pStruct, maxImageCount);
        currentExtent.write(VkSurfaceCapabilitiesKHR.currentExtent$slice(pStruct), additional);
        minImageExtent.write(VkSurfaceCapabilitiesKHR.minImageExtent$slice(pStruct), additional);
        maxImageExtent.write(VkSurfaceCapabilitiesKHR.maxImageExtent$slice(pStruct), additional);
        VkSurfaceCapabilitiesKHR.maxImageArrayLayers$set(pStruct, maxImageArrayLayers);
        VkSurfaceCapabilitiesKHR.supportedTransforms$set(pStruct, supportedTransforms);
        VkSurfaceCapabilitiesKHR.currentTransform$set(pStruct, currentTransform);
        VkSurfaceCapabilitiesKHR.supportedCompositeAlpha$set(pStruct, supportedCompositeAlpha);
        VkSurfaceCapabilitiesKHR.supportedUsageFlags$set(pStruct, supportedUsageFlags);
    }

    @Override
    protected void read(MemorySegment pStruct) {
        this.minImageCount = VkSurfaceCapabilitiesKHR.minImageCount$get(pStruct);
        this.maxImageCount = VkSurfaceCapabilitiesKHR.maxImageCount$get(pStruct);
        this.currentExtent.read(VkSurfaceCapabilitiesKHR.currentExtent$slice(pStruct));
        this.minImageExtent.read(VkSurfaceCapabilitiesKHR.minImageExtent$slice(pStruct));
        this.maxImageExtent.read(VkSurfaceCapabilitiesKHR.maxImageExtent$slice(pStruct));
        this.maxImageArrayLayers = VkSurfaceCapabilitiesKHR.maxImageArrayLayers$get(pStruct);
        this.supportedTransforms = VkSurfaceCapabilitiesKHR.supportedTransforms$get(pStruct);
        this.currentTransform = VkSurfaceCapabilitiesKHR.currentTransform$get(pStruct);
        this.supportedCompositeAlpha = VkSurfaceCapabilitiesKHR.supportedCompositeAlpha$get(pStruct);
        this.supportedUsageFlags = VkSurfaceCapabilitiesKHR.supportedUsageFlags$get(pStruct);
    }
}
