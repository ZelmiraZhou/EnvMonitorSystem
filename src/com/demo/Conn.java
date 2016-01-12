package com.demo;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;

/**
 * ����������
 *
 */
public class Conn implements Runnable {
	/**
	 * ����
	 */
	private static SerialPort port;
	/**
	 * ��������
	 */
	private static String portName;
	/**
	 * �����
	 */
	private static OutputStream outputStream;
	/**
	 * ������
	 */
	private static InputStream inputStream;
	/**
	 * �¶��ı���
	 */
	private JTextField tv_tmp;
	/**
	 * ����Ũ���ı���
	 */
	private JTextField tv_gas;
	/**
	 * ʪ���ı���
	 */
	private JTextField tv_hmp;
	/**
	 * ����
	 */
	private JFrame frame;
	private BufferedWriter bw;

	private boolean isRunning = false;

	/**
	 * ���캯��
	 * 
	 * @param frame
	 * 
	 * @param tv_tmp
	 * @param tv_hmp
	 * @param tv_gas
	 */
	Conn(JFrame frame, JTextField tv_tmp, JTextField tv_hmp, JTextField tv_gas) {
		this.frame = frame;
		this.tv_tmp = tv_tmp;
		this.tv_hmp = tv_hmp;
		this.tv_gas = tv_gas;
	}

	/**
	 * ���д�������
	 * 
	 * @throws InterruptedException
	 */
	private void runConn() throws InterruptedException {
		listPortChoices();

		try {
			// ��ȡ���ڱ�־ʵ��
			CommPortIdentifier portIdentifier = CommPortIdentifier
					.getPortIdentifier(portName);
			// �򿪴���
			port = (SerialPort) portIdentifier.open(portName, 1000);
			// ���ô���
			setPort();
			// �����
			outputStream = new BufferedOutputStream(port.getOutputStream());
			// ������
			inputStream = new BufferedInputStream(port.getInputStream());
			// ��Ϣ
			byte[] msg = new byte[] { 0x01 };
			// д������
			outputStream.write(msg);
			// ����ˢ������
			outputStream.flush();
			// �ı����б�־
			isRunning = true;

			bw = new BufferedWriter(new FileWriter(new File("D:/data.txt")));

			// ����������
			byte[] re = new byte[8];
			// ���峤��
			int len = 0;
			int gas = 0, tmp = 0, hmp = 0;
			ArrayBlockingQueue<Byte> queue = new ArrayBlockingQueue<Byte>(8);
			while (isRunning) {

				while (isRunning && ((len = inputStream.read(re)) != -1)) {
					for (int j = 0; j < len; j++) {
						re[j]--;
						queue.put(Byte.valueOf(re[j]));
						if (queue.size() == 8) {
							int a = queue.poll().byteValue() * 1000;
							int b = queue.poll().byteValue() * 100;
							int c = queue.poll().byteValue() * 10;
							int d = queue.poll().byteValue() * 1;
							if (a + b + c + d - 700 > 0)
								gas = a + b + c + d - 700;
							int e = queue.poll().byteValue() * 10;
							int f = queue.poll().byteValue() * 1;
							if (e + f > 0)
								tmp = e + f;
							int g = queue.poll().byteValue() * 10;
							int h = queue.poll().byteValue() * 1;
							if (g + h > 0)
								hmp = g + h;

							System.out.println("���壺" + gas + "; �¶ȣ�" + tmp
									+ "; ʪ�ȣ�" + hmp);
							bw.write("���壺" + gas + "; �¶ȣ�" + tmp + "; ʪ�ȣ�"
									+ hmp);
							bw.newLine();
							bw.flush();

							// �����ı�������
							tv_gas.setText(gas + "");
							tv_tmp.setText(tmp + "");
							tv_hmp.setText(hmp + "");

							// ����
							if (gas >= Constant.MAX_GAS) {
								tv_gas.setForeground(Color.RED);
								UI.tf_gas.setForeground(Color.RED);
								UI.tf_gas_unit.setForeground(Color.RED);
								Toolkit toolkit = frame.getToolkit();
								toolkit.beep();
							} else {
								tv_gas.setForeground(Color.DARK_GRAY);
								UI.tf_gas.setForeground(Color.DARK_GRAY);
								UI.tf_gas_unit.setForeground(Color.DARK_GRAY);
							}
							if (tmp >= Constant.MAX_TMP) {
								UI.tf_tmp.setForeground(Color.RED);
								UI.tf_tmp_unit.setForeground(Color.RED);
								tv_tmp.setForeground(Color.RED);
								Toolkit toolkit = frame.getToolkit();
								toolkit.beep();
							} else {
								tv_tmp.setForeground(Color.DARK_GRAY);
								UI.tf_tmp.setForeground(Color.DARK_GRAY);
								UI.tf_tmp_unit.setForeground(Color.DARK_GRAY);
							}
							if (hmp >= Constant.MAX_HMP) {
								UI.tf_hmp.setForeground(Color.RED);
								UI.tf_hmp_unit.setForeground(Color.RED);
								tv_hmp.setForeground(Color.RED);
								Toolkit toolkit = frame.getToolkit();
								toolkit.beep();
							} else {
								tv_hmp.setForeground(Color.DARK_GRAY);
								UI.tf_hmp.setForeground(Color.DARK_GRAY);
								UI.tf_hmp_unit.setForeground(Color.DARK_GRAY);
							}
						}
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// �ر��� �رմ���
			try {
				outputStream.close();
				inputStream.close();
				port.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			port.close();
			try {
				bw.close();
			} catch (IOException e) {
				bw = null;
			}
		}
	}

	/**
	 * �Զ�����Ϊ���ô���
	 */
	private static void setPort() {
		try {
			// ���ô��ڲ���
			port.setSerialPortParams(9600, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN
					| SerialPort.FLOWCONTROL_XONXOFF_OUT);
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * չʾ���д���
	 */
	private static void listPortChoices() {
		// ��ȡ���п��ö˿�
		Enumeration<CommPortIdentifier> enumeration = CommPortIdentifier
				.getPortIdentifiers();
		while (enumeration.hasMoreElements()) {
			CommPortIdentifier cp = enumeration.nextElement();
			// �ж��Ƿ�Ϊ�����
			if (cp.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portName = cp.getName();
				System.out.println("port: " + portName);
			}
		}
	}

	@Override
	public void run() {
		try {
			runConn();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		isRunning = false;
	}

}