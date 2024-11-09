package ch.szclsb.kerinci.internal.vulkan;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueueFamilyIndices {
    int graphicsFamily;
    int presentFamily;
    boolean graphicsFamilyHasValue = false;
    boolean presentFamilyHasValue = false;

    public int getGraphicsFamily() {
        return graphicsFamily;
    }

    public void setGraphicsFamily(int graphicsFamily) {
        this.graphicsFamily = graphicsFamily;
    }

    public int getPresentFamily() {
        return presentFamily;
    }

    public void setPresentFamily(int presentFamily) {
        this.presentFamily = presentFamily;
    }

    public boolean isGraphicsFamilyHasValue() {
        return graphicsFamilyHasValue;
    }

    public void setGraphicsFamilyHasValue(boolean graphicsFamilyHasValue) {
        this.graphicsFamilyHasValue = graphicsFamilyHasValue;
    }

    public boolean isPresentFamilyHasValue() {
        return presentFamilyHasValue;
    }

    public void setPresentFamilyHasValue(boolean presentFamilyHasValue) {
        this.presentFamilyHasValue = presentFamilyHasValue;
    }

    public boolean isComplete() {
        return graphicsFamilyHasValue && presentFamilyHasValue;
    }

    public Set<Integer> getUniqueFamilies() {
        return Stream.of(graphicsFamily, presentFamily)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
