package ddwu.moblie.finalproject.ma01_20200962;

import java.io.Serializable;

public class ClothesDto implements Serializable {
    private long _id;
    private String name;
    private String category;
    private String photoPath;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public String toString() {
        return "ClothesDto{" +
                "_id=" + _id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", photoPath='" + photoPath + '\'' +
                '}';
    }
}
