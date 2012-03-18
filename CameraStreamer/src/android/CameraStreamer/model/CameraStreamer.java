package android.CameraStreamer.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import android.CameraStreamer.view.CameraView;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraStreamer extends MediaRecorder{

		// local socket variables
    	private LocalServerSocket lss = null;
    	private LocalSocket receiver, sender = null;
        private static int id = 0;

    	// Streaming variables
        private H263Packetizer vstream = null;
        private boolean streaming = false;
        
        // Camera Variables
        Camera cam = null; 
        
        
        // Sets up camera and streamer
        public void setup(SurfaceHolder holder, String ip, int resX, int resY, int fps) throws IOException {
           
        	// Open Camera
        	cam = Camera.open();

    		// Step 1: Unlock and set camera to MediaRecorder
    		cam.unlock();
    		setCamera(cam);

    		// Step 2: Set sources
    		setAudioSource(MediaRecorder.AudioSource.DEFAULT);
    		setVideoSource(MediaRecorder.VideoSource.CAMERA);

    		// Step 3: Set output format and encoding 
    		setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    		setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    		setVideoEncoder(MediaRecorder.VideoEncoder.H263);
    		
    		// Step 4: Set the video settings
    		setVideoFrameRate(fps);
            setVideoSize(resX,resY);
            
            
            // Step 5: Set the preview output
    		setPreviewDisplay(holder.getSurface());

    		// Step 6: Prepare configured MediaRecorder and setup local sockets
    		try {
    			prepare();
    		} catch (IllegalStateException e) {
    			Log.d("CameraStreamer", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
       		} catch (IOException e) {
    		    Log.d("CameraStreamer", "IOException preparing MediaRecorder: " + e.getMessage());
    		}

    		// Step 7: Setup packetizer streamer
        	try {
        		Log.d(CameraView.LOG_TAG+" streamer","ip: "+ip);
        		vstream = new H263Packetizer(getInputStream(), InetAddress.getByName(ip), 7476);
            } catch (IOException e) {
                Log.e(CameraView.LOG_TAG,"Unknown host");
                throw new IOException("Can't resolve host :(");
            }
                
        }
    	
        // Called by view when ending streaming
        public void destroy(){
        	// Close sockets and relase the camera for future use
    		closeSockets();
    		cam.release();
    	}    	
        
        // Called by setup, connects the local sockets
        public void prepare() throws IllegalStateException,IOException {
			// Open receiver local socket
			receiver = new LocalSocket();
			int buffersize = 500000;
			try {
				// open local server socket and connect with receiving socket
				lss = new LocalServerSocket("librtp-"+id);
				receiver.connect(new LocalSocketAddress("librtp-"+id));
				
				// Set receiver buffer sizes
				receiver.setReceiveBufferSize(buffersize);
				receiver.setSendBufferSize(buffersize);
				
				// Connect the server and receiver sockets
				sender = lss.accept();

				// Set sender buffer
				sender.setReceiveBufferSize(buffersize);
				sender.setSendBufferSize(buffersize); 
				id++;
			} catch (IOException e1) {
				throw new IOException("Can't create local socket !");
			}
			
			// Pipes camera output to the local sockets
			setOutputFile(sender.getFileDescriptor());
			
			// Try to further prepare camera, closes sockets on failure
			try {
				super.prepare();
			} catch (IllegalStateException e) {
				closeSockets();
				throw e;
			} catch (IOException e) {
				closeSockets();
				throw e;
			}
			
		}
      
        
        // Start streaming
        public void start() {
        		//Log.d(CameraView.LOG_TAG,"Camera Starting");
               
        		// Start video streaming
                super.start();
                vstream.startStreaming();
                streaming = true;
                Log.d(CameraView.LOG_TAG,"Camera Started");
                             
        }
        
        // Stop streaming
        public void stop() {
        
                // Stop video streaming
                vstream.stopStreaming();
        		super.stop();
                streaming = false;
                
        }
        
        // returns streaming variable
        public boolean isStreaming() {
                return streaming;
        }
        
        
        // Gets input stream from local sockets which is connected to the camera
    	public InputStream getInputStream() {
    		
    		InputStream out = null;
    		
    		// Get input stream from receiver socket
    		try {
    			out = receiver.getInputStream();
    		} catch (IOException e) {
    		}

    		return out;
    		
    	}
    	
    	// Close the local sockets
    	private void closeSockets() {
    		if (lss!=null) {
    			try {
    				lss.close();
    				sender.close();
    				receiver.close();
    			}
    			catch (IOException e) {
    				Log.e(CameraView.LOG_TAG,"Error while attempting to close local sockets");
    			}
    			lss = null; sender = null; receiver = null;
    		}
    	}
    	

}
