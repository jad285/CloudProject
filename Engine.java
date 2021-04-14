import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.*;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dataproc.v1.HadoopJob;
import com.google.cloud.dataproc.v1.Job;
import com.google.cloud.dataproc.v1.JobControllerClient;
import com.google.cloud.dataproc.v1.JobControllerSettings;
import com.google.cloud.dataproc.v1.JobMetadata;
import com.google.cloud.dataproc.v1.JobPlacement;
import com.google.cloud.dataproc.v1.SparkJob;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Engine implements ActionListener
{
	
	// Important Paths to set
//	public static String myCredentials = "/Users/jacobdiecidue/Downloads/cool-ship-305200-926cc1329eb1.json";
	public static String myCredentials = "/mnt/mydata/cool-ship-305200-926cc1329eb1.json";
	public static String myBucket = "msf-bucket";
	public static String myProjectID = "cool-ship-305200";
//	public static String myLocalPathway = "/Users/jacobdiecidue/Downloads/downloadedOutput/";
	public static String myLocalPathway = "/root/";
	public static String myCluster = "enginecluster";
	public static String myRegion = "us-central1";
	
	// Create a file chooser and other Variable Declaration
	final JFileChooser fc = new JFileChooser();
	public static JFrame frame = new JFrame();
	public static JTextField field = new JTextField(0);
	public static ArrayList<File> files = new ArrayList<File>();
	public static HashMap<String, HashMap<String, Integer>> mapIndex= new HashMap<String, HashMap<String, Integer>>();
	public static String searchTerm = "";
	public static Integer searchNumber = 0;
	public static HashMap<String, Integer> searchValue = new HashMap<String, Integer>();
	public static HashMap<String, Integer> nIndex= new HashMap<String, Integer>();
	public static ArrayList<String> nIndexList = new ArrayList<String>();
	public static ArrayList<Integer> nIndexListFreq = new ArrayList<Integer>();
	public static HashMap<Integer, ArrayList<String>> nIndexRev = new HashMap<Integer, ArrayList<String>>();
	
	public Engine() 
	{
		loadInitial();
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException
	{
		new Engine();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		System.out.println(files);
		if(e.getActionCommand().equals("File"))
		{
			int returnVal = fc.showOpenDialog(fc);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            System.out.println("You chose the file " + fc.getSelectedFile().getName());
	            files.add(file);
			}
		
			loadInitial();
		}
		if(e.getActionCommand().equals("Index"))
		{
			System.out.println("Selected Index");
			try 
			{
				listObjects(myProjectID, myBucket);
				listObjects2(myProjectID, myBucket);
				for(int i = 0; i < files.size(); i++)
				{
					uploadObject(myProjectID, myBucket, files.get(i).getName(), files.get(i).getPath());
				}
				submitJob();
				getOutput(myLocalPathway);
			} 
			catch (IOException | InterruptedException e1) 
			{
				e1.printStackTrace();
			}
			loadUploaded();
		}
		if(e.getActionCommand().equals("Term"))
		{
			System.out.println("Selected Term");
			loadTermInput();
		}
		if(e.getActionCommand().equals("Top"))
		{
			System.out.println("Selected Top");
			loadTopInput();
		}
		if(e.getActionCommand().equals("Search-Term"))
		{
			System.out.println("Selected Search Term");
			searchTerm = field.getText();
			if(mapIndex.containsKey(searchTerm))
			{
				searchValue = mapIndex.get(searchTerm);
			}
			else
			{
				searchValue.clear();
			}
			System.out.println("You input: " + searchTerm);
			loadTermResults();
		}
		if(e.getActionCommand().equals("Search-N"))
		{
			System.out.println("Selected Search N");
			try 
			{
				searchNumber = Integer.parseInt(field.getText());
				if(searchNumber < 1)
				{
					searchNumber = 0;
				}
				System.out.println("You input: " + searchNumber);
				loadTopResults();
			}
			catch (NumberFormatException e2)
			{
				searchNumber = 0;
				loadTopResults();
			}
			
		}
		if(e.getActionCommand().equals("Back"))
		{
			System.out.println("Back");
			loadUploaded();
		}
		if(e.getActionCommand().equals("Home"))
		{
			System.out.println("Home");
			files.clear();
			loadInitial();
		}
	}
	
	public void loadInitial()
	{

		JButton button = new JButton("Choose Files");
		JButton button2 = new JButton("Construct Inverted Indicies");
		JLabel label = new JLabel("<html><h1><strong>Load My Engine</html></h1></strong>", SwingConstants.CENTER);
		button.addActionListener(this);
		button.setActionCommand("File");
		button2.addActionListener(this);
		button2.setActionCommand("Index");

		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
		panel.setLayout(new GridLayout(0,1));
		panel.add(label);
		panel.add(button);
		if(files.size() > 0)
		{
			for(int i = 0; i < files.size(); i++)
			{
				panel.add(new Label(files.get(i).toString()));
			}
			panel.add(button2);
		}
		
		frame.getContentPane().removeAll();
		frame.repaint();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("JAD285 Search Engine");
		frame.pack();
		frame.setVisible(true);
	}
	
	public void loadUploaded()
	{
		JButton button = new JButton("Search For Term");
		JButton button2 = new JButton("Top-N");
		JLabel label = new JLabel("<html><h1><strong>Engine was Loaded and Inverted Indicies were Constructed Successfully!</strong></h1></html>", SwingConstants.CENTER);
		JLabel label2 = new JLabel("<html><h1><strong>Please Select Action</strong></h1></html>", SwingConstants.CENTER);
		button.addActionListener(this);
		button.setActionCommand("Term");
		button2.addActionListener(this);
		button2.setActionCommand("Top");
		JButton button3 = new JButton("Home");
		button3.addActionListener(this);
		button3.setActionCommand("Home");
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
		panel.setLayout(new GridLayout(0,1));
		panel.add(label);
		panel.add(label2);
		panel.add(button);
		panel.add(button2);
		panel.add(button3);
		
		frame.getContentPane().removeAll();
		frame.repaint();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("JAD285 Search Engine");
		frame.pack();
		frame.setVisible(true);
	}

	public void loadTermInput()
	{
		JButton button = new JButton("Search");
		JLabel label = new JLabel("<html><h1><strong>Enter Your Search Term</strong></h1></html>", SwingConstants.CENTER);
		button.addActionListener(this);
		button.setActionCommand("Search-Term");
		field = new JTextField(0);
		field.setToolTipText("Type Your Search Here...");
		JButton button3 = new JButton("Home");
		button3.addActionListener(this);
		button3.setActionCommand("Home");
		JButton button4 = new JButton("Back");
		button4.addActionListener(this);
		button4.setActionCommand("Back");
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
		panel.setLayout(new GridLayout(0,1));
		panel.add(label);
		panel.add(field);
		panel.add(button);
		panel.add(button4);
		panel.add(button3);
		
		
		frame.getContentPane().removeAll();
		frame.repaint();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("JAD285 Search Engine");
		frame.pack();
		frame.setVisible(true);
	}
	
	public void loadTopInput()
	{
		JButton button = new JButton("Search");
		JLabel label = new JLabel("<html><h1><strong>Enter Your N Value</strong></h1></html>", SwingConstants.CENTER);
		button.addActionListener(this);
		button.setActionCommand("Search-N");
		field = new JTextField(0);
		field.setToolTipText("Type Your Search Here...");
		JButton button3 = new JButton("Home");
		button3.addActionListener(this);
		button3.setActionCommand("Home");
		JButton button4 = new JButton("Back");
		button4.addActionListener(this);
		button4.setActionCommand("Back");
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
		panel.setLayout(new GridLayout(0,1));
		panel.add(label);
		panel.add(field);
		panel.add(button);
		panel.add(button4);
		panel.add(button3);
		
		frame.getContentPane().removeAll();
		frame.repaint();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("JAD285 Search Engine");
		frame.pack();
		frame.setVisible(true);
	}
	
	public void loadTermResults()
	{
		JPanel panel = new JPanel();
		if(searchValue.isEmpty())
		{
			JLabel label = new JLabel("<html><h1><strong>Your Search for the Term: " + searchTerm + " did not return any results!</strong></h1></html>", SwingConstants.CENTER);
			JButton button3 = new JButton("Home");
			button3.addActionListener(this);
			button3.setActionCommand("Home");
			JButton button4 = new JButton("Back");
			button4.addActionListener(this);
			button4.setActionCommand("Back");
			
			panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
			panel.setLayout(new GridLayout(0,1));
			panel.add(label);
			panel.add(button4);
			panel.add(button3);
		}
		else
		{
			JLabel label = new JLabel("<html><h1><strong>Your Search for the Term: " + searchTerm + "</strong></h1></html>", SwingConstants.CENTER);
			String[][] data = new String[searchValue.size()][2];
			int i = 0;
			for(String docName : searchValue.keySet())
			{
				data[i][0] = docName;
				data[i][1] = Integer.toString(searchValue.get(docName));
				i++;
			}
			String[] columnNames = {"File Name", "Frequency"};
			
			JTable table = new JTable(data, columnNames);
			JButton button3 = new JButton("Home");
			button3.addActionListener(this);
			button3.setActionCommand("Home");
			JButton button4 = new JButton("Back");
			button4.addActionListener(this);
			button4.setActionCommand("Back");
			JScrollPane scrollPane = new JScrollPane(table);
			table.setFillsViewportHeight(true);
			
			panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
			panel.setLayout(new GridLayout(0,1));
			panel.add(label);
			panel.add(scrollPane);
			panel.add(button4);
			panel.add(button3);
		}
		
		frame.getContentPane().removeAll();
		frame.repaint();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("JAD285 Search Engine");
		frame.pack();
		frame.setVisible(true);
	}
	
	public void loadTopResults()
	{
		JPanel panel = new JPanel();
		if(searchNumber == 0)
		{
			JLabel label = new JLabel("<html><h1><strong>You did not put in a valid number! (N > 0)</strong></h1></html>", SwingConstants.CENTER);
			JButton button3 = new JButton("Home");
			button3.addActionListener(this);
			button3.setActionCommand("Home");
			JButton button4 = new JButton("Back");
			button4.addActionListener(this);
			button4.setActionCommand("Back");
			
			panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
			panel.setLayout(new GridLayout(0,1));
			panel.add(label);
			panel.add(button4);
			panel.add(button3);
		}
		else
		{
			JLabel label = new JLabel("<html><h1><strong>Top-" + searchNumber + " Frequent Terms</strong></h1></html>", SwingConstants.CENTER);
			String[][] data = new String[searchNumber][2];
			if(searchNumber > nIndexListFreq.size())
			{
				data = new String[nIndexListFreq.size()][2];
			}
			
			int counter = 0;
			for(int i = 0; i < searchNumber; i++)
			{
				if(i == nIndexListFreq.size())
				{
					break;
				}
				int a = nIndexListFreq.get(i);
				ArrayList<String> temp = nIndexRev.get(a);
				for(int j = 0; j < temp.size(); j++)
				{
					data[counter][0] = temp.get(j);
					data[counter][1] = Integer.toString(a);
					counter++;
					if(counter == searchNumber)
					{
						break;
					}
				}
				if(counter == searchNumber)
				{
					break;
				}
			}
			String[] columnNames = {"Term", "Total Frequencies"};
			
			JTable table = new JTable(data, columnNames);
			JButton button3 = new JButton("Home");
			button3.addActionListener(this);
			button3.setActionCommand("Home");
			JButton button4 = new JButton("Back");
			button4.addActionListener(this);
			button4.setActionCommand("Back");
			JScrollPane scrollPane = new JScrollPane(table);
			table.setFillsViewportHeight(true);
			
			panel.setBorder(BorderFactory.createEmptyBorder(200, 200, 200, 200));
			panel.setLayout(new GridLayout(0,1));
			panel.add(label);
			panel.add(scrollPane);
			panel.add(button4);
			panel.add(button3);
		}
		
		frame.getContentPane().removeAll();
		frame.repaint();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("JAD285 Search Engine");
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void submitJob() throws IOException, InterruptedException
	{
		submitJob(myProjectID, myRegion, myCluster);
	}
	
	public static void submitJob(String projectId, String region, String clusterName) throws IOException, InterruptedException
	{
		
		String myEndpoint = String.format("%s-dataproc.googleapis.com:443", region);

		// Configure the settings for the job controller client.
	    JobControllerSettings jobControllerSettings =
	        JobControllerSettings.newBuilder().setEndpoint(myEndpoint).build();

	    // Create a job controller client with the configured settings. Using a try-with-resources
	    // closes the client,
	    // but this can also be done manually with the .close() method.
	    try (JobControllerClient jobControllerClient =
	        JobControllerClient.create(jobControllerSettings)) {

	      // Configure cluster placement for the job.
	      JobPlacement jobPlacement = JobPlacement.newBuilder().setClusterName(clusterName).build();

	      ArrayList<String> arg = new ArrayList<String>();
//	      arg.add("WordCount2");
	      arg.add("gs://"+myBucket+"/input");
	      arg.add("gs://"+myBucket+"/output");
	      
	      ArrayList<String> uri = new ArrayList<String>();
	      uri.add("gs://"+myBucket+"/WordCount2.jar");
	      
	      // Configure Hadoop job settings. The HadoopFS query is set here.
	      HadoopJob hadoopJob =
	          HadoopJob.newBuilder()
//	              .addAllFileUris(uri)
	              .setMainJarFileUri("gs://"+myBucket+"/WordCount2.jar")
//	              .setMainClass("gs://msf-bucket/WordCount2.jar")
	              .addAllArgs(arg)
	              .build();

	      Job job = Job.newBuilder().setPlacement(jobPlacement).setHadoopJob(hadoopJob).build();

	      // Submit an asynchronous request to execute the job.
	      OperationFuture<Job, JobMetadata> submitJobAsOperationAsyncRequest =
	          jobControllerClient.submitJobAsOperationAsync(projectId, region, job);

	      Job response = submitJobAsOperationAsyncRequest.get();

	      // Print output from Google Cloud Storage.
	      Matcher matches =
	          Pattern.compile("gs://(.*?)/(.*)").matcher(response.getDriverOutputResourceUri());
	      matches.matches();

	      Storage storage = StorageOptions.getDefaultInstance().getService();
	      Blob blob = storage.get(matches.group(1), String.format("%s.000000000", matches.group(2)));

	      System.out.println(
	          String.format("Job finished successfully: %s", new String(blob.getContent())));

	    } catch (ExecutionException e) {
	      // If the job does not complete successfully, print the error message.
	      System.err.println(String.format("submitHadoopFSJob: %s ", e.getMessage()));
	    }
	}
	
	static void authImplicit() throws FileNotFoundException, IOException {
		  // If you don't specify credentials when constructing the client, the client library will
		  // look for credentials via the environment variable GOOGLE_APPLICATION_CREDENTIALS.
		  GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(myCredentials))
		          .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		  Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		  System.out.println("Buckets:");
		  Page<Bucket> buckets = storage.list();
		  for (Bucket bucket : buckets.iterateAll()) {
		    System.out.println(bucket.toString());
		  }
		}
	

	  public static void downloadObject(
	      String projectId, String bucketName, String objectName, String destFilePath) throws FileNotFoundException, IOException {
	    // The ID of your GCP project
	    // String projectId = "your-project-id";

	    // The ID of your GCS bucket
	    // String bucketName = "your-unique-bucket-name";

	    // The ID of your GCS object
	    // String objectName = "your-object-name";

	    // The path to which the file should be downloaded
	    // String destFilePath = "/local/path/to/file.txt";
		  GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(myCredentials))
		          .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		  Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

	    Blob blob = storage.get(BlobId.of(bucketName, objectName));
	    blob.downloadTo(Paths.get(destFilePath));

	    System.out.println(
	        "Downloaded object "
	            + objectName
	            + " from bucket name "
	            + bucketName
	            + " to "
	            + destFilePath);
	  }
	  
	  public static void uploadObject(String projectId, String bucketName, String objectName, String filePath) throws IOException 
	  {
	    // The ID of your GCP project
	    // String projectId = "your-project-id";

	    // The ID of your GCS bucket
	    // String bucketName = "your-unique-bucket-name";

	    // The ID of your GCS object
	    // String objectName = "your-object-name";

	    // The path to your file to upload
	    // String filePath = "path/to/your/file"

	    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
	    BlobId blobId = BlobId.of(bucketName, "input/" + objectName);
	    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
	    storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

	    System.out.println(
	        "File " + filePath + " uploaded to bucket " + bucketName + "/input as " + objectName);
	  }
	  
	  public static void listObjects(String projectId, String bucketName) 
	  {
		    // The ID of your GCP project
		    // String projectId = "your-project-id";

		    // The ID of your GCS bucket
		    // String bucketName = "your-unique-bucket-name";

		    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		    Page<Blob> blobs = storage.list(bucketName);

		    for (Blob blob : blobs.iterateAll()) 
		    {
		    	if(blob.getName().startsWith("input/"))
		    	{
		    		deleteObject(myProjectID, myBucket, blob.getName());
		    	}
		    	//System.out.println(blob.getName());
		    }
	  }
	  
	  public static void listObjects2(String projectId, String bucketName) 
	  {
		    // The ID of your GCP project
		    // String projectId = "your-project-id";

		    // The ID of your GCS bucket
		    // String bucketName = "your-unique-bucket-name";

		    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		    Page<Blob> blobs = storage.list(bucketName);

		    for (Blob blob : blobs.iterateAll()) 
		    {
		    	if(blob.getName().startsWith("output/"))
		    	{
		    		deleteObject(myProjectID, myBucket, blob.getName());
		    	}
		    	//System.out.println(blob.getName());
		    }
	  }
	  
	  public static void deleteObject(String projectId, String bucketName, String objectName) 
	  {
	    // The ID of your GCP project
	    // String projectId = "your-project-id";

	    // The ID of your GCS bucket
	    // String bucketName = "your-unique-bucket-name";

	    // The ID of your GCS object
	    // String objectName = "your-object-name";

	    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
	    storage.delete(bucketName, objectName);

	    System.out.println("Object " + objectName + " was deleted from " + bucketName);
	  }
	  
	  public static void getOutput(String dest) throws IOException
	  {
		  	// Download the output files from the job
		  	for(int i = 0; i < 7; i++)
		  	{
			  	String dest2 = dest+i+".txt";
			  	File myObj = new File(dest2);
			  	myObj.createNewFile();
			  	downloadObject(myProjectID, myBucket, "output/part-r-0000"+i, dest2 );
		  	}

		  	// Merge the output files together into map.txt
	      	File dir = new File(dest);
	      	String[] fileNames = dir.list();
	      	PrintWriter pw = new PrintWriter(dest+"map.txt");
	  
	        for (String fileName : fileNames) 
	        {
	        	if(fileName.endsWith(".txt"))
	        	{
	        		System.out.println("Reading from " + fileName);
		            File f = new File(dir, fileName);
		            BufferedReader br = new BufferedReader(new FileReader(f));
		            // Read from current file
		            String line = br.readLine();
		            while (line != null) 
		            {
		                pw.println(line);
		                line = br.readLine();
		            }
		            pw.flush();
		            br.close();
	        	}
	        }
	        System.out.println("Reading from all files" + 
	        " in directory " + dir.getName() + " Completed");
	        pw.close();
	        
	        // Populate a hashmap with inverted indexes
	        mapIndex.clear();
	        File mapping = new File(dir, "map.txt");
	        BufferedReader br = new BufferedReader(new FileReader(mapping));
	        String line = br.readLine();
	        while(line != null)
	        {
	        	HashMap<String, Integer> temp = new HashMap<String, Integer>();
	        	StringTokenizer tok = new StringTokenizer(line, "\t`");
	        	String key = tok.nextToken();
	      	  	while(tok.hasMoreTokens())
	      	  	{
	      		  Integer number = Integer.parseInt(tok.nextToken());
	      		  String fn = tok.nextToken();
          		  temp.put(fn, number);
	      	  	}
	      	  	mapIndex.put(key, temp);
	      	  	line = br.readLine();
	        }
	        
	        // Populate a hashmap then list with Top-N style
	        nIndex.clear();
	        File mapping2 = new File(dir, "map.txt");
	        BufferedReader br2 = new BufferedReader(new FileReader(mapping2));
	        String line2 = br2.readLine();
	        while(line2 != null)
	        {
	        	StringTokenizer tok = new StringTokenizer(line2, "\t`");
	        	String key = tok.nextToken();
	        	Integer sum = 0;
	      	  	while(tok.hasMoreTokens())
	      	  	{
	      		  Integer number = Integer.parseInt(tok.nextToken());
	      		  String fn = tok.nextToken();
	      		  sum = sum + number;
	      	  	}
	      	  	nIndex.put(key, sum);
	      	  	line2 = br2.readLine();
	        }
        	
        	for(String keyName : nIndex.keySet())
			{
        		nIndexListFreq.add(nIndex.get(keyName));
			}
        	
        	Collections.sort(nIndexListFreq, Collections.reverseOrder());
        	
        	for(String keyName : nIndex.keySet())
			{
        		if(nIndexRev.containsKey(nIndex.get(keyName)))
        		{
        			ArrayList<String> temp = nIndexRev.get(nIndex.get(keyName));
        			temp.add(keyName);
        			nIndexRev.put(nIndex.get(keyName), temp);
        		}
        		else
        		{
        			ArrayList<String> temp = new ArrayList<String>();
        			temp.add(keyName);
        			nIndexRev.put(nIndex.get(keyName), temp);
        		}
			}
	        
//	        mapIndex.entrySet().forEach(entry -> {
//	            System.out.println(entry.getKey() + " " + entry.getValue());
//	        });
	  }
}
