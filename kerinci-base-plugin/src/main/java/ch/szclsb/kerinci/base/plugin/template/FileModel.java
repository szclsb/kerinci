package ch.szclsb.kerinci.base.plugin.template;

// generic itself cannot be read by freemarker
public abstract class FileModel<T> {
    private final String packageName;
    private final String className;
    private final T content;

    public FileModel(String packageName, String className, T content) {
        this.packageName = packageName;
        this.className = className;
        this.content = content;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public T getContent() {
        return content;
    }
}
