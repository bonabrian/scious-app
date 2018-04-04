package id.bonabrian.scious.source.dao;

import java.util.List;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class Recommended {
    String id;
    String user_id;
    String title;
    String image;
    String content;
    String category;
    String time;

    public Recommended(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public class ListRecommended extends BaseModel {
        private List<Recommended> recommended;

        public List<Recommended> getRecommended() {
            return recommended;
        }

        public void setRecommended(List<Recommended> recommended) {
            this.recommended = recommended;
        }
    }
}
