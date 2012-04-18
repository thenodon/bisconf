package models;

import javax.persistence.Entity;

import play.data.validation.Email;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class User extends Model {

	@Required
	@Email
	public String email;
	
	@Required
    @MinSize(8)
	public String password;
	
	@Required
	public String username;
	public boolean isAdmin;

	public User(String password, String username, String email) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.isAdmin = false;
	}

	public User(String password, String username, String email, Boolean isAdmin) {
		this.email = email;
		this.password = password;
		this.username = username;
		this.isAdmin = isAdmin;
	}

	public static User connect(String username, String password) {
		System.out.println("User " + username);
		System.out.println("Pasword " + password);

		// If first time and admin do not exists
		if ("admin".equals(username)) {
			System.out.println("User " + username);
			if (find("byUsername", username).first() == null) {
				new User("admin", "admin", "admin", true).save();
			}
		}
		return find("byUsernameAndPassword", username, password).first();
	}

	public String toString() {
		return username;
	}

}
