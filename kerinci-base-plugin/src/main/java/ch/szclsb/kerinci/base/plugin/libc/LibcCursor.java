package ch.szclsb.kerinci.base.plugin.libc;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LibcCursor {
    public static final String KIND_FUNCTION = "CursorKind.FUNCTION_DECL";
    public static final String KIND_PARAMETER = "CursorKind.PARM_DECL";
    public static final String KIND_STRUCT = "CursorKind.STRUCT_DECL";
    public static final String KIND_FIELD = "CursorKind.FIELD_DECL";
    public static final String KIND_ENUM = "CursorKind.ENUM_DECL";
    public static final String KIND_ENUM_CONST = "CursorKind.ENUM_CONSTANT_DECL";
    public static final String KIND_REF_EXPR = "CursorKind.DECL_REF_EXPR";
    public static final String KIND_INT_LITERAL = "CursorKind.INTEGER_LITERAL";
    public static final String KIND_TYPEDEF = "CursorKind.TYPEDEF_DECL";
    public static final String KIND_TYPEREF = "CursorKind.TYPE_REF";

    private String kind;
    private String spelling;
    private LibcType type;
    private LibcType resultType;
    private LibcType underlyingTypedefType;
    private List<LibcCursor> children;

    public LibcCursor() {
        this.children = new ArrayList<>();
    }

    public LibcCursor(String kind, String spelling, LibcType type, LibcType resultType, LibcType underlyingTypedefType, List<LibcCursor> children) {
        this.kind = kind;
        this.spelling = spelling;
        this.type = type;
        this.resultType = resultType;
        this.underlyingTypedefType = underlyingTypedefType;
        this.children = children;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getSpelling() {
        return spelling;
    }

    public void setSpelling(String spelling) {
        this.spelling = spelling;
    }

    public LibcType getType() {
        return type;
    }

    public void setType(LibcType type) {
        this.type = type;
    }

    public LibcType getResultType() {
        return resultType;
    }

    public void setResultType(LibcType resultType) {
        this.resultType = resultType;
    }

    public LibcType getUnderlyingTypedefType() {
        return underlyingTypedefType;
    }

    public void setUnderlyingTypedefType(LibcType underlyingTypedefType) {
        this.underlyingTypedefType = underlyingTypedefType;
    }

    public List<LibcCursor> getChildren() {
        return children;
    }

    public void setChildren(List<LibcCursor> children) {
        this.children = children;
    }
}
