package view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import structure.Album;
import structure.Photo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PhotoController implements Serializable{

	private static final long serialVersionUID = 1L;

	@FXML private ListView<Photo> photo_list = new ListView<Photo>();
	@FXML private ListView<String> tag_list;
	@FXML private ImageView pic_display;
    @FXML private Text album_name;
    @FXML private Text created_date;
    @FXML private TextField tagv;
    @FXML private TextField tagn;
    @FXML private TextField cap;
    @FXML private ChoiceBox<Album> album_D;


	private static Stage primaryStage;
	private static boolean saved;
	private static String user;
	private static Album album;
	private static Photo photo;
	private static String alb;
	private static int index;
	private static String user_dir;
	private static ObservableList<Album> album_list;
	
    public void start(Stage mainStage, String userName, Album album, Photo p, ObservableList<Album> album_list) throws ClassNotFoundException, IOException {
    	primaryStage = mainStage;
    	user_dir = "users" + File.separator + userName + File.separator;
    	user = userName;
    	alb = album.name;
    	this.album = album;
    	this.album_list = album_list;
    	photo = this.album.photos.get(this.album.photos.indexOf(p));
		tag_list.getItems().addAll(photo.tags);
    	album_D.setItems(album_list);
    	album_D.getItems().remove(this.album);
    	display();
    	
    	Image image = new Image("file:" + p.path);
    	pic_display.setImage(image);
    	mainStage.setOnCloseRequest(e -> {
			try {
				save();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
    }
    
    @FXML
    private void next() {
    	index = album.photos.indexOf(photo);
    	if(index == album.photos.size()-1) {
    		error("No Next Photo", "No Next Photo");
    	}else {
	    	index++;
	    	photo = album.photos.get(index);
			tag_list.getItems().addAll(photo.tags);
	    	Image image = new Image("file:" + photo.path);
	    	pic_display.setImage(image);
	    	display();
    	}
    }
    
    @FXML
    private void pre() {
    	index = album.photos.indexOf(photo);
    	if(index <= 0) {
    		error("No previous Photo", "No previous Photo");
    	}else {
	    	index--;
	    	photo = album.photos.get(index);
			tag_list.getItems().addAll(photo.tags);
	    	Image image = new Image("file:" + photo.path);
	    	pic_display.setImage(image);
	    	display();
    	}
    }
    
    @FXML
    private void addtag() {
    	if(tagn.getText().length() < 1 || tagv.getText().length() < 1){
    		error("Empty Field" , "Pls Enter Tag TAG TAG Tag");
    	}else {
    		String tag = tagn.getText().toLowerCase() + "=" + tagv.getText().toLowerCase();
    		if (photo.tags.contains(tag))
    			error("Duplicate Tag", "Pls Enter other Tag");
    		else {
    			photo.tags.add(tag);
    			tag_list.getItems().add(tag);
    		}
    	}
    }
    
    @FXML
    private void removetag() {
    	String tt = tag_list.getSelectionModel().getSelectedItem();
    	tag_list.getItems().remove(tt);
    	photo.tags.remove(tt);
    }
    
    @FXML
    private void addcap() {
    	if(cap.getText().length() < 1) {
    		error("Empty Field" , "Pls Enter CAPTION");
    	}else
    		photo.caption = cap.getText();
    		
    }
    
    @FXML
    private void save() throws IOException {
    	if (album_list.isEmpty()) return;	// No albums
    	for (Album a : album_list) {
    		ObjectOutputStream ois = new ObjectOutputStream(
    				new FileOutputStream(user_dir + a.name));
    		ois.writeObject(a);
    		ois.close();
    	}
    	saved = true;
    }
	
	@FXML
	private void copy() {
		Album name = (Album) album_D.getSelectionModel().getSelectedItem();
		
		if(name == null) {
			error("","");
		}else if (name.photos.contains(photo))
			error("Duplicate Photo", "Photo already exists in album");
		else
			name.add(photo);
	}
	
	@FXML
	private void move() {
		Album name = (Album) album_D.getSelectionModel().getSelectedItem();
		
		if(name == null) {
			error("","");
		}else if (name.photos.contains(photo))
			error("Duplicate Photo", "Photo already exists in album");
		else {
			name.add(photo);
			album.remove(photo);
			System.out.print(index);


			if ((index-1) >= 0) {
				pre();
			}else if ((index+1) < album.photos.size()) {
				next();
			}else {
		    	pic_display.setImage(null);;
			}
		}
	}
	
	@FXML
    public void logout() {
    	if (!saved) {
    		Alert alert = new Alert(AlertType.CONFIRMATION);
       	    alert.setHeaderText("Save Changes Made to Albums?");
       	    ButtonType saveButton = new ButtonType("Save", ButtonData.CANCEL_CLOSE);
       	    ButtonType noSave = new ButtonType("Do Not Save");
       	    alert.getButtonTypes().setAll(saveButton, noSave);
            Optional<ButtonType> result = alert.showAndWait();
            result.ifPresent(choice -> {
            	if (choice == saveButton) {
					try {
						save();
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            });
    	}
    	
    	try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/Login.fxml"));
			AnchorPane root = (AnchorPane)loader.load();
			
			LoginController controller = loader.getController();
			controller.start(primaryStage);
			
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Photos");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
	
    private void display() {
    	if (album == null) {
    		album_name.setText("N/A");
    		created_date.setText("N/A");
    		cap.setText("N/A");
    		tag_list = new ListView<String>();
    		tagv.setText("");
    		tagn.setText("");
    	} else {
    		album_name.setText(album.name);
    		created_date.setText(album.firstDate == null? "N/A" : 
    			new SimpleDateFormat("MMM-dd-yyyy").format(album.firstDate));
    		cap.setText(photo.caption);
    		tag_list.getItems().clear();
    		tag_list.getItems().addAll(photo.tags);
    		System.out.print(photo.tags.toString());
    		tagv.setText("");
    		tagn.setText("");
    	}
    }
    
    private void error(String title, String content) {
    	Alert alert = new Alert(AlertType.INFORMATION);
   	    alert.setTitle(title);
   	    alert.setHeaderText(content);
        alert.showAndWait();
    }
    
    public void back() {
		try {
			save();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/Album.fxml"));
			AnchorPane root = (AnchorPane)loader.load();
			
			AlbumController controller = loader.getController();
			controller.start(primaryStage, user, user_dir+alb, album_list);
			
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Album Display");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
}
