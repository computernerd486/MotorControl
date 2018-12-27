package tech.shadowland.piborg;

import java.awt.*;
import java.nio.ByteBuffer;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;


public class ThunderBorg {

    I2CBus bus;
    I2CDevice device;

    // Constant values
    static final int intI2C_SLAVE                 = 0x0703;
    static final int intPWM_MAX                   = 255;
    static final byte bytePWM_MAX                 = 0xF;
    static final int intI2C_MAX_LEN               = 6;
    static final float VOLTAGE_PIN_MAX            = 36.3f;  // Maximum voltage from the analog voltage monitoring pin
    static final float VOLTAGE_PIN_CORRECTION     = 0.0f;   // Correction value for the analog voltage monitoring pin
    static final float BATTERY_MIN_DEFAULT        = 7.0f;   // Default minimum battery monitoring voltage
    static final float BATTERY_MAX_DEFAULT        = 35.0f;  // Default maximum battery monitoring voltage

    static final byte I2C_ID_THUNDERBORG          = 0x15;

    static final byte COMMAND_SET_LED1            = 1;     // Set the colour of the ThunderBorg LED
    static final byte COMMAND_GET_LED1            = 2;     // Get the colour of the ThunderBorg LED
    static final byte COMMAND_SET_LED2            = 3;     // Set the colour of the ThunderBorg Lid LED
    static final byte COMMAND_GET_LED2            = 4;     // Get the colour of the ThunderBorg Lid LED
    static final byte COMMAND_SET_LEDS            = 5;     // Set the colour of both the LEDs
    static final byte COMMAND_SET_LED_BATT_MON    = 6;     // Set the colour of both LEDs to show the current battery level
    static final byte COMMAND_GET_LED_BATT_MON    = 7;     // Get the state of showing the current battery level via the LEDs
    static final byte COMMAND_SET_A_FWD           = 8;     // Set motor A PWM rate in a forwards direction
    static final byte COMMAND_SET_A_REV           = 9;     // Set motor A PWM rate in a reverse direction
    static final byte COMMAND_GET_A               = 10;    // Get motor A direction and PWM rate
    static final byte COMMAND_SET_B_FWD           = 11;    // Set motor B PWM rate in a forwards direction
    static final byte COMMAND_SET_B_REV           = 12;    // Set motor B PWM rate in a reverse direction
    static final byte COMMAND_GET_B               = 13;    // Get motor B direction and PWM rate
    static final byte COMMAND_ALL_OFF             = 14;    // Switch everything off
    static final byte COMMAND_GET_DRIVE_A_FAULT   = 15;    // Get the drive fault flag for motor A, indicates faults such as short-circuits and under voltage
    static final byte COMMAND_GET_DRIVE_B_FAULT   = 16;    // Get the drive fault flag for motor B, indicates faults such as short-circuits and under voltage
    static final byte COMMAND_SET_ALL_FWD         = 17;    // Set all motors PWM rate in a forwards direction
    static final byte COMMAND_SET_ALL_REV         = 18;    // Set all motors PWM rate in a reverse direction
    static final byte COMMAND_SET_FAILSAFE        = 19;    // Set the failsafe flag, turns the motors off if communication is interrupted
    static final byte COMMAND_GET_FAILSAFE        = 20;    // Get the failsafe flag
    static final byte COMMAND_GET_BATT_VOLT       = 21;    // Get the battery voltage reading
    static final byte COMMAND_SET_BATT_LIMITS     = 22;    // Set the battery monitoring limits
    static final byte COMMAND_GET_BATT_LIMITS     = 23;    // Get the battery monitoring limits
    static final byte COMMAND_WRITE_EXTERNAL_LED  = 24;    // Write a 32bit pattern out to SK9822 / APA102C
    static final  int COMMAND_GET_ID              = 0x99;  // Get the board identifier
    static final  int COMMAND_SET_I2C_AD0D        = 0xAA;  // Set a new I2C address

    static final byte COMMAND_VALUE_FWD           = 1;     // I2C value representing forward
    static final byte COMMAND_VALUE_REV           = 2;     // I2C value representing reverse

    static final byte COMMAND_VALUE_ON            = 1;     // I2C value representing on
    static final byte COMMAND_VALUE_OFF           = 0;     // I2C value representing off

    static final  int COMMAND_ANALOG_MAX          = 0x3FF; // Maximum value for analog readings

    public enum Motor { BOTH, LSide, RSide }

