import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class BeatBox {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame jFrame;
    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Share",
            "Crash Cymbal", "Hand Clap", "High Tom",  "Hi Bongo", "Maracas", "Whistle",
            "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};


    public static void main(String[] args){
        new BeatBox().buildGUI();
    }

    public void buildGUI(){
        jFrame = new JFrame("BeatBox");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        checkBoxList = new ArrayList<>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("upTempo");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("downTempo");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);


        JButton sendScheme = new JButton("Save");
        sendScheme.addActionListener(new SendListener());
        buttonBox.add(sendScheme);

        JButton loadScheme = new JButton("Load");
        loadScheme.addActionListener(new LoadListener());
        buttonBox.add(loadScheme);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i<16; i++){
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.WEST,nameBox);
        background.add(BorderLayout.EAST,buttonBox);

        jFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);

        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER,mainPanel);

        for(int i = 0; i<256; i++){
            JCheckBox b = new JCheckBox();
            b.setSelected(false);
            checkBoxList.add(b);
            mainPanel.add(b);
        }

        setUpmidi();

        jFrame.setBounds(50,50,300,300);
        jFrame.pack();
        jFrame.setVisible(true);

    }

    public void setUpmidi(){
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart(){
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();


        for(int i=0; i<16;i++){
            trackList = new int[16];

            int key = instruments[i];

            for(int j=0;j<16;j++){
                JCheckBox c = (JCheckBox)checkBoxList.get(j+16*i);
                if(c.isSelected()){
                    trackList[j] = key;
                }else {
                    trackList[j] = 0;
                }
            }
            makeTrack(trackList);
            track.add(makeEvent(176,1,127,0,16));

            try {
                sequencer.setSequence(sequence);
                sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
                sequencer.start();
                sequencer.setTempoInBPM(120);
            }catch (Exception e){e.printStackTrace();}
        }

    }

    public void makeTrack(int [] list){
        for(int i=0; i<16;i++){
            int key = list[i];

            if(key!=0){
                track.add(makeEvent(144,9,key,100,i));
                track.add(makeEvent(128,9,key,100,i+1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd,chan,one,two);
            event = new MidiEvent(a,tick);
        }catch (Exception e){
            e.printStackTrace();
        }
        return event;
    }


    public class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();

        }
    }
    public class MyStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }
    public class MyUpTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float temp = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(temp*1.03));
        }
    }
    public class MyDownTempoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float temp = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(temp*0.97));
        }
    }
    public class SendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxstate = new boolean[256];
            for (int i =0; i<checkboxstate.length;i++){
                JCheckBox tmpCheck = checkBoxList.get(i);
                if(tmpCheck.isSelected()){
                    checkboxstate[i] = true;
                }
            }

            try(FileOutputStream fos = new FileOutputStream(new File("checkBoxSer.ser"));
                ObjectOutputStream stream = new ObjectOutputStream(fos))
            {
                stream.writeObject(checkboxstate);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public class LoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxstate = null;


            try(FileInputStream fis = new FileInputStream(new File("checkBoxSer.ser"));
                ObjectInputStream stream = new ObjectInputStream(fis))
            {
                checkboxstate = (boolean[])stream.readObject();
            }catch (Exception ex){
                ex.printStackTrace();
            }

            for (int i =0; i<checkboxstate.length;i++){
                JCheckBox tmpCheck = checkBoxList.get(i);
                if(checkboxstate[i]){
                    tmpCheck.setSelected(true);
                }else {
                    tmpCheck.setSelected(false);
                }
            }

            sequencer.stop();
            buildTrackAndStart();
        }
    }

}
