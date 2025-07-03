package bot;
public abstract class Node {
    protected int _id;

    public Node(int id) {
        this._id = id;
    }

    public int getAct() {
        return _id;
    }
}
