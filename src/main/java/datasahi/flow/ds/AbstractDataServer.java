package datasahi.flow.ds;

public class AbstractDataServer implements DataServer {

    private String id;
    private final DataServerType type;

    public AbstractDataServer(DataServerType type) {
        this.type = type;
    }

    public AbstractDataServer setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DataServerType getType() {
        return type;
    }
}
