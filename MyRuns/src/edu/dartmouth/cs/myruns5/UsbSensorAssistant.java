package edu.dartmouth.cs.myruns5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.usb.UsbDevice;

/**
 * Manages lookups to the hardware SQLite database containing sensor profile information supported by the app.
 * 
 * @author daniel
 *
 */
public class UsbSensorAssistant extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "hardware.db";
	private static final String TABLE_NAME = "UsbSensorProfile";
	
	public static final int ID_ANY = -1;
	public static final int ERROR_BAUD = Integer.MIN_VALUE;
	
	private static final String COL_VENDOR_ID = "vendorId";
	private static final String COL_PRODUCT_ID = "productId";
	private static final String COL_BAUD = "baud";
	private static final String COL_PROTOCOL = "protocol";
	
	private SQLiteDatabase mDatabase;
	
	private final Context mContext;
	private final UsbSensorManager mUsbSensorManager;
	
	private final String mDatabasePath;
	
	public UsbSensorAssistant(Context context)
	{
		this(context, MyRunsApplication.getUsbSensorManager());
	}
	
	public UsbSensorAssistant(Context context, UsbSensorManager usbSensorManager)
	{
		super(context, DATABASE_NAME, null, 1);
		
		mContext = context;
		
		mUsbSensorManager = usbSensorManager;
		mDatabasePath = mContext.getApplicationContext().getDatabasePath(DATABASE_NAME).getAbsolutePath();
		
		if(!localDatabaseExists())
		{
			// @ERROR: Local database does not exist
			
			if(!copyDatabaseToLocalDevice())
			{
				mDatabase = null;
				return;
			}
		}
		
		// The device now has a local copy of the assets database  
		// in the application's private directory
		mDatabase = SQLiteDatabase.openDatabase(mDatabasePath, null, SQLiteDatabase.OPEN_READONLY);
	}
	
	/**
	 * Returns the baud rate of the target USB device
	 * 
	 * @param device The target USB device
	 * 
	 * @return Returns the baud rate of the target USB device.  If the device is not 
	 *         recognized, it returns ERR_BAUD.
	 */
	public int getDeviceBaudRate(UsbDevice device)
	{
		return(getDeviceBaudRate(device.getVendorId(), device.getProductId()));
	}
	
	/**
	 * Returns the baud rate of the target USB device
	 * 
	 * @param vendorId The vendor ID of the USB device
	 * @param productId The product ID of the USB device
	 * 
	 * @return Returns the baud rate of the target USB device.  If the device is not 
	 *         recognized, it returns ERR_BAUD.
	 */
	public int getDeviceBaudRate(int vendorId, int productId)
	{
		final String columns[] = new String[] {COL_BAUD};
		
		Cursor queryCursor = mDatabase.query(TABLE_NAME, columns, COL_VENDOR_ID + "=" + vendorId + " AND "
				+ COL_PRODUCT_ID + "=" + productId, null, null, null, null);
		
		if(queryCursor.getCount() <= 0)
		{
			return(ERROR_BAUD);
		}
		
		queryCursor.moveToFirst();
		
		return(queryCursor.getInt(0));
	}
	
	/**
	 * Checks if the USB device is registered in the hardware database
	 * 
	 * @param device The target USB device
	 * 
	 * @return True if the USB device is recognized, false otherwise
	 */
	public boolean isValidSensor(UsbDevice device)
	{
		return(isValidSensor(device.getVendorId(), device.getProductId()));
	}
	
	/**
	 * Checks if the USB device is registered in the hardware database
	 * 
	 * @param vendorId The vendor ID of the USB device
	 * @param productId The product ID of the USB device
	 * 
	 * @return True if the USB device is recognized, false otherwise
	 */
	public boolean isValidSensor(int vendorId, int productId)
	{
		final String columns[] = new String[] {COL_VENDOR_ID, COL_PRODUCT_ID};
		
		Cursor queryCursor = mDatabase.query(TABLE_NAME, columns, COL_VENDOR_ID + "=" + vendorId + " AND "
				+ COL_PRODUCT_ID + "=" + productId, null, null, null, null);
		
		return(queryCursor.getCount() > 0 ? true : false);
	}
	
	/**
	 * Returns an ILightSensor object for the USB device if it is a recognized sensor
	 * 
	 * @param device The target USB device
	 * 
	 * @return A light sensor interface object that represents the USB device
	 */
	public ILightSensor getLightSensor(UsbDevice device)
	{
		if(device == null || mDatabase == null)
		{
			return(null);
		}
		
		final String columns[] = new String[] {COL_PROTOCOL};

		Cursor queryCursor = mDatabase.query(TABLE_NAME, columns, 
				COL_VENDOR_ID + "=" + device.getVendorId() + " AND " 
			    + COL_PRODUCT_ID + "=" + device.getProductId(), 
				null, null, null, null);
		
		if(queryCursor.getCount() <= 0)
		{
			return(null);
		}
		
		queryCursor.moveToFirst();
		
		int protocol = queryCursor.getInt(0);
		
		switch(protocol)
		{
			case Sensor.PROTOCOL_ACM:
			case Sensor.PROTOCOL_FTDI:
			{
				return(new UsbLightSensor(mContext, mUsbSensorManager, device, protocol));
			}
			default:
			{
				return(null);
			}
		}
	}
	
	/**
	 * Returns an IUVSensor object for the USB device if it is a recognized sensor
	 * 
	 * @param device The target USB device
	 * 
	 * @return A UV sensor interface object that represents the USB device
	 */
	public IUVSensor getUVSensor(UsbDevice device)
	{
		if(device == null || mDatabase == null)
		{
			return(null);
		}
		
		final String columns[] = new String[] {COL_PROTOCOL};

		Cursor queryCursor = mDatabase.query(TABLE_NAME, columns, 
				COL_VENDOR_ID + "=" + device.getVendorId() + " AND " 
			    + COL_PRODUCT_ID + "=" + device.getProductId(), 
				null, null, null, null);
		
		if(queryCursor.getCount() <= 0)
		{
			return(null);
		}
		
		queryCursor.moveToFirst();
		
		int protocol = queryCursor.getInt(0);
		
		switch(protocol)
		{
			case Sensor.PROTOCOL_ACM:
			case Sensor.PROTOCOL_FTDI:
			{
				return(new UsbUVSensor(mContext, mUsbSensorManager, device, protocol));
			}
			default:
			{
				return(null);
			}
		}
	}
	
	private boolean copyDatabaseToLocalDevice()
	{
		try
		{
			File localDatabaseFile = new File(mDatabasePath);
			
			localDatabaseFile.getParentFile().mkdirs();
			localDatabaseFile.createNewFile();

			InputStream inStream = mContext.getAssets().open(DATABASE_NAME);
			FileOutputStream outStream = new FileOutputStream(mDatabasePath);
			
			byte buffer[] = new byte[4096];
			int readLength = 0;
			
			readLength = inStream.read(buffer);
			
			while(readLength > 0)
			{
				outStream.write(buffer, 0, readLength);
				readLength = inStream.read(buffer);
			}
			
			inStream.close();
			outStream.close();
		}
		catch(IOException exception)
		{
			exception.printStackTrace();
			return(false);
		}
		
		return(true);
	}
	
	private boolean localDatabaseExists()
	{
		return(new File(mDatabasePath).exists());
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int Version, int newVersion)
	{
	}
	
	@Override
	public void finalize()
	{
		mDatabase.close();
	}
}
