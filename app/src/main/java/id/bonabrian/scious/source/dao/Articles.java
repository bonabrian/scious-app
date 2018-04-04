package id.bonabrian.scious.source.dao;

import java.util.List;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class Articles {

    private String id;
    private String user_id;
    private String title;
    private String image;
    private String content;
    private String time;
    private String author;

    public Articles(String title) {
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public class ListArticles extends BaseModel {
        private List<Articles> articles;

        public List<Articles> getArticles() {
            return articles;
        }

        public void setArticles(List<Articles> articles) {
            this.articles = articles;
        }
    }
}
