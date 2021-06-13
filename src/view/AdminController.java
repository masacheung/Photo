package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.util.Optional;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.AnchorPane;
import javafx.collections.ObservableList;

public class AdminController {

    @FXML private ListView<String> user_list;

    @FXML private Button add_button;

    @FXML private Button delete_button;

    public static Stage primaryStage;
    
    private static boolean saved;

    public void start(Stage mainStage) throws FileNotFoundException {
    	primaryStage = mainStage;
    	primaryStage.setOnCloseRequest(e -> {
			try {
				save();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
    	
    	load();
    	saved = true;
    }
    
    private boolean confirm(String header, String message) {
    	Alert alert = new Alert(AlertType.CONFIRMATION);
   	    alert.setHeaderText(header);
   	    alert.setContentText(message);
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
    	File fp = new File("users.txt");
    	FileWriter writer = new FileWriter(fp);
    	for (String user : user_list.getItems())
    		writer.write(user + "\n");
    	writer.close();
    	saved = true;
    }
    
    @FXML
    public void load() throws FileNotFoundException {
    	File fp = new File("users.txt");
    	Scanner sc = new Scanner(fp);
    	ObservableList<String> users = user_list.getItems();
    	
    	while (sc.hasNext())
    		users.add(sc.next());
    	
    	sc.close();
    }
    
    @FXML
    public void logout() {
    	if (!saved) {
    		Alert alert = new Alert(AlertType.CONFIRMATION);
       	    alert.setHeaderText("Save Changes Made to Users?");
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

    @FXML public void keyPress(KeyEvent event) {
    	if (event.getCode().equals(KeyCode.BACK_SPACE) || event.getCode().equals(KeyCode.DELETE))
    		delete();
    }

    @FXML public void add() {
    	TextInputDialog dialog = new TextInputDialog();
    	dialog.setTitle("Add New User");
    	dialog.setHeaderText("Please enter a username");
    	dialog.setContentText("User name:");

    	Optional<String> result = dialog.showAndWait();
    	result.ifPresent(name -> {
    		if (name.length() < 1)
    			error("Empty Username", "Username cannot be empty");
    		else if (name.contains(" "))
				error("Username contains space", "Username cannot have spaces");
    		else {
    			File dir = new File("users/" + name);
    			if (dir.exists())
    				error("Duplicate User", "User alreday exists");
    			else {
    				dir.mkdir();
		    		user_list.getItems().add(name);
		    		saved = false;
    			}
    		}
    	});
    }

    @FXML
    void delete() {
    	String user = user_list.getSelectionModel().getSelectedItem();

    	if (user == null)
    		error("No User Selected", "Please select a user");
    	
    	else if (confirm("Remove '" + user + "'?", "All of user's photos will be removed")) {
    		user_list.getItems().remove(user);
    		// Remove user files
    		File dir = new File("users/" + user);
    		if (dir.exists()) {
    			for (File fp : dir.listFiles())
    				fp.delete();
    			dir.delete();
    		}
    		saved = false;
    	}
    }
    
}