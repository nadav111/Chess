package bot;
import java.util.function.Function;

public class AnswerNode extends Node {
    private Function<long[][], String> _func;

    public AnswerNode(int id, Function<long[][], String> func) {
        super(id);
        _func = func;
    }

    public String getMove(long[][] boards) {
        return _func.apply(boards);
    }
}
