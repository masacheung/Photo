package view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import structure.Album;
import structure.Photo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class AlbumController implements Serializable {
	private static final long serialVersionUID = 1L;

	@FXML private ListView<Photo> photo_list;
    @FXML private Text album_name;
    @FXML private Text created_date;
    @FXML private Text last_edit_date;
    @FXML private Text photo_count;

	private static Stage primaryStage;
	private static boolean saved;
	private static String user;
	private static String albumDir;
	private static Album album;
	private static ObservableList<Album> album_list;

    public void start(Stage mainStage, String userName, String album, ObservableList<Album> album_list) 
    		throws ClassNotFoundException, IOException {
    	primaryStage = mainStage;
    	user = userName;
    	albumDir = album;
    	this.album_list = album_list;
    	load();
    	display();

    	photo_list.setCellFactory(param -> new ListCell<Photo>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(Photo p, boolean empty) {
                super.updateItem(p, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(p.caption);
                    imageView.setImage(new Image("file:" + p.path));
                    imageView.setFitHeight(20);
                    imageView.setFitWidth(20);
                    setGraphic(imageView);
                }
            }
        });
    	
    	primaryStage.setOnCloseRequest(e -> {
			try {
				File alb = new File(albumDir);
				if (alb.exists()) save();	// save if album still exists
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
    }

//    @FXML
//    public void deleteAlbum() {
//    	File alb = new File(albumDir);
//    	alb.delete();
//    }
    
    private void error(String title, String content) {
    	Alert alert = new Alert(AlertType.INFORMATION);
   	    alert.setTitle(title);
   	    alert.setHeaderText(content);
        alert.showAndWait();
    }
    
    @FXML
    public void addphoto() {
    	FileChooser fc = new FileChooser();
    	fc.setTitle("Select a picture");
    	File fp = fc.showOpenDialog(primaryStage);
    	
    	if (fp != null) {
    		Photo p = new Photo(fp.getPath());
    		if (album.photos.contains(p))
    			error("Duplicate Photo", "Photo already exists in album");
    		else {
	    		photo_list.getItems().add(p);
	    		album.add(p);
    		}
    	}
//    	for (int i=1; i<7; i++)
//    		album.add(new Photo("stock_pic" + File.separator + i + ".png"));

    	display();
    	display();
    }
    
    @FXML
    public void deletePhoto() {
//    	String p = photo_list.getSelectionModel().getSelectedItem();
    	Photo p = photo_list.getSelectionModel().getSelectedItem();
    	photo_list.getItems().remove(p);
    	album.remove(p);
    	display();
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
			Stage secondStage = new Stage();
			
			LoginController controller = loader.getController();
			controller.start(secondStage);
			
			Scene scene = new Scene(root);
			secondStage.setScene(scene);
			secondStage.setTitle("Photos");
			secondStage.initModality(Modality.APPLICATION_MODAL);
			secondStage.showAndWait();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
	
	public void back() {
		try {
			save();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/Users.fxml"));
			AnchorPane root = (AnchorPane)loader.load();
			
			UsersController controller = loader.getController();
			controller.start(primaryStage, user);
			
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setTitle(user + "'s Homepage");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

    @FXML
    public void load() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(albumDir));
		album = (Album) ois.readObject();
		for (Photo p : album.photos)
			photo_list.getItems().add(p);
		ois.close();
    }
    
	@FXML
    private void save() throws IOException {
    	/* Saving stuff */
		ObjectOutputStream ois = new ObjectOutputStream(
				new FileOutputStream(albumDir));
		ois.writeObject(album);
		ois.close();
    }

    @FXML void mouseClick(MouseEvent event) {
    	if (event.getClickCount() < 2) {	// Refresh (Single click)
    		display();
    		return;
    	}
    	
    	// Open album (multiple click)
    	Photo photo = photo_list.getSelectionModel().getSelectedItem();
    	if (photo == null) return;	// No album selected
    	    	
    	try {
    		save();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/Photo.fxml"));
			AnchorPane root = (AnchorPane)loader.load();
			
			Stage secondStage = new Stage();
			PhotoController controller = loader.getController();
			controller.start(secondStage, user, album, photo, album_list);
			
			secondStage.setScene(new Scene(root));
			secondStage.setTitle("Photo Display");
			secondStage.initModality(Modality.APPLICATION_MODAL);
			secondStage.showAndWait();
			// Update photo list
			photo_list.getItems().clear();
			for (Photo p : album.photos)
				photo_list.getItems().add(p);
//			controller.start(primaryStage, user, album, photo, album_list);
//			
//			primaryStage.setScene(new Scene(root));
//			primaryStage.setTitle("Photo Display");
//			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
	
    private void display() {
    	
    	if (album == null) {
    		album_name.setText("N/A");
    		created_date.setText("N/A");
    		last_edit_date.setText("N/A");
    		photo_count.setText("N/A");
    	} else {
    		album_name.setText(album.name);
    		created_date.setText(album.firstDate == null? "N/A" : 
    			new SimpleDateFormat("MMM-dd-yyyy").format(album.firstDate));
    		last_edit_date.setText(album.lastDate == null? "N/A" : 
    			new SimpleDateFormat("MMM-dd-yyyy").format(album.lastDate));
    		photo_count.setText(album.photos.size() + "");
    	}
    }

}
