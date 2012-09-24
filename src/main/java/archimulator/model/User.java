package archimulator.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

@DatabaseTable(tableName = "User")
public class User implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String email;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private String password;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.createTime = DateHelper.toTick(new Date());
    }

    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    @Override
    public String getTitle() {
        return email;
    }

    public String getEmail() {
        return email;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}