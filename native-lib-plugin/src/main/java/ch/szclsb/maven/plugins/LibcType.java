package ch.szclsb.maven.plugins;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LibcType {
    @JsonProperty("kind")
    private String kind;
    @JsonProperty("const")
    private boolean constValue;
    @JsonProperty("ref")
    private LibcType ref;

    public LibcType() {
    }

    public LibcType(String kind, boolean constValue, LibcType ref) {
        this.kind = kind;
        this.constValue = constValue;
        this.ref = ref;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public boolean isConstValue() {
        return constValue;
    }

    public void setConstValue(boolean constValue) {
        this.constValue = constValue;
    }

    public LibcType getRef() {
        return ref;
    }

    public void setRef(LibcType ref) {
        this.ref = ref;
    }
}
