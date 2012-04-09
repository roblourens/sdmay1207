package com.CameraStreamer.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import sdmay1207.ais.network.NetworkController;
import android.os.SystemClock;
import android.util.Log;

import com.androidhive.dashboard.CameraActivity;

public class H263Packetizer extends Thread implements Runnable
{
    // Video stream variables
    private byte[] streamBuffer;
    private byte[] eofBytes;
    private int leftoverAmount;
    private boolean endOfFrame;
    private int cycle;
    private InputStream fis;
    protected final int rtphl = 12;

    // RTP Socket
    protected SmallRtpSocket rsock = null;
    protected boolean running = false;
    protected byte[] buffer = new byte[15000];

    // sets up RTP socket
    public H263Packetizer(InputStream in, int destNodeNum,
            NetworkController netController) throws SocketException
    {
        // set up RTP socket and store input stream
        this.fis = in;
        this.rsock = new SmallRtpSocket(destNodeNum, buffer, netController);

        // Set up variables to parse video stream
        streamBuffer = new byte[15000 - rtphl - 2];
        eofBytes = new byte[] { (byte) 0x00, (byte) 0x00 };
        leftoverAmount = 0;
        endOfFrame = true;
        cycle = 0;
        Log.d(CameraActivity.LOG_TAG, "Streaming to node" + destNodeNum);
    }

    // Start streaming
    public void startStreaming()
    {
        running = true;
        start();
    }

    // Stop streaming
    public void stopStreaming()
    {
        try
        {
            fis.close();
        } catch (IOException e)
        {

        }
        interrupt();
        running = false;
    }

    // The thread meat
    public void run()
    {
        try
        {
            int totalBytes = 0;
            fis.read(new byte[1634]);
            setHeader(1);

            // Start Cycle
            while (running)
            {
                // Make Packet and send
                int length = readNextFrame();
                if (length > 0)
                {
                    // Store length read
                    totalBytes += length;
                    System.out.println("Bytes read: " + length
                            + "\tTotal bytes: " + totalBytes);
                    length += rtphl + 2;

                    // Send packet
                    rsock.send(length);

                    // If end of frame then set header and update timestamp
                    if (endOfFrame)
                    {
                        setHeader(1);
                        rsock.updateTimestamp(SystemClock.elapsedRealtime() * 90);
                    } else
                    {
                        setHeader(0);
                    }
                    endOfFrame = false;

                    // Update cycle
                    System.out.println("Packet Sent: " + length + "bytes \n");
                    cycle += 1;

                }

            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Read the next frame or portion of a frame if frame is bigger than buffer
    private int readNextFrame()
    {

        // Variables
        int length = 0;
        int index = 0;
        int len = 0;
        int available = 0;

        try
        {
            // get available and read in as much as buffer will hold without
            // destroying necessary data (also known as leftover)
            available = fis.available();
            len = fis.read(streamBuffer, leftoverAmount, streamBuffer.length
                    - leftoverAmount - 1);

            if (len > 0)
            {
                // Get index of next end of frame
                index = nextEOF();

                // If no end of frame found, then copy all of data read into
                // buffer
                if (index >= leftoverAmount + len - 2)
                {
                    System.arraycopy(streamBuffer, 0, buffer, rtphl + 2,
                            leftoverAmount + len - 2);
                    length = len;
                    leftoverAmount = 2;

                } else
                {
                    // Copy frame into buffer and mark next packet as end of
                    // frame
                    System.arraycopy(streamBuffer, 0, buffer, rtphl + 2, index);
                    length = index;
                    rsock.markNextPacket();

                    // Mark leftover and move leftover to beginning of stream
                    // buffer
                    leftoverAmount = leftoverAmount + len - (index + 2);
                    System.arraycopy(streamBuffer, index + 2, streamBuffer, 0,
                            leftoverAmount);
                    endOfFrame = true;
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
            // Debuggin purposes
            System.out.println("CYCLE_E: " + cycle);
            System.out.println("Available: " + available + "\tIndex: " + index);
            System.out.println("Len: " + len + "\t\tLeftover: "
                    + leftoverAmount);

            System.exit(0);
        }

        // Debugging purposes
        System.out.println("CYCLE: " + cycle);
        System.out.println("Available: " + available + "\t\tIndex: " + index);
        System.out.println("Len: " + len + "\t\tLeftover: " + leftoverAmount);

        return length;
    }

    // Find the start of next frame
    private int nextEOF()
    {
        int index = 0;

        // While not at end of streamBuffer
        while (index < streamBuffer.length - 2)
        {
            // Check if two zeros in a row, then check if the third is
            // 0x80/0x81/0x82/0x83
            if (streamBuffer[index] == eofBytes[0]
                    && streamBuffer[index + 1] == eofBytes[1])
            {
                if ((streamBuffer[index + 2] & 0xfc) == 0x80)
                {
                    break;
                }
            }
            index++;
        }
        return index;
    }

    /*
     * H263 header -> 2 bytes
     * 
     * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ | RR
     * |P|V| PLEN |PEBIT| +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */
    private boolean setHeader(int p)
    {
        // Set first byte
        if (p == 1)
        {
            buffer[rtphl] = (byte) 0x04;
        } else
        {
            buffer[rtphl] = (byte) 0x00;
        }

        // Set second byte
        buffer[rtphl + 1] = (byte) 0x00;

        return true;
    }
}