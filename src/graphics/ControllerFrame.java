package graphics;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import logic.GameMonitor;

public class ControllerFrame extends JFrame {
    private JTextArea _moveArea;
    private JButton _playBtn, _pauseBtn, _stopBtn;
    private GameMonitor _monitor;
    private Thread _gameThread;
    private boolean _paused = false;
    private List<String> _moves;
    private int _cur;

    public ControllerFrame(GameMonitor gm) {
        _monitor = gm;

        setTitle("Chess Game Player");
        setSize(400, 300);
        setLayout(new BorderLayout());

        _moveArea = new JTextArea();
        JScrollPane scroll = new JScrollPane(_moveArea);
        add(scroll, BorderLayout.CENTER);

        _playBtn = new JButton("Play");
        _pauseBtn = new JButton("Pause");
        _stopBtn = new JButton("Step");

        JPanel bottom = new JPanel();
        bottom.add(_playBtn);
        bottom.add(_pauseBtn);
        bottom.add(_stopBtn);
        add(bottom, BorderLayout.SOUTH);

        _playBtn.addActionListener(e -> playMoves());
        _pauseBtn.addActionListener(e -> _paused = true);
        _stopBtn.addActionListener(e -> step());

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void playMoves() {
        _moves = Arrays.asList(_moveArea.getText().split("\\s+"));
        if (_moves == null)
            return;
        _paused = false;
        
        _gameThread = new Thread(() -> {
            for (String move : _moves) {
                while (_paused) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }

                if(!_monitor.handleMove(move))
                {
                    JOptionPane.showMessageDialog(this, "Illegal move: " + _moves.get(_cur));
                }
                
                _cur++;

                try {
                    Thread.sleep(500); // delay
                } catch (InterruptedException e) {}
            }
        });

        _gameThread.start();
    }

    private void step() {
        if (_gameThread != null) _gameThread.interrupt();
        if (_moves == null)
            _moves = Arrays.asList(_moveArea.getText().split("\\s+"));
        if (_moves != null){
            if(!_monitor.handleMove(_moves.get(_cur++)))
            {
                JOptionPane.showMessageDialog(this, "Illegal move: " + _moves.get(_cur));
            }
        }
    }
}
