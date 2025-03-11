package datasahi.flow.sync;

public class CrudDefinition {

    private String create;
    private String read;
    private String update;
    private String delete;

    public String getCreate() {
        return create;
    }

    public CrudDefinition setCreate(String create) {
        this.create = create;
        return this;
    }

    public String getRead() {
        return read;
    }

    public CrudDefinition setRead(String read) {
        this.read = read;
        return this;
    }

    public String getUpdate() {
        return update;
    }

    public CrudDefinition setUpdate(String update) {
        this.update = update;
        return this;
    }

    public String getDelete() {
        return delete;
    }

    public CrudDefinition setDelete(String delete) {
        this.delete = delete;
        return this;
    }

    @Override
    public String toString() {
        return "CrudDefinition{" +
                "create='" + create + '\'' +
                ", read='" + read + '\'' +
                ", update='" + update + '\'' +
                ", delete='" + delete + '\'' +
                '}';
    }
}