    public ThunderBorg()  {
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            System.out.println("Connected to bus OK!");


            device = bus.getDevice(I2C_ID_THUNDERBORG);

            byte[] read = new byte[intI2C_MAX_LEN];
            device.read(COMMAND_GET_ID, read, 0, read.length);
            System.out.println(read[1]);

            /**
             * Since i will forget this, the i2c packet is 4 bytes long:
             *
             * [a,b,c,d]
             *
             * where:
             * 	a = command
             * 	b = value
             *
             * so to set
             *
             */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the
     * @param m  Which motor to set the speed to
     * @param spd  Speed in percentage, 0.0-1.0
     * @throws Exception
     */
    public void setMotor(Motor m, float spd) throws Exception {
        boolean rev = spd < 0;
        byte pwm = (byte)(intPWM_MAX * Math.abs(spd));

        byte[] write = ByteBuffer.allocate(intI2C_MAX_LEN).array();

        switch (m) {
            case RSide:
                write[0] = rev ? COMMAND_SET_A_REV : COMMAND_SET_A_FWD;
                write[1] = pwm;
                device.write(write);
                break;

            case LSide:
                write[0] = rev ? COMMAND_SET_B_REV : COMMAND_SET_B_FWD;
                write[1] = pwm;
                device.write(write);
                break;

            case BOTH:
                write[0] = rev ? COMMAND_SET_A_REV : COMMAND_SET_A_FWD;
                write[1] = pwm;
                device.write(write);

                write[0] = rev ? COMMAND_SET_B_REV : COMMAND_SET_B_FWD;
                write[1] = pwm;
                device.write(write);
                break;
        }

    }

    /**
     * Stops the motors
     *
     * @throws Exception
     */
    public void stop() throws Exception {
        byte[] write = ByteBuffer.allocate(intI2C_MAX_LEN).array();

        write[0] = COMMAND_ALL_OFF;
        device.write(write);
    }

    /**
     * Returns the led color
     * @throws Exception
     */
    public void getLedColor() throws Exception {
        byte[] write = ByteBuffer.allocate(intI2C_MAX_LEN).array();
        write[0] = COMMAND_GET_LED1;
        device.write(write);

        byte[] read = new byte[intI2C_MAX_LEN];
        device.read(COMMAND_GET_ID, read, 0, read.length);

        System.out.println("Reading LED color");
        for (byte b : read) {
            System.out.println(b);
        }
    }

    /**
     *
     * @param c
     * @throws Exception
     */
    public void setLedColor(Color c) throws Exception {
        setLedColor(
                (byte)(c.getRed() / 0xFF * 0xF ),
                (byte)(c.getGreen() / 0xFF * 0xF ),
                (byte)(c.getBlue() / 0xFF * 0xF ));
    }

    /**
     *
     * @param r
     * @param g
     * @param b
     * @throws Exception
     */
    public void setLedColor(byte r, byte g, byte b) throws Exception {
        byte[] write = ByteBuffer.allocate(intI2C_MAX_LEN).array();

        //Sets the module to ignore battery LED
        device.write(COMMAND_SET_LED_BATT_MON, write);

        write[0] = (byte) Math.max(0, Math.min(bytePWM_MAX, r));
        write[1] = (byte) Math.max(0, Math.min(bytePWM_MAX, g));
        write[2] = (byte) Math.max(0, Math.min(bytePWM_MAX, b));

        //Writes the new color
        device.write(COMMAND_SET_LEDS, write);
    }

    public static void main(String[] args) {

        System.out.println("Testing connection to ThunderBorg motor controller");
        ThunderBorg tb = new ThunderBorg();

        try {
            tb.getLedColor();

            System.out.println("Setting to Green");
            tb.setLedColor(Color.GREEN);
            Thread.sleep(2000);

            System.out.println("Setting to Blue");
            tb.setLedColor(Color.BLUE);
            Thread.sleep(2000);

            System.out.println("Setting to Red");
            tb.setLedColor(Color.RED);
            Thread.sleep(2000);


            /*
            tb.setMotor(Motor.LSide, 1.0f);
            Thread.sleep(2000);

            tb.setMotor(Motor.LSide, 0.0f);
            tb.setMotor(Motor.RSide, 1.0f);
            Thread.sleep(2000);

            tb.setMotor(Motor.RSide, 0.0f);


            tb.setMotor(Motor.BOTH, 1.0f);
            Thread.sleep(2000);

            tb.setMotor(Motor.RSide, 0.0f);
            */

        } catch (Exception e) {
            System.err.println("Unable to setup / read motor control");
        }
    }


}
