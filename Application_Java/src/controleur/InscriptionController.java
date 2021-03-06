package controleur;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dbstuff.DbAdapter;
import dbstuff.QueriesItr;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;

/**
 * Classe InscriptionController, definit le controleur pour la vue d'incription
 * de user
 * 
 * @author Jonathan Caspar, Jules Cohen, Jean-Francois Blanchette et Tanahel
 *         Huot-Roberge
 *
 */
public class InscriptionController {

	@FXML
	AnchorPane inscriptionPane;
	@FXML
	TextField usernameTF;
	@FXML
	PasswordField passwordTF;
	@FXML
	TextField nomTF;
	@FXML
	TextField prenomTF;
	@FXML
	TextField telTF;
	@FXML
	Button inscription;
	private String username, password, prenom, nom, tel;
	private int userID = 0;

	@FXML
	public void initialize() {

		inscriptionPane.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				inscrire();
			}
		});
	}

	/**
	 * Verifie si le formulaire est entierement rempli et si le numero de telephone
	 * est au bon format
	 * 
	 * @return vrai si le formulaire est valide, faux sinon
	 */
	public boolean validForm() {

		try {

			username = usernameTF.getText();
			password = passwordTF.getText();
			prenom = prenomTF.getText();
			nom = nomTF.getText();
			tel = telTF.getText();

			if (username.length() == 0 || password.length() == 0 || prenom.length() == 0) {
				errorPopup("Données manquante", "Vous n'avez pas rempli tous les champs.");
				return false;
			}

			if (tel.length() > 0 && tel.matches("^\\d{3}-\\d{3}-\\d{4}$")) {
				return true;
			} else if (tel.length() > 0 && !tel.matches("^\\d{3}-\\d{3}-\\d{4}$")) {
				errorPopup("Format numéro de téléphone",
						"Le numéro saisi n'est pas au bon format \n Rappel: 123-456-7777.");
				return false;
			} else {
				return true;
			}

		} catch (NullPointerException e) {

			e.printStackTrace();
			errorPopup("Données manquante", "Vous n'avez pas rempli tous les champs.");
			return false;
		}
	}

	/**
	 * Construit la query en fonction de la presence ou non de nom ou de numero de
	 * telephone
	 * 
	 * @return query la requete
	 */
	public String getQuery() {
		String query = "INSERT INTO users ( userid, username, password, firstname";
		String values = " VALUES ( ( SELECT MAX(userid) AS max FROM users ) + 1 ,'" + username + "', '" + password
				+ "', '" + prenom + "' ";

		String end = ")";
		if (this.nom.length() > 0) {
			query += " , lastname ";
			values += ", '" + this.nom + "'";
		}
		if (this.tel.length() > 0) {
			query += " , phonenumber ";
			values += ", '" + this.tel + "'";
		}
		values += end;
		query += end;
		query += values;


		return query;
	}
  

	/**
	 * Insertion d'un nouvel utilisateur dans la base de données.
	 */
	@FXML
	public void inscrire() {

		if (validForm()) {

//			setNewMaxId();

			try {

				Statement stmt = DbAdapter.con.createStatement();
				if (stmt != null) {
					stmt.executeUpdate(getQuery());
					stmt.close();

					Stage stage = (Stage) inscriptionPane.getScene().getWindow();
					stage.close();

				}
			} catch (SQLException e) {
				errorPopup("Probleme Base de Données", "L'insertion n'a pas pu être effectuée.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Popup d'erreur customizable.
	 * 
	 * @param typeError
	 * @param message
	 */
	public static void errorPopup(String typeError, String message) {

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("PROBLEME !");
		alert.setHeaderText(typeError);
		alert.setContentText(message);

		alert.showAndWait();
	}

}
