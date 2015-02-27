package io.input;

import java.io.IOException;

import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.SystemStatusMessage;
import network.messages.TemperatureMessage;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class I2CTemperatureModuleInput extends Thread implements ControllerInput,
		MessageProvider {
	/*
	 * I2C Device variables and settings
	 */
	private final static int ADDR = 0x91 >>1 ; //maybe shift right 1 bit?

	private final static int I2C_DEVICE_UPDATE_DELAY = 1000;

	private I2CDevice tmp102;
	private I2CBus i2cBus;
	private boolean available = false;
	private double temperature = 0;
	
	public I2CTemperatureModuleInput(I2CBus i2cBus) {
		this.i2cBus = i2cBus;
		configure();
	}
	
	private void configure() {
		try {
			tmp102 = i2cBus.getDevice(ADDR);
			available = true;
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public Message getMessage(Message request) {
		if(request instanceof InformationRequest && ((InformationRequest)request).getMessageTypeQuery().equals(InformationRequest.MessageType.TEMPERATURE)){
			if (!available) {
				return new SystemStatusMessage("[CompassModule] Unable to send Temperature data");
			}
			return new TemperatureMessage(temperature);
		}
		return null;
	}

	@Override
	public Object getReadings() {
		return temperature;
	}

	private double readTemperature() throws IOException, InterruptedException {
		if (available) {
			
			byte[] bytes = new byte[2];
			
			int bytesRead = tmp102.read(bytes, 0, 2);
			
			if(bytesRead != 2)
				throw new IOException("[Temperature Sensor] Incorrect number of bytes read!");
			
			int res = ((int) (bytes[0] & 0x0ff) << 8) | (bytes[1] & 0x0ff);
			
			if ((res & 0x01) != 0) {
				// ExtendMode(13bit)
				res = res >> 3;
				//13bit signed int to 32bit signed int
				if((res&0x00001000)!=0){
					res|=0xfffff000;
				}
			} else {
				// Normal(12bit)
				res = res >> 4;
				//12bit signed int to 32bit signed int
				if((res&0x00000800)!=0){
					res|=0xfffff800;
				}
			}
			
//			int t = ((msb) << 4);  // MSB
//		    t|= (lsb >> 4);   // LSB
			double td = res * 0.0625;
		    
		    return td;
		} else {
			return -1;
		}
	}

	@Override
	public void run() {
		try {
			
			long startTime = System.currentTimeMillis();
			
			while (true) {
				
				try {
				
				temperature = readTemperature();
				long elapsed = System.currentTimeMillis() - startTime;
				
				System.out.println((elapsed/1000)+" "+temperature);
				
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				Thread.sleep(I2C_DEVICE_UPDATE_DELAY);
			}
			
		} catch (InterruptedException e) {
			System.out.println("[Compass] Terminated!");
		}
	}
}
