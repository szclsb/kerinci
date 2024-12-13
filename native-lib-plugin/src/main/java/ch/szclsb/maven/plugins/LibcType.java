package ch.szclsb.maven.plugins;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LibcType {
    public static final String KIND_POINTER = "TypeKind.POINTER";
    public static final String KIND_ELABORATED = "TypeKind.ELABORATED";
    public static final String KIND_ARRAY = "TypeKind.CONSTANTARRAY";
    public static final String KIND_INT = "TypeKind.INT";
    public static final String KIND_FLOAT = "TypeKind.FLOAT";
    public static final String KIND_CHAR = "TypeKind.CHAR_S";
    public static final String KIND_VOID = "TypeKind.VOID";

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
