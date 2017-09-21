package com.piotrmajcher.piwind.services.impl;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.piotrmajcher.piwind.domain.Snapshot;
import com.piotrmajcher.piwind.repositories.SnapshotRepository;
import com.piotrmajcher.piwind.services.WebcamService;


@Component
public class WebcamServiceImpl implements WebcamService{

	private static final String RM_COMMAND = "rm ";
	private static final String TAKE_PICTURE_COMMAND = "raspistill -o ";
	private static final String SNAPSHOT_FILENAME_PREFIX = "./snapshots/snapshot_"; //snapshot filename : snapshot_2017-09-21T12:56:23.5.jpg
	
	@Autowired
	private SnapshotRepository snapshotRepository;
	
    @Transactional
    @Scheduled(fixedRate = 60000, initialDelay = 5000)
    private void takePictureAndSaveToDB() {
    	try {
    		final String snapshotFilename = takePicture();
    		Path path = Paths.get(snapshotFilename);
    		byte[] snapshotData = Files.readAllBytes(path);
    	
    		Snapshot snapshot = new Snapshot();
    		snapshot.setFilename(snapshotFilename);
    		snapshot.setSnapshotImage(snapshotData);
    		
    		Integer id = snapshotRepository.save(snapshot).getId();
    		System.out.println("Snapshot saved");
    		
    		deleteISnapshotFile(snapshotFilename);	
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    private String takePicture() throws IOException{
    	final String snapshotFilename = getSnapshotFilename();
    	final String command = TAKE_PICTURE_COMMAND + snapshotFilename;
    	
    	Process process = Runtime.getRuntime().exec(command);
		 
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    	
		Assert.isNull(stdError.readLine());
		return snapshotFilename;
    }

	private String getSnapshotFilename() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
    	final Date now = new Date();
    	return SNAPSHOT_FILENAME_PREFIX + dateFormat.format(now);
	}
	
	private void deleteISnapshotFile(String snapshotFilename) throws IOException{
		final String command = RM_COMMAND + snapshotFilename;
		
		Process process = Runtime.getRuntime().exec(command);
		 
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    	
		Assert.isNull(stdError.readLine());
	}

	@Override
	public Snapshot getLatestSnapshot() {
		return null;
	}

	@Override
	public Iterable<Snapshot> getAllSnapshots() {
		return snapshotRepository.findAll();
	}
}