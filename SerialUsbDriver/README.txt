== PORTING TO ANDROID PROJECT ==

1. Right-click the project you wish to port and select 'Build Path' -> 
   'Configure Build Path'.
2. Click the 'Android' tab.
3. Click the 'Add...' button and select 'SerialUsbDriver'.
4. Create a new class that extends the 'Application' class 
   (e.g. MyRunsCollectorApplication) and fill in the following:

import edu.repo.ucla.serialusbdriver.UsbSensorManager;

public class MyRunsCollectorApplication extends Application
{
    @Override
    public void onCreate()
    {
        UsbSensorManager.init(getApplicationContext());
    }
}

5. Modify the following code in the Android manifest.

<application
    android:name=".MyRunsCollectorApplication"
    android:allowBackup="true"
    android:icon="@drawable/ic_icon"
    android:label="@string/app_name"
    >
    <!-- Additional content here... -->
</application>

6. If it doesn't already exist, create an 'assets' directory in the root 
   directory of the project.

7. Copy the SerialUsbDriver/assets/hardware.db to the 'assets' directory in the 
   local project.

8. If the application was previously installed, remove it completely from the 
   device.


== SUPPORTING A NEW HARDWARE PLATFORM ==

1. Thus far, the 'SerialUsbDriver' project only supports FTDI and ATMEL 
   Mega16U2 (ACM) chips natively.
2. Add a new entry in the SQLite database by typing the following: 
        INSERT INTO UsbSensorProfile VALUES(<ID>, <VENDOR_ID>, <PRODUCT_ID>, 
        <PROTOCOL>, <BAUD>);
        
        * ID: This is the primary key of the table and must be a unique value.
        * VENDOR_ID: The vendor ID of the USB device.  This can be determined 
                     using the 'lsusb' utility.  See details online.
        * PRODUCT_ID: The product ID of the USB device.  This can be determined 
                      using the 'lsusb' utility.  See details online.
        * PROTOCOL: This field is used to dynamically determine which USB driver
                    should be loaded.  The values should match what is found in 
                    SerialUsbDriver/src/Sensor.java
        * BAUD: The default baud rate that the device should use.  Most devices 
                with a 16MHz clock have shown good results with 57600.
3. Uninstall the application completely from the device and then reinstall it.


== TROUBLESHOOTING ==

1. Project shows errors on perfectly good code.
    > Try cleaning all projects in Eclipse by going to 'Project' -> 'Clean' -> 
      'Clean all projects' and pressing OK.
2. Application keeps throwing SQLite exceptions.
    > Uninstall the application completely and then reinstall it.
3. Application does not recognize my USB device.
    > If the device is natively supported, add the vendor ID and product ID to 
      the 'hardware.db' asset file to load the correct driver.
    > If the device is not natively supported, a new USB serial driver may need
      to be written.  This is beyond the scope of this README.  Please refer to 
      existing implementations for an idea on how to do this.
