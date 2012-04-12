package archimulator.util.arduino;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class ArduinoHelper implements SerialPortEventListener {
    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;

    private static final String PORT_NAME = "/dev/ttyACM0";
    private static final int TIMEOUT = 2000;
    private static final int DATA_RATE = 9600;

    public void run() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(PORT_NAME)) {
                portId = currPortId;
                break;
            }
        }

        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIMEOUT);

            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public void blink(int i) {
        try {
            out.write(i);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                int available = in.available();
                byte chunk[] = new byte[available];
                in.read(chunk, 0, available);

                System.out.print(new String(chunk));
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ArduinoHelper arduinoHelper = new ArduinoHelper();
        arduinoHelper.run();
    }
}