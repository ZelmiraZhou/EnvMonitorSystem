package com.demo;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

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
	private JTextField tv_tmp;
	private JTextField tv_gas;
	private JTextField tv_hmp;

	Conn(JTextField tv_tmp, JTextField tv_hmp, JTextField tv_gas) {
		this.tv_tmp = tv_tmp;
		this.tv_hmp = tv_hmp;
		this.tv_gas = tv_gas;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// runConn();
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
			try {
				// �򿪴���
				port = (SerialPort) portIdentifier.open(portName, 1000);
				setPort();
				try {
					// �����
					outputStream = new BufferedOutputStream(
							port.getOutputStream());
					// ������
					inputStream = new BufferedInputStream(port.getInputStream());
					// inputStream = port.getInputStream();
					// InputStreamReader inputStreamReader = new
					// InputStreamReader(
					// inputStream);
					// ��Ϣ
					byte[] msg = new byte[] { 0x01 };
					// д������
					outputStream.write(msg);
					// ����ˢ������
					outputStream.flush();

					// ����������
					byte[] re = new byte[8];
					// char[] ch = new char[8];
					// ���峤��
					int len = 0;
					// int i = 0;
					// int flag = 1;
					int gas = 0, tmp = 0, hmp = 0;
					// re[0] = 0;
					ArrayBlockingQueue<Byte> queue = new ArrayBlockingQueue<Byte>(
							8);
					while (true) {

						// while(inputStream.available()>0){
						// len = inputStream.read(re);
						// // System.out.println(bytesToHexString(re));
						//
						// }
						//

						// while ((len = inputStreamReader.read(ch)) > 0) {
						// for(char c : ch){
						// System.out.println("reader"+c);
						// }
						//
						//
						// // System.out.println(new String(ch));
						// }

						// while ((len = inputStream.read(re)) != -1) {

						while ((len = inputStream.read(re)) != -1) {
							// ����̨������
							// System.out.println("���: "
							// + bytesToHexString(subBytes(re, 0, len)));
							// String result = bytesToHexString(subBytes(re, 0,
							// len));
							//

							// System.out.println(result);
							// subBytes(re, 0,len);
							// flag = 0;
							// i = 1;
							// System.out.println("****" + new
							// String(re).getBytes().toString());
							// for (byte b : re) {
							// b--;
							// System.out.println(b);
							// }
							for (int j = 0; j < len; j++) {
								re[j]--;
								queue.put(Byte.valueOf(re[j]));
								if (queue.size() == 8) {
									int a = queue.poll().byteValue() * 1000;
									int b = queue.poll().byteValue() * 100;
									int c = queue.poll().byteValue() * 10;
									int d = queue.poll().byteValue() * 1;
									gas = a + b + c + d;
									int e = queue.poll().byteValue() * 10;
									int f = queue.poll().byteValue() * 1;
									tmp = e + f;
									int g = queue.poll().byteValue() * 10;
									int h = queue.poll().byteValue() * 1;
									hmp = g + h;
								}
								System.out.println("re[" + j + "] = " + re[j]);
							}
							// System.out.println(bytesToHexString(re));

//							gas = (re[0] * 1000 + re[1] * 100 + re[2] * 10 + re[3]) != 0 ? re[0]
//									* 1000 + re[1] * 100 + re[2] * 10 + re[3]
//									: gas;
//
//							tmp = (re[4] * 10 + re[5]) != 0 ? (re[4] * 10 + re[5])
//									: tmp;
//							hmp = (re[6] * 10 + re[7]) != 0 ? re[6] * 10
//									+ re[7] : hmp;

							System.out.println("���壺" + gas + "; �¶ȣ�" + tmp
									+ "; ʪ�ȣ�" + hmp);

							tv_gas.setText(gas + "");
							tv_tmp.setText(tmp + "��");
							tv_hmp.setText(hmp + "");

							// result = result.replace(" ", "");
							// result = result.replace("0", "");
							// result = result.replace("3", "");
							// System.out.println(result);

							// i++;
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
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
				}
			} catch (PortInUseException e) {
				e.printStackTrace();
			}
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} finally {
			port.close();
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
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		while (enumeration.hasMoreElements()) {
			CommPortIdentifier cp = (CommPortIdentifier) enumeration
					.nextElement();
			// �ж��Ƿ�Ϊ�����
			if (cp.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portName = cp.getName();
				System.out.println("port: " + portName);
			}
		}
	}

	private static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		int i = 0;
		for (; i < bArray.length - 1; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			// System.out.println("sTmp = " + sTemp);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
			sb.append(" ");
		}
		sTemp = Integer.toHexString(0xFF & bArray[i]);
		if (sTemp.length() < 2)
			sb.append(0);
		sb.append(sTemp.toUpperCase());
		return sb.toString();
	}

	private static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static int toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	private static byte[] arrCat(byte[] first, byte[] second) {
		byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private static byte[] subBytes(byte[] src, int begin, int count) {
		byte[] bs = new byte[count];
		for (int i = begin; i < begin + count; i++)
			bs[i - begin] = src[i];
		return bs;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			runConn();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}