package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import structure.Album;
import structure.Photo;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.collections.ObservableList;

public class UsersController implements Serializable{
	private static final long serialVersionUID = 1L;

	@FXML private ListView<Album> album_list;

    @FXML private Button add_button;
    @FXML private Button delete_button;
    @FXML private Button date_search_button;
    
    @FXML private TextField album_name;
    @FXML private Text created_date;
    @FXML private Text last_edit_date;
    @FXML private Text photo_count;
    
    @FXML private TextField tag_1_name;
    @FXML private TextField tag_1_val;
    @FXML private TextField tag_2_name;
    @FXML private TextField tag_2_val;
    
    @FXML private DatePicker date_from;
    @FXML private DatePicker date_to;

    public static Stage primaryStage;
    
    private static String user;
    private static String user_dir;

    public void start(Stage mainStage, String userName) throws ClassNotFoundException, IOException {
    	primaryStage = mainStage;
    	user = userName;
    	user_dir = "users" + File.separator + user + File.separator;
    	primaryStage.setOnCloseRequest(e -> {
			try {
				save();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
    	
    	load();
    	display();
    }
    
    private boolean confirm(String content) {
    	Alert alert = new Alert(AlertType.CONFIRMATION);
   	    alert.setHeaderText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return (result.isPresent() && result.get() == ButtonType.OK);
    }
    
    private void error(String title, String content) {
    	Alert alert = new Alert(AlertType.INFORMATION);
   	    alert.setTitle(title);
   	    alert.setHeaderText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void save() throws IOException {
    	if (album_list.getItems().isEmpty()) return;	// No albums
    	for (Album a : album_list.getItems()) {
    		ObjectOutputStream ois = new ObjectOutputStream(
    				new FileOutputStream(user_dir + a.name));
    		ois.writeObject(a);
    		ois.close();
    	}
    }
    
    @FXML
    public void load() throws IOException, ClassNotFoundException {
    	ObservableList<Album> albums = album_list.getItems();
    	File dir = new File(user_dir);
    	String[] dir_list = dir.list();
    	// check if empty
    	if (dir.list().length < 1) return;
    	if (dir_list[0].charAt(0) == '.') {
    		dir_list = Arrays.copyOfRange(dir_list, 1, dir_list.length);
    	}
    	for (String fn : dir_list) {
    		System.out.println(fn);
    		ObjectInputStream ois = new ObjectInputStream(
    				new FileInputStream(user_dir + fn));
    		albums.add((Album) ois.readObject());
    		ois.close();
    	}
    }
    
    private void display() {
    	Album album = album_list.getSelectionModel().getSelectedItem();
    	
    	if (album == null) {
    		album_name.setText("N/A");
    		created_date.setText("N/A");
    		last_edit_date.setText("N/A");
    		photo_count.setText("N/A");
    	} else {
    		album_name.setText(album.name);
    		created_date.setText(album.firstDate == null? "N/A" : album.firstDate.toString());
    		last_edit_date.setText(album.lastDate == null? "N/A" : album.lastDate.toString());
    		photo_count.setText(album.photos.size() + "");
    	}
    }
    
    @FXML
    public void logout() {
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

    @FXML public void keyPress(KeyEvent event) {
    	if (event.getCode().equals(KeyCode.BACK_SPACE) || event.getCode().equals(KeyCode.DELETE))
    		delete();
    }

    @FXML void mouseClick(MouseEvent event) {
    	if (event.getClickCount() < 2) {	// Refresh (Single click)
    		display();
    		return;
    	}
    	
    	// Open album (multiple click)
    	Album album = album_list.getSelectionModel().getSelectedItem();
    	if (album == null) return;	// No album selected
    	    	
    	try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/view/Album.fxml"));
			AnchorPane root = (AnchorPane)loader.load();
			
			AlbumController controller = loader.getController();
			controller.start(primaryStage, user, user_dir + album.name, album_list.getItems());
			
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Album Display");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

    @FXML void searchByDate() {
    	// Null Check
    	if (date_from.getValue() == null || date_to.getValue() == null)
    		error("Parameter Error", "Start or end date is empty");
    	Date start = new Date(date_from.getValue().toEpochDay());
    	Date end = new Date(date_to.getValue().toEpochDay());
    	// Check parameters
    	if (end.before(start))
    		error("Date Error", "End date must be after start date");
    	// Actual Search
    	ListView<Photo> photos = new ListView<Photo>();
    	
    	for (Album a : album_list.getItems()) {
    		for (Photo p : a.photos) {
    			if (p.date.compareTo(start) >= 0 || p.date.compareTo(end) < 1)
    				photos.getItems().add(p);
    		}
    	}
    	
    	// display results
    	displayMasa(photos);
    }
    
    @FXML public void singleSearch() {
    	String tag1 = tag_1_name.getText() + "=" + tag_1_val.getText();
    	if (tag1.length() < 1)
    		error("Input Error", "Please enter a tag");
    	Predicate<Photo> predicate = p -> p.tags.contains(tag1);
    	
    	filterPhotos(predicate);
    }

    @FXML public void andSearch() {
    	String tag1 = tag_1_name.getText() + "=" + tag_1_val.getText();
    	String tag2 = tag_2_name.getText() + "=" + tag_2_val.getText();
    	if (tag1.length() < 1 || tag2.length() < 1)
    		error("Input Error", "Both tags must be present");
    	Predicate<Photo> predicate = p -> p.tags.contains(tag1) && p.tags.contains(tag2);
    	
    	filterPhotos(predicate);
    }

    @FXML public void orSearch() {
    	String tag1 = tag_1_name.getText() + "=" + tag_1_val.getText();
    	String tag2 = tag_2_name.getText() + "=" + tag_2_val.getText();
    	if (tag1.length() < 1 || tag2.length() < 1)
    		error("Input Error", "Both tags must be present");
    	Predicate<Photo> predicate = p -> p.tags.contains(tag1) || p.tags.contains(tag2);
    	
    	filterPhotos(predicate);
    }

    public void filterPhotos(Predicate<Photo> predicate) {
    	ListView<Photo> photos = new ListView<Photo>();
    	for (Album a : album_list.getItems()) {
    		photos.getItems().addAll(a.photos.stream()
    				.filter(predicate).collect(Collectors.<Photo>toList()));
    	}
    	// display results
    	if (photos.getItems().isEmpty())
    		error("No Results", "No matching results");
    	else
    		displayMasa(photos);
    }

    public void displayMasa(ListView<Photo> photos) {
    	ListView<Photo> p = photos;
//        System.out.print(p.getItems().toString());
        p.setCellFactory(param -> new ListCell<Photo>() {
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
        
        Button button = new Button("Add New Album");

        button.setOnAction(action -> {
           add(photos.getItems());
        });
        
        VBox box = new VBox(p, button);
        box.setAlignment(Pos.CENTER);

        Stage secondStage = new Stage();
        Scene scene = new Scene(box);
        
        secondStage.setScene(scene);
        secondStage.initModality(Modality.APPLICATION_MODAL);
        secondStage.showAndWait();
    }
    
    @FXML void add() {
    	TextInputDialog dialog = new TextInputDialog();
    	dialog.setTitle("Add New Album");
    	dialog.setHeaderText("Please enter a name for the Album");
    	dialog.setContentText("Album name:");

    	Optional<String> result = dialog.showAndWait();
    	result.ifPresent(name -> {
    		if (name.length() < 1)
    			error("Empty Album Name", "Album name cannot be empty");
    		else if (name.charAt(0) == '.')
    			error("Unaccepted Format", "Album name cannot start with'.'");
    		else {
    			name = name.replaceAll(" ", "_");
	    		File fp = new File(user_dir + name);
	    		Album album = new Album(name);
	    		try {
					fp.createNewFile();
					/* Saving stuff */
					ObjectOutputStream ois = new ObjectOutputStream(
							new FileOutputStream(user_dir + name));
					ois.writeObject(album);
					ois.close();
				} catch (IOException e) {
					error("Duplicate Album", "Album alreday exists");
					return;
				}
	    		album_list.getItems().add(album);
    		}
    	});
    }
    
    private void add(ObservableList<Photo> photos) {
    	TextInputDialog dialog = new TextInputDialog();
    	dialog.setTitle("Add New Album");
    	dialog.setHeaderText("Please enter a name for the Album");
    	dialog.setContentText("Album name:");

    	Optional<String> result = dialog.showAndWait();
    	result.ifPresent(name -> {
    		if (name.length() < 1)
    			error("Empty Album Name", "Album name cannot be empty");
    		else {
    			name = name.replaceAll(" ", "_");
	    		File fp = new File(user_dir + name);
	    		Album album = new Album(name, photos);
	    		try {
					fp.createNewFile();
					/* Saving stuff */
					ObjectOutputStream ois = new ObjectOutputStream(
							new FileOutputStream(user_dir + name));
					ois.writeObject(album);
					ois.close();
				} catch (IOException e) {
					error("Duplicate Album", "Album alreday exists");
					return;
				}
	    		album_list.getItems().add(album);
    		}
    	});
    	
    }
    
    @FXML public void edit() {
    	String name = album_name.getText();
    	if (name.length() < 1) {
    		error("Empty Album Name", "Album name cannot be empty");
    		return;
    	}
    	// Actual Edit
    	Album album = album_list.getSelectionModel().getSelectedItem();
    	for (Album a : album_list.getItems()) {
    		// Check Duplicate
    		if (a.name.toLowerCase().equals(name.toLowerCase()))
    			error("Name Conflict", "Album already exists");
    		else {
    			album.name = name;
    			display();
    		}
    	}
    }
        
    @FXML void delete() {
    	Album album = album_list.getSelectionModel().getSelectedItem();

    	if (album == null)
    		error("No Album Selected", "Please select an album");
    	
    	else if (confirm("Deleting '" + album.name + "'?")) {
    		// Remove user files
    		File fp = new File(user_dir + album.name);
    		if (fp.exists()) {
    			fp.delete();
    			album_list.getItems().remove(album);
    		}
    	}
    	display();
    }
    
}