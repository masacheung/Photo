package view;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

public class LoginController {

    @FXML private TextField login_user;
    @FXML private Button login_button;
    
    private static ArrayList<String> users;
    private static Stage primaryStage;

    public void start(Stage mainStage) throws FileNotFoundException {
    	primaryStage = mainStage;
    	
    	File fp = new File("users.txt");
    	Scanner sc = new Scanner(fp);
    	users = new ArrayList<String>();
    	users.add("admin");
    	users.add("stock");
    	
    	while (sc.hasNext())
    		users.add(sc.next());
    	
    	sc.close();
    }
    
    void error(String message) {
    	Alert alert = new Alert(AlertType.ERROR);
    	alert.setTitle("Username Incorrect");
    	alert.setHeaderText(message);
    	alert.showAndWait();
    }
    
    private void adminWindow(String fxml) {
    	try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(fxml));
			AnchorPane root = (AnchorPane)loader.load();
			
			AdminController controller = loader.getController();
			controller.start(primaryStage);
			
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Admin");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
    
    private void userWindow(String fxml, String user) {
    	try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(fxml));
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
    public void login(ActionEvent event) {
//    	Node e = (Node) event.getSource();
//    	primaryStage = (Stage) e.getScene().getWindow();
    	
    	String userName = login_user.getText();
    	if (userName.isEmpty()) {
    		error("Username cannot be empty");
    		return;
    	}
    		
    	if (userName.toLowerCase().equals("admin")) {			// Admin Window
    		adminWindow("/view/admin.fxml");
    	} else 
    	if (userName.toLowerCase().equals("stock")) {	// Stock Stuff
    		userWindow("/view/Users.fxml", "stock");
    	} else {												// User Stuff
    		if (!users.contains(userName)) {
    			error("User not found");
    			return;
    		}
    		userWindow("/view/Users.fxml", userName);
    	}
    }
    
    @FXML
    public void forgot() {
    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setTitle("Forgot ID");
//    	alert.setHeaderText("R U ReTARTed?");
   	    alert.setHeaderText("List of users:");
   	    alert.setContentText(users.toString());
        alert.showAndWait();
    }
    
}
