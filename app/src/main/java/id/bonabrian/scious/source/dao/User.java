package id.bonabrian.scious.source.dao;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class User extends BaseModel {
    private String user_id;
    private String name;
    private String email;
    private String weight;
    private String height;
    private String birthday;
    private String admin;

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public class UserList extends BaseModel {
        private User result;

        public User getResult() {
            return result;
        }

        public void setResult(User result) {
            this.result = result;
        }
    }

    public class UserData extends BaseModel {
        private  User data;

        public User getData() {
            return data;
        }

        public void setData(User data) {
            this.data = data;
        }
    }
}
