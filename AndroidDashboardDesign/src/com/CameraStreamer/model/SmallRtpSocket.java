package com.CameraStreamer.model;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import com.androidhive.dashboard.PhotosActivity;

import android.util.Log;



public class SmallRtpSocket {

	// UDP socket/packet
	private DatagramSocket usock;
	private DatagramPacket upack;
	
	// Data variables
	private byte[] buffer;
	private int seq = 0;
	private boolean upts = false;
	public static final int headerLength = 12;
	
	public SmallRtpSocket(InetAddress dest, int dport, byte[] buffer) throws SocketException {
		// Get connection to the buffer
		this.buffer = buffer;
		
		// Set RTP header information
		// Byte 1		   ->  Payload (74 => 4A)
		// Byte 2,3        ->  Sequence Number                   
		// Byte 4,5,6,7    ->  Timestamp                         
		// Byte 8,9,10,11  ->  Sync Source Identifier            
		buffer[0] = (byte) Integer.parseInt("10000000",2);
		buffer[1] = (byte) Integer.parseInt("01001010",2);

		// Set Source Identifier
		setLong((new Random()).nextLong(),8,12);
		
		// Set up UDP socket/packet
		usock = new DatagramSocket();
		upack = new DatagramPacket(buffer,1,dest,dport);

	}

	// Close the socket
	public void close() {
		usock.close();
	}
	
	// Send RTP packet
	public void send(int length) {
		
		// Update the packet
		updateSequence();
		upack.setLength(length);
		
		// send
		try {
			usock.send(upack);
			Log.d(PhotosActivity.LOG_TAG,"Packet sent to "+upack.getAddress().toString());
		} catch (IOException e) {
			Log.e(PhotosActivity.LOG_TAG,"Send failed");
		}
		
		// If it was marked as end of frame, unmark
		if (upts) {
			upts = false;
			buffer[1] -= 0x80;
		}
		
	}
	
	// Update packet sequence number
	private void updateSequence() {
		setLong(++seq, 2, 4);
	}
	
	// Update time stamp
	public void updateTimestamp(long timestamp) {
		setLong(timestamp, 4, 8);
	}
	
	// Mark the next packet to be sent as end of frame
	public void markNextPacket() {
		upts = true;
		buffer[1] += 0x80; // Mark next packet
	}
	
	// Returns if the packet is marked
	public boolean isMarked() {
		return upts;
	}
	
	// Call this only one time !
	public void markAllPackets() {
		buffer[1] += 0x80;
	}
	
	// Sets the long value at particular bytes
	private void setLong(long n, int begin, int end) {
		for (end--; end >= begin; end--) {
			buffer[end] = (byte) (n % 256);
			n >>= 8;
		}
	}	
	
}
