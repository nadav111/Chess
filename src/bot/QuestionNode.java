package bot;
import java.util.function.Predicate;

public class QuestionNode extends Node {
    private Predicate<long[][]> _predicate;
    private Node _yes;
    private Node _no;

    public QuestionNode(int id, Predicate<long[][]> predicate) {
        super(id);
        _predicate = predicate;
    }

    public boolean test(long[][] boards) {
        return _predicate.test(boards);
    }

    public Node next(boolean answer) {
        return answer ? _yes : _no;
    }

    public void setYes(Node yes) {
        _yes = yes;
    }

    public void setNo(Node no) {
        _no = no;
    }
}
